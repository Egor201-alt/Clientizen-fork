package com.denizenscript.clientizen.events;

import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class ClientGuiScaleChangeScriptEvent extends ScriptEvent {

    // <--[event]
    // @Events
    // client gui scale changes
    //
    // @Group Client
    //
    // @Switch to:<scale> to only process the event if the new scale matches the specified value.
    //
    // @Triggers when the client's GUI scale setting is changed in the options.
    //
    // @Context
    // <context.previous_scale> returns an ElementTag(Number) of the previous GUI scale.
    // <context.new_scale> returns an ElementTag(Number) of the new GUI scale.
    //
    // @Example
    // # Notifies the server when the player changes their GUI scale
    // on client gui scale changes:
    // - serverevent id:gui_update data:<map[scale=<context.new_scale>]>
    //
    // @Example
    // # Only fires when the scale becomes 'Auto' (0)
    // on client gui scale changes to:0:
    // - debug log "Switched to Auto scale!"
    // -->

    public static ClientGuiScaleChangeScriptEvent instance;

    public int previousScale;
    public int newScale;

    public ClientGuiScaleChangeScriptEvent() {
        registerCouldMatcher("client gui scale changes");
        registerSwitches("to");
        instance = this;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "to", String.valueOf(newScale))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "previous_scale" -> new ElementTag(previousScale);
            case "new_scale" -> new ElementTag(newScale);
            default -> super.getContext(name);
        };
    }

    public void handleScaleChange(int previousScale, int newScale) {
        this.previousScale = previousScale;
        this.newScale = newScale;
        fire();
    }
}
