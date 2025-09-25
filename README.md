# PiMonitorBot

[![GitHub Release](https://img.shields.io/github/v/release/GamingProVideos/PiMonitorBot?color=green)](https://github.com/GamingProVideos/PiMonitorBot/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

PiMonitorBot is a Discord bot designed to monitor your Raspberry Pi's temperature and automatically report it to a Discord channel. It supports auto-restart, configurable thresholds, and runs as a systemd service.

---

## Features

- Monitors CPU and GPU temperature on Raspberry Pi.
- Sends automated updates to a Discord channel.
- Warns when temperature exceeds a configurable threshold.
- Allows setting fan speed as a percentage (0–100%), safely mapped to Raspberry Pi fan states.
- Auto-restarts on crash.
- Systemd service for running on boot.
- Easy installation with a single `install.sh`.

---


[//]: # (## Note for Setting Fan Speed to Run `tee` on the Fan File Without a Password)

[//]: # ()
[//]: # (To allow your bot user to run `tee` on the fan control file without a password:)

[//]: # ()
[//]: # (1. Open sudoers for editing:)

[//]: # ()
[//]: # (    ```bash)

[//]: # (    sudo visudo)

[//]: # (    ```)

[//]: # ()
[//]: # (2. Add this line &#40;replace `pi` with your bot user&#41;:)

[//]: # ()
[//]: # (    ```)

[//]: # (    pi ALL=&#40;ALL&#41; NOPASSWD: /usr/bin/tee /sys/class/thermal/cooling_device0/cur_state)

[//]: # (    ```)

[//]: # ()
[//]: # (This avoids running the entire bot as root, which is much safer. The admin role check remains intact.)

[//]: # ()
[//]: # (---)

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

To update the bot while keeping your configuration, use the update.sh script:
# Using curl
curl -sSL https://github.com/GamingProVideos/PiMonitorBot/raw/master/update.sh -o update.sh
chmod +x update.sh
./update.sh

# Or using wget
wget https://github.com/GamingProVideos/PiMonitorBot/raw/master/update.sh -O update.sh
chmod +x update.sh
./update.sh