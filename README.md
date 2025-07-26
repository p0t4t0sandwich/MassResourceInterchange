# Mass Resource Interchange

This mod is a submission in NeoForge's [Server-Side Summer](<https://neoforged.net/news/2025serversidesummer/>) modjam.

A Minecraft Plugin/Mod to sync player data across servers. Inspired by [HuskSync](https://www.spigotmc.org/resources/husksync-1-16-1-19-synchronize-player-inventories-data-cross-server.97144/) and the [clusterio](<https://github.com/clusterio/clusterio>) Factorio mod, but with my own spin on things. I feel like there's a bit more that a cross-API/cross-version implementation will allow, and I'm excited to see where this goes. Can't wait for serialization hell.

The main goal during Serverside Summer is to produce something stable, portable, and hopefully useful, all the while showing off some really cool core ideas to show real-world use cases for this kind of API.

There are some amazing projects that accomplish similar feats already, like [fabric-transfer-api](<https://wiki.fabricmc.net/tutorial:transfer-api>) and [Common-Storage-Lib](<https://github.com/terrarium-earth/Common-Storage-Lib>), I’m not reinventing the wheel because I think I can do it better, I’m doing it because I think an API like this has wider potential across a multitude of boundaries (I'm hoping to incorporate some compatibility layers with those libraries at some point).
And in my usual fashion, after the proof of concept is complete and stable, I’ll be porting it across most (and eventually all) major Minecraft versions/platforms.


## Features

### Extensible Storage Backends

You can write a mod that defines additional storage backends, which you can then use in the MRI's main config.

Currently, the mod supports:

- **mysql**: MySQL
- **mariadb**: MariaDB
- **postgresql**: PostgreSQL
- **sqlite**: SQLite

### Backpacks

Enable players to store items in a backpack that can be accessed across servers.

[//]: # (Add additonal notes on command usage)

### Crates

Similar to backpacks, but placable in the world. Though they're currently only accessable via one server, thinking of taking things in a couple directions:
- Syncing crates across servers
- Allowing crates to act as inputs/outputs (similar to how the clusterio mod works)

[//]: # (Add additonal notes on command usage)

### Player Data Sync

Currently only handles player inventories since the API is item-focused, so I'll need to think of ways to generalize the API further to allow for more data types.
Maybe some sort of serialization API, similar to how you can define additonal storage backends.

## Work In Progress/Ideas

- Storage Vault/Guild Bank: A shared storage system for players to store items that can be accessed across servers.
- Safe-Trade System: Allow players to safely trade items similar to other mods/plugins.
- Auction House: A system for players to auction items to each other. (would require some broad eco integrations, so it's not too high on the list)
