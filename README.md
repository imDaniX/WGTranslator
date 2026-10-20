<div align="center">

**WorldGuard** 7.0.20 | **Minecraft** <ins>26.3</ins>

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

On startup, the plugin will automatically create few files in the `./plugins/WorldGuard/translator` folder:
* `config.yml` - settings of the translator;
* `messages.yml` - contains all the main translatable strings of the plugin;
* `commands.yml` - contains descriptions and usage examples for the plugin's commands.

After editing, you need to execute the command `/wgtranslator` (permission `wgtranslator.reload`) - 
it will reload `config.yml` and `messages.yml`. 
Some strings may require a server restart due to how WG works.

# Message Format

Every message in `messages.yml` can be either a plain string or a section:
```yaml
region.bypass.enabled: 'You are now bypassing region protection.' # uses the default serializer

wand.regions.empty:
  text: '<red>No defined regions here!'
  type: ERROR            # optional
  serializer: MINIMESSAGE # optional
  place: ACTIONBAR        # optional
```

* `text` - the message itself. Placeholders (e.g. `{region}`) are the same for every serializer 
  and are safe to use inside MiniMessage tag arguments, like `<click:run_command:'/rg info {region}'>`;
* `serializer` - `LEGACY_AMPERSAND` (`&a`, `&#rrggbb`), `LEGACY_SECTION` (`§a`) or `MINIMESSAGE`. 
  When omitted, the `serializer` from `config.yml` is used (`LEGACY_AMPERSAND` by default);
* `type` - base formatting of the message, the same ones WorldGuard uses: `PLAIN`, `INFO`, `DEBUG`, `ERROR`, 
  `SUBTLE`, `LABEL`, `CODE`. When omitted, WorldGuard picks it depending on where the message is sent. 
  Colors inside the text take priority over the type;
* `place` - `CHAT` (default), `TITLE` (a new line separates the title from the subtitle), `SUBTITLE` or `ACTIONBAR`. 
  Console and non-player senders always receive chat messages.

Messages that are meant to be rebuilt are generated with `serializer: MINIMESSAGE` already set.
`/rg info` is one of them - `region.info.layout` is the whole output, and every `{placeholder}` is a ready-made 
component (with its hover and click events) that can be moved, wrapped or dropped:
```yaml
region.info.layout:
  serializer: MINIMESSAGE
  text: |-
    {header}
    <blue>Region: <yellow>{id} <gray>[{type}]
    <blue>Owners: {owners}
    {flags}
region.info.flags.entry:
  serializer: MINIMESSAGE
  text: '<{color}>{name} <dark_gray>=<yellow> {value}'
```

`{parents}` and `{last-seen}` are optional and carry their own leading new line, so place them right after the previous placeholder.

# Translation Challenges

The list of strings requiring a restart includes the entire `commands.yml` file - 
commands are registered on plugin startup via WorldEdit's outdated system and cannot be easily changed later. 
Due to this same system, some strings cannot be translated at all. 
For example, the string "Too few arguments." when typing `/rg f` is sent by WorldEdit itself.

The keys in the `messages.yml` file might seem randomly scattered. 
To some extent, that's true - 
they were formed based on WorldGuard's code and only occasionally grouped into something logical.

Some blocks of text strangely consist of multiple lines. 
This isn't my whim - WorldGuard itself assembles such text pieces into something coherent.

Messages that are built into WorldGuard's components (hover texts, region info, flag lists and so on) 
use Adventure's components, so click and hover events from MiniMessage work there, but `place` does not.
Messages handed over to WorldEdit as plain strings (like the ones shown while a region is being saved, 
`onFailure` messages, kick and ban reasons, flag errors) are converted to the legacy format: 
colors and decorations survive, events and `place` do not.

Technically, the rest of the stuff mentioned can be fixed using various tricks and Java's reflection, 
but that would significantly increase the project's complexity, which I wouldn't be very happy about - 
I envisioned it as a small fork that doesn't change anything at the core of WorldGuard's operation, 
so that the project can be easily updated to the latest versions of the original plugin.
