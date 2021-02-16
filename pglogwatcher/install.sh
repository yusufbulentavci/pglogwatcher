#!/bin/bash
	
if [ ! -d "/etc/pglogwatcher.ini" ]
	then
		cp pglogwatcher.ini /etc/
		chown postgres:postgres /etc/pglogwatcher.ini
		echo "/etc/pglogwatcher.ini file created"
	else
		echo "/etc/pglogwatcher.ini file exists, not modified"
fi

if [ -d "/opt/pglogwatcher" ]
	then
		rm -rf /opt/pglogwatcher
		echo "/opt/pglogwatcher directory exist, deleted"
fi

mkdir /opt/pglogwatcher
echo "/opt/pglogwatcher directory created"

cp pglogwatcher-jar-with-dependencies.jar /opt/pglogwatcher
chown -R postgres:postgres /opt/pglogwatcher

if [ ! -d "/var/log/pglogwatcher" ]
	then
		mkdir /var/log/pglogwatcher
		chown postgres:postgres /var/log/pglogwatcher
fi


cp pglogwatcher.service /lib/systemd/system/

systemctl daemon-reload

echo "Installation completed"
