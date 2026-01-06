package com.denizenscript.clientizen.tags.modules;

import com.denizenscript.clientizen.tags.ClientTagBase;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ClientGuiTags {

    public static void register(ObjectTagProcessor<ClientTagBase> tagProcessor) {
        
        // <--[tag]
        // @attribute <client.chat_width>
        // @returns ElementTag(Number)
        // @description
        // Returns the width of the client's chat HUD, in pixels.
        // @example
        // # Use to check if a line of text can fit in the chat HUD without splitting into multiple lines.
        // - if <[text].text_width.mul[<client.chat_scale>]> <= <client.chat_width>:
        //   - narrate <[text]>
        // - else:
        //   - narrate "Too long!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "chat_width", (attribute, object) -> {
            return new ElementTag(Minecraft.getInstance().gui.getChat().getWidth());
        });

        // <--[tag]
        // @attribute <client.chat_scale>
        // @returns ElementTag(Number)
        // @description
        // Returns the client's chat scale, which is a multiplier for text's size.
        // @example
        // # Use to get the width text would have if displayed in the chat HUD.
        // - narrate "The text would be <[text].text_width.mul[<client.chat_scale>]> pixels wide."
        // -->
        tagProcessor.registerTag(ElementTag.class, "chat_scale", (attribute, object) -> {
            return new ElementTag(Minecraft.getInstance().gui.getChat().getScale());
        });

        // <--[tag]
        // @attribute <client.gui_scale>
        // @returns ElementTag(Number)
        // @description
        // Returns the client's GUI scale (0 = Auto, 1 = Small, 2 = Normal, 3 = Large, etc).
        // -->
        tagProcessor.registerTag(ElementTag.class, "gui_scale", (attribute, object) -> {
            return new ElementTag(Minecraft.getInstance().options.guiScale().get());
        });

        // <--[tag]
        // @attribute <client.real_gui_scale>
        // @returns ElementTag(Number)
        // @description
        // Returns the ACTUAL calculated GUI scale factor currently being used.
        // Even if the setting is "Auto" (0), this will return the real number (e.g. 3, 4).
        // -->
        tagProcessor.registerTag(ElementTag.class, "real_gui_scale", (attribute, object) -> {
            return new ElementTag(Minecraft.getInstance().getWindow().getGuiScale());
        });

        // <--[mechanism]
        // @object client
        // @name gui_scale
        // @input ElementTag(Number)
        // @description
        // Sets the client's GUI scale.
        // 0 = Auto, 1 = Small, 2 = Normal, 3 = Large, etc.
        // @tags
        // <client.gui_scale>
        // -->
        tagProcessor.registerMechanism("gui_scale", false, ElementTag.class, (object, mechanism, input) -> {
            if (mechanism.requireInteger()) {
                Minecraft.getInstance().options.guiScale().set(input.asInt());
            }
        });

        // <--[tag]
        // @attribute <client.screen>
        // @returns ElementTag
        // @description
        // Returns the type of the currently open screen (GUI) in a simplified format (class name without "Screen").
        // Returns null if no GUI is open (player is in the game world).
        // Example returns: Inventory, Pause (Esc menu), Chat, Advancements, CreativeModeInventory, Container.
        // -->
        tagProcessor.registerTag(ElementTag.class, "screen", (attribute, object) -> {
            Screen currentScreen = Minecraft.getInstance().screen;
            if (currentScreen == null) {
                return null;
            }
            String className = currentScreen.getClass().getSimpleName();
            if (className.endsWith("Screen")) {
                className = className.substring(0, className.length() - "Screen".length());
            }
            return new ElementTag(className);
        });

        // <--[tag]
        // @attribute <client.screen_title>
        // @returns ElementTag
        // @description
        // Returns the visible title of the currently open screen (GUI).
        // Returns null if no GUI is open (player is in the game world).
        // Example returns: "Crafting", "Chest", "Large Chest", "Furnace", "Advancements", "Creative Inventory".
        // -->
        tagProcessor.registerTag(ElementTag.class, "screen_title", (attribute, object) -> {
            Screen currentScreen = Minecraft.getInstance().screen;
            if (currentScreen == null) {
                return null;
            }
            return new ElementTag(currentScreen.getTitle().getString());
        });
    }
}
