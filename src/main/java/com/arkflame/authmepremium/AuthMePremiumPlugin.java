package com.arkflame.authmepremium;

import com.arkflame.authmepremium.listeners.PostLoginListener;
import com.arkflame.authmepremium.listeners.PreLoginListener;
import com.arkflame.authmepremium.listeners.PremiumPostLoginListener;
import com.arkflame.authmepremium.listeners.PremiumPreLoginListener;
import com.arkflame.authmepremium.managers.ConfigManager;
import com.arkflame.authmepremium.providers.DataProvider;
import com.arkflame.authmepremium.providers.MemoryProvider;
import com.arkflame.authmepremium.providers.MySQLProvider;
import com.arkflame.authmepremium.providers.YAMLProvider;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;

public class AuthMePremiumPlugin extends Plugin {

    @Override
    public void onEnable() {
        // Set static instance
        setInstance(this);

        ConfigManager configManager = new ConfigManager();
        Configuration config = configManager.loadDefault(this, "config.yml");
        String provider = config.getString("provider");

        switch (provider) {
            case "YAML":
                setDataProvider(new YAMLProvider(this, configManager));
                break;
            case "MYSQL":
                // Assuming you have configured the MySQL connection details elsewhere
                setDataProvider(new MySQLProvider(config));
                break;
            case "MEMORY":
                setDataProvider(new MemoryProvider());
                break;
            default:
                // Handle default case, maybe log an error or throw an exception
                break;
        }

        PluginManager pluginManager = getProxy().getPluginManager();

        pluginManager.registerListener(this, new PostLoginListener());
        pluginManager.registerListener(this, new PreLoginListener());
        pluginManager.registerListener(this, new PremiumPostLoginListener());
        pluginManager.registerListener(this, new PremiumPreLoginListener());
    }

    private static AuthMePremiumPlugin instance;
    private static DataProvider dataProvider;

    public static DataProvider getDataProvider() {
        return dataProvider;
    }

    public static void setDataProvider(DataProvider dataProvider) {
        AuthMePremiumPlugin.dataProvider = dataProvider;
    }

    public static void setInstance(AuthMePremiumPlugin instance) {
        AuthMePremiumPlugin.instance = instance;
    }

    public static AuthMePremiumPlugin getInstance() {
        return AuthMePremiumPlugin.instance;
    }
}