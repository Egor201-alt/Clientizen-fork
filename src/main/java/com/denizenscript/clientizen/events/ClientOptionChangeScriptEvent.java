package com.denizenscript.clientizen.events;

import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class ClientOptionChangeScriptEvent extends ScriptEvent {

    // <--[event]
    // @Events
    // client option changes
    //
    // @Group Client
    //
    // @Switch option:<name> to only process the event if the option name matches.
    //
    // @Triggers when a generic client option (video, sound, game settings) changes.
    //
    // @Context
    // <context.option> returns the name of the option that changed (e.g. 'gamma', 'sound_master').
    // <context.new_value> returns the new value.
    // <context.old_value> returns the previous value.
    //
    // @Example
    // # Track sound changes
    // on client option changes sound_master:
    // - narrate "Master volume changed to <context.new_value>!"
    // -->

public static ClientOptionChangeScriptEvent instance;

    public String option;
    public Object newValue;
    public Object oldValue;

    public ClientOptionChangeScriptEvent() {
        registerCouldMatcher("client option changes");
        registerCouldMatcher("client option changes *");
        registerSwitches("option");
        instance = this;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String arg = path.eventArgLowerAt(3);
        if (!arg.isEmpty() && !CoreUtilities.runGenericCheck(arg, option)) {
            return false;
        }

        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "option" -> new ElementTag(option);
            case "new_value" -> new ElementTag(String.valueOf(newValue));
            case "old_value" -> new ElementTag(String.valueOf(oldValue));
            default -> super.getContext(name);
        };
    }

    public void handleOptionChange(String optionName, Object oldVal, Object newVal) {
        this.option = optionName;
        this.oldValue = oldVal;
        this.newValue = newVal;
        fire();
    }
}
