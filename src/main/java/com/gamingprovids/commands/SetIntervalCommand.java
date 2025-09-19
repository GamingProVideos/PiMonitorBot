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

        int minutes = event.getOption("minutes").getAsInt();
        if (minutes >= 1) {
            Config.setIntervalMinutes(minutes);
            event.reply("✅ Auto-report interval set to " + minutes + " minutes.").queue();

            TextChannel channel = event.getJDA().getTextChannelById(Config.getChannelId());
            if (channel != null) AutoReporter.restart(event.getJDA());
        } else {
            event.reply("⚠️ Interval must be at least 1 minute.").setEphemeral(true).queue();
        }
    }
}
