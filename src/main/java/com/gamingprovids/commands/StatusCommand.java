package com.gamingprovids.commands;

import com.gamingprovids.utils.Config;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class StatusCommand extends ListenerAdapter {

    public static CommandData getCommand() {
        return Commands.slash("status", "Show current settings or change bot status/activity")
                .addOption(OptionType.STRING, "state", "Change bot status (online, idle, dnd, offline)", false)
                .addOption(OptionType.STRING, "activity", "Set bot activity text", false);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("status")) return;

        // Defer reply
        event.deferReply(true).queue();

        if (event.getMember() == null) {
            event.getHook().sendMessage("⛔ You must run this command in a server.").queue();
            return;
        }

        String stateArg = event.getOption("state") != null ? event.getOption("state").getAsString().toLowerCase() : null;
        String activityArg = event.getOption("activity") != null ? event.getOption("activity").getAsString() : null;

        // Check role if changing status/activity
        String allowedRoleId = Config.getAllowedRoleId();
        boolean hasRole = event.getMember().getRoles().stream()
                .anyMatch(r -> r.getId().equals(allowedRoleId));

        if ((stateArg != null || activityArg != null) && !hasRole) {
            event.getHook().sendMessage("⛔ You don’t have permission to change my status or activity.").queue();
            return;
        }

        // Update status
        if (stateArg != null) {
            OnlineStatus newStatus;
            switch (stateArg) {
                case "online" -> newStatus = OnlineStatus.ONLINE;
                case "idle" -> newStatus = OnlineStatus.IDLE;
                case "dnd", "donotdisturb" -> newStatus = OnlineStatus.DO_NOT_DISTURB;
                case "offline", "invisible" -> newStatus = OnlineStatus.INVISIBLE;
                default -> {
                    event.getHook().sendMessage("❌ Invalid status. Use: `online`, `idle`, `dnd`, `offline`.").queue();
                    return;
                }
            }
            event.getJDA().getPresence().setStatus(newStatus);
        }

        // Update activity
        if (activityArg != null) {
            event.getJDA().getPresence().setActivity(Activity.playing(activityArg));
        }

        // Show current settings if no args
        if (stateArg == null && activityArg == null) {
            String reply = String.format(
                    "**Current Settings**\n" +
                            "Interval: %d minutes\n" +
                            "Warning Threshold: %.1f °C\n" +
                            "Bot Version: %s",
                    Config.getIntervalMinutes(),
                    Config.getWarnThreshold(),
                    Config.CURRENT_VERSION
            );
            event.getHook().sendMessage(reply).queue();
        } else {
            event.getHook().sendMessage("✅ Bot status/activity updated successfully.").queue();
        }
    }
}
