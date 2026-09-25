<div align="center">

**WorldGuard** 7.0.19 | **Minecraft** <ins>26.2 - 26.3</ins>

![WGTranslator](wgtranslator-logo.png)

</div>

<div align="right">

**<big>[Download](https://github.com/imDaniX/WGTranslator/releases)</big>**

</div>

# Description

**WGTranslator** is a WorldGuard fork that allows you to freely translate the plugin at your discretion <ins>without</ins> 
using InJarTranslator or similar tools - everything is done through good old configs. Most of the plugin's strings are 
available for translation. Logging strings and various debug messages were omitted 
*(so I wouldn't go crazy editing those too)*.

The project had been gathering dust on my shelf for several years. 
I decided to revive it because no alternatives have appeared.

Pretranslated files for Russian are available at this [GitHub Gist](https://gist.github.com/imDaniX/7a23930c29c05fd7b7a4687d6ccafc7b).

# Translation Process

Since this is a fork, the original WorldGuard <ins>must be replaced</ins> with this one.

On startup, the plugin will automatically create two files in the `./plugins/WorldGuard` folder:
* `translator.yml` - contains all the main translatable strings of the plugin;
* `translator-commands.yml` - contains descriptions and usage examples for the plugin's commands.

After editing, you need to execute the command `/wgtranslator` (permission `wgtranslator.reload`) - 
it will reload the strings from the `translator.yml` file. 
Some strings may require a server restart due to how WG works.

# Translation Challenges

The list of strings requiring a restart includes the entire `translator-commands.yml` file - 
commands are registered on plugin startup via WorldEdit's outdated system and cannot be easily changed later. 
Due to this same system, some strings cannot be translated at all. 
For example, the string "Too few arguments." when typing `/rg f` is sent by WorldEdit itself.

The keys in the `translator.yml` file might seem randomly scattered. 
To some extent, that's true - 
they were formed based on WorldGuard's code and only occasionally grouped into something logical.

Some blocks of text strangely consist of multiple lines. 
This isn't my whim - WorldGuard itself assembles such text pieces into something coherent. 
It's all because of its poor integration with adventure. Moreover, the adventure version used is <ins>very</ins> old, 
making it almost impossible to embed something like MiniMessage without rewriting the plugin's fundamental workings.

Technically, some of the stuff mentioned can be fixed using various tricks and Java's reflection, 
but that would significantly increase the project's complexity, which I wouldn't be very happy about - 
I envisioned it as a small fork that doesn't change anything at the core of WorldGuard's operation, 
so that the project can be easily updated to the latest versions of the original plugin.
