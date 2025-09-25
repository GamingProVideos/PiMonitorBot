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

        // Check admin role
        String allowedRoleId = Config.getAllowedRoleId();
        boolean hasRole = event.getMember() != null &&
                event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(allowedRoleId));

        if (!hasRole) {
            event.reply("⛔ You don’t have permission to set the warning threshold.")
                    .setEphemeral(true).queue();
            return;
        }

        // Defer reply
        event.deferReply(true).queue();

        // Get temperature option
        Double threshold = event.getOption("temperature") != null ?
                event.getOption("temperature").getAsDouble() : null;

        if (threshold == null || threshold <= 0) {
            event.getHook().sendMessage("❌ Threshold must be a positive number.").queue();
            return;
        }

        // Set threshold in config
        Config.setWarnThreshold(threshold);

        event.getHook().sendMessage("✅ Warning threshold set to " + threshold + " °C.").queue();
    }
}
