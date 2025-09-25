package com.gamingprovids.commands;

import com.gamingprovids.utils.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class RebootPiCommand extends ListenerAdapter {

    public static CommandData getCommand() {
        return Commands.slash("rebootpi", "Reboot this Raspberry Pi (admin only)");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("rebootpi")) return;

        // Check admin role
        String allowedRoleId = Config.getAllowedRoleId();
        boolean hasRole = event.getMember() != null &&
                event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(allowedRoleId));

        if (!hasRole) {
            event.reply("⛔ You don’t have permission to reboot the Pi.").setEphemeral(true).queue();
            return;
        }

        // Defer reply to allow time for reboot
        event.deferReply(true).queue(); // ephemeral defer

        try {
            Runtime.getRuntime().exec("sudo reboot");
            event.getHook().sendMessage("♻️ Reboot command sent successfully!").queue();
        } catch (Exception e) {
            event.getHook().sendMessage("❌ Failed to reboot Pi: " + e.getMessage()).queue();
            e.printStackTrace();
        }
    }
}
