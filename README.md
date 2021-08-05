# Searchlight (& Wall Lights) (Forge / Fabric)

## Showcase Video
[![Video Demonstration](https://user-images.githubusercontent.com/701551/125157958-18a9ec80-e198-11eb-9386-e45f4491cc73.png)](https://youtu.be/F529FUwWBxc)

## Installation
Grab the jar file from the [Release page](https://github.com/Lizard-Of-Oz/Searchlight/releases/).

Or from CurseForge: [Fabric Version](https://www.curseforge.com/minecraft/mc-mods/searchlight) and [Forge Version](https://www.curseforge.com/minecraft/mc-mods/searchlight-forge)
 
Copy the jar file into `%root_folder%/mods/` alongside other mods.

Fabric version has a dependency: [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)

## Searchlight
This mod adds Searchlight - a special block that doesn't emit light on its own, but casts an invisible Light Source Block in the direction of choosing.

Available for **1.16.3+** and **1.17+** (both Forge and Fabric)

![Searchlights](https://user-images.githubusercontent.com/701551/122744905-ac338000-d2b2-11eb-9b3e-fe84d65922d7.png)

When a player right-clicks a Searchlight block, it rotates in the direction of the player, moving a Light Source block. 

The purpose of this block is to light up areas where having a visible light source would be an aesthetic detriment, but do it in a way that would fit Mojang's vision of Vanilla Survival Minecraft.

![Searchlight](https://user-images.githubusercontent.com/701551/122747855-bc009380-d2b5-11eb-9254-e45831b856af.png)

Holding a Searchlight will display a beam that points from a Searchlight to its Light Source and displays the Light Source's hitbox.

(Forge doesn't display a beam unless a Searchlight is in frame)

![Beam](https://user-images.githubusercontent.com/701551/122748245-2c0f1980-d2b6-11eb-927f-501392e17f5a.png)

## Wall Lights
This mod also adds a set of Wall Lights: Iron, Copper, Prismarine and 16 Colored variants (matching terracotta's color palette).

![4](https://user-images.githubusercontent.com/701551/122748901-e868df80-d2b6-11eb-83d1-fe943735a641.png)

When placed on a wall, its height is adjusted to the eye level specifically to hide the glowing part.  

To follow Mojang's vision of Vanilla, wood and stone variants of Wall Lights are absent by design, because most existing light sources match the wood/stone build style quite well, while there is the lack of fitting light sources for modern and futuristic builds.

## Recipes

![Searchlight Recipe](https://user-images.githubusercontent.com/701551/122753504-9034dc00-d2bc-11eb-8e51-15d2c68ee507.png) ![Wall Light Recipes](https://user-images.githubusercontent.com/701551/122753512-91fe9f80-d2bc-11eb-8ebc-11dd4afb8c8e.gif) ![Colored Wall Light Recipes](https://user-images.githubusercontent.com/701551/122753518-932fcc80-d2bc-11eb-878f-0eab985922c5.gif)

## Technical details
A searchlight tries to face the player when it gets placed. If a raycast in the player's direction yields no result, if won't create a Light Source. If you right click a Searchlight and a raycast yields no result, it won't move a Light Source.

Light Source is placed 1 block away from any surfaces (if possible) to avoid collision with liquids, is not waterloggable, and gets moved in the direction of a Searchlight when replaced, broken or attempted to get pushed by a piston.

Light Source block can interfere with Observers, tree growth, plant growth etc.

Both Searchlight and Light Source are tile entities and get broken by pistons.

Light Source placing raycast (when you right-click a Searchlight) doesn't go through water, but it goes through transparent blocks and transparent sides according to vanilla lighting rules. E.g. it can go through appropriately rotated stairs.

Raycast works within the chunks loaded by the server, but manipulating a Searchlight will cause an unloaded chunk with a Light Source to load and vice-versa. 

Light Source that somehow happened to have no associated Searchlight will not be automatically deleted, but won't get restated if replaced manually.

## Use in modpacks and with other mods
Feel free to use this mod in a modpack.

For more Vanilla-esque improvements for vanilla problems, consider [Inventorio](https://github.com/Lizard-Of-Oz/Inventorio).

If you want to use this mode as a dependency, I recommend using JitPack. Please note that me using Architectury plugin causes the gradle setup to be different that normal:

Fabric:
```
repositories {
  ...
  maven { url 'https://jitpack.io' }
}

dependencies {
  ...
  modCompileOnly 'com.github.Lizard-Of-Oz.Searchlight:searchlight-1.17-fabric:1.17-SNAPSHOT'
}
```

Forge:
```
repositories {
  ...
  maven { url 'https://jitpack.io' }
}

dependencies {
  ...
  compileOnly 'com.github.Lizard-Of-Oz.Searchlight:searchlight-1.17-forge:1.17-SNAPSHOT'
}
```
