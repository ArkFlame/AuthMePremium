package com.arkflame.authmepremium.listeners;

import com.arkflame.authmepremium.events.PremiumPostLoginEvent;

import net.md_5.bungee.BungeeTitle;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

public class PremiumPostLoginListener implements Listener {
    private Configuration messages;

    public PremiumPostLoginListener(Configuration messages) {
        this.messages = messages;
    }

    @EventHandler
    public void onPremiumLogin(PremiumPostLoginEvent event) {
        ProxiedPlayer player = event.getPostLoginEvent().getPlayer();
        String loggedInMessage = ChatColor.translateAlternateColorCodes('&', messages.getString("messages.logged_in"));
        if (loggedInMessage != null) {
            loggedInMessage = loggedInMessage.replace("{uuid}",
                    player.getUniqueId().toString());
            player.sendMessage(loggedInMessage);
            BaseComponent title = TextComponent.fromLegacy(ChatColor.translateAlternateColorCodes('&', messages.getString("messages.title")));
            BaseComponent subtitle = TextComponent.fromLegacy(ChatColor.translateAlternateColorCodes('&', messages.getString("messages.subtitle")));
            player.sendTitle(new BungeeTitle().title(title).subTitle(subtitle).fadeIn(20).fadeOut(20).stay(60));
        }
    }
}
