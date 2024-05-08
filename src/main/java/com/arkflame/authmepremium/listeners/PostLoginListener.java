package com.arkflame.authmepremium.listeners;

import java.util.UUID;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PostLoginListener implements Listener {
    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (player != null) {
            String name = player.getName();
            UUID uuid = player.getUniqueId();
            String premiumMessage = PreLoginListener.premium.contains(name) ? ChatColor.GREEN + "You are premium!" : ChatColor.RED +  "You aren't premium!";
            player.sendMessage(premiumMessage + " UUID: " + uuid);

            ServerInfo serverInfo = BungeeCord.getInstance().getServerInfo("bw-1");
            if (serverInfo != null) {
                player.connect(serverInfo);
            }
        }
    }
}
