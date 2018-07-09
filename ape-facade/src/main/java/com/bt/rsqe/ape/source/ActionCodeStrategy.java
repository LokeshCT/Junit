package com.bt.rsqe.ape.source;

import pricing.ape.bt.com.schemas.Leg.ActionCodes;

public enum ActionCodeStrategy {

    Add (false, true, ActionCodes.Add),
    Update(true, true, ActionCodes.Update),
    None(true, false, ActionCodes.None),
    Move(true, true, ActionCodes.None);

    private final boolean requiresAsIs;
    private final boolean requiresToBe;
    private ActionCodes actionCodes;

    ActionCodeStrategy(boolean requiresAsIs, boolean requiresToBe, ActionCodes actionCodes) {
        this.requiresAsIs = requiresAsIs;
        this.requiresToBe = requiresToBe;
        this.actionCodes = actionCodes;
    }

    public boolean requiresAsIs() {
        return requiresAsIs;
    }

    public boolean requiresToBe() {
        return requiresToBe;
    }

    public ActionCodes getApeActionCodes() {
        return actionCodes;
    }
}
