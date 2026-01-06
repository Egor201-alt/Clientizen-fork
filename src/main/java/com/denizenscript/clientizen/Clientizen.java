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
import com.denizenscript.clientizen.util.FpsMonitor;
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
    public static int tickCounter = 0;

    public static final List<String> WATCHED_OPTIONS = List.of(
        "fov", "gamma", "sensitivity", "render_distance", "particles", "clouds", "graphics",
        "sound_master", "sound_music", "sound_record", "sound_weather", "sound_block", 
        "sound_hostile", "sound_neutral", "sound_player", "sound_ambient", "sound_voice",
        "auto_jump", "main_hand", "narrator"
    );

    private static OptionInstance<Double> getSoundOption(SoundSource source) {
        return Minecraft.getInstance().options.getSoundSourceOptionInstance(source);
    }

    private static final Map<String, Supplier<Object>> OPTION_PROVIDERS = new HashMap<>();

    private static void registerOption(String name, Supplier<Object> supplier) {
        OPTION_PROVIDERS.put(name, supplier);
    }

    private static void registerSoundOption(String name, SoundSource source) {
        OPTION_PROVIDERS.put(name, () -> Minecraft.getInstance().options.getSoundSourceOptionInstance(source).get());
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

        registerOption("fov", () -> Minecraft.getInstance().options.fov().get());
        registerOption("gamma", () -> Minecraft.getInstance().options.gamma().get());
        registerOption("sensitivity", () -> Minecraft.getInstance().options.sensitivity().get());
        registerOption("render_distance", () -> Minecraft.getInstance().options.renderDistance().get());
        registerOption("particles", () -> Minecraft.getInstance().options.particles().get().toString());
        registerOption("clouds", () -> Minecraft.getInstance().options.cloudStatus().get().toString());
        registerOption("graphics", () -> Minecraft.getInstance().options.graphicsMode().get().toString());
        registerOption("auto_jump", () -> Minecraft.getInstance().options.autoJump().get());
        registerOption("main_hand", () -> Minecraft.getInstance().options.mainHand().get().toString());
        registerOption("narrator", () -> Minecraft.getInstance().options.narrator().get().toString());

        registerSoundOption("sound_master", SoundSource.MASTER);
        registerSoundOption("sound_music", SoundSource.MUSIC);
        registerSoundOption("sound_record", SoundSource.RECORDS);
        registerSoundOption("sound_weather", SoundSource.WEATHER);
        registerSoundOption("sound_block", SoundSource.BLOCKS);
        registerSoundOption("sound_hostile", SoundSource.HOSTILE);
        registerSoundOption("sound_neutral", SoundSource.NEUTRAL);
        registerSoundOption("sound_player", SoundSource.PLAYERS);
        registerSoundOption("sound_ambient", SoundSource.AMBIENT);
        registerSoundOption("sound_voice", SoundSource.VOICE);

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
            
            if (tickCounter++ % 20 == 0) {
                FpsMonitor.update();
                if (tickCounter > 10000) tickCounter = 0;
            }
            
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
            for (Map.Entry<String, Supplier<Object>> entry : OPTION_PROVIDERS.entrySet()) {
                String key = entry.getKey();
                Object currentVal = entry.getValue().get();
                
                if (currentVal == null) continue;

                if (!lastOptionValues.containsKey(key)) {
                    lastOptionValues.put(key, currentVal);
                } else {
                    Object oldVal = lastOptionValues.get(key);
                    if (!currentVal.equals(oldVal)) {
                        if (ClientOptionChangeScriptEvent.instance != null) {
                            ClientOptionChangeScriptEvent.instance.handleOptionChange(key, oldVal, currentVal);
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
