# GeyserConnect using Docker
This contains the docker image and a basic way of running GeyserConnect

## Setup
1. Download GeyserConnect to a subfolder called `data`
2. Build the docker file using `docker build -t geyser-connect .`
3. Start geyser using this:
```
docker run --name "geyser-c" -d --restart always -p 19132:19132/udp -p 19133:19133/udp -v $(pwd)/data:/gsc geyser-connect
```