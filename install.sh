#!/bin/bash
set -e

# Directory for bot
BOT_DIR="$HOME/PiMonitorBot"
mkdir -p "$BOT_DIR"
cd "$BOT_DIR"

# Download latest prebuilt JAR from GitHub release
JAR_URL="https://github.com/GamingProVideos/PiMonitorBot/releases/latest/download/PiMonitorBot-1.0-SNAPSHOT.jar"
echo "Downloading PiMonitorBot.jar..."
wget -O PiMonitorBot.jar "$JAR_URL"

# Prompt user for configuration variables
echo "Let's configure your PiMonitorBot:"
read -p "Enter your BOT_TOKEN: " BOT_TOKEN
read -p "Enter your GUILD_ID: " GUILD_ID
read -p "Enter your CHANNEL_ID: " CHANNEL_ID
read -p "Enter INTERVAL_MINUTES (default 5): " INTERVAL_MINUTES
INTERVAL_MINUTES=${INTERVAL_MINUTES:-5}
read -p "Enter WARN_THRESHOLD (default 70.0): " WARN_THRESHOLD
WARN_THRESHOLD=${WARN_THRESHOLD:-70.0}

# Create .env file using variable references
cat > .env <<'EOF'
BOT_TOKEN=$BOT_TOKEN
GUILD_ID=$GUILD_ID
CHANNEL_ID=$CHANNEL_ID
INTERVAL_MINUTES=$INTERVAL_MINUTES
WARN_THRESHOLD=$WARN_THRESHOLD
EOF

echo ".env created with variable references."

# Create bot.sh to start the bot with auto-restart
cat > bot.sh << 'EOF'
#!/bin/bash
cd "$(dirname "$0")"

# Load environment variables
export $(grep -v '^#' .env | xargs)

while true; do
  java -jar PiMonitorBot.jar
  echo "Bot crashed. Restarting in 5 seconds..."
  sleep 5
done
EOF

chmod +x bot.sh
echo "bot.sh created and made executable."

# Set up systemd service
SERVICE_FILE="/etc/systemd/system/pimonitorbot.service"
sudo bash -c "cat > $SERVICE_FILE" <<EOF
[Unit]
Description=PiMonitorBot Service
After=network.target

[Service]
Type=simple
User=$USER
WorkingDirectory=$BOT_DIR
ExecStart=$BOT_DIR/bot.sh
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable pimonitorbot
sudo systemctl start pimonitorbot

echo "PiMonitorBot systemd service installed and started!"
echo "Check status: sudo systemctl status pimonitorbot"
echo "Manual start: $BOT_DIR/bot.sh"
