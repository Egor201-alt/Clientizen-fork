package com.denizenscript.clientizen.tags.modules;

import com.denizenscript.clientizen.tags.ClientTagBase;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import net.minecraft.client.CameraType;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.OptionInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.HumanoidArm;

public class ClientSettingsTags {

    private static OptionInstance<Double> getSoundOption(SoundSource source) {
        return Minecraft.getInstance().options.getSoundSourceOptionInstance(source);
    }

    public static void register(ObjectTagProcessor<ClientTagBase> tagProcessor) {

        // <--[tag]
        // @attribute <client.camera_mode>
        // @returns ElementTag
        // @description
        // Returns the client's current camera perspective.
        // Values: FIRST_PERSON, THIRD_PERSON_BACK, THIRD_PERSON_FRONT.
        // -->
        tagProcessor.registerTag(ElementTag.class, "camera_mode", (attribute, object) -> {
            return new ElementTag(Minecraft.getInstance().options.getCameraType().name());
        });

        // <--[mechanism]
        // @object client
        // @name camera_mode
        // @input ElementTag
        // @description
        // Sets the client's camera perspective.
        // Input must be one of: FIRST_PERSON, THIRD_PERSON_BACK, THIRD_PERSON_FRONT.
        // @tags
        // <client.camera_mode>
        // -->
        tagProcessor.registerMechanism("camera_mode", false, ElementTag.class, (object, mechanism, input) -> {
            try {
                CameraType mode = CameraType.valueOf(input.asString().toUpperCase());
                Minecraft.getInstance().options.setCameraType(mode);
            } catch (IllegalArgumentException e) {
                mechanism.echoError("Invalid camera mode specified: " + input.asString());
            }
        });

        // <--[tag]
        // @attribute <client.sensitivity>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the client's mouse sensitivity setting.
        // -->
        tagProcessor.registerTag(ElementTag.class, "sensitivity", (attribute, object) -> {
            return new ElementTag(Minecraft.getInstance().options.sensitivity().get());
        });

        // <--[mechanism]
        // @object client
        // @name sensitivity
        // @input ElementTag(Decimal)
        // @description
        // Sets the client's mouse sensitivity.
        // Useful for "heavy" effects or sniper zooming.
        // @tags
        // <client.sensitivity>
        // -->
        tagProcessor.registerMechanism("sensitivity", false, ElementTag.class, (object, mechanism, input) -> {
            if (mechanism.requireDouble()) {
                Minecraft.getInstance().options.sensitivity().set(input.asDouble());
            }
        });

        // <--[tag]
        // @attribute <client.option[<name>]>
        // @returns ElementTag
        // @description
        // Returns the value of a specific client option.
        // Available options:
        // - VIDEO: gamma, gui_scale, render_distance, particles, clouds, fullscreen, vsync
        // - SOUND: sound_master, sound_music, sound_record, sound_weather, sound_block, sound_hostile, sound_neutral, sound_player, sound_ambient, sound_voice
        // - GAME: auto_jump, narrator, main_hand
        // -->
        tagProcessor.registerTag(ElementTag.class, "option", (attribute, object) -> {
            if (!attribute.hasParam()) return null;
            String key = attribute.getParam().toLowerCase();
            var opts = Minecraft.getInstance().options;

            if (key.startsWith("sound_")) {
                try {
                    String soundName = key.substring("sound_".length()).toUpperCase();
                    if (soundName.equals("RECORD")) soundName = "RECORDS";
                    if (soundName.equals("BLOCK")) soundName = "BLOCKS";
                    SoundSource source = SoundSource.valueOf(soundName);
                    OptionInstance<Double> option = getSoundOption(source);
                    if (option != null) return new ElementTag(option.get());
                } catch (IllegalArgumentException ignored) {}
            }

            return switch (key) {
                case "gamma" -> new ElementTag(opts.gamma().get());
                case "gui_scale" -> new ElementTag(opts.guiScale().get());
                case "render_distance" -> new ElementTag(opts.renderDistance().get());
                case "fullscreen" -> new ElementTag(opts.fullscreen().get());
                case "vsync" -> new ElementTag(opts.enableVsync().get());
                case "particles" -> new ElementTag(opts.particles().get().toString());
                case "clouds" -> new ElementTag(opts.cloudStatus().get().toString());
                case "graphics" -> new ElementTag(opts.graphicsMode().get().toString());
                case "auto_jump" -> new ElementTag(opts.autoJump().get());
                case "narrator" -> new ElementTag(opts.narrator().get().toString());
                case "main_hand" -> new ElementTag(opts.mainHand().get().toString());
                default -> null;
            };
        });

        // <--[mechanism]
        // @object client
        // @name option
        // @input MapTag
        // @description
        // Sets a generic client option.
        // Input must be a map with 'name' and 'value'.
        // Example: adjust <client> option:[name=sound_music;value=0.5]
        // -->
        tagProcessor.registerMechanism("option", false, MapTag.class, (object, mechanism, input) -> {
            String name = input.getElement("name").asString().toLowerCase();
            ElementTag value = input.getElement("value");
            var opts = Minecraft.getInstance().options;

            if (name.startsWith("sound_")) {
                try {
                    String soundName = name.substring("sound_".length()).toUpperCase();
                    if (soundName.equals("RECORD")) soundName = "RECORDS";
                    if (soundName.equals("BLOCK")) soundName = "BLOCKS";
                    SoundSource source = SoundSource.valueOf(soundName);
                    OptionInstance<Double> option = getSoundOption(source);
                    if (option != null) {
                        option.set(value.asDouble());
                        return;
                    }
                } catch (Exception e) {
                   mechanism.echoError("Error setting sound: " + e.getMessage());
                }
            }

            try {
                switch (name) {
                    case "gamma" -> opts.gamma().set(value.asDouble());
                    case "gui_scale" -> opts.guiScale().set(value.asInt());
                    case "render_distance" -> opts.renderDistance().set(value.asInt());
                    case "fullscreen" -> opts.fullscreen().set(value.asBoolean());
                    case "vsync" -> opts.enableVsync().set(value.asBoolean());
                    case "particles" -> {
                        OptionInstance<Object> particleOption = (OptionInstance<Object>) (Object) opts.particles();
                        Class<? extends Enum> enumClass = (Class<? extends Enum>) particleOption.get().getClass();
                        Object status = Enum.valueOf(enumClass, value.asString().toUpperCase());
                        particleOption.set(status);
                    }
                    case "clouds" -> opts.cloudStatus().set(CloudStatus.valueOf(value.asString().toUpperCase()));
                    case "graphics" -> opts.graphicsMode().set(GraphicsStatus.valueOf(value.asString().toUpperCase()));
                    case "auto_jump" -> opts.autoJump().set(value.asBoolean());
                    case "narrator" -> opts.narrator().set(NarratorStatus.valueOf(value.asString().toUpperCase()));
                    case "main_hand" -> opts.mainHand().set(HumanoidArm.valueOf(value.asString().toUpperCase()));
                    default -> {
                        if (!name.startsWith("sound_")) mechanism.echoError("Unknown option: " + name);
                    }
                }
            } catch (Exception e) {
                mechanism.echoError("Invalid value '" + value + "' for option '" + name + "'. Error: " + e.getMessage());
            }
        });
    }
}
