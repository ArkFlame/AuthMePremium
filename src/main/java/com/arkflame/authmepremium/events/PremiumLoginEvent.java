package com.arkflame.authmepremium.events;

import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.connection.InitialHandler;

/**
 * This class represents a custom event for when a premium player logins.
 * It extends the BungeeCord Event class and encapsulates the InitialHandler instance.
 * Key functionalities include encapsulating the InitialHandler instance.
 */
public class PremiumLoginEvent extends Event {
    private InitialHandler initialHandler;

    public PremiumLoginEvent(InitialHandler initialHandler) {
        this.initialHandler = initialHandler;
    }
    
    public InitialHandler getInitialHandler() {
        return initialHandler;
    }
}
