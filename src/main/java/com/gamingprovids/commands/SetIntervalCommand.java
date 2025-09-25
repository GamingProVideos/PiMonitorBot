package com.gamingprovids.commands;

import com.gamingprovids.utils.Config;
import com.gamingprovids.utils.AutoReporter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class SetIntervalCommand extends ListenerAdapter {

    public static CommandData getCommand() {
        return Commands.slash("setinterval", "Set auto-report interval in minutes")
                .addOption(OptionType.INTEGER, "minutes", "Interval in minutes", true);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("setinterval")) return;

        // Check admin role
        String allowedRoleId = Config.getAllowedRoleId();
        boolean hasRole = event.getMember() != null &&
                event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(allowedRoleId));

        if (!hasRole) {
            event.reply("⛔ You don’t have permission to set the auto-report interval.")
                    .setEphemeral(true).queue();
            return;
        }

        // Defer reply for safety
        event.deferReply(true).queue();

        // Get minutes option
        Integer minutes = event.getOption("minutes") != null ?
                event.getOption("minutes").getAsInt() : null;

        if (minutes == null || minutes < 1) {
            event.getHook().sendMessage("⚠️ Interval must be at least 1 minute.").queue();
            return;
        }

        // Set interval in config
        Config.setIntervalMinutes(minutes);

        // Restart AutoReporter if channel exists
        TextChannel channel = event.getJDA().getTextChannelById(Config.getChannelId());
        if (channel != null) AutoReporter.restart(event.getJDA());

        event.getHook().sendMessage("✅ Auto-report interval set to " + minutes + " minutes.").queue();
    }
}
