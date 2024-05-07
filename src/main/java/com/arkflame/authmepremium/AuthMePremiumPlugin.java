package com.arkflame.authmepremium;

import com.arkflame.authmepremium.listeners.PreLoginListener;

import net.md_5.bungee.api.plugin.Plugin;

public class AuthMePremiumPlugin extends Plugin {

    @Override
    public void onEnable() {
        // Set static instance
        setInstance(this);
        
        getProxy().getPluginManager().registerListener(this, new PreLoginListener());
    }

    private static AuthMePremiumPlugin instance;

    public static void setInstance(AuthMePremiumPlugin instance) {
        AuthMePremiumPlugin.instance = instance;
    }

    public static AuthMePremiumPlugin getInstance() {
        return AuthMePremiumPlugin.instance;
    }
}