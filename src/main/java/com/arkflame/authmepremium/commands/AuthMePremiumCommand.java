package com.arkflame.authmepremium.commands;

import com.arkflame.authmepremium.providers.DataProvider;

import net.md_5.bungee.api.CommandSender;
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
            sender.sendMessage(messages.getString(MESSAGE_PREFIX + "no_permission"));
            return;
        }

        if (args.length == 0) {
            sendUsageMessage(sender);
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "setpremium":
                if (args.length < 3) {
                    sendUsageMessage(sender);
                    return;
                }
                String setPremiumPlayer = args[1];
                boolean status = Boolean.parseBoolean(args[2]);
                dataProvider.setPremium(setPremiumPlayer, status);
                sender.sendMessage(messages.getString(MESSAGE_PREFIX + "setpremium_success")
                        .replace("%player%", setPremiumPlayer)
                        .replace("%status%", String.valueOf(status)));
                break;
            case "check":
                if (args.length < 2) {
                    sendUsageMessage(sender);
                    return;
                }
                String checkPlayer = args[1];
                boolean isPremium = dataProvider.getPremium(checkPlayer);
                if (isPremium) {
                    sender.sendMessage(messages.getString(MESSAGE_PREFIX + "check_premium")
                            .replace("%player%", checkPlayer));
                } else {
                    sender.sendMessage(messages.getString(MESSAGE_PREFIX + "check_nonpremium")
                            .replace("%player%", checkPlayer));
                }
                break;
            case "clear":
                if (args.length == 1) {
                    // Logic for clearing all player data
                    dataProvider.clear();
                    sender.sendMessage(messages.getString(MESSAGE_PREFIX + "clear_all_success"));
                } else if (args.length == 2) {
                    String clearPlayer = args[1];
                    // Logic for clearing data of specific player
                    dataProvider.clear(clearPlayer);
                    sender.sendMessage(messages.getString(MESSAGE_PREFIX + "clear_player_success")
                            .replace("%player%", clearPlayer));
                } else {
                    sendUsageMessage(sender);
                }
                break;
            default:
                sendUsageMessage(sender);
                break;
        }
    }

    private void sendUsageMessage(CommandSender sender) {
        sender.sendMessage(messages.getString(MESSAGE_PREFIX + "usage"));
    }
}
