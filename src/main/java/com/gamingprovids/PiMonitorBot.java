package com.gamingprovids;

import com.gamingprovids.commands.*;
import com.gamingprovids.utils.Config;
import com.gamingprovids.utils.AutoReporter;
import com.gamingprovids.utils.UpdateChecker;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Guild;

import javax.security.auth.login.LoginException;

public class PiMonitorBot extends ListenerAdapter {

    private AutoReporter autoReporter;
    private UpdateChecker updateChecker;

    public static void main(String[] args) throws LoginException {
        JDABuilder.createDefault(Config.getToken())
                .addEventListeners(new PiMonitorBot())
                .setActivity(Activity.playing("Temp Monitor"))
                .build();
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("✅ Bot is online!");

        Guild guild = event.getJDA().getGuildById(Config.getGuildId());
        if (guild != null) {
            guild.updateCommands().addCommands(
                    TempCommand.getCommand(),
                    SetIntervalCommand.getCommand(),
                    SetWarnCommand.getCommand(),
                    StatusCommand.getCommand(),
                    CheckUpdateCommand.getCommand()
            ).queue();
        }

        // Start utilities
        autoReporter = new AutoReporter(event.getJDA());
        autoReporter.start();

        updateChecker = new UpdateChecker(event.getJDA());
        updateChecker.checkForUpdates();
    }
}
