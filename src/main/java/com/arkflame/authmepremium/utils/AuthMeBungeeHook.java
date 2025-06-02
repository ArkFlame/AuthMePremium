package com.arkflame.authmepremium.utils;

import fr.xephi.authmebungee.AuthMeBungee;
import fr.xephi.authmebungee.data.AuthPlayer;
import fr.xephi.authmebungee.services.AuthPlayerManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;

public class AuthMeBungeeHook {

    public static void hookAuthMeBungee(String name)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Plugin plugin = BungeeCord.getInstance().getPluginManager().getPlugin("AuthMeBungee");
        if (plugin instanceof AuthMeBungee) {
            AuthMeBungee authMeBungee = (AuthMeBungee) plugin;
            AuthPlayerManager authPlayerManager = HandlerReflectionUtil.getFieldValue(authMeBungee,
                    "authPlayerManager");
            AuthPlayer authPlayer = new AuthPlayer(name);
            authPlayer.setLogged(true);
            authPlayerManager.addAuthPlayer(authPlayer);
        }
    }
}
