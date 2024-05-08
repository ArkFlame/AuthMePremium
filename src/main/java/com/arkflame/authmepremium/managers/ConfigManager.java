package com.arkflame.authmepremium.managers;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ConfigManager {

    public Configuration load(File file) {
        try {
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void save(File file, Configuration config) {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(Plugin plugin, String resource, Configuration config) {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(plugin.getDataFolder(), resource));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateDefaultFromResource(File file, String resourcePath) {
        if (!file.exists()) {
            try {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
                try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath);
                     FileOutputStream out = new FileOutputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Configuration load(Plugin plugin, String resource) {
        File configFile = new File(plugin.getDataFolder(), resource);
        return load(configFile);
    }

    /*
     * Load from plugin folder.
     * Creates resource if not present.
     */
    public Configuration loadDefault(Plugin plugin, String resource) {
        File configFile = new File(plugin.getDataFolder(), resource);
        generateDefaultFromResource(configFile, resource);
        return load(configFile);
    }
}
