package com.gamingprovids;

import com.gamingprovids.commands.*;
import com.gamingprovids.utils.*;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PiMonitorBot extends ListenerAdapter {

    private AutoReporter autoReporter;
    private UpdateChecker updateChecker;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) throws LoginException {
        JDABuilder builder = JDABuilder.createDefault(Config.getToken());

        // Add main bot listener
        PiMonitorBot botListener = new PiMonitorBot();
        builder.addEventListeners(botListener);

        // Add all command listeners
        builder.addEventListeners(
                new TempCommand(),
                new SetIntervalCommand(),
                new SetWarnCommand(),
                new StatusCommand(),
                new CheckUpdateCommand(),
                new SetFanCommand(),
                new RebootPiCommand()
        );

        builder.build();
    }

    @Override
    public void onReady(net.dv8tion.jda.api.events.session.ReadyEvent event) {
        System.out.println("✅ Bot is online!");

        Guild guild = event.getJDA().getGuildById(Config.getGuildId());
        if (guild == null) {
            System.err.println("❌ Guild not found! Check Config.getGuildId()");
            return;
        }

        // Register all slash commands
        guild.updateCommands().addCommands(
                TempCommand.getCommand(),
                SetIntervalCommand.getCommand(),
                SetWarnCommand.getCommand(),
                StatusCommand.getCommand(),
                CheckUpdateCommand.getCommand(),
                SetFanCommand.getCommand(),
                RebootPiCommand.getCommand()
        ).queue(
                success -> System.out.println("✅ Commands registered successfully!"),
                error -> System.err.println("❌ Failed to register commands: " + error.getMessage())
        );

        // Start AutoReporter
        autoReporter = new AutoReporter(event.getJDA());
        autoReporter.start();

        // Start UpdateChecker
        updateChecker = new UpdateChecker(event.getJDA());
        updateChecker.checkForUpdates();

        // Update bot activity every 30 seconds
        scheduler.scheduleAtFixedRate(() -> {
            double cpuTemp = PiUtils.getCpuTemperature();
            double gpuTemp = PiUtils.getGpuTemperature();
            event.getJDA().getPresence().setActivity(
                    Activity.watching(String.format("CPU: %.1f°C | GPU: %.1f°C", cpuTemp, gpuTemp))
            );
        }, 0, 30, TimeUnit.SECONDS);
    }
}
