package com.arkflame.authmepremium.hook;

import java.util.UUID;

import org.geysermc.floodgate.api.FloodgateApi;

import com.arkflame.authmepremium.AuthMePremiumPlugin;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * BungeeCord Floodgate hook to detect Bedrock (Floodgate) players and interact
 * with Floodgate API.
 */
public class FloodgateHook {

    private final FloodgateApi floodgateApi;

    /**
     * Constructor attempts to obtain the FloodgateApi instance.
     * If Floodgate is not installed or not yet available, floodgateApi will be
     * null.
     */
    public FloodgateHook() {
        if (AuthMePremiumPlugin.getInstance().isFloodgateEnabled()
                && BungeeCord.getInstance().getPluginManager().getPlugin("floodgate") != null) {
            this.floodgateApi = FloodgateApi.getInstance();
        } else {
            this.floodgateApi = null;
            ProxyServer.getInstance().getLogger().warning(
                    "[AuthMePremium] Floodgate API not found or not enabled! Bedrock detection will be disabled.");
        }
    }

    /**
     * Checks whether the given UUID belongs to a Floodgate (Bedrock) player.
     *
     * @param uuid the UUID of the player to check
     * @return true if the player is authenticated via Floodgate (Bedrock), false
     *         otherwise
     */
    public boolean isFloodgatePlayer(String name, UUID uuid) {
        if (uuid != null) {
            if (floodgateApi == null) {
                return false;
            }
            try {
                return floodgateApi.isFloodgatePlayer(uuid);
            } catch (Exception e) {
                return false;
            }
        }
        // Check name for asterisk
        if (name != null) {
            return name.startsWith("*");
        }
        return false;
    }

    /**
     * Convenience method: checks if the given ProxiedPlayer is a Bedrock user.
     *
     * @param player the Bungee ProxiedPlayer instance
     * @return true if the player is authenticated via Floodgate
     */
    public boolean isBedrockPlayer(ProxiedPlayer player) {
        return isFloodgatePlayer(player.getName(), player.getUniqueId());
    }
}
