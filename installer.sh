#!/bin/bash

if [ -d "target/pglogwatcher" ]
	then
	rm -rf target/pglogwatcher
fi

cp -a pglogwatcher target/

echo "Copy jar to install directory"
cp target/pglogwatcher-jar-with-dependencies.jar target/pglogwatcher

echo "Creating package file target/pglogwatcher-install.tar.gz"
tar -zcvf target/pglogwatcher-install.tar.gz target/pglogwatcher

echo "Done"
