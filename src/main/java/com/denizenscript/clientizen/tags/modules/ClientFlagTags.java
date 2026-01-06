package com.denizenscript.clientizen.tags.modules;

import com.denizenscript.clientizen.tags.ClientTagBase;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.core.TimeTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;

public class ClientFlagTags {

    public static void register(ObjectTagProcessor<ClientTagBase> tagProcessor) {
        
        // <--[tag]
        // @attribute <client.has_flag[<flag>]>
        // @returns ElementTag(Boolean)
        // @description
        // See <@link tag FlaggableObject.has_flag>.
        // -->
        tagProcessor.registerTag(ElementTag.class, ElementTag.class, "has_flag", (attribute, object, param) -> {
            return new ElementTag(DenizenCore.serverFlagMap.hasFlag(param.asString()));
        });

         // <--[tag]
        // @attribute <client.flag[<flag_name>]>
        // @returns ObjectTag
        // @description
        // See <@link tag FlaggableObject.flag>
        // -->
        tagProcessor.registerTag(ObjectTag.class, ElementTag.class, "flag", (attribute, object, param) -> {
            return DenizenCore.serverFlagMap.doFlagTag(attribute);
        });

        // <--[tag]
        // @attribute <client.flag_expiration[<flag_name>]>
        // @returns TimeTag
        // @description
        // See <@link tag FlaggableObject.flag_expiration>
        // -->
        tagProcessor.registerTag(TimeTag.class, ElementTag.class, "flag_expiration", (attribute, object, param) -> {
            return DenizenCore.serverFlagMap.doFlagExpirationTag(attribute);
        });

        // <--[tag]
        // @attribute <client.list_flags>
        // @returns ListTag
        // @description
        // See <@link tag FlaggableObject.list_flags>
        // -->
        tagProcessor.registerTag(ListTag.class, "list_flags", (attribute, object) -> {
            return DenizenCore.serverFlagMap.doListFlagsTag(attribute);
        });

        // <--[tag]
        // @attribute <client.flag_map[<name>|...]>
        // @returns MapTag
        // @description
        // See <@link tag FlaggableObject.flag_map>
        // -->
        tagProcessor.registerTag(MapTag.class, "flag_map", (attribute, object) -> {
            return DenizenCore.serverFlagMap.doFlagMapTag(attribute);
        });
    }
}
