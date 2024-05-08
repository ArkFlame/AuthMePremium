package com.arkflame.authmepremium.events;

import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Event;

/**
 * This class represents a custom event for when a premium player logins.
 * It extends the BungeeCord Event class and encapsulates the InitialHandler instance.
 * Key functionalities include encapsulating the InitialHandler instance.
 */
public class PremiumPostLoginEvent extends Event {
    private PostLoginEvent postLoginEvent;

    public PremiumPostLoginEvent(PostLoginEvent postLoginEvent) {
        this.postLoginEvent = postLoginEvent;
    }
    
    public PostLoginEvent getPostLoginEvent() {
        return postLoginEvent;
    }
}
