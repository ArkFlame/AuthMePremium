package com.arkflame.authmepremium.commands;

import com.arkflame.authmepremium.AuthMePremiumPlugin;
import com.arkflame.authmepremium.providers.DataProvider;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

public class PremiumCommand extends Command {
    private final Configuration messages;
    private final DataProvider dataProvider;
    private static final String MESSAGE_PREFIX = "messages.";

    public PremiumCommand(Configuration messages, DataProvider dataProvider) {
        super("premium");
        this.messages = messages;
        this.dataProvider = dataProvider;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("authmepremium.premium")) {
            sendMessage(sender, "no_permission");
            return;
        }

        if (!(sender instanceof ProxiedPlayer)) {
            sendMessage(sender, "no_console");
            return;
        }

        BungeeCord.getInstance().getScheduler().runAsync(AuthMePremiumPlugin.getInstance(), () -> {
            String name = sender.getName();
            Boolean isPremium = dataProvider.getPremium(name);
            if (isPremium == null) {
                isPremium = false;
            }
            boolean newStatus = !isPremium;
            dataProvider.setPremium(name, newStatus);
            sendMessage(sender, "premium_success", "%status%", String.valueOf(newStatus));
        });
    }

    private void sendMessage(CommandSender sender, String key, String... replacements) {
        String message = messages.getString(MESSAGE_PREFIX + key);
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
    }
}
