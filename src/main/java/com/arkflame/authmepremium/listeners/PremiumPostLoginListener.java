package com.arkflame.authmepremium.listeners;

import com.arkflame.authmepremium.events.PremiumPostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PremiumPostLoginListener implements Listener {
    @EventHandler
    public void onPremiumLogin(PremiumPostLoginEvent event) {
        // This is a example listener
    }
}
