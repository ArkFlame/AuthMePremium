package com.arkflame.authmepremium.providers;

import com.arkflame.authmepremium.managers.ConfigManager;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.HashMap;
import java.util.Map;

public class YAMLProvider implements DataProvider {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private final Map<String, Configuration> playerConfigs;

    public YAMLProvider(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.playerConfigs = new HashMap<>();
    }

    @Override
    public Boolean getPremium(String name) {
        if (!playerConfigs.containsKey(name)) {
            loadPlayerConfig(name);
        }
        Configuration config = playerConfigs.get(name);
        if (config.contains("premium")) {
            return config.getBoolean("premium", false);
        } else {
            return null;
        }
    }

    @Override
    public void setPremium(String name, boolean premium) {
        Configuration config = getPlayerConfig(name);
        config.set("premium", premium);
        savePlayerConfig(name, config);
    }

    private Configuration getPlayerConfig(String name) {
        if (!playerConfigs.containsKey(name)) {
            loadPlayerConfig(name);
        }
        return playerConfigs.get(name);
    }

    private void loadPlayerConfig(String name) {
        Configuration config = configManager.load(plugin, "users/" + name + ".yml");
        if (config == null) {
            config = new Configuration();
        }
        playerConfigs.put(name, config);
    }

    private void savePlayerConfig(String name, Configuration config) {
        configManager.save(plugin, "users/" + name + ".yml", config);
    }
}
