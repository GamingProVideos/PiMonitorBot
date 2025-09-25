package com.gamingprovids.commands;

import com.gamingprovids.utils.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
                event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(allowedRoleId));

        if (!hasRole) {
            event.reply("⛔ You don’t have permission to set the fan speed.")
                    .setEphemeral(true).queue();
            return;
        }

        // Defer reply
        event.deferReply(true).queue();

        Integer speedPercent = event.getOption("speed") != null ?
                (int) event.getOption("speed").getAsLong() : null;

        if (speedPercent == null || speedPercent < 0 || speedPercent > 100) {
            event.getHook().sendMessage("❌ Speed must be between 0 and 100").queue();
            return;
        }

        try {
            // Get max_state from Pi
            Process maxStateProc = new ProcessBuilder("cat", "/sys/class/thermal/cooling_device0/max_state")
                    .start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(maxStateProc.getInputStream()));
            int maxState = Integer.parseInt(reader.readLine().trim());
            reader.close();

            // Map percentage to integer state
            int state = (int) Math.round(speedPercent / 100.0 * maxState);

            // Write using sudo tee
            Process proc = new ProcessBuilder("sudo", "tee", "/sys/class/thermal/cooling_device0/cur_state")
                    .redirectErrorStream(true)
                    .start();
            proc.getOutputStream().write(String.valueOf(state).getBytes());
            proc.getOutputStream().flush();
            proc.getOutputStream().close();

            proc.waitFor();

            event.getHook().sendMessage("✅ Fan speed set to " + speedPercent + "% (state " + state + ")").queue();

        } catch (Exception e) {
            event.getHook().sendMessage("❌ Failed to set fan speed. Ensure sudoers allows tee on the fan file.").queue();
            e.printStackTrace();
        }
    }
}
