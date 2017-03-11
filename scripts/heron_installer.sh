
DOWNLOAD_DIR=/users/rohitsd/heron-setup
mkdir $DOWNLOAD_DIR

setup_heron() {
    echo "Downloading Heron"
    wget -q -O $DOWNLOAD_DIR/heron-client-install-0.14.5-ubuntu.sh https://github.com/twitter/heron/releases/download/0.14.5/heron-client-install-0.14.5-ubuntu.sh 
    wget -q -O $DOWNLOAD_DIR/heron-tools-install-0.14.5-ubuntu.sh https://github.com/twitter/heron/releases/download/0.14.5/heron-tools-install-0.14.5-ubuntu.sh 

    echo "Installing Heron"
    chmod +x $DOWNLOAD_DIR/heron-client-install-0.14.5-ubuntu.sh
    chmod +x $DOWNLOAD_DIR/heron-tools-install-0.14.5-ubuntu.sh
    $DOWNLOAD_DIR/heron-client-install-0.14.5-ubuntu.sh --user
    $DOWNLOAD_DIR/heron-tools-install-0.14.5-ubuntu.sh --user
    echo "Done - Heron"
}

setup_heron
