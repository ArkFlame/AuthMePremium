package com.arkflame.authmepremium;

import com.arkflame.authmepremium.commands.AuthMePremiumCommand;
import com.arkflame.authmepremium.commands.PremiumCommand;
import com.arkflame.authmepremium.commands.PremiumUUIDCommand;
import com.arkflame.authmepremium.listeners.PostLoginListener;
import com.arkflame.authmepremium.listeners.PreLoginListener;
import com.arkflame.authmepremium.listeners.PremiumPostLoginListener;
import com.arkflame.authmepremium.listeners.PremiumPreLoginListener;
import com.arkflame.authmepremium.listeners.ServerConnectListener;
import com.arkflame.authmepremium.managers.ConfigManager;
import com.arkflame.authmepremium.providers.DataProvider;
import com.arkflame.authmepremium.providers.MemoryProvider;
import com.arkflame.authmepremium.providers.MySQLProvider;
import com.arkflame.authmepremium.providers.YAMLProvider;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;

public class AuthMePremiumPlugin extends Plugin {
    private static AuthMePremiumPlugin instance;
    private static DataProvider dataProvider;
    private static Configuration config;

    public static DataProvider getDataProvider() {
        return dataProvider;
    }

    public static void setDataProvider(DataProvider dataProvider) {
        AuthMePremiumPlugin.dataProvider = dataProvider;
    }

    public static Configuration getConfig() {
        return config;
    }

    public static void setConfig(Configuration config) {
        AuthMePremiumPlugin.config = config;
    }

    public static void setInstance(AuthMePremiumPlugin instance) {
        AuthMePremiumPlugin.instance = instance;
    }

    public static AuthMePremiumPlugin getInstance() {
        return AuthMePremiumPlugin.instance;
    }

    @Override
    public void onEnable() {
        // Set static instance
        setInstance(this);

        ConfigManager configManager = new ConfigManager();
        setConfig(configManager.loadDefault(this, "config.yml"));
        Configuration messages = configManager.loadDefault(this, "messages.yml");
        String provider = config.getString("provider") == null ? "" : config.getString("provider").toUpperCase();

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
                throw new RuntimeException("You entered an invalid provider: " + provider);
        }

        PluginManager pluginManager = getProxy().getPluginManager();

        pluginManager.registerListener(this, new PostLoginListener());
        pluginManager.registerListener(this, new PreLoginListener());
        pluginManager.registerListener(this, new PremiumPostLoginListener(messages));
        pluginManager.registerListener(this, new PremiumPreLoginListener());
        pluginManager.registerListener(this, new ServerConnectListener());

        pluginManager.registerCommand(this, new AuthMePremiumCommand(messages, dataProvider));
        pluginManager.registerCommand(this, new PremiumCommand(messages, dataProvider));
        pluginManager.registerCommand(this, new PremiumUUIDCommand(messages, dataProvider));
    }

    @Override
    public void onDisable() {
        getProxy().getPluginManager().unregisterListeners(this);
        getProxy().getPluginManager().unregisterCommands(this);
    }
}