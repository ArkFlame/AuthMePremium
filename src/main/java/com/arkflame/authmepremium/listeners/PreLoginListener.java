package com.arkflame.authmepremium.listeners;

import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;

public class PreLoginListener implements Listener {
    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        PendingConnection pendingConnection = event.getConnection();

        if (pendingConnection instanceof InitialHandler) {
            InitialHandler initialHandler = (InitialHandler) pendingConnection;
            initialHandler.setOnlineMode(true);
        }
    }
}
