package com.denizenscript.clientizen.tags;

import com.denizenscript.clientizen.tags.modules.*;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.objects.Adjustable;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.scripts.commands.core.AdjustCommand;
import com.denizenscript.denizencore.tags.PseudoObjectTagBase;
import com.denizenscript.denizencore.tags.TagManager;

public class ClientTagBase extends PseudoObjectTagBase<ClientTagBase> implements FlaggableObject, Adjustable {

    public static ClientTagBase instance;

    public ClientTagBase() {
        instance = this;
        TagManager.registerStaticTagBaseHandler(ClientTagBase.class, "client", t -> instance);
        AdjustCommand.specialAdjustables.put("client", mechanism -> tagProcessor.processMechanism(instance, mechanism));
    }

    @Override
    public void register() {
        // Register all tag-modules
        ClientEntityTags.register(tagProcessor);
        ClientWorldTags.register(tagProcessor);
        ClientGuiTags.register(tagProcessor);
        ClientSettingsTags.register(tagProcessor);
        ClientSystemTags.register(tagProcessor);
        ClientFlagTags.register(tagProcessor);
    }

    @Override
    public void adjust(Mechanism mechanism) {
        tagProcessor.processMechanism(this, mechanism);
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        adjust(mechanism);
    }

    @Override
    public String toString() {
        return "client";
    }

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return DenizenCore.serverFlagMap;
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {}
}
