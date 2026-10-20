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

package com.sk89q.worldguard.commands.region;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.*;
import com.sk89q.worldguard.protection.regions.RegionQuery.QueryOption;
import com.sk89q.worldguard.protection.util.WorldEditRegionConverter;
import me.imdanix.wgtranslator.Messenger;
import me.imdanix.wgtranslator.Msg;
import me.imdanix.wgtranslator.text.Text;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class RegionCommandsBase {

    protected RegionCommandsBase() {
    }

    /**
     * Get the permission model to lookup permissions.
     *
     * @param sender the sender
     * @return the permission model
     */
    protected static RegionPermissionModel getPermissionModel(Actor sender) {
        return new RegionPermissionModel(sender);
    }

    /**
     * Gets the world from the given flag, or falling back to the the current player
     * if the sender is a player, otherwise reporting an error.
     *
     * @param args the arguments
     * @param sender the sender
     * @param flag the flag (such as 'w')
     * @return a world
     * @throws CommandException on error
     */
    protected static World checkWorld(CommandContext args, Actor sender, char flag) throws CommandException {
        return checkWorld(args, sender, flag, true);
    }

    protected static World checkWorld(CommandContext args, Actor sender, char flag, boolean allowWorldEditOverride) throws CommandException {
        if (args.hasFlag(flag)) {
            return WorldGuard.getInstance().getPlatform().getMatcher().matchWorld(sender, args.getFlag(flag));
        } else {
            if (allowWorldEditOverride) {
                try {
                    World override = WorldEdit.getInstance().getSessionManager().get(sender).getWorldOverride();
                    if (override != null) {
                        if (sender instanceof LocalPlayer && !override.equals(((LocalPlayer) sender).getWorld())) {
                            Msg.REGION__COMMANDS__USING_WORLD.debug(sender, override.getName());
                        }
                        return override;
                    }
                } catch (NoSuchMethodError ignored) {
                }
            }
            if (sender instanceof LocalPlayer) {
                return ((LocalPlayer) sender).getWorld();
            } else {
                throw Msg.REGION__COMMANDS__WORLD_SPECIFY.cmdException(flag);
            }
        }
    }

    /**
     * Validate a region ID.
     *
     * @param id the id
     * @param allowGlobal whether __global__ is allowed
     * @return the id given
     * @throws CommandException thrown on an error
     */
    protected static String checkRegionId(String id, boolean allowGlobal) throws CommandException {
        if (!ProtectedRegion.isValidId(id)) {
            throw Msg.REGION__COMMANDS__REGION_INVALID_ID.cmdException(id);
        }

        if (!allowGlobal && id.equalsIgnoreCase("__global__")) { // Sorry, no global
            throw Msg.REGION__COMMANDS__REGION_NO_GLOBAL.cmdException();
        }

        return id;
    }

    /**
     * Get a protected region by a given name, otherwise throw a
     * {@link CommandException}.
     *
     * <p>This also validates the region ID.</p>
     *
     * @param regionManager the region manager
     * @param id the name to search
     * @param allowGlobal true to allow selecting __global__
     * @throws CommandException thrown if no region is found by the given name
     */
    protected static ProtectedRegion checkExistingRegion(RegionManager regionManager, String id, boolean allowGlobal) throws CommandException {
        // Validate the id
        checkRegionId(id, allowGlobal);

        ProtectedRegion region = regionManager.getRegion(id);

        // No region found!
        if (region == null) {
            // But we want a __global__, so let's create one
            if (id.equalsIgnoreCase("__global__")) {
                region = new GlobalProtectedRegion(id);
                regionManager.addRegion(region);
                return region;
            }

            throw Msg.REGION__COMMANDS__REGION_NOT_FOUND.cmdException(id);
        }

        return region;
    }


    /**
     * Get the region at the player's location, if possible.
     *
     * <p>If the player is standing in several regions, an error will be raised
     * and a list of regions will be provided.</p>
     *
     * @param regionManager the region manager
     * @param player the player
     * @param rgCmd the command to construct when clicking one of multiple items
     * @param permissionPredicate a predicate to filter regions based on permission access
     * @return a region
     * @throws CommandException thrown if no region was found
     */
    protected static ProtectedRegion checkRegionStandingIn(RegionManager regionManager, LocalPlayer player, String rgCmd, Predicate<ProtectedRegion> permissionPredicate) throws CommandException {
        return checkRegionStandingIn(regionManager, player, false, rgCmd, permissionPredicate);
    }

    /**
     * Get the region at the player's location, if possible.
     *
     * <p>If the player is standing in several regions, an error will be raised
     * and a list of regions will be provided.</p>
     *
     * <p>If the player is not standing in any regions, the global region will
     * returned if allowGlobal is true and it exists.</p>
     *
     * @param regionManager the region manager
     * @param player the player
     * @param allowGlobal whether to search for a global region if no others are found
     * @param rgCmd the command to construct when clicking one of multiple items
     * @param permissionPredicate a predicate to filter regions based on permission access
     * @return a region
     * @throws CommandException thrown if no region was found
     */
    protected static ProtectedRegion checkRegionStandingIn(RegionManager regionManager, LocalPlayer player, boolean allowGlobal, String rgCmd, Predicate<ProtectedRegion> permissionPredicate) throws CommandException {
        ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation().toVector().toBlockPoint(), QueryOption.SORT);

        if (set.size() == 0) {
            if (allowGlobal) {
                ProtectedRegion global = checkExistingRegion(regionManager, "__global__", true);
                Msg.REGION__COMMANDS__NOT_STANDING_GLOBAL.debug(player);
                return global;
            }
            throw Msg.REGION__COMMANDS__NOT_STANDING_IN_REGION.cmdException();
        } else if (set.size() > 1) {
            boolean first = true;
            Set<ProtectedRegion> filteredRegions = set.getRegions().stream().filter(permissionPredicate).collect(Collectors.toSet());
            int hiddenRegions = set.size() - filteredRegions.size();

            final TextComponent.Builder builder = Text.builder("");
            if (!filteredRegions.isEmpty()) {
                builder.append(Msg.REGION__COMMANDS__CURRENT_REGIONS.text());
                for (ProtectedRegion region : filteredRegions) {
                    if (!first) {
                        builder.append(Text.of(", "));
                    }
                    first = false;
                    TextComponent regionComp = Text.of(region.getId(), NamedTextColor.AQUA);
                    if (rgCmd != null && rgCmd.contains("%id%")) {
                        regionComp = regionComp.hoverEvent(HoverEvent.showText(Msg.REGION__COMMANDS__CLICK_TO_PICK.text()))
                                .clickEvent(ClickEvent.runCommand(rgCmd.replace("%id%", region.getId())));
                    }
                    builder.append(regionComp);
                }
                if (hiddenRegions > 0) {
                    builder.append(Text.of(Msg.REGION__COMMANDS__CURRENT_REGIONS_WITH_HIDDEN.text(hiddenRegions), NamedTextColor.GRAY));
                }
            } else {
                builder.append(Text.of(Msg.REGION__COMMANDS__CURRENT_REGIONS.text(), NamedTextColor.GOLD));
                builder.append(Text.of(Msg.REGION__COMMANDS__CURRENT_REGIONS_HIDDEN.text(hiddenRegions), NamedTextColor.GRAY));
            }
            Messenger.plain(player, builder.build());
            throw Msg.REGION__COMMANDS__STANDING_IN_MULTIPLE.cmdException();
        }

        return set.iterator().next();
    }

    /**
     * Get a WorldEdit selection for an actor, or emit an exception if there is none
     * available.
     *
     * @param actor the actor
     * @return the selection
     * @throws CommandException thrown on an error
     */
    protected static Region checkSelection(Actor actor) throws CommandException {
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(actor);
        try {
            if (localSession == null || localSession.getSelectionWorld() == null) {
                throw new IncompleteRegionException();
            }
            return localSession.getRegionSelector(localSession.getSelectionWorld()).getRegion();
        } catch (IncompleteRegionException e) {
            throw Msg.REGION__COMMANDS__SELECT_AREA_FIRST.cmdException();
        }
    }

    /**
     * Check that a region with the given ID does not already exist.
     *
     * @param manager the manager
     * @param id the ID
     * @throws CommandException thrown if the ID already exists
     */
    protected static void checkRegionDoesNotExist(RegionManager manager, String id, boolean mayRedefine) throws CommandException {
        if (manager.hasRegion(id)) {
            throw new CommandException(Msg.REGION__COMMANDS__REGION_EXISTS.get() +
                    (mayRedefine ? Msg.REGION__COMMANDS__REGION_EXISTS_REDEFINE.get(id) : ""));
        }
    }

    /**
     * Check that the given region manager is not null.
     *
     * @param world the world
     * @throws CommandException thrown if the manager is null
     */
    protected static RegionManager checkRegionManager(World world) throws CommandException {
        if (!WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(world).useRegions) {
            throw Msg.REGION__COMMANDS__REGIONS_DISABLED.cmdException();
        }

        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        if (manager == null) {
            throw Msg.REGION__COMMANDS__REGION_DATA_LOAD_FAIL.cmdException();
        }
        return manager;
    }

    /**
     * Create a {@link ProtectedRegion} from the actor's selection.
     *
     * @param actor the actor
     * @param id the ID of the new region
     * @return a new region
     * @throws CommandException thrown on an error
     */
    protected static ProtectedRegion checkRegionFromSelection(Actor actor, String id) throws CommandException {
        Region selection = checkSelection(actor);

        // Detect the type of region from WorldEdit
        if (selection instanceof Polygonal2DRegion) {
            Polygonal2DRegion polySel = (Polygonal2DRegion) selection;
            int minY = polySel.getMinimumPoint().y();
            int maxY = polySel.getMaximumPoint().y();
            return new ProtectedPolygonalRegion(id, polySel.getPoints(), minY, maxY);
        } else if (selection instanceof CuboidRegion) {
            BlockVector3 min = selection.getMinimumPoint();
            BlockVector3 max = selection.getMaximumPoint();
            return new ProtectedCuboidRegion(id, min, max);
        } else {
            throw Msg.REGION__COMMANDS__UNSUPPORTED_SHAPE.cmdException();
        }
    }

    /**
     * Warn the region saving is failing.
     *
     * @param sender the sender to send the message to
     */
    protected static void warnAboutSaveFailures(Actor sender) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        Set<RegionManager> failures = container.getSaveFailures();

        if (!failures.isEmpty()) {
            String failingList = Joiner.on(", ").join(failures.stream()
                    .map(regionManager -> "'" + regionManager.getName() + "'").collect(Collectors.toList()));

            Messenger.plain(sender, Msg.REGION__COMMANDS__SAVE_FAILURES.text(failingList));
        }
    }

    /**
     * Warn the sender if the dimensions of the given region are worrying.
     *
     * @param sender the sender to send the message to
     * @param region the region
     */
    protected static void warnAboutDimensions(Actor sender, ProtectedRegion region) {
        if (region instanceof GlobalProtectedRegion) {
            return;
        }
        int height = region.getMaximumPoint().y() - region.getMinimumPoint().y();
        if (height <= 2) {
            Msg.REGION__COMMANDS__HEIGHT_WARNING.debug(sender, height + 1);
        }
    }

    /**
     * Inform a new user about automatic protection.
     *
     * @param sender the sender to send the message to
     * @param manager the region manager
     * @param region the region
     */
    protected static void informNewUser(Actor sender, RegionManager manager, ProtectedRegion region) {
        if (manager.size() <= 2) {
            Msg.REGION__COMMANDS__NEW_USER_INFO.plain(sender, "/rg flag " + region.getId() + " passthrough allow");
        }
    }

    /**
     * Inform a user if the region overlaps spawn protection.
     *
     * @param sender the sender to send the message to
     * @param world the world the region is in
     * @param region the region
     */
    protected static boolean checkSpawnOverlap(Actor sender, World world, ProtectedRegion region) {
        ProtectedRegion spawn = WorldGuard.getInstance().getPlatform().getSpawnProtection(world);
        if (spawn != null) {
            if (!spawn.getIntersectingRegions(ImmutableList.of(region)).isEmpty()) {
                Msg.REGION__COMMANDS__SPAWN_OVERLAP.plain(sender);
                return true;
            }
        }
        return false;
    }

    /**
     * Set an actor's selection to a given region.
     *
     * @param actor the actor
     * @param region the region
     * @throws CommandException thrown on a command error
     */
    protected static void setPlayerSelection(Actor actor, ProtectedRegion region, World world) throws CommandException {
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(actor);

        RegionSelector selector = WorldEditRegionConverter.convertToSelector(region);
        if (selector != null) {
            selector.setWorld(world);
            session.setRegionSelector(world, selector);
            selector.explainRegionAdjust(actor, session);
            Messenger.plain(actor, Msg.REGION__SELECT__SELECTED.text(region.getType().getName()));
        } else {
            throw Msg.REGION__SELECT__TYPE_FAIL.cmdException(region.getType().getName());
        }
    }

    /**
     * Utility method to set a flag.
     *
     * @param region the region
     * @param flag the flag
     * @param sender the sender
     * @param value the value
     * @throws InvalidFlagFormat thrown if the value is invalid
     */
    protected static <V> V setFlag(ProtectedRegion region, Flag<V> flag, Actor sender, String value) throws InvalidFlagFormat {
        V val = flag.parseInput(FlagContext.create().setSender(sender).setInput(value).setObject("region", region).build());
        region.setFlag(flag, val);
        return val;
    }

}
