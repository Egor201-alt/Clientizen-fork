package com.denizenscript.clientizen;

import com.denizenscript.clientizen.debuggui.ClientizenDebugScreen;
import com.denizenscript.clientizen.events.ClientCameraModeChangeScriptEvent;
import com.denizenscript.clientizen.events.ClientGuiScaleChangeScriptEvent;
import com.denizenscript.clientizen.events.ClientWindowFocusChangeScriptEvent;
import com.denizenscript.clientizen.events.ClientizenScriptEventRegistry;
import com.denizenscript.clientizen.network.NetworkManager;
import com.denizenscript.clientizen.objects.ClientizenObjectRegistry;
import com.denizenscript.clientizen.objects.extensions.ClientizenExtensionRegistry;
import com.denizenscript.clientizen.objects.properties.PropertyRegistry;
import com.denizenscript.clientizen.scripts.commands.ClientizenCommandRegistry;
import com.denizenscript.clientizen.scripts.containers.ClientizenContainerRegistry;
import com.denizenscript.clientizen.tags.ClientizenTagContext;
import com.denizenscript.clientizen.tags.ClientizenTagRegistry;
import com.denizenscript.clientizen.util.ClientExecuteCommand;
import com.denizenscript.clientizen.util.impl.DenizenCoreImpl;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.DenizenImplementation;
import com.denizenscript.denizencore.scripts.ScriptHelper;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CameraType;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Clientizen implements ClientModInitializer {

    public static final String ID = "clientizen";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    public static final Event<Runnable> SYNC_DISCONNECT = EventFactory.createArrayBacked(Runnable.class, listeners -> () -> {
        for (Runnable listener : listeners) {
            listener.run();
        }
    });

    public static String version;

    public DenizenImplementation coreImplementation = new DenizenCoreImpl();

    // Trackers for event triggers
    public static int lastGuiScale = -1;
    public static CameraType lastCameraType = null;
    public static Boolean lastFocused = null;

    @Override
    public void onInitializeClient() {
        // Note: intentionally before initializing Denizen-Core as it reads the implementation version
        version = FabricLoader.getInstance().getModContainer(ID).get().getMetadata().getVersion().toString();
        LOGGER.info("Initializing Clientizen v" + version);

        // Initialize Denizen-Core
        CoreUtilities.noDebugContext = new ClientizenTagContext(false, null, null);
        CoreUtilities.noDebugContext.showErrors = () -> false;
        CoreUtilities.basicContext = new ClientizenTagContext(true, null, null);
        CoreUtilities.errorButNoDebugContext = new ClientizenTagContext(false, null, null);
        DenizenCore.init(coreImplementation);
        DenizenCore.reloadSaves();

        // Configure Denizen-Core
        CoreConfiguration.allowConsoleRedirection = false;
        CoreConfiguration.allowFileCopy = false;
        CoreConfiguration.allowFileRead = false;
        CoreConfiguration.allowFileWrite = false;
        CoreConfiguration.allowLog = false;
        CoreConfiguration.allowRedis = false;
        CoreConfiguration.allowRestrictedActions = false;
        CoreConfiguration.allowSQL = false;
        CoreConfiguration.allowStrangeFileSaves = false;
        CoreConfiguration.allowWebget = false;

        // Register Clientizen script features
        ClientizenObjectRegistry.registerObjects();
        ClientizenCommandRegistry.registerCommands();
        ClientizenContainerRegistry.registerContainers();
        ClientizenScriptEventRegistry.registerEvents();
        ClientizenTagRegistry.registerTagHandlers();
        ClientizenExtensionRegistry.registerExtensions();
        PropertyRegistry.register();

        // Initialize Clientizen systems
        NetworkManager.init();
        ClientizenDebugScreen.register();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> new ClientExecuteCommand(dispatcher));

        // Check for the client scripts folder
        File scriptsFolder = DenizenCore.implementation.getScriptFolder();
        if (!scriptsFolder.exists()) {
            Debug.log("Creating scripts folder at " + scriptsFolder);
            scriptsFolder.mkdirs();
        }

        // Load all scripts in
        DenizenCore.reloadScripts(false, null);

        // Tick Denizen-Core
        ClientTickEvents.START_CLIENT_TICK.register(client -> DenizenCore.tick(50));

        // Tick Clientizen triggers (GUI Scale, Camera Mode, etc.)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.options == null) return;

            // GUI Scale Trigger
            int currentScale = client.options.guiScale().get();
            if (lastGuiScale == -1) {
                lastGuiScale = currentScale;
            }
            else if (lastGuiScale != currentScale) {
                if (ClientGuiScaleChangeScriptEvent.instance != null) {
                    ClientGuiScaleChangeScriptEvent.instance.handleScaleChange(lastGuiScale, currentScale);
                }
                lastGuiScale = currentScale;
            }

            // Camera Mode Trigger
            CameraType currentCamera = client.options.getCameraType();
            if (lastCameraType == null) {
                lastCameraType = currentCamera;
            }
            else if (lastCameraType != currentCamera) {
                if (ClientCameraModeChangeScriptEvent.instance != null) {
                    ClientCameraModeChangeScriptEvent.instance.handleCameraChange(lastCameraType, currentCamera);
                }
                lastCameraType = currentCamera;
            }
            
            // --- Window Focus Trigger ---
            boolean currentFocused = client.isWindowActive();
            
            if (lastFocused == null) {
                lastFocused = currentFocused;
            }
            else if (lastFocused != currentFocused) {
                if (ClientWindowFocusChangeScriptEvent.instance != null) {
                    ClientWindowFocusChangeScriptEvent.instance.handleFocusChange(currentFocused);
                }
                lastFocused = currentFocused;
            }
        });

        // Shutdown Denizen-Core when the client is stopping
        // TODO: DenizenCore#shutdown saves files (e.g. flags) to disk, should not be done here
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> DenizenCore.shutdown());

        // Remove scripts received from the server once the client disconnects from it
        SYNC_DISCONNECT.register(() -> {
            ScriptHelper.buildAdditionalScripts.clear();
            DenizenCore.reloadScripts(false, null);
        });
    }
}
