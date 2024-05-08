package com.arkflame.authmepremium.listeners;

import java.lang.reflect.Field;

import com.arkflame.authmepremium.events.PremiumLoginEvent;

import fr.xephi.authmebungee.AuthMeBungee;
import fr.xephi.authmebungee.data.AuthPlayer;
import fr.xephi.authmebungee.services.AuthPlayerManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class PremiumLoginListener implements Listener {
    public void hookAuthMeBungee(String name) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Plugin plugin = BungeeCord.getInstance().getPluginManager().getPlugin("AuthMeBungee");
        if (plugin instanceof AuthMeBungee) {
            AuthMeBungee authMeBungee = (AuthMeBungee) plugin;
            Field authPlayerManagerField = authMeBungee.getClass().getField("authPlayerManager");
            authPlayerManagerField.setAccessible(true);
            AuthPlayerManager authPlayerManager = (AuthPlayerManager) authPlayerManagerField.get(authMeBungee);
            AuthPlayer authPlayer = authPlayerManager.getAuthPlayer(name);
            authPlayer.setLogged(true);
        }
    }

    @EventHandler
    public void onPremiumLogin(PremiumLoginEvent event) {
        String name = event.getInitialHandler().getName();
        BungeeCord.getInstance().broadcast(name + " is premium! Congratulations!");

        // Hook into AuthMeBungee
        try {
            hookAuthMeBungee(name);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
