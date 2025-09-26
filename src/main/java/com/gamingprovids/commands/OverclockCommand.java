package com.gamingprovids.commands;

import com.gamingprovids.utils.PiUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class OverclockCommand extends ListenerAdapter {

    public static CommandData getCommand() {
        return Commands.slash("overclock", "Check CPU and GPU overclock settings");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("overclock")) return;

        event.deferReply(true).queue();

        double cpuOC = PiUtils.getCpuOverclock(); // In MHz
        double gpuOC = PiUtils.getGpuOverclock(); // In MHz

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("⚡ Raspberry Pi Overclock Status")
                .addField("CPU Overclock", cpuOC > 0 ? String.format("%.0f MHz", cpuOC) : "Unknown", true)
                .addField("GPU Overclock", gpuOC > 0 ? String.format("%.0f MHz", gpuOC) : "Unknown", true)
                .setColor(Color.ORANGE);

        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }
}
