package com.gamingprovids.commands;

import com.gamingprovids.utils.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class SetWarnCommand extends ListenerAdapter {

    public static CommandData getCommand() {
        return Commands.slash("setwarn", "Set warning threshold in °C")
                .addOption(OptionType.NUMBER, "temperature", "Temperature in °C", true);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("setwarn")) return;

        double threshold = event.getOption("temperature").getAsDouble();
        Config.setWarnThreshold(threshold);
        event.reply("✅ Warning threshold set to " + threshold + " °C.").queue();
    }
}
