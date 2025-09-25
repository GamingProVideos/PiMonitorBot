package com.gamingprovids.commands;

import com.gamingprovids.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.awt.*;

public class StatusCommand extends ListenerAdapter {

    // Define slash command with an optional "state" argument for changing status
    public static CommandData getCommand() {
        return Commands.slash("status", "Show current settings or change bot status")
                .addOption(OptionType.STRING, "state", "Change bot status (online, idle, dnd, offline)", false);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("status")) return;

        String stateArg = event.getOption("state") != null ? event.getOption("state").getAsString().toLowerCase() : null;

        // If stateArg is provided, attempt to change status
        if (stateArg != null) {
            String allowedRoleId = Config.getAllowedRoleId();

            boolean hasRole = event.getMember() != null &&
                    event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(allowedRoleId));

            if (!hasRole) {
                event.reply("⛔ You don’t have permission to change my status.").setEphemeral(true).queue();
                return;
            }

            OnlineStatus newStatus;
            switch (stateArg) {
                case "online" -> newStatus = OnlineStatus.ONLINE;
                case "idle" -> newStatus = OnlineStatus.IDLE;
                case "dnd", "donotdisturb" -> newStatus = OnlineStatus.DO_NOT_DISTURB;
                case "offline", "invisible" -> newStatus = OnlineStatus.INVISIBLE;
                default -> {
                    event.reply("❌ Invalid status. Use: `online`, `idle`, `dnd`, `offline`.").setEphemeral(true).queue();
                    return;
                }
            }

            event.getJDA().getPresence().setStatus(newStatus);
            event.reply("✅ Status changed to **" + newStatus.getKey() + "**").queue();
            return;
        }

        // Otherwise, just show current settings
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📊 Current Settings")
                .addField("Interval", Config.getIntervalMinutes() + " minutes", true)
                .addField("Warning Threshold", Config.getWarnThreshold() + " °C", true)
                .addField("Bot Version", Config.CURRENT_VERSION, true)
                .setColor(Color.BLUE);

        event.replyEmbeds(embed.build()).queue();
    }
}
