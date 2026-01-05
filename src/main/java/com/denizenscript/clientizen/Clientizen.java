package com.denizenscript.clientizen;

import net.minecraft.client.Minecraft;
import com.denizenscript.clientizen.debuggui.ClientizenDebugScreen;
import com.denizenscript.clientizen.events.ClientCameraModeChangeScriptEvent;
import com.denizenscript.clientizen.events.ClientGuiScaleChangeScriptEvent;
import com.denizenscript.clientizen.events.ClientWindowFocusChangeScriptEvent;
import com.denizenscript.clientizen.events.ClientOptionChangeScriptEvent;
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
import net.minecraft.client.OptionInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

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
    public static Map<String, Object> lastOptionValues = new HashMap<>();

    public static final List<String> WATCHED_OPTIONS = List.of(
        "fov", "gamma", "sensitivity", "render_distance", "particles", "clouds", "graphics",
        "sound_master", "sound_music", "sound_record", "sound_weather", "sound_block", 
        "sound_hostile", "sound_neutral", "sound_player", "sound_ambient", "sound_voice",
        "auto_jump", "main_hand", "narrator"
    );

    private static OptionInstance<Double> getSoundOption(SoundSource source) {
        return Minecraft.getInstance().options.getSoundSourceOptionInstance(source);
    }

    private Object getCurrentOptionValue(String key) {
        var opts = Minecraft.getInstance().options;
        if (key.startsWith("sound_")) {
            try {
                String soundName = key.substring("sound_".length()).toUpperCase();
                if (soundName.equals("RECORD")) soundName = "RECORDS";
                if (soundName.equals("BLOCK")) soundName = "BLOCKS";
                SoundSource source = SoundSource.valueOf(soundName);
                return getSoundOption(source).get();
            } catch (Exception ignored) {}
        }
        return switch (key) {
            case "fov" -> opts.fov().get();
            case "gamma" -> opts.gamma().get();
            case "sensitivity" -> opts.sensitivity().get();
            case "render_distance" -> opts.renderDistance().get();
            case "particles" -> opts.particles().get().toString();
            case "clouds" -> opts.cloudStatus().get().toString();
            case "graphics" -> opts.graphicsMode().get().toString();
            case "auto_jump" -> opts.autoJump().get();
            case "main_hand" -> opts.mainHand().get().toString();
            case "narrator" -> opts.narrator().get().toString();
            default -> null;
        };
    }

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

        // Tick Clientizen triggers
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
            
            // --- Window Focus Trigger
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
            // Generic Options Trigger
            for (String key : WATCHED_OPTIONS) {
                Object currentVal = getCurrentOptionValue(key);
                if (currentVal == null) continue;

                if (!lastOptionValues.containsKey(key)) {
                    lastOptionValues.put(key, currentVal);
                } else {
                    Object oldVal = lastOptionValues.get(key);
                    if (!currentVal.equals(oldVal)) {
                        if (ClientOptionChangeScriptEvent.instance != null) {
                            ClientOptionChangeScriptEvent.instance.handleHtmlChange(key, oldVal, currentVal);
                        }
                        lastOptionValues.put(key, currentVal);
                    }
                }
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
