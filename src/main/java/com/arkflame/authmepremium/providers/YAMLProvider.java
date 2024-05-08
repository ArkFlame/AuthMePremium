package com.arkflame.authmepremium.providers;

import com.arkflame.authmepremium.managers.ConfigManager;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.io.File;
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

    @Override
    public void clear(String name) {
        File file = new File(plugin.getDataFolder(), "users/" + name + ".yml");
        if (file.exists()) {
            file.delete();
        }
        playerConfigs.remove(name);
    }

    @Override
    public void clear() {
        playerConfigs.clear();
        File folder = new File(plugin.getDataFolder(), "users");
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }
}
