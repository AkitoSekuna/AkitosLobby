# AkitosLobby

Lobby teleportation addon for the Akitos plugin network. Provides a seamless `/lobby` and `/hub` command to teleport players back to the designated lobby spawn point from any world or dimension without dropping server performance.

## Requirements

* Paper 1.21.1+
* Java 21+
* AkitosCore v21.2.0+ (Major & Minor versions must match, e.g. 21.2.X)

## Installation

1. Install AkitosCore first.
2. Drop `AkitosLobby.jar` into your `plugins/` folder.
3. Restart the server or reload plugins.
4. (Optional) Configure your target lobby world in `plugins/AkitosLobby/config.yml`.

[NOTE: AkitosLobby strictly requires AkitosCore. On startup, it checks that both plugins share the same Major and Minor version numbers (21.2.X). If a version mismatch is detected, AkitosLobby will automatically disable itself to prevent errors.]

## How It Works

Upon executing `/lobby` or `/hub`, AkitosLobby checks that the target lobby world is loaded on the server and utilizes Paper's asynchronous teleportation API (`teleportAsync`). This ensures smooth cross-world and cross-dimension travel across the Overworld, Nether, and End without causing server tick spikes.

## Commands & Permissions

* `/lobby` (Alias: `/hub`) — Teleports the executing player to the lobby spawn point.
* **Permission:** `akitoslobby.use`



## Configuration

A simple `config.yml` is generated in `plugins/AkitosLobby/`:

```yaml
# AkitosLobby Configuration
# Specify the name of your lobby world
lobby-world: "world"

```

## Part of the Akitos Plugin Network

* [AkitosCore](https://github.com/AkitoSekuna/AkitosCore) (required)
