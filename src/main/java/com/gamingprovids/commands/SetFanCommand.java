package com.gamingprovids.commands;

import com.gamingprovids.utils.Config;
import com.gamingprovids.utils.FanController;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SetFanCommand extends ListenerAdapter {

    public static CommandData getCommand() {
        OptionData modeOption = new OptionData(OptionType.STRING, "mode", "Choose manual or auto", true)
                .addChoice("manual", "manual")
                .addChoice("auto", "auto");

        OptionData speedOption = new OptionData(OptionType.INTEGER, "speed", "Fan speed 0-100 (manual only)", false);

        return Commands.slash("setfan", "Control Raspberry Pi fan")
                .addOptions(modeOption, speedOption);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("setfan")) return;

        // Check admin role
        boolean hasRole = event.getMember() != null &&
                event.getMember().getRoles().stream()
                        .anyMatch(r -> r.getId().equals(Config.getAllowedRoleId()));
        if (!hasRole) {
            event.reply("⛔ You don’t have permission.").setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();

        String mode = event.getOption("mode").getAsString();
        if (mode.equalsIgnoreCase("manual")) {
            OptionMapping speedOption = event.getOption("speed");
            if (speedOption == null) {
                event.getHook().sendMessage("❌ Provide speed 0-100 for manual mode").queue();
                return;
            }
            int percent = (int) speedOption.getAsLong();
            if (percent < 0 || percent > 100) {
                event.getHook().sendMessage("❌ Speed must be 0-100").queue();
                return;
            }
            FanController.setManualPercent(percent);
            event.getHook().sendMessage("✅ Manual fan set to " + percent + "%").queue();
        } else if (mode.equalsIgnoreCase("auto")) {
            FanController.enableAuto();
            event.getHook().sendMessage("✅ Auto fan mode enabled").queue();
        }
    }
}
