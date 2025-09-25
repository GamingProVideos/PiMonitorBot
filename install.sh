#!/bin/bash
set -e

echo "=== PiMonitorBot Installer ==="

# Prompt for installation directory
read -p "Enter installation directory (default: $HOME/PiMonitorBot): " BOT_DIR
BOT_DIR=${BOT_DIR:-$HOME/PiMonitorBot}
mkdir -p "$BOT_DIR"
cd "$BOT_DIR"

# Install Zulu OpenJDK 21
if ! java -version 2>&1 | grep -q '21'; then
    echo "Installing Zulu OpenJDK 21..."
    sudo mkdir -p /etc/apt/keyrings
    curl -fsSL https://repos.azul.com/azul-repo.key | sudo gpg --dearmor -o /etc/apt/keyrings/azul.gpg
    echo "deb [arch=arm64 signed-by=/etc/apt/keyrings/azul.gpg] https://repos.azul.com/zulu/deb stable main" | sudo tee /etc/apt/sources.list.d/zulu.list
    sudo apt update
    sudo apt install -y zulu21-jdk-headless
else
    echo "Java 21 already installed."
fi

java -version

# Download PiMonitorBot.jar
JAR_URL="https://github.com/GamingProVideos/PiMonitorBot/releases/latest/download/PiMonitorBot-1.0-SNAPSHOT.jar"
echo "Downloading PiMonitorBot.jar..."
wget -O "$BOT_DIR/PiMonitorBot.jar" "$JAR_URL"
chmod +x "$BOT_DIR/PiMonitorBot.jar"

# Prompt for config
echo "Let's configure your PiMonitorBot:"
read -p "Enter BOT_TOKEN: " BOT_TOKEN
read -p "Enter GUILD_ID: " GUILD_ID
read -p "Enter CHANNEL_ID: " CHANNEL_ID
read -p "Enter INTERVAL_MINUTES (default 5): " INTERVAL_MINUTES
INTERVAL_MINUTES=${INTERVAL_MINUTES:-5}
read -p "Enter WARN_THRESHOLD (default 70.0): " WARN_THRESHOLD
WARN_THRESHOLD=${WARN_THRESHOLD:-70.0}
read -p "Enter ALLOWED_ROLE_ID (role allowed to use /status command): " ALLOWED_ROLE_ID

# Create .env
cat > "$BOT_DIR/.env" <<EOF
BOT_TOKEN=$BOT_TOKEN
GUILD_ID=$GUILD_ID
CHANNEL_ID=$CHANNEL_ID
INTERVAL_MINUTES=$INTERVAL_MINUTES
WARN_THRESHOLD=$WARN_THRESHOLD
ALLOWED_ROLE_ID=$ALLOWED_ROLE_ID
EOF

# Create bot.sh with absolute paths
cat > "$BOT_DIR/bot.sh" <<EOF
#!/bin/bash
# Absolute path to bot directory
BOT_DIR="$BOT_DIR"
cd "\$BOT_DIR"

# Load env variables
export \$(grep -v '^#' .env | xargs)

while true; do
    java -jar "\$BOT_DIR/PiMonitorBot.jar"
    echo "Bot crashed. Restarting in 5 seconds..."
    sleep 5
done
EOF

chmod +x "$BOT_DIR/bot.sh"

# Create systemd service with absolute paths
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
sudo systemctl restart pimonitorbot

echo "Installation complete!"
echo "Check status: sudo systemctl status pimonitorbot"
echo "Manual start: $BOT_DIR/bot.sh"
