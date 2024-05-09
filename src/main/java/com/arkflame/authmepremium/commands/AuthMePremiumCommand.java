package com.arkflame.authmepremium.commands;

import com.arkflame.authmepremium.AuthMePremiumPlugin;
import com.arkflame.authmepremium.providers.DataProvider;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

public class AuthMePremiumCommand extends Command {
    private final Configuration messages;
    private final DataProvider dataProvider;
    private static final String MESSAGE_PREFIX = "messages.";

    public AuthMePremiumCommand(Configuration messages, DataProvider dataProvider) {
        super("authmepremium");
        this.messages = messages;
        this.dataProvider = dataProvider;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("authmepremium.usage")) {
            sendMessage(sender, "authme_premium_no_permission", "%version%", AuthMePremiumPlugin.getInstance().getDescription().getVersion());
            return;
        }

        if (args.length == 0) {
            sendUsageMessage(sender);
            return;
        }

        String subCommand = args[0].toLowerCase();

        BungeeCord.getInstance().getScheduler().runAsync(AuthMePremiumPlugin.getInstance(), () -> {
            switch (subCommand) {
                case "setpremium":
                    if (args.length < 3) {
                        sendUsageMessage(sender);
                        return;
                    }
                    String setPremiumPlayer = args[1];
                    boolean status = Boolean.parseBoolean(args[2]);
                    dataProvider.setPremium(setPremiumPlayer, status);
                    sendMessage(sender, "setpremium_success",
                            "%player%", setPremiumPlayer,
                            "%status%", String.valueOf(status));
                    break;
                case "check":
                    if (args.length < 2) {
                        sendUsageMessage(sender);
                        return;
                    }
                    String checkPlayer = args[1];
                    Boolean isPremium = dataProvider.getPremium(checkPlayer);
                    if (isPremium != null && isPremium) {
                        sendMessage(sender, "check_premium", "%player%", checkPlayer);
                    } else {
                        sendMessage(sender, "check_nonpremium", "%player%", checkPlayer);
                    }
                    break;
                case "clear":
                    if (args.length == 1) {
                        // Logic for clearing all player data
                        dataProvider.clear();
                        sendMessage(sender, "clear_all_success");
                    } else if (args.length == 2) {
                        String clearPlayer = args[1];
                        // Logic for clearing data of specific player
                        dataProvider.clear(clearPlayer);
                        sendMessage(sender, "clear_player_success", "%player%", clearPlayer);
                    } else {
                        sendUsageMessage(sender);
                    }
                    break;
                case "setpremiumuuid": // New sub-command to set premium_uuid
                    if (args.length < 3) {
                        sendUsageMessage(sender);
                        return;
                    }
                    String playerName = args[1];
                    boolean premiumUUIDStatus = Boolean.parseBoolean(args[2]);
                    // Logic to update premium_uuid status in dataProvider
                    dataProvider.setPremiumUUID(playerName, premiumUUIDStatus);
                    sendMessage(sender, "setpremiumuuid_success",
                            "%player%", playerName,
                            "%status%", String.valueOf(premiumUUIDStatus));
                    break;
                case "reload": {
                    AuthMePremiumPlugin.getInstance().onDisable();
                    AuthMePremiumPlugin.getInstance().onEnable();
                    sendMessage(sender, "reloaded");
                    break;
                }
                default:
                    sendUsageMessage(sender);
                    break;
            }
        });
    }

    private void sendUsageMessage(CommandSender sender) {
        sendMessage(sender, "authme_premium_usage");
    }

    private void sendMessage(CommandSender sender, String key, String... replacements) {
        String message = messages.getString(MESSAGE_PREFIX + key);
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
    }
}
