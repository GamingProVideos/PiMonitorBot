package com.gamingprovids.commands;

import com.gamingprovids.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.awt.*;

public class StatusCommand extends ListenerAdapter {

    public static CommandData getCommand() {
        return Commands.slash("status", "Show current settings");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("status")) return;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📊 Current Settings")
                .addField("Interval", Config.getIntervalMinutes() + " minutes", true)
                .addField("Warning Threshold", Config.getWarnThreshold() + " °C", true)
                .addField("Bot Version", Config.CURRENT_VERSION, true)
                .setColor(Color.BLUE);

        event.replyEmbeds(embed.build()).queue();
    }
}
