package com.denizenscript.clientizen.events;

import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class ClientWindowFocusChangeScriptEvent extends ScriptEvent {

    // <--[event]
    // @Events
    // client window focus changes
    //
    // @Group Client
    //
    // @Switch state:<true/false> to only process the event if the focus state matches the input.
    //
    // @Triggers when the Minecraft window gains or loses focus (e.g. Alt+Tab).
    //
    // @Context
    // <context.focused> returns true if the window is now focused, false otherwise.
    //
    // @Example
    // # Send a message when the player tabs out
    // on client window focus changes state:false:
    // - serverevent id:afk_start
    // -->

    public static ClientWindowFocusChangeScriptEvent instance;

    public boolean focused;

    public ClientWindowFocusChangeScriptEvent() {
        registerCouldMatcher("client window focus changes|change");
        registerSwitches("state");
        instance = this;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "state", String.valueOf(focused))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("focused") || name.equals("state")) {
            return new ElementTag(focused);
        }
        return super.getContext(name);
    }

    public void handleFocusChange(boolean focused) {
        this.focused = focused;
        fire();
    }
}
