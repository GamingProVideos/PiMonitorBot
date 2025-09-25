package com.gamingprovids;

import com.gamingprovids.commands.*;
import com.gamingprovids.utils.Config;
import com.gamingprovids.utils.AutoReporter;
import com.gamingprovids.utils.UpdateChecker;
import com.gamingprovids.utils.PiUtils;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Guild;

import javax.security.auth.login.LoginException;
import java.util.Timer;
import java.util.TimerTask;

public class PiMonitorBot extends ListenerAdapter {

    private AutoReporter autoReporter;
    private UpdateChecker updateChecker;

    public static void main(String[] args) throws LoginException {
        JDABuilder.createDefault(Config.getToken())
                .addEventListeners(new PiMonitorBot())
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

        // 🔹 Update activity with CPU & GPU temps every 30 seconds
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double cpuTemp = PiUtils.getCpuTemperature();
                double gpuTemp = PiUtils.getGpuTemperature();
                event.getJDA().getPresence().setActivity(
                        Activity.watching(String.format("CPU: %.1f°C | GPU: %.1f°C", cpuTemp, gpuTemp))
                );
            }
        }, 0, 30 * 1000L); // every 30 seconds
    }
}
