package com.arkflame.authmepremium.listeners;

import com.arkflame.authmepremium.events.PremiumLoginEvent;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PremiumLoginListener implements Listener{
    @EventHandler
    public void onPremiumLogin(PremiumLoginEvent event) {
        String name = event.getInitialHandler().getName();
        BungeeCord.getInstance().broadcast(name + " is premium! Congratulations!");
    }
}
