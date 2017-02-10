sudo apt-get install unzip
INSTALL_FOLDER=/opt/LWM2MClient
echo $INSTALL_FOLDER
sudo mkdir $INSTALL_FOLDER
sudo unzip -j Lwm2mClient-0.0.1.zip -d $INSTALL_FOLDER
sudo chmod 755 $INSTALL_FOLDER/Start.sh
sudo chmod 644 $INSTALL_FOLDER/lwm2mclient.service
sudo cp $INSTALL_FOLDER/lwm2mclient.service /etc/systemd/system/lwm2mclient.service
sudo systemctl enable lwm2mclient.service
sudo systemctl start lwm2mclient.service

