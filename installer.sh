#!/bin/bash

if [ -d "target/pglogwatcher" ]
	then
	rm -rf target/pglogwatcher
fi

cp -a pglogwatcher target/

cd target

echo "Copy jar to install directory"
cp pglogwatcher-jar-with-dependencies.jar pglogwatcher

echo "Creating package file target/pglogwatcher-install.tar.gz"
tar -zcvf pglogwatcher-install.tar.gz pglogwatcher

cd ..

echo "Done"
