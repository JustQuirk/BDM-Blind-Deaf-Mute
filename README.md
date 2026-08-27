# Blind, Deaf, Mute

Fabric server + client mod for Minecraft 1.21.x. It integrates with Simple Voice Chat and Mod Menu.

Author: JustQuirk. Source: https://github.com/JustQuirk/BDM-Blind-Deaf-Mute

## Roles

- `/blind <player>` toggles Blindness and the client status badge.
- `/deaf <player>` prevents incoming Simple Voice Chat audio and shows headphones.
- `/mute <player>` cancels the player's outgoing microphone packets.
- `/start` activates role effects for the server.
- `/bdm:test` applies all three roles to the command sender; Blindness is tested for 10 seconds.
- `/bdm status` and `/bdm reload` are developer commands and are disabled by default.
- `/dev <password>` unlocks the developer commands for the current session.

The `/bdm:test` command is disabled by default. On the server, edit `config/bdm.json` and set `testCommandEnabled` to `true`, then restart the server before using it.

Role commands and `/start` require operator permission level 2. Role state is stored on the server and synchronized to clients.

## Developer commands

The server generates a random 16-character developer password on first start and writes it to `config/bdm.json` as `devPassword`. The password is intentionally not hard-coded in the mod or this README. Read it from the server config, then run:

```text
/dev <devPassword>
/bdm status
/bdm reload
```

Without unlocking, `/bdm status` and `/bdm reload` print a red disabled message in chat. `/bdm reload` reloads the JSON settings; restart the server after changing settings that belong to Simple Voice Chat itself.

## Dependencies

Install Fabric API and Simple Voice Chat on both the server and clients. Mod Menu is optional and can be used to view BDM in the installed-mod list. Server settings live in `config/bdm.json`; the configured UDP port must also match Simple Voice Chat's own server configuration. Other players show their active roles in their in-world nameplates.

Example server configuration:

```json
{
	"testCommandEnabled": false,
	"voicePort": 24454,
	"voiceTestEnabled": true
}
```

## Build

Use Java 21 and run `gradlew build`. The distributable jar is written to `build/libs/`.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
