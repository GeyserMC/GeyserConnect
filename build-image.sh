#!/bin/bash

wget -O ./data/GeyserConnect.jar https://ci.opencollab.dev//job/GeyserMC/job/GeyserConnect/job/master/lastSuccessfulBuild/artifact/target/GeyserConnect.jar --no-check-certificate

docker build -t $1 .