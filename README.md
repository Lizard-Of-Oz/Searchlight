# Searchlight (& Wall Lights) (Forge / Fabric)

## Showcase Video
[![Video Demonstration](https://user-images.githubusercontent.com/701551/175928969-93675674-7117-4912-8468-f249edca7229.png)](https://youtu.be/F529FUwWBxc)

If you want to support the development of mods like this, you can do it [here](https://boosty.to/lizardofoz).

[![Support me](https://static.boosty.to/assets/images/boostyLogo.WbAVE.svg)](https://boosty.to/lizardofoz)

Every dollar (or, in this case, ruble) counts and allows me to spend more time making various projects for your enjoyment.

## Installation
Grab the jar file from the [Release page](https://github.com/Lizard-Of-Oz/Searchlight/releases/).

Or from CurseForge: [Fabric Version](https://www.curseforge.com/minecraft/mc-mods/searchlight) and [Forge Version](https://www.curseforge.com/minecraft/mc-mods/searchlight-forge)
 
Copy the jar file into `%root_folder%/mods/` alongside other mods.

Fabric version has a dependency: [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)

## Searchlight
This mod adds Searchlight - a special block that doesn't emit light on its own, but casts an invisible Light Source Block in the direction of choosing.

![Searchlights](https://user-images.githubusercontent.com/701551/175929070-85e9697a-c601-463d-85c9-66b20b617c3f.png)

When a player right-clicks a Searchlight block, it rotates in the direction of the player, moving a Light Source block. 

A Searchlight can be turned off by providing a redstone signal.

The purpose of this block is to light up areas where having a visible light source would be an aesthetic detriment, but do it in a way that would fit Mojang's vision of Vanilla Survival Minecraft.

![Searchlight](https://user-images.githubusercontent.com/701551/175929127-2375417a-6b80-488d-bc0d-9fa2f7f79532.png)

Holding a Searchlight will display a beam that points from a Searchlight to its Light Source and displays the Light Source's hitbox.

(Forge doesn't display a beam unless a Searchlight is in frame)

![Beam](https://user-images.githubusercontent.com/701551/175929214-3e6ee714-5469-45b9-b6cf-5c5bc289f7d4.png)

## Wall Lights
This mod also adds a set of Wall Lights: Iron, Copper, Prismarine and 16 Colored variants (matching terracotta's color palette).

![4](https://user-images.githubusercontent.com/701551/175929275-acd1dfa1-27da-4025-b1a3-185f0a02c1b6.png)

When placed on a wall, its height is adjusted to the eye level specifically to hide the glowing part.  

To follow Mojang's vision of Vanilla, wood and stone variants of Wall Lights are absent by design, because most existing light sources match the wood/stone build style quite well, while there is the lack of fitting light sources for modern and futuristic builds.

## Recipes

![Searchlight Recipe](https://user-images.githubusercontent.com/701551/175929378-df3beb85-ed58-427b-80e5-a0f797c4f9c3.png) ![Wall Light Recipes](https://user-images.githubusercontent.com/701551/175929439-de3dfe20-630d-4681-8de8-65393c2e79ae.gif) ![Colored Wall Light Recipes](https://user-images.githubusercontent.com/701551/175929481-1f6237ab-5b67-4a45-9fc1-34369b4c72cb.gif)

## Technical details
A searchlight tries to face the player when it gets placed. If a raycast in the player's direction yields no result, if won't create a Light Source. If you right click a Searchlight and a raycast yields no result, it won't move a Light Source.

Light Source is placed 1 block away from any surfaces (if possible) to avoid collision with liquids, is not waterloggable, and gets moved in the direction of a Searchlight when replaced, broken or attempted to get pushed by a piston.

Light Source block can interfere with Observers, tree growth, plant growth etc.

Both Searchlight and Light Source are tile entities and get broken by pistons.

Light Source placing raycast (when you right-click a Searchlight) doesn't go through water, but it goes through transparent blocks and transparent sides according to vanilla lighting rules. E.g. it can go through appropriately rotated stairs.

Raycast works within the chunks loaded by the server, but manipulating a Searchlight will cause an unloaded chunk with a Light Source to load and vice-versa. 

Light Source that somehow happened to have no associated Searchlight will not be automatically deleted, but won't get restated if replaced manually.

## Use in modpacks and with other mods
You can include this mod in a modpack or as a dependency for your own mod.

I just ask you to respect my work and include it in a way that would count as a download of my mod by CurseForge and its Reward Program.

* For Modpacks, CurseForge by default links a mod in the modpack manifest when you add it. Use _that_ instead of embedding the mod's jar into the modpack.
* For Mods, don't embed the mod's jar inside your mod, but mark it as a dependency.

If you want to use this mode as a dependency, I recommend using [CurseMaven](https://www.cursemaven.com/).

Be advised that you need to manually keep track of the latest version available.

```
repositories {
  maven {
    url "https://cursemaven.com"
    content {
      includeGroup "curse.maven"
    }
  }
}

dependencies {
  modCompileOnly "curse.maven:drg_flares-496793:4537621" //Fabric
  modCompileOnly "curse.maven:drg_flares-497127:4537620" //Forge
}
```