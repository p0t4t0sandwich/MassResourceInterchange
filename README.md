# Mass Resource Interchange

A Minecraft Plugin/Mod to sync player data across servers. Inspired by [HuskSync](https://www.spigotmc.org/resources/husksync-1-16-1-19-synchronize-player-inventories-data-cross-server.97144/) and the [clusterio](<https://github.com/clusterio/clusterio>) Factorio mod, but with my own spin on things. I feel like there's a bit more that a cross-API/cross-version implementation will allow, and I'm excited to see where this goes. Can't wait for serialization hell.

The main goal during Serverside Summer is to produce something stable, portable, and hopefully useful, all the while showing off some really cool core ideas to show real-world use cases for this kind of API.

There are some amazing projects that accomplish similar feats already, like [fabric-transfer-api](<https://wiki.fabricmc.net/tutorial:transfer-api>) and [Common-Storage-Lib](<https://github.com/terrarium-earth/Common-Storage-Lib>), I’m not reinventing the wheel because I think I can do it better, I’m doing it because I think an API like this has wider potential across a multitude of boundaries (I'm hoping to incorporate some compatibility layers with those libraries at some point).
And in my usual fashion, after the proof of concept is complete and stable, I’ll be porting it across most (and eventually all) major Minecraft versions/platforms.
