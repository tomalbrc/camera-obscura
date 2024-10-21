# Camera Obscura

![Camera Obscura Icon](https://github.com/tomalbrc/camera-obscura/raw/main/src/main/resources/camera-obscura-icon.png "Camera Obscura Icon")

Take photos of your world / server!\
When used on a server: no client side mods required!\
Everything is rendered on the server using simple raytracing.

![Comparison](https://github.com/tomalbrc/camera-obscura/raw/main/comparison.png "Camera Obscura Icon")

## Item:

The mod adds a Camera Item, for clients it looks and functions like a normal spyglass
but has a custom name and consumes a map item or other, as specified in the configs.

There is no recipe for the item (yet)

## Configs

The config file will be created on first launch and is located in  
`configs/camera-obscura.json`

Example config with default values:
```json
{
"renderDistance": 128,
"showSystemMessages": false,
"renderAsyncMap": true,
"renderAsyncImage": true,
"cameraItem": "minecraft:spyglass",
"cameraConsumesItem": true,
"cameraConsumeItem": "minecraft:map",
"commandPermissionLevel": 4
}
```

Explanation:
- `renderDistance`: Render distance in blocks
- `showSystemMessages`: Flag wether system messages should be shown when a photo is being taken when run as command
- `renderAsyncMap`: Flag to render as map items asynchronously
- `renderAsyncImage`: Flag to render PNG images asynchronously
- `cameraItem`: Vanilla Item to use as camera item model/texture
- `cameraConsumesItem`: Flag if an item should be consumed
- `cameraConsumeItem`: Item to consume when the camera item is used. No items will be consumed when ran as command
- `commandPermissionLevel`: Vanilla Permission level


## Commands:

```
/camera-obscura
```
Takes a picture of the player running the command and gives the resulting map item to that player

---

```
/camera-obscura <[Entity|Player]> <scale>
```
Takes a picture as the source entity and gives the player the resulting maps,
a scale of up to 3 (3x3 maps) can be specified optionally.

---

```
/camera-obscura save <scale>
```
Takes a picture of the player running the command and saves it as png in  
`renders/<imagename>.png`,  
where imagename is the current date in the format  
`YYYY-MM-dd HH:mm:ss.SSS`.
A scale of up to 10 (1280px * 1280px) can be specified optionally.
Default image size is 128px * 128px

---

```
/camera-obscura save <Entity> <scale>
```

Takes a picture as the source entity and saves it as PNG with optional scale of up to 10

---
```
/camera-obscura clear-cache
```

Clears the cache (textures, resourcepack models and blockstate definitions, cached triangle models)

---
```
/camera-obscura clear-cache
```

Clears the cache (textures, resourcepack models and blockstate definitions, cached triangle models)


## Permissions:

Default Vanilla permission level is 4, this can be configured with commandPermissionLevel
For luckperms, etc. - those should be self-explanatory:

---

`camera-obscura.command`\
`camera-obscura.command.scale`

---

`camera-obscura.command.entity`\
`camera-obscura.command.entity.scale`

---

`camera-obscura.command.save`\
`camera-obscura.command.save.scale`

---

`camera-obscura.command.save.entity`\
`camera-obscura.command.save.entity.scale`

---

`camera-obscura.clear-cache`

---

## Known Issues:

#### All of those will be fixed sooner or later
- Entities are not rendered
- Liquids are "blocky"
- Hard lighting
- No biome water colors
- End Gateway and end portal blocks are pure black (they use the black concrete texture temporarily)
- Models that are larger than a single block can get cut-off during rendering
- Rendering breaks with coordinates in the millions

## How it works:

The game assets are downloaded from mojangs servers and read to render the world using raytracing.  
When the render is done, a Map item with the image is created or a png is saved to the `renders` folder.
