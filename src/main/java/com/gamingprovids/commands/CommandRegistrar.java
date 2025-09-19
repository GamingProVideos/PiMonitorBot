package com.gamingprovids.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandRegistrar {
    public static void registerCommands(Guild guild) {
        guild.updateCommands().addCommands(
                Commands.slash("temp", "Show current Raspberry Pi temperature and fan speed"),
                Commands.slash("setinterval", "Set auto-report interval in minutes")
                        .addOption(OptionType.INTEGER, "minutes", "Interval in minutes", true),
                Commands.slash("setwarn", "Set warning threshold in °C")
                        .addOption(OptionType.NUMBER, "temperature", "Temperature in °C", true),
                Commands.slash("status", "Show current settings"),
                Commands.slash("checkupdate", "Check if a new version of PiMonitorBot is available")
        ).queue();
    }
}
