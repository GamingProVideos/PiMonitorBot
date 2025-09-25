package com.gamingprovids.commands;

import com.gamingprovids.utils.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

public class SetFanCommand extends ListenerAdapter {

    public static CommandData getCommand() {
        return Commands.slash("setfan", "Set Raspberry Pi fan speed (0-100%)")
                .addOption(OptionType.INTEGER, "speed", "Fan speed percentage", true);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("setfan")) return;

        // Check admin role
        String allowedRoleId = Config.getAllowedRoleId();
        boolean hasRole = event.getMember() != null &&
                event.getMember().getRoles().stream()
                        .anyMatch(r -> r.getId().equals(allowedRoleId));

        if (!hasRole) {
            event.reply("⛔ You don’t have permission to set the fan speed.")
                    .setEphemeral(true).queue();
            return;
        }

        // Defer reply
        event.deferReply(true).queue();

        // Get speed option
        Integer speed = event.getOption("speed") != null ?
                (int) event.getOption("speed").getAsLong() : null;

        if (speed == null || speed < 0 || speed > 100) {
            event.getHook().sendMessage("❌ Speed must be between 0 and 100").queue();
            return;
        }

        try {
            // Run 'sudo tee' to write to the fan control file
            ProcessBuilder pb = new ProcessBuilder("sudo", "tee", "/sys/class/thermal/cooling_device0/cur_state");
            Process process = pb.start();

            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                bw.write(String.valueOf(speed));
                bw.flush();
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                event.getHook().sendMessage("✅ Fan speed set to " + speed + "%").queue();
            } else {
                event.getHook().sendMessage("❌ Failed to set fan speed. Ensure 'sudo tee' is allowed for the bot user.").queue();
            }

        } catch (Exception e) {
            event.getHook().sendMessage("❌ Failed to set fan speed. Ensure bot has proper permissions.").queue();
            e.printStackTrace();
        }
    }
}
