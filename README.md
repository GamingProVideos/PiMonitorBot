# PiMonitorBot

PiMonitorBot is a Discord bot designed to monitor your Raspberry Pi's temperature and automatically report it to a Discord channel. It supports auto-restart, configurable thresholds, and runs as a systemd service.

---

## Features

- Monitors CPU temperature on Raspberry Pi.
- Sends automated updates to a Discord channel.
- Warns when temperature exceeds a configurable threshold.
- Auto-restarts on crash.
- Systemd service for running on boot.
- Easy installation with a single `install.sh`.

---

## Installation

### Requirements

- Raspberry Pi running Raspberry Pi OS or compatible Linux.
- Java 21 installed.
- Discord bot token.
- Maven (for building from source, optional if using prebuilt JAR).

### Using the Installer Script

You can install PiMonitorBot easily using the provided `install.sh` script:

```bash
# Using curl
curl -sSL https://github.com/GamingProVideos/PiMonitorBot/raw/master/install.sh -o install.sh
chmod +x install.sh
./install.sh

# Or using wget
wget https://github.com/GamingProVideos/PiMonitorBot/raw/master/install.sh -O install.sh
chmod +x install.sh
./install.sh
