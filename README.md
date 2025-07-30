# Mass Resource Interchange

This mod is a submission in NeoForge's [Server-Side Summer](<https://neoforged.net/news/2025serversidesummer/>) modjam.

Mass Resource Interchange (MRI) is a Minecraft Plugin/Mod that can sync player data across servers. Inspired by
[HuskSync](https://www.spigotmc.org/resources/husksync-1-16-1-19-synchronize-player-inventories-data-cross-server.97144/) and the [clusterio](<https://github.com/clusterio/clusterio>) Factorio mod, but with my own spin on
things. I feel like there's a bit more that a cross-API/cross-version implementation will allow, and I'm excited to see
where this goes. Can't wait for serialization hell.

The main goal during Serverside Summer is to produce something stable, portable, and hopefully useful, all the while
showing off some really cool core ideas to show real-world use cases for this kind of API.

There are some amazing projects that accomplish similar feats already, like
[fabric-transfer-api](<https://wiki.fabricmc.net/tutorial:transfer-api>) and
[Common-Storage-Lib](<https://github.com/terrarium-earth/Common-Storage-Lib>), I’m not reinventing the wheel because I
think I can do it better, I’m doing it because I think an API like this has wider potential across a multitude of
boundaries (I'm hoping to incorporate some compatibility layers with those libraries at some point).
And in my usual fashion, after the proof of concept is complete and stable, I’ll be porting it across most
(and eventually all) major Minecraft versions/platforms.

## Features

### Extensible Storage Backends (DataStores) and config sections (Modules)

You can write a mod that defines additional storage backends, which you can then use in MRI's main config.
SQLite is the default storage backend to avoid errors during initial setup and in cases where servers aren't using the
mod in a multiserver setup.

Currently, the mod supports:

- **mysql**: MySQL
- **mariadb**: MariaDB
- **postgresql**: PostgreSQL
- **sqlite**: SQLite

On that same note you can write a mod that defines additional sections in MRI's config, which you can then use when
designing your own addons. Do note however, that you'll need to handle updating your own config via Configurate's
transformation/serialization system.

### Player Data Sync (PlayerSync)

At the moment MRI only handles player inventories since the other aspects of the mod are item-focused, so I'll need to
think of ways to generalize the API further to allow for more data types. Maybe some sort of extensible serialization
API similar to how you can define additional storage backends.

If you run into any issues with modded items, please open an issue on the GitHub repository and add the `playersync`
and `integration` labels (still need to put together some templates for common issues).

### Backpacks

Enable players to store items in a backpack that can be accessed across servers.

#### Command Usage

| Command                            | Permission                 | Description                                          |
|------------------------------------|----------------------------|------------------------------------------------------|
| `/backpack`                        | `mri.backpack.open`        | Opens the backpack inventory for the player.         |
| `/backpack <player>`               | `mri.backpack.open.others` | View the backpack inventory of the specified player. |
| `/backpack create <player> <size>` | `mri.backpack.create`      | Creates a new backpack for the player.               |
| `/backpack delete` <player>        | `mri.backpack.delete`      | Deletes the player's backpack.                       |
| `/backpack item` <player>          | `mri.backpack.item`        | Gives the player a backpack item.                    |
| `/backpack item <player>`          | `mri.backpack.item.others` | Gives the specified player a backpack item.          |

#### Extra Notes

- The `mri.backpack.open` and `mri.backpack.open.others` permissions also apply when the player uses the backpack item in-game.

## Work In Progress

- Metadata checks to ensure that the MC version and modlists match across servers
- "Overflow" storage where items that can't be deserialized are stored, so they can be retrieved later
- Allow database disconnects+reconnects to be handled gracefully
- Translatable messages for in-game notifications and command responses
- A utility for admins to manually remove database locks (ie if the original server has crashed and is inoperable)
- Rate limit players when accessing their backpacks
- Add command handling to allow admins to manage offline players' backpacks (no, not that kind of offline player)
- Tweak create and delete commands so they can be run in the console
- DataStore migration utility (via SQLite as an intermediary most likely)

### Crates

Similar to backpacks, but placable in the world via commands. Though they're currently only (safely) accessable via one
server, thinking of taking things in a couple directions:
- Syncing crates across servers
- Allowing crates to act as inputs/outputs (similar to how the clusterio mod works)

[//]: # (TODO: Add additonal notes on command usage)

## Future Ideas

- Deep storage container: Similar idea to storage drawers, just a container that can hold a lot of one item.
- Half-Effort RF/ME System: Mass storage for a oodles of items, will need a paginated UI to make it usable.
- Storage Vault/Guild Bank: A shared storage system for players to store items that can be accessed across servers.
- Safe-Trade System: Allow players to safely trade items similar to other mods/plugins.
- Auction House: A system for players to auction items to each other. (would require some broad eco integrations, so it's not too high on the list)
