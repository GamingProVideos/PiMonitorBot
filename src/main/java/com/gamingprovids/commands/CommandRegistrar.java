package com.gamingprovids.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandRegistrar {

    public static void registerCommands(Guild guild) {
        if (guild == null) {
            System.err.println("❌ Guild is null. Cannot register commands!");
            return;
        }

        System.out.println("Registering commands for guild: " + guild.getName());

        guild.updateCommands().addCommands(
                Commands.slash("temp", "Show current Raspberry Pi temperature and fan speed"),
                Commands.slash("setinterval", "Set auto-report interval in minutes")
                        .addOption(OptionType.INTEGER, "minutes", "Interval in minutes", true),
                Commands.slash("setwarn", "Set warning threshold in °C")
                        .addOption(OptionType.NUMBER, "temperature", "Temperature in °C", true),
                Commands.slash("status", "Show current settings"),
                Commands.slash("checkupdate", "Check if a new version of PiMonitorBot is available"),
                Commands.slash("setfan", "Set fan speed (0-100%)")
                        .addOption(OptionType.INTEGER, "speed", "Fan speed percentage", true),
                Commands.slash("rebootpi", "Reboot the Raspberry Pi")
        ).queue(
                success -> System.out.println("✅ Commands registered successfully!"),
                error -> System.err.println("❌ Failed to register commands: " + error.getMessage())
        );
    }
}
