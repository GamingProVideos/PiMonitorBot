#!/bin/bash
set -e

# Directory for bot
BOT_DIR="$HOME/PiMonitorBot"
SERVICE_NAME="pimonitorbot"

# Stop the bot service
echo "Stopping PiMonitorBot service..."
sudo systemctl stop $SERVICE_NAME

cd "$BOT_DIR"

# Backup current JAR
if [ -f PiMonitorBot.jar ]; then
    echo "Backing up existing PiMonitorBot.jar..."
    mv PiMonitorBot.jar PiMonitorBot.jar.bak
fi

# Download latest prebuilt JAR from GitHub release
echo "Downloading latest PiMonitorBot.jar..."
JAR_URL="https://github.com/GamingProVideos/PiMonitorBot/releases/latest/download/PiMonitorBot-1.0-SNAPSHOT.jar"
wget -O PiMonitorBot.jar "$JAR_URL"
chmod +x PiMonitorBot.jar

# Prompt user if they want to update config
read -p "Do you want to update your configuration? (y/N): " update_config
if [[ "$update_config" =~ ^[Yy]$ ]]; then
    read -p "Enter your BOT_TOKEN (leave blank to keep current): " BOT_TOKEN
    read -p "Enter your GUILD_ID (leave blank to keep current): " GUILD_ID
    read -p "Enter your CHANNEL_ID (leave blank to keep current): " CHANNEL_ID
    read -p "Enter INTERVAL_MINUTES (leave blank to keep current): " INTERVAL_MINUTES
    read -p "Enter WARN_THRESHOLD (leave blank to keep current): " WARN_THRESHOLD

    # Use existing values if left blank
    BOT_TOKEN=${BOT_TOKEN:-$(grep BOT_TOKEN .env | cut -d '=' -f2)}
    GUILD_ID=${GUILD_ID:-$(grep GUILD_ID .env | cut -d '=' -f2)}
    CHANNEL_ID=${CHANNEL_ID:-$(grep CHANNEL_ID .env | cut -d '=' -f2)}
    INTERVAL_MINUTES=${INTERVAL_MINUTES:-$(grep INTERVAL_MINUTES .env | cut -d '=' -f2)}
    WARN_THRESHOLD=${WARN_THRESHOLD:-$(grep WARN_THRESHOLD .env | cut -d '=' -f2)}

    # Update .env file
    cat > .env <<'EOF'
BOT_TOKEN=$BOT_TOKEN
GUILD_ID=$GUILD_ID
CHANNEL_ID=$CHANNEL_ID
INTERVAL_MINUTES=$INTERVAL_MINUTES
WARN_THRESHOLD=$WARN_THRESHOLD
EOF

    echo ".env updated."
fi

# Restart the service
echo "Starting PiMonitorBot service..."
sudo systemctl start $SERVICE_NAME

echo "Update complete!"
echo "Check status: sudo systemctl status $SERVICE_NAME"