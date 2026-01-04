package com.denizenscript.clientizen.events;

import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.minecraft.client.CameraType;

public class ClientCameraModeChangeScriptEvent extends ScriptEvent {

    // <--[event]
    // @Events
    // client camera mode changes
    //
    // @Group Client
    //
    // @Switch to:<mode> to only process the event if the new mode matches the specified value.
    //
    // @Triggers when the client's camera perspective (first person / third person) changes.
    //
    // @Context
    // <context.previous_mode> returns an ElementTag of the previous camera mode.
    // <context.new_mode> returns an ElementTag of the new camera mode.
    //
    // @Example
    // # Prevent players from using third person view
    // on client camera mode changes:
    // - if <context.new_mode> != FIRST_PERSON:
    //   - adjust <client> camera_mode:FIRST_PERSON
    // -->

    public static ClientCameraModeChangeScriptEvent instance;

    public CameraType previousMode;
    public CameraType newMode;

    public ClientCameraModeChangeScriptEvent() {
        registerCouldMatcher("client camera mode changes|change");
        registerSwitches("to");
        instance = this;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "to", newMode.name())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "previous_mode", "old_mode" -> new ElementTag(previousMode.name());
            case "new_mode" -> new ElementTag(newMode.name());
            default -> super.getContext(name);
        };
    }

    public void handleCameraChange(CameraType previousMode, CameraType newMode) {
        this.previousMode = previousMode;
        this.newMode = newMode;
        fire();
    }
}
