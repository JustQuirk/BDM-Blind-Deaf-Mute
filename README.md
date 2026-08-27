# Blind, Deaf, Mute

Fabric server + client mod for Minecraft 1.21.x. It integrates with Simple Voice Chat and Mod Menu.

Author: JustQuirk. Source: https://github.com/paprocki-tymon/BDM-Blind-Deaf-Mute

## Roles

- `/blind <player>` toggles Blindness and the client status badge.
- `/deaf <player>` prevents incoming Simple Voice Chat audio and shows headphones.
- `/mute <player>` cancels the player's outgoing microphone packets.
- `/start` activates role effects for the server.
- `/bdm:test` applies all three roles to the command sender; Blindness is tested for 10 seconds.

The `/bdm:test` command is disabled by default. Open BDM in Mod Menu and enable it in the configuration screen before using it.

Role commands and `/start` require operator permission level 2. Role state is stored on the server and synchronized to clients.

## Dependencies

Install Fabric API, Simple Voice Chat, and Mod Menu on both the server and clients. The BDM config screen stores the configured UDP port and includes a voice setup check. Simple Voice Chat still needs the same port in its own server configuration.

## Build

Use Java 21 and run `gradlew build`. The distributable jar is written to `build/libs/`.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
