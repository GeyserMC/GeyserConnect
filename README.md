# GeyserConnect

[![forthebadge made-with-java](https://forthebadge.com/images/badges/made-with-java.svg)](https://java.com/)

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Build Status](https://github.com/GeyserMC/GeyserConnect/actions/workflows/build.yml/badge.svg)](https://github.com/GeyserMC/GeyserConnect/actions/workflows/build.yml)
[![Discord](https://img.shields.io/discord/613163671870242838.svg?color=%237289da&label=discord)](http://discord.geysermc.org/)
[![HitCount](http://hits.dwyl.com/GeyserMC/GeyserConnect.svg)](http://hits.dwyl.io/GeyserMC/GeyserConnect)

GeyserConnect is an easy way for Bedrock Edition clients to connect to any Java Edition servers without having to run anything.

## What is GeyserConnect?
GeyserConnect is an extension for Geyser that allows for a list of Minecraft: Java Edition servers to be displayed and accessed through 1 public Geyser instance. It is effectively give the customisability of [BedrockConnect](https://github.com/Pugmatt/BedrockConnect) to [Geyser](https://github.com/GeyserMC/Geyser).

If you wish to use DNS redirection please see the [bind9](bind9) folder in this repository.

## Commands
All commands are prefixed ingame with `/geyserconnect` or in console with `geyserconnect`

| Command                            | Description                                  | Example                                                | Console only       |
|------------------------------------|----------------------------------------------|--------------------------------------------------------|--------------------|
| `menu`                             | Reconnect and get back to the menu.          | `/geyserconnect menu`                                  | :x:                |
| `messageall (chat\|gui) <message>` | Send a message to all online users.          | `/geyserconnect messageall gui This is a test message` | :heavy_check_mark: |
| `transferall <ip> [passAsVhost]`   | Transfer all online users to another server. | `/geyserconnect transferall gc.example.com true`       | :heavy_check_mark: |
