#!/usr/bin/bash

su -p -c "java -jar target/quick-dns-1.0-SNAPSHOT.jar" "$1"

exit 0