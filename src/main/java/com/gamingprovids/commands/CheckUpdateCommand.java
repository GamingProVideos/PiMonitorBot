package com.gamingprovids.commands;

import com.gamingprovids.utils.UpdateChecker;
import com.gamingprovids.utils.Config;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CheckUpdateCommand extends ListenerAdapter {

    public static CommandData getCommand() {
        return Commands.slash("checkupdate", "Check if a new version of PiMonitorBot is available");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("checkupdate")) return;

        System.out.println("Slash command triggered: checkupdate");

        TextChannel channel = event.getJDA().getTextChannelById(Config.getChannelId());
        if (channel == null) {
            event.reply("❌ Channel not found!").setEphemeral(true).queue();
            return;
        }

        new UpdateChecker(event.getJDA()).checkForUpdates();
        event.reply("🔍 Checking for updates...").setEphemeral(true).queue();
    }
}
