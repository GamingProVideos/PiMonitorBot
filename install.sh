#!/bin/bash
set -e

echo "=== PiMonitorBot Installer ==="

# Prompt for installation directory
read -p "Enter installation directory (default: $HOME/PiMonitorBot): " BOT_DIR
BOT_DIR=${BOT_DIR:-$HOME/PiMonitorBot}
mkdir -p "$BOT_DIR"
cd "$BOT_DIR"

echo "Installing Zulu OpenJDK 21 (if not already installed)..."
if ! java -version 2>&1 | grep -q '21'; then
    # Add Azul repository key
    sudo mkdir -p /etc/apt/keyrings
    curl -fsSL https://repos.azul.com/azul-repo.key | sudo gpg --dearmor -o /etc/apt/keyrings/azul.gpg

    # Add Zulu repository
    echo "deb [arch=arm64 signed-by=/etc/apt/keyrings/azul.gpg] https://repos.azul.com/zulu/deb stable main" | sudo tee /etc/apt/sources.list.d/zulu.list

    # Update and install
    sudo apt update
    sudo apt install -y zulu21-jdk-headless
else
    echo "Java 21 is already installed."
fi

java -version

# Download the bot JAR
JAR_URL="https://github.com/GamingProVideos/PiMonitorBot/releases/latest/download/PiMonitorBot-1.0-SNAPSHOT.jar"
echo "Downloading PiMonitorBot.jar..."
wget -O PiMonitorBot.jar "$JAR_URL"
chmod +x PiMonitorBot.jar

# Prompt user for configuration variables
echo "Let's configure your PiMonitorBot:"
read -p "Enter your BOT_TOKEN: " BOT_TOKEN
read -p "Enter your GUILD_ID: " GUILD_ID
read -p "Enter your CHANNEL_ID: " CHANNEL_ID
read -p "Enter INTERVAL_MINUTES (default 5): " INTERVAL_MINUTES
INTERVAL_MINUTES=${INTERVAL_MINUTES:-5}
read -p "Enter WARN_THRESHOLD (default 70.0): " WARN_THRESHOLD
WARN_THRESHOLD=${WARN_THRESHOLD:-70.0}

# Create .env
cat > .env <<EOF
BOT_TOKEN=$BOT_TOKEN
GUILD_ID=$GUILD_ID
CHANNEL_ID=$CHANNEL_ID
INTERVAL_MINUTES=$INTERVAL_MINUTES
WARN_THRESHOLD=$WARN_THRESHOLD
EOF

echo ".env created."

# Create bot.sh with absolute paths
cat > bot.sh <<EOF
#!/bin/bash
cd "$BOT_DIR"
export \$(grep -v '^#' .env | xargs)

while true; do
    java -jar "$BOT_DIR/PiMonitorBot.jar"
    echo "Bot crashed. Restarting in 5 seconds..."
    sleep 5
done
EOF

chmod +x bot.sh
echo "bot.sh created and made executable."

# Create systemd service
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

echo "PiMonitorBot installation complete!"
echo "Check status: sudo systemctl status pimonitorbot"
echo "Manual start: $BOT_DIR/bot.sh"
