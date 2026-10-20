/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.util.Entities;
import me.imdanix.wgtranslator.Messenger;
import me.imdanix.wgtranslator.Msg;
import me.imdanix.wgtranslator.text.LabelFormat;

public class ToggleCommands {
    private final WorldGuard worldGuard;

    public ToggleCommands(WorldGuard worldGuard) {
        this.worldGuard = worldGuard;
    }

    @Command(aliases = {"stopfire"}, usage = "[<world>]",
            desc = "Disables all fire spread temporarily", max = 1)
    @CommandPermissions({"worldguard.fire-toggle.stop"})
    public void stopFire(CommandContext args, Actor sender) throws CommandException {
        
        World world;
        
        if (args.argsLength() == 0) {
            world = worldGuard.checkPlayer(sender).getWorld();
        } else {
            world = worldGuard.getPlatform().getMatcher().matchWorld(sender, args.getString(0));
        }
        
        WorldConfiguration wcfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(world);

        if (!wcfg.fireSpreadDisableToggle) {
            Messenger.broadcast("worldguard.notify",
                    LabelFormat.wrap(Msg.FIRE__STOP__SUCCESS.text(world.getName(), sender.getDisplayName())));
        } else {
            Msg.FIRE__STOP__ALREADY.info(sender);
        }

        wcfg.fireSpreadDisableToggle = true;
    }

    @Command(aliases = {"allowfire"}, usage = "[<world>]",
            desc = "Allows all fire spread temporarily", max = 1)
    @CommandPermissions({"worldguard.fire-toggle.stop"})
    public void allowFire(CommandContext args, Actor sender) throws CommandException {
        
        World world;
        
        if (args.argsLength() == 0) {
            world = worldGuard.checkPlayer(sender).getWorld();
        } else {
            world = worldGuard.getPlatform().getMatcher().matchWorld(sender, args.getString(0));
        }
        
        WorldConfiguration wcfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(world);

        if (wcfg.fireSpreadDisableToggle) {
            Messenger.broadcast("worldguard.notify",
                    LabelFormat.wrap(Msg.FIRE__ALLOW__SUCCESS.text(world.getName(), sender.getDisplayName())));
        } else {
            Msg.FIRE__ALLOW__ALREADY.info(sender);
        }

        wcfg.fireSpreadDisableToggle = false;
    }

    @Command(aliases = {"halt-activity", "stoplag", "haltactivity"}, usage = "[confirm]",
            desc = "Attempts to cease as much activity in order to stop lag", flags = "cis", max = 1)
    @CommandPermissions({"worldguard.halt-activity"})
    public void stopLag(CommandContext args, Actor sender) throws CommandException {

        ConfigurationManager configManager = WorldGuard.getInstance().getPlatform().getGlobalStateManager();

        if (args.hasFlag('i')) {
            if (configManager.activityHaltToggle) {
                Msg.HALT__STATUS__HALTED.info(sender);
            } else {
                Msg.HALT__STATUS__ALLOWED.info(sender);
            }
        } else {
            boolean activityHaltToggle = !args.hasFlag('c');

            if (activityHaltToggle && (args.argsLength() == 0 || !args.getString(0).equalsIgnoreCase("confirm"))) {
                String confirmCommand = "/" + args.getCommand() + " confirm";

                Msg.HALT__CONFIRM__MESSAGE.plain(sender, confirmCommand);
                return;
            }

            configManager.activityHaltToggle = activityHaltToggle;

            if (activityHaltToggle) {
                if (!(sender instanceof LocalPlayer)) {
                    Msg.HALT__HALTED__INFO.info(sender);
                }

                if (!args.hasFlag('s')) {
                    Messenger.broadcast("worldguard.notify",
                            LabelFormat.wrap(Msg.HALT__HALTED__BROADCAST.text(sender.getDisplayName())));
                } else {
                    Msg.HALT__HALTED__SILENT.info(sender, sender.getDisplayName());
                }

                for (World world : WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getWorlds()) {
                    int removed = 0;

                    for (Entity entity : world.getEntities()) {
                        if (Entities.isIntensiveEntity(entity)) {
                            entity.remove();
                            removed++;
                        }
                    }

                    if (removed > 10) {
                        Msg.HALT__ENTITIES_REMOVED.plain(sender, removed, world.getName());
                    }
                }
            } else {
                if (!args.hasFlag('s')) {
                    Messenger.broadcast("worldguard.notify",
                            LabelFormat.wrap(Msg.HALT__ALLOWED__BROADCAST.text()));

                    if (!(sender instanceof LocalPlayer)) {
                        Msg.HALT__ALLOWED__INFO.info(sender);
                    }
                } else {
                    Msg.HALT__ALLOWED__SILENT.info(sender);
                }
            }
        }
    }
}
