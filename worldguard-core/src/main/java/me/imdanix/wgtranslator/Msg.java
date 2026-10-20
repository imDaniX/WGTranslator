package me.imdanix.wgtranslator;

import com.sk89q.minecraft.util.commands.CommandException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum Msg {
    BLACKLIST__ACTION__ACQUIRE("acquire"),
    BLACKLIST__ACTION__BREAK("break"),
    BLACKLIST__ACTION__DESTROY("destroy with"),
    BLACKLIST__ACTION__DISPENSE("dispense"),
    BLACKLIST__ACTION__DROP("drop"),
    BLACKLIST__ACTION__EQUIP("equip"),
    BLACKLIST__ACTION__INTERACT("interact with"),
    BLACKLIST__ACTION__PLACE("place"),
    BLACKLIST__ACTION__USE("use"),

    BLACKLIST__PUNISHMENT__BAN__DEFAULT("Banned: You can't {action} {type}.", "action", "type"),
    BLACKLIST__PUNISHMENT__BAN__REASONED("Banned: {reason}", "reason"),
    BLACKLIST__PUNISHMENT__KICK__DEFAULT("You can't {action} {type}.", "action", "type"),
    BLACKLIST__PUNISHMENT__KICK__REASONED("{reason}", "reason"),
    BLACKLIST__PUNISHMENT__TELL__DEFAULT("You're not allowed to {action} {type}.", "action", "type"),
    BLACKLIST__PUNISHMENT__TELL__TEXT("{text}", "text"),

    CHEST_LOCK__DISABLED("&cWorldGuard's sign chest protection is disabled."),
    CHEST_LOCK__LOCK("[Lock]"),
    CHEST_LOCK__LOCK_POST("&cThe {lock} sign must be a sign post, not a wall sign.", "lock"),
    CHEST_LOCK__NOT_OWN_ADJACENT("&4You do not own the adjacent chest."),
    CHEST_LOCK__OWNER_LINE("&cThe first owner line must be your name."),
    CHEST_LOCK__PROTECTED("&eA chest or double chest above is now protected."),
    CHEST_LOCK__PROTECTED_CHEST("&4This chest is protected."),
    CHEST_LOCK__SPOT("&4This spot is for a chest that you don't have permission for."),
    CHEST_LOCK__UNSAFE("&cThat is not a safe block that you're putting this sign on."),

    COMMANDBOOK__WHOIS__BUILD("Can build"),
    COMMANDBOOK__WHOIS__CURRENT("Current Regions"),

    DISABLED__ARROWS("&cSorry, arrows with {effect} are presently disabled.", "effect"),
    DISABLED__POTIONS("&cSorry, potions with {effect} are presently disabled.", "effect"),
    DISABLED__POTIONS_BYPASS("&cSorry, potions with {effect} can't be thrown, " +
            "even if you have a permission to bypass it, " +
            "due to limitations (and because overly-reliable potion blocking is on).", "effect"),

    ERROR__INFO("&c{message}", "message"),
    ERROR__INVALID_HOST_KICK("You did not join with the valid host key!"),
    ERROR__MATCHER__EXPECT_ARG("Argument expected for #player."),
    ERROR__MATCHER__INVALID_GROUP("Invalid group '{group}'.", "group"),
    ERROR__MATCHER__INVALID_ID("Invalid identifier '{identifier}'.", "identifier"),
    ERROR__MATCHER__NO_END_WORLD("No end world found."),
    ERROR__MATCHER__NO_NETHER_WORLD("No nether world found."),
    ERROR__MATCHER__NO_NORMAL_WORLD("No normal world found."),
    ERROR__MATCHER__NO_PLAYERS("No players matched query."),
    ERROR__MATCHER__NO_WORLD("No world by that exact name found."),
    ERROR__NO_PERMISSIONS("&cYou don't have permission."),
    ERROR__OP_DEOP("&c/op and /deop can only be used in console (as set by a WG setting)."),

    ERROR__USAGE("&c{usage}", "usage"),

    FIRE__ALLOW__ALREADY("Fire spread was already globally enabled."),
    FIRE__ALLOW__SUCCESS("Fire spread has been globally for '{world}' re-enabled by {name}.", "world", "name"),
    FIRE__DISABLED("&eFire spread is currently globally disabled for this world."),
    FIRE__STOP__ALREADY("Fire spread was already globally disabled."),
    FIRE__STOP__SUCCESS("Fire spread has been globally disabled for '{world}' by {name}.", "world", "name"),

    HALT__ALLOWED__BROADCAST("ALL intensive server activity is now allowed."),
    HALT__ALLOWED__INFO("ALL intensive server activity is now allowed."),
    HALT__ALLOWED__SILENT("(Silent) ALL intensive server activity is now allowed."),
    HALT__AUTO_REMOVE("Halt-Act: {removed} entities (>10) auto-removed from {world}", "removed", "world"),
    HALT__CONFIRM__MESSAGE(true, """
            <red>This command will <bold>PERMANENTLY</bold> erase ALL animals in ALL loaded chunks in ALL loaded worlds.
            <click:run_command:'{command}'><hover:show_text:'Click to confirm {command}'><green>[Click]</green></hover></click>\
            <red> or type <aqua><click:suggest_command:'{command}'>{command}</click><red> to confirm.""",
            "command"),
    HALT__ENTITIES_REMOVED("{count} entities (>10) auto-removed from {world}", "count", "world"),
    HALT__HALTED__BROADCAST("ALL intensive server activity halted by {name}.", "name"),
    HALT__HALTED__INFO("ALL intensive server activity halted."),
    HALT__HALTED__SILENT("(Silent) ALL intensive server activity halted by {name}.", "name"),
    HALT__SERVER_HALTED("&eIntensive server activity has been HALTED."),
    HALT__STATUS__ALLOWED("ALL intensive server activity is allowed."),
    HALT__STATUS__HALTED("ALL intensive server activity is not allowed."),

    HEAL__HEALED("Healed!"),
    HEAL__HEALED_BY("Healed by {name}.", "name"),
    HEAL__HEALED_OTHER("Players healed."),

    GOD__DISABLED("God mode disabled!"),
    GOD__DISABLED_BY("God disabled by {name}.", "name"),
    GOD__DISABLED_OTHER("Players no longer have god mode."),
    GOD__ENABLED("God mode enabled! Use /ungod to disable."),
    GOD__ENABLED_BY("God enabled by {name}.", "name"),
    GOD__ENABLED_OTHER("Players now have god mode."),

    INFINITESTACK__REMOVE("&cInfinite stack removed."),
    INFINITESTACK__REMOVE_SLOT("&cInfinite stack in slot #{slot} removed.", "slot"),

    LOCATE__REPOINTED("Compass repointed."),
    LOCATE__RESET("Compass reset to spawn."),

    SESSION__BYPASS__DISABLED("&5You are not bypassing region protection. You can toggle this with /rg bypass"),
    SESSION__BYPASS__ENABLED("&5You are bypassing region protection. You can toggle this with /rg bypass"),

    SLAY__SLAIN("Slain!"),
    SLAY__SLAIN_BY("Slain by {name}.", "name"),
    SLAY__SLAIN_OTHER("Players slain."),

    STACK__COMPACTED("Items compacted into stacks!"),

    PAGINATION__EMPTY("No results found."),
    PAGINATION__INVALID_PAGE("Invalid page number"),
    PAGINATION__NAVIGATE("Click to navigate"),
    PAGINATION__PAGE("&ePage &6{page}&r of &6{pages}", "page", "pages"),

    REGION__BYPASS__DISABLED("You are no longer bypassing region protection."),
    REGION__BYPASS__ENABLED("You are now bypassing region protection (as long as you have permission)."),
    REGION__BYPASS__INVALID("Allowed optional arguments are: on, off"),

    REGION__CLAIM__ADDING("Claiming region '{region}'", "region"),
    REGION__CLAIM__ERROR__ALREADY_EXIST("This region already exists and you don't own it."),
    REGION__CLAIM__ERROR__MAX_INTEGER("The maximum claim volume get in the configuration is higher than is supported. Currently, it must be " + Integer.MAX_VALUE + " or smaller. Please contact a server administrator."),
    REGION__CLAIM__ERROR__NO_POLYGONS("Polygons are currently not supported for /rg claim."),
    REGION__CLAIM__ERROR__ONLY_INSIDE("You may only claim regions inside existing regions that you or your group own."),
    REGION__CLAIM__ERROR__OVERLAPS("This region overlaps with someone else's region."),
    REGION__CLAIM__ERROR__TOO_LARGE("This region is too large to claim.\nMax. volume: {max}, your volume: {current}", "max", "current"),
    REGION__CLAIM__ERROR__TOO_MANY("You own too many regions, delete one first to claim a new one."),
    REGION__CLAIM__FAIL("Failed to claim region"),
    REGION__CLAIM__SUCCESS("A new region has been claimed named '{region}'", "region"),

    REGION__COMMANDS__USING_WORLD("Using //world override for region command: {command}", "command"),
    REGION__COMMANDS__CLICK_TO_PICK("Click to pick this region"),
    REGION__COMMANDS__CURRENT_REGIONS("Current regions: "),
    REGION__COMMANDS__CURRENT_REGIONS_WITH_HIDDEN(", and {count} hidden regions", "count"),
    REGION__COMMANDS__CURRENT_REGIONS_HIDDEN("{count} hidden regions", "count"),
    REGION__COMMANDS__HEIGHT_WARNING("(Warning: The height of the region was {height} block(s).)", "height"),
    REGION__COMMANDS__NEW_USER_INFO(true, "<gray>(This region is NOW PROTECTED from modification from others. Don't want that? Use <aqua>{command}</aqua>)", "command"),
    REGION__COMMANDS__NOT_STANDING_IN_REGION("You're not standing in a region. Specify an ID if you want to select a specific region."),
    REGION__COMMANDS__NOT_STANDING_GLOBAL("You're not standing in any regions. Using the global region for this world instead."),
    REGION__COMMANDS__REGIONS_DISABLED("Region support is disabled in the target world. It can be enabled per-world in WorldGuard's configuration files. However, you may need to restart your server afterwards."),
    REGION__COMMANDS__REGION_DATA_LOAD_FAIL("Region data failed to load for this world. Please ask a server administrator to read the logs to identify the reason."),
    REGION__COMMANDS__REGION_EXISTS("A region with that name already exists. Please choose another name."),
    REGION__COMMANDS__REGION_EXISTS_REDEFINE(" To change the shape, use /region redefine {id}.", "id"),
    REGION__COMMANDS__REGION_INVALID_ID("The region name of '{id}' contains characters that are not allowed.", "id"),
    REGION__COMMANDS__REGION_NOT_FOUND("No region could be found with the name of '{id}'.", "id"),
    REGION__COMMANDS__REGION_NO_GLOBAL("Sorry, you can't use __global__ here."),
    REGION__COMMANDS__SAVE_FAILURES("(Warning: The background saving of region data is failing for these worlds: {worlds}. Your changes are getting lost. See the server log for more information.)", "worlds"),
    REGION__COMMANDS__SELECT_AREA_FIRST("Please select an area first. Use WorldEdit to make a selection! (see: https://worldedit.enginehub.org/en/latest/usage/regions/selections/)."),
    REGION__COMMANDS__SPAWN_OVERLAP(true, "<red>Warning! This region overlaps vanilla's spawn protection. WorldGuard cannot override this, and only server operators will be able to interact with this area."),
    REGION__COMMANDS__STANDING_IN_MULTIPLE("You're standing in several regions (please pick one)."),
    REGION__COMMANDS__UNSUPPORTED_SHAPE("Sorry, you can only use cuboids and polygons for WorldGuard regions."),
    REGION__COMMANDS__WORLD_SPECIFY("Please specify the world with -{flag} world_name.", "flag"),

    REGION__DEFINE__ADDING("Adding region '{region}'", "region"),
    REGION__DEFINE__FAIL("Failed to add the region '{region}'", "region"),
    REGION__DEFINE__SUCCESS("A new region has been made named '{region}'", "region"),

    REGION__FLAGS__CHANGE("Click to change"),
    REGION__FLAGS__CONSEQUENCES(true, """
            <red>Setting this flag may have unintended consequences.</red>
            Please read the documentation and set this flag manually if you really intend to.
            (Hint: You do not need to set this to protect the region!)"""
    ),
    REGION__FLAGS__FAIL("Failed to get region flags"),
    REGION__FLAGS__INHERITED__DEFAULT(true, "<u>Default</u> value"),
    REGION__FLAGS__INHERITED__INHERITED("Inherited value"),
    REGION__FLAGS__INHERITED__INHERITED_DEFAULT(true, "Inherited & <u>default</u> value"),
    REGION__FLAGS__SET("Click to set"),
    REGION__FLAGS__SET_CUSTOM("Click to set custom value"),
    REGION__FLAGS__SPECIAL__BUILD("This is a special flag which overrides build checks. (Not movement related!)"),
    REGION__FLAGS__SPECIAL__MEMBER("This is a special flag which defaults to allow for members, and deny for non-members"),
    REGION__FLAGS__SPECIFY("Please specify the region with /region flags -w world_name region_name."),
    REGION__FLAGS__THIRD_PARTY("Third-Party Flags"),
    REGION__FLAGS__TITLE("Flags for {region}", "region"),
    REGION__FLAGS__UNSET("Click to unset"),
    REGION__FLAGS__VALUES("Current values:"),

    REGION__FLAGS__VALUE__DEFAULT__DENY("&c&lHey!&7 Sorry, but you can't %what% here."),
    REGION__FLAGS__VALUE__DEFAULT__ENTRY_DENY("&c&lHey!&7 You are not permitted to enter this area."),
    REGION__FLAGS__VALUE__DEFAULT__EXIT_DENY("&c&lHey!&7 You are not permitted to leave this area."),
    REGION__FLAGS__VALUE__DEFAULT__TELEPORT("&dTeleported you to the region '%id%'."),

    REGION__FLAGS__VALUE__INVALID__BOOLEAN("Not a yes/no value: {input}", "input"),
    REGION__FLAGS__VALUE__INVALID__DATETIME("Expected 'now' or ISO 8601 formatted input."),
    REGION__FLAGS__VALUE__INVALID__DOUBLE("Not a number: {input}", "input"),
    REGION__FLAGS__VALUE__INVALID__ENTITY_TYPE("Unknown entity type: {input}", "input"),
    REGION__FLAGS__VALUE__INVALID__ENUM("Unknown value '{input}' in {class}", "input", "class"),
    REGION__FLAGS__VALUE__INVALID__GAMEMODE("Unknown game mode: {input}", "input"),
    REGION__FLAGS__VALUE__INVALID__INTEGER("Not a number: {input}", "input"),
    REGION__FLAGS__VALUE__INVALID__LOCATION("Expected 'here' or x,y,z."),
    REGION__FLAGS__VALUE__INVALID__MAP("Input must be in a 'key:value,key1=value1' format. Either ':' or '=' can be used."),
    REGION__FLAGS__VALUE__INVALID__OUTSIDE("You can't set that flag outside of the region boundaries."),
    REGION__FLAGS__VALUE__INVALID__OUTSIDE_SUCCESS("You can't set that flag outside of the region boundaries."),
    REGION__FLAGS__VALUE__INVALID__PLUGIN("The plugin that registered this flag is not currently installed"),
    REGION__FLAGS__VALUE__INVALID__REGISTRY("Unknown {registry}: {key}", "registry", "key"),
    REGION__FLAGS__VALUE__INVALID__SENDER_NOT_PLAYER("Not a player"),
    REGION__FLAGS__VALUE__INVALID__STATE("Expected none/allow/deny, but got '{input}'", "input"),
    REGION__FLAGS__VALUE__INVALID__UNRECOGNIZED("Unrecognized input."),
    REGION__FLAGS__VALUE__INVALID__UUID("Not a valid uuid: {input}", "input"),
    REGION__FLAGS__VALUE__INVALID__WEATHER("Unknown weather type: {input}", "input"),

    REGION__FLAGS__VALUE__LOCATION__CURRENT("Current value:"),
    REGION__FLAGS__VALUE__LOCATION__DEFAULT("Default value:"),
    REGION__FLAGS__VALUE__LOCATION__UNSET("unset location"),
    REGION__FLAGS__VALUE__NUMERIC__CUSTOM("[custom]"),
    REGION__FLAGS__VALUE__NUMERIC__UNSET("unset number"),
    REGION__FLAGS__VALUE__STRING__CURRENT("Current value:"),
    REGION__FLAGS__VALUE__STRING__DEFAULT("Default value:"),
    REGION__FLAGS__VALUE__STRING__UNSET("unset string"),

    REGION__FLAGS__WARNING__BUILD(true, """
            <red><bold>WARNING:</bold> Setting this flag is not required for protection.</red>
            Setting this flag will completely override default protection, and apply to members, non-members, pistons, sand physics, and everything else that can modify blocks.
            Only set this flag if you are sure you know what you are doing. See \
            <aqua><click:open_url:'https://worldguard.enginehub.org/en/latest/regions/flags/#protection-related'>this documentation page</click></aqua>."""
    ),
    REGION__FLAGS__WARNING__PASSTHROUGH(true, """
            <red><bold>Setting this flag may have unintended consequences.</bold> \
            Please read the documentation and set this flag manually if you really intend to.</red>
            (Hint: You do not need to set this to protect the region!) See \
            <aqua><click:open_url:'https://worldguard.enginehub.org/en/latest/regions/flags/#overrides'>this documentation page</click></aqua>."""
    ),

    REGION__FLAG__CURRENT_LIST(true, "<gray>(Current flags: {flags}<gray>)", "flags"),

    REGION__FLAG__ERROR__EMPTY("You cannot use -e(mpty) with a flag value."),
    REGION__FLAG__ERROR__NO_GROUP("Region flag '{flag}' does not have a group flag!", "flag"),

    REGION__FLAG__GROUP__RESET("Region group flag for '{flag}' reset to default.", "flag"),
    REGION__FLAG__GROUP__SET("Region group flag for '{flag}' set.", "flag"),

    REGION__FLAG__LIST__AVAILABLE_LIST(true, "Available flags: {flags}", "flags"),
    REGION__FLAG__LIST__SEPARATOR(true, ", "),
    REGION__FLAG__LIST__USE_COMMAND(true, "<light_purple>Or use the command <aqua><click:run_command:'{command}'>/rg flags {region}</click>", "command", "region"),
    REGION__FLAG__LIST__FETCHING("Flag list for invalid flag command."),

    REGION__FLAG__LIST__UNKNOWN("Unknown flag specified: {flag}", "flag"),
    REGION__FLAG__REMOVED("Region flag {flag} removed from '{region}'. (Any -g(roups) were also removed.)", "flag", "region"),
    REGION__FLAG__SET("Region flag {flag} set on '{region}' to '{value}'.", "flag", "region", "value"),

    REGION__INFO__ACTION__CENTER_TELEPORT(true, "<gray><hover:show_text:'Click to teleport to the center of the region'><click:run_command:'{command}'>[Center Teleport]", "command"),
    REGION__INFO__BOUNDS__CLICK_SELECT("Click to select"),
    REGION__INFO__ACTION__TELEPORT(true, "<gray><hover:show_text:'Click to teleport<newline>{location}'><click:run_command:'{command}'>[Teleport]", "command", "location"),

    REGION__INFO__ACTION__ADD(true, "<green><hover:show_text:'Click to add a player or group'><click:suggest_command:'{command}'>[Add]", "command"),
    REGION__INFO__ACTION__CLEAR(true, "<red><hover:show_text:'Click to clear'><click:suggest_command:'{command}'>[Clr]", "command"),
    REGION__INFO__DOMAIN__NONE("(none)"),
    REGION__INFO__ACTION__REMOVE(true, "<red><hover:show_text:'Click to remove a player or group'><click:suggest_command:'{command}'>[Rem]", "command"),

    REGION__INFO__FAIL("Failed to fetch region information"),
    REGION__INFO__FETCHING("Fetching region info"),

    REGION__INFO__FLAGS__CLICK_TO_SET("Click to set flag"),
    REGION__INFO__ACTION__FLAGS(true, "<{color}><hover:show_text:'Click to set a flag'><click:run_command:'{command}'>[Flags]", "color", "command"),
    REGION__INFO__FLAGS__ENTRY(true, "<{color}>{name}: <yellow>{value}", "color", "name", "value"),
    REGION__INFO__FLAGS__NONE("(none)"),
    REGION__INFO__FLAGS__SEPARATOR(true, ", "),

    REGION__INFO__LAST_SEEN("Any names suffixed by * are 'last seen names' and may not be up to date."),

    REGION__INFO__LAYOUT(true, """
            {header}
            <blue>Region: <yellow>{id}<gray> (type=<white>{type}<gray>, priority={priority}<gray>)
            <blue>Flags: {flags}{parents}
            <blue>Owners: {owners}
            <blue>Members: {members}
            <blue>Volume: <yellow>{volume}
            <blue>Bounds: {bounds}{last-seen}""",
            "header", "id", "type", "priority", "flags", "parents", "owners", "members", "volume", "bounds", "last-seen"
    ),

    REGION__INFO__PARENT__CLICK_INFO("Click for info"),
    REGION__INFO__PARENT__SUFFIX(true, "<gray> (parent, priority={priority}<gray>)", "priority"),
    REGION__INFO__ACTION__UNLINK(true, "<red><hover:show_text:'Click to unlink parent'><click:suggest_command:'{command}'>[X]", "command"),

    REGION__INFO__PRIORITY__CLICK_CHANGE("Click to change"),

    REGION__INFO__SPECIFY("Please specify the region with /region info -w world_name region_name."),

    REGION__INFO__TITLE("Region Info"),

    REGION__INFO__WAIT("(Please wait... fetching region information...)"),

    REGION__LIST__FAIL("Failed to fetch region list"),
    REGION__LIST__FETCHING("Getting region list"),
    REGION__LIST__WAIT("(Please wait... fetching region list...)"),

    REGION__LIST__INFO__HEADER__ALL("Regions"),
    REGION__LIST__INFO__HEADER__PLAYER("Regions for {player}", "player"),
    REGION__LIST__INFO__OWNER("Region Owner"),
    REGION__LIST__INFO__MEMBER("Region Member"),
    REGION__LIST__INFO__ACTION__INFO(true, "<gray><hover:show_text:'Click for info'><click:run_command:'{command}'>[Info]", "command"),
    REGION__LIST__INFO__ACTION__TP(true, "<gray><hover:show_text:'Click to teleport'><click:run_command:'{command}'>[TP]", "command"),
    REGION__LIST__INFO__ACTION__TP_CENTER(true, "<gray><hover:show_text:'Click to teleport to the center'><click:run_command:'{command}'>[TP-Center]", "command"),
    REGION__LIST__INFO__ERROR__UUID_IO("Failed to lookup the UUID of '{name}'", "name"),
    REGION__LIST__INFO__ERROR__UUID_INTERRUPT("The lookup the UUID of '{name}' was interrupted", "name"),
    REGION__LIST__INFO__ERROR__DOESNT_EXIST("A user by the name of '{name}' does not seem to exist.", "name"),

    REGION__LOAD__ALL__FAIL("Failed to load regions for all worlds"),
    REGION__LOAD__ALL__LOADING("Loading regions for all worlds"),
    REGION__LOAD__ALL__SUCCESS("Successfully load the region data for all worlds."),
    REGION__LOAD__ALL__WAIT("(Please wait... loading region data for all worlds...)"),
    REGION__LOAD__FAIL("Failed to load region data for '{world}'", "world"),
    REGION__LOAD__LOADING("Loading region data for '{world}'.", "world"),
    REGION__LOAD__NO_MANAGER("No region manager exists for world '{world}'.", "world"),
    REGION__LOAD__SUCCESS("Loaded region data for '{world}'", "world"),
    REGION__LOAD__WAIT("Please wait... {description}", "description"),

    REGION__MEMBERSHIP__ADDMEMBER__ADDING("Adding members to the region '{region}' on '{world}'", "region", "world"),
    REGION__MEMBERSHIP__ADDMEMBER__FAIL("Failed to add new members"),
    REGION__MEMBERSHIP__ADDMEMBER__SUCCESS("Region '{region}' updated with new members.", "region"),

    REGION__MEMBERSHIP__ADDOWNER__ADDING("Adding owners to the region '{region}' on '{world}'", "region", "world"),
    REGION__MEMBERSHIP__ADDOWNER__FAIL("Failed to add new owners"),
    REGION__MEMBERSHIP__ADDOWNER__MAXIMUM("You already own the maximum allowed amount of regions."),
    REGION__MEMBERSHIP__ADDOWNER__SUCCESS("Region '{region}' updated with new owners.", "region"),

    REGION__MEMBERSHIP__REMOVEMEMBER__FAIL("Failed to remove members"),
    REGION__MEMBERSHIP__REMOVEMEMBER__LIST("List some names to remove, or use -a to remove all."),
    REGION__MEMBERSHIP__REMOVEMEMBER__REMOVING("Removing members from the region '{region}' on '{world}'", "region", "world"),
    REGION__MEMBERSHIP__REMOVEMEMBER__SUCCESS("Region '{region}' updated with members removed.", "region"),
    REGION__MEMBERSHIP__REMOVEMEMBER__WAIT("(Please wait... querying player names...)"),

    REGION__MEMBERSHIP__REMOVEOWNER__FAIL("Failed to remove owners"),
    REGION__MEMBERSHIP__REMOVEOWNER__LIST("List some names to remove, or use -a to remove all."),
    REGION__MEMBERSHIP__REMOVEOWNER__REMOVING("Removing owners from the region '{region}' on '{world}'", "region", "world"),
    REGION__MEMBERSHIP__REMOVEOWNER__SUCCESS("Region '{region}' updated with owners removed.", "region"),
    REGION__MEMBERSHIP__REMOVEOWNER__WAIT("(Please wait... querying player names...)"),

    REGION__MIGRATEDB__COMPLETE("Migration complete! This only migrated the data. If you already changed your settings to use the target driver, then WorldGuard is now using the new data. If not, you have to adjust your configuration to use the new driver and then restart your server."),
    REGION__MIGRATEDB__CONFIRM("This command is potentially dangerous.\nPlease ensure you have made a backup of your data, and then re-enter the command with -y tacked on at the end to proceed."),
    REGION__MIGRATEDB__ERROR__BASE("Error encountered while migrating: {message}", "message"),
    REGION__MIGRATEDB__ERROR__INVALID__FROM("The value of 'from' is not a recognized type of region data database."),
    REGION__MIGRATEDB__ERROR__INVALID__TO("The value of 'to' is not a recognized type of region region data database."),
    REGION__MIGRATEDB__ERROR__SAME("It is not possible to migrate between the same types of region data databases."),
    REGION__MIGRATEDB__ERROR__UNSUPPORTED__FROM("The driver specified as 'from' does not seem to be supported in your version of WorldGuard."),
    REGION__MIGRATEDB__ERROR__UNSUPPORTED__TO("The driver specified as 'to' does not seem to be supported in your version of WorldGuard."),
    REGION__MIGRATEDB__PERFORMING("Now performing migration... this may take a while."),

    REGION__MIGRATEHEIGHTS__COMPLETE("Migration complete!"),
    REGION__MIGRATEHEIGHTS__CONFIRM("This command is potentially dangerous.\nPlease ensure you have made a backup of your data, and then re-enter the command with -y tacked on at the end to proceed."),
    REGION__MIGRATEHEIGHTS__ERROR("Error encountered while migrating: {message}", "message"),

    REGION__MIGRATEUUID__COMPLETE("Migration complete!"),
    REGION__MIGRATEUUID__ERROR("Error encountered while migrating: {message}", "message"),
    REGION__MIGRATEUUID__PERFORMING("Now performing migration... this may take a while."),

    REGION__PARENT__INHERITANCE__CURRENT(true, "<gray>(Current inheritance:\n{tree}<gray>)", "tree"),
    REGION__PARENT__INHERITANCE__ON(true, "<gray>(Current inheritance on '{parent}':\n{tree}<gray>)", "parent", "tree"),
    REGION__PARENT__ERROR__CIRCULAR("Uh oh! Setting '{parent}' to be the parent of '{child}' would cause circular inheritance.", "parent", "child"),
    REGION__PARENT__ORPHANED(" Region is now orphaned."),
    REGION__PARENT__SET("Inheritance set for region '{region}'.", "region"),

    REGION__PRIORITY__SET("Priority of '{region}' set to {priority} (higher numbers override).", "region", "priority"),

    REGION__PROTECTION__ACTION__ANCHOR("use anchor"),
    REGION__PROTECTION__ACTION__ANVIL("use that"),
    REGION__PROTECTION__ACTION__BREAK("break that block"),
    REGION__PROTECTION__ACTION__CHANGE("change that"),
    REGION__PROTECTION__ACTION__CHAT("chat"),
    REGION__PROTECTION__ACTION__CREATE_PORTALS("create portals"),
    REGION__PROTECTION__ACTION__DAMAGE("damage that"),
    REGION__PROTECTION__ACTION__DISEMBARK("&6Don't disembark here!&7 You can't get back on."),
    REGION__PROTECTION__ACTION__DYNAMITE("use dynamite"),
    REGION__PROTECTION__ACTION__ENTITY_BREAK("break things"),
    REGION__PROTECTION__ACTION__EXPLOSIVES("use explosives"),
    REGION__PROTECTION__ACTION__FIRE("place fire"),
    REGION__PROTECTION__ACTION__FROSTWALKER("use frostwalker"),
    REGION__PROTECTION__ACTION__HARM("harm that"),
    REGION__PROTECTION__ACTION__HIT("hit that"),
    REGION__PROTECTION__ACTION__ITEM_DROP("drop items"),
    REGION__PROTECTION__ACTION__ITEM_PICKUP("pick up items"),
    REGION__PROTECTION__ACTION__OPEN("open that"),
    REGION__PROTECTION__ACTION__OTHER("use that"),
    REGION__PROTECTION__ACTION__PLACE("place that block"),
    REGION__PROTECTION__ACTION__POTION("use lingering potions"),
    REGION__PROTECTION__ACTION__PVP("PvP"),
    REGION__PROTECTION__ACTION__RIDE("ride that"),
    REGION__PROTECTION__ACTION__SLEEP("sleep"),
    REGION__PROTECTION__ACTION__TAKE("take that"),
    REGION__PROTECTION__ACTION__THINGS("place things"),
    REGION__PROTECTION__ACTION__USE("use that"),
    REGION__PROTECTION__ACTION__USE_WHAT("use {what}", "what"),
    REGION__PROTECTION__ACTION__VEHICLE_BREAK("break vehicles"),
    REGION__PROTECTION__ACTION__VEHICLE_PLACE("place vehicles"),
    REGION__PROTECTION__ACTION__XP_DROP("drop xp"),

    REGION__REDEFINE__ADDING("Updating region '{region}'", "region"),
    REGION__REDEFINE__FAIL("Failed to update the region '{region}'", "region"),
    REGION__REDEFINE__SUCCESS("Region '{region}' has been updated with a new area.", "region"),
    REGION__REDEFINE__WAIT("(Please wait... {description})", "description"),

    REGION__REMOVE__ERROR__FLAGS("You cannot use both -u (unset parent) and -f (remove children) together."),
    REGION__REMOVE__FAIL("Failed to remove region"),
    REGION__REMOVE__REMOVING("Removing region '{region}' in '{world}'", "region", "world"),
    REGION__REMOVE__SUCCESS("Successfully removed {region}.", "region"),
    REGION__REMOVE__WAIT("Please wait... removing region."),

    REGION__SAVE__ALL__FAIL("Failed to save regions for all worlds"),
    REGION__SAVE__ALL__SAVING("Saving regions for all worlds"),
    REGION__SAVE__ALL__SUCCESS("Successfully saved the region data for all worlds."),
    REGION__SAVE__ALL__WAIT("(Please wait... saving region data for all worlds...)"),

    REGION__SAVE__FAIL("Failed to save region data for '{world}'", "world"),
    REGION__SAVE__NO_MANAGER("No region manager exists for world '{world}'.", "world"),
    REGION__SAVE__SAVING("Saving region data for '{world}'.", "world"),
    REGION__SAVE__SUCCESS("Saving region data for '{world}'", "world"),
    REGION__SAVE__WAIT("Please wait... {description}", "description"),

    REGION__SELECT__SELECTED("Region selected as {region}", "region"),
    REGION__SELECT__SPECIFY("Please specify a region name."),
    REGION__SELECT__TYPE_FAIL("Can't select that region! The region type '{type}' can't be selected.", "type"),

    REGION__TELEPORT__FAIL("Unable to teleport to region '{region}'.", "region"),
    REGION__TELEPORT__NO_CENTER("The region has no center point."),
    REGION__TELEPORT__NO_SPAWN("The region has no spawn point associated."),
    REGION__TELEPORT__NO_TELEPORT("The region has no teleport point associated."),
    REGION__TELEPORT__SPECTATOR_ONLY("Center teleport is only available in Spectator gamemode."),

    WAND__BUILD__INFO("&eCan you build? {check}", "check"),
    WAND__BUILD__NO("No"),
    WAND__BUILD__YES("Yes"),
    WAND__REGIONS__EMPTY("WorldGuard: No defined regions here!"),
    WAND__REGIONS__LIST("Applicable regions: {regions}", "regions"),

    ;

    private final String defaultMsg;
    private final boolean miniMessage;
    private final String[] placeholders;
    private volatile Compiled current;

    Msg(String def, String... phs) {
        this(false, def, phs);
    }

    Msg(boolean miniMessage, String def, String... phs) {
        defaultMsg = def;
        this.miniMessage = miniMessage;
        placeholders = new String[phs.length];
        for (int i = 0; i < phs.length; i++) {
            placeholders[i] = "{" + phs[i] + "}";
        }
        current = compile(getDefaultMessage());
    }

    public String getDefault() {
        return defaultMsg;
    }

    public boolean isMiniMessage() {
        return miniMessage;
    }

    public Message getDefaultMessage() {
        return Message.of(defaultMsg, miniMessage ? Message.Serializer.MINIMESSAGE : Message.Serializer.LEGACY_AMPERSAND);
    }

    public String get(Object... args) {
        return Message.Serializer.toLegacy(text(args));
    }

    public CommandException cmdException(Object... args) {
        return new CommandException(get(args));
    }

    public Component text(Object... args) {
        Compiled compiled = current;
        Rendered rendered = render(compiled, args);
        return compiled.styled(Messenger.combine(compiled.message.place(), rendered.main, rendered.sub));
    }

    private void send(Object receiver, Message.Type fallback, Object... args) {
        Compiled compiled = current;
        Rendered rendered = render(compiled, args);
        Message.Place place = compiled.message.place();
        Message.Type type = place == Message.Place.CHAT && compiled.message.type() == null ? fallback : compiled.message.type();
        if (type == null) {
            Messenger.send(receiver, place, rendered.main, rendered.sub);
        } else {
            Messenger.send(receiver, place, type.apply(rendered.main), rendered.sub == null ? null : type.apply(rendered.sub));
        }
    }

    public void info(Object receiver, Object... args) {
        send(receiver, Message.Type.INFO, args);
    }

    public void error(Object receiver, Object... args) {
        send(receiver, Message.Type.ERROR, args);
    }

    public void debug(Object receiver, Object... args) {
        send(receiver, Message.Type.DEBUG, args);
    }

    public void plain(Object receiver, Object... args) {
        send(receiver, Message.Type.PLAIN, args);
    }

    public boolean setMessage(@Nullable Message message) {
        if (message == null) {
            current = compile(getDefaultMessage());
            return false;
        }
        current = compile(message);
        return true;
    }

    public String asSection() {
        return name().toLowerCase(Locale.ENGLISH)
                .replace("__", ".")
                .replace('_', '-');
    }

    private Compiled compile(Message message) {
        Message.Serializer serializer = message.serializer() != null ? message.serializer() : Messenger.defaultSerializer();
        String mainText = message.text();
        String subText = null;
        if (message.place() == Message.Place.SUBTITLE) {
            subText = mainText;
            mainText = "";
        } else if (message.place() == Message.Place.TITLE) {
            int newline = mainText.indexOf('\n');
            if (newline >= 0) {
                subText = mainText.substring(newline + 1);
                mainText = mainText.substring(0, newline);
            }
        }
        boolean mini = serializer == Message.Serializer.MINIMESSAGE;
        return new Compiled(message, serializer, mainText, subText, mini ? tags(mainText) : null, mini && subText != null ? tags(subText) : null);
    }

    private Rendered render(Compiled compiled, Object[] args) {
        if (placeholders.length == 0 || args.length == 0) {
            return new Rendered(compiled.main, compiled.sub);
        }
        Message.Serializer serializer = compiled.serializer;
        if (serializer == Message.Serializer.MINIMESSAGE) {
            TagResolver resolver = resolver(args);
            return new Rendered(
                    Message.Serializer.deserializeMiniMessage(compiled.mainTags, resolver),
                    compiled.subTags == null ? null : Message.Serializer.deserializeMiniMessage(compiled.subTags, resolver)
            );
        }
        return new Rendered(fill(compiled.main, args), compiled.sub == null ? null : fill(compiled.sub, args));
    }

    private Component fill(Component template, Object[] args) {
        List<Component> children = template.children().stream().map(child -> fill(child, args)).toList();
        if (!(template instanceof TextComponent text)) {
            return template.children(children);
        }
        List<Piece> pieces = split(text.content());
        if (pieces.stream().noneMatch(piece -> piece.placeholder >= 0)) {
            return template.children(children);
        }
        TextComponent.Builder builder = Component.text().style(text.style());
        for (Piece piece : pieces) {
            builder.append(piece.placeholder < 0 ? Component.text(piece.text) : argument(args, piece.placeholder));
        }
        return builder.append(children).build();
    }

    private String tags(String text) {
        String result = text;
        for (String placeholder : placeholders) {
            result = result.replace(placeholder, "<" + placeholder.substring(1, placeholder.length() - 1) + ">");
        }
        return result;
    }

    @SuppressWarnings("PatternValidation")
    private TagResolver resolver(Object[] args) {
        List<TagResolver> resolvers = new ArrayList<>();
        for (int i = 0; i < placeholders.length && i < args.length; i++) {
            String name = placeholders[i].substring(1, placeholders[i].length() - 1);
            String value = String.valueOf(args[i]);
            if (args[i] instanceof ComponentLike like) {
                resolvers.add(Placeholder.component(name, like));
            } else if (value.indexOf('§') >= 0) {
                resolvers.add(Placeholder.component(name, Message.Serializer.fromLegacy(value)));
            } else {
                resolvers.add(Placeholder.parsed(name, MiniMessage.miniMessage().escapeTags(value)));
            }
        }
        return TagResolver.resolver(resolvers);
    }

    private List<Piece> split(String text) {
        List<Piece> pieces = new ArrayList<>();
        int from = 0;
        while (true) {
            int start = -1;
            int found = -1;
            for (int i = 0; i < placeholders.length; i++) {
                int index = text.indexOf(placeholders[i], from);
                if (index >= 0 && (start < 0 || index < start)) {
                    start = index;
                    found = i;
                }
            }
            if (found < 0) {
                break;
            }
            if (start > from) {
                pieces.add(new Piece(from, text.substring(from, start), -1));
            }
            pieces.add(new Piece(start, placeholders[found], found));
            from = start + placeholders[found].length();
        }
        if (from < text.length()) {
            pieces.add(new Piece(from, text.substring(from), -1));
        }
        return pieces;
    }

    private Component argument(Object[] args, int index) {
        if (index >= args.length) {
            return Component.text(placeholders[index]);
        }
        return args[index] instanceof ComponentLike like ? like.asComponent() : Message.Serializer.fromLegacy(String.valueOf(args[index]));
    }

    private static final class Compiled {
        private final Message message;
        private final Message.Serializer serializer;
        private final String mainTags;
        private final String subTags;
        private final Component main;
        private final Component sub;

        private Compiled(Message message, Message.Serializer serializer, String mainText, String subText, String mainTags, String subTags) {
            this.message = message;
            this.serializer = serializer;
            this.mainTags = mainTags;
            this.subTags = subTags;
            this.main = serializer.deserialize(mainText);
            this.sub = subText == null ? null : serializer.deserialize(subText);
        }

        private Component styled(Component component) {
            return message.type() == null ? component : message.type().apply(component);
        }
    }

    private record Rendered(Component main, @Nullable Component sub) { }

    private record Piece(int start, String text, int placeholder) { }
}
