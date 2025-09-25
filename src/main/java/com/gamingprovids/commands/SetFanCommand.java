package com.gamingprovids.commands;

import com.gamingprovids.utils.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.io.FileWriter;

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

        // Defer reply for safety
        event.deferReply(true).queue();

        // Get the speed option
        Integer speedOption = event.getOption("speed") != null ?
                (int) event.getOption("speed").getAsLong() : null;

        if (speedOption == null || speedOption < 0 || speedOption > 100) {
            event.getHook().sendMessage("❌ Speed must be between 0 and 100").queue();
            return;
        }

        try (FileWriter fw = new FileWriter("/sys/class/thermal/cooling_device0/cur_state")) {
            fw.write(String.valueOf(speedOption));
            event.getHook().sendMessage("✅ Fan speed set to " + speedOption + "%").queue();
        } catch (Exception e) {
            event.getHook().sendMessage("❌ Failed to set fan speed. Ensure bot has proper permissions.")
                    .queue();
            e.printStackTrace();
        }
    }
}
