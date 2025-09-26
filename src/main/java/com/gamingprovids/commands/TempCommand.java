package com.gamingprovids.commands;

import com.gamingprovids.utils.PiUtils;
import com.gamingprovids.utils.Config;
import com.gamingprovids.utils.FanController;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class TempCommand extends ListenerAdapter {

    public static CommandData getCommand() {
        return Commands.slash("temp", "Show current Raspberry Pi CPU, GPU temperatures and fan speed");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("temp")) return;

        event.deferReply(true).queue();

        double cpuTemp = PiUtils.getCpuTemperature();
        double gpuTemp = PiUtils.getGpuTemperature();
        double fanPercent = PiUtils.getFanPercentageFromRpm();

        // Get auto fan status and current fan percent
        boolean autoFan = FanController.isAuto();
        int currentFanPercent = FanController.getCurrentPercent();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🌡️ Raspberry Pi Status")
                .addField("CPU Temperature", cpuTemp >= 0 ? String.format("%.1f °C", cpuTemp) : "Unknown", true)
                .addField("GPU Temperature", gpuTemp >= 0 ? String.format("%.1f °C", gpuTemp) : "Unknown", true)
                .addField("Fan Speed (Target)", currentFanPercent + "%", true)
                .addField("Fan Speed (Actual)", fanPercent >= 0 ? String.format("%.0f%%", fanPercent) : "Unknown", true)
                .addField("Auto Fan Mode", autoFan ? "✅ Enabled" : "❌ Disabled", true)
                .setColor(cpuTemp >= Config.getWarnThreshold() ? Color.RED : Color.GREEN);

        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }
}
