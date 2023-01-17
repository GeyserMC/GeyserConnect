# GeyserConnect using Docker
This contains the Docker image and a basic way of running GeyserConnect

## Setup
1. Make a directory for data: `mkdir -m 1777 data`
2. Set the owner: `chown nobody data`
   This matches the user that geyser-connect runs as inside the Docker
   container.
3. Then use either `docker-compose` or `docker` below

## Docker Compose
1. Build with `docker-compose build`
2. Start Geyser `docker-compose up -d`

* To check logs `docker-compose logs`
* To stop `docker-compose down`

## Docker
1. Build the Dockerfile using `docker build -t geyser-connect -f docker/Dockerfile .`
2. Start geyser using this:
```
docker run --name "geyser-c" -d --restart always -p 19132:19132/udp -v $(pwd)/data:/gsc geyser-connect
```
