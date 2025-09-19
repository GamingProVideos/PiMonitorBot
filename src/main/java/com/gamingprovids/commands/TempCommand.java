package com.gamingprovids.commands;

import com.gamingprovids.utils.PiUtils;
import com.gamingprovids.utils.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class TempCommand extends ListenerAdapter {

    public static CommandData getCommand() {
        return Commands.slash("temp", "Show current Raspberry Pi temperature and fan speed");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("temp")) return;

        double tempC = PiUtils.getPiTemperature();
        double fanPercent = PiUtils.getFanPercentage();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🌡️ Raspberry Pi Status")
                .addField("Temperature", String.format("%.1f °C", tempC), true)
                .addField("Fan Speed", (fanPercent >= 0 ? String.format("%.0f%%", fanPercent) : "Unknown"), true)
                .setColor(tempC >= Config.getWarnThreshold() ? Color.RED : Color.GREEN);

        event.replyEmbeds(embed.build()).queue();
    }
}
