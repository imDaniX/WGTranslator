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

package com.sk89q.worldguard.commands.task;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.FlagValueCalculator;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.profile.Profile;
import me.imdanix.wgtranslator.Messenger;
import me.imdanix.wgtranslator.Msg;
import me.imdanix.wgtranslator.text.PaginationBox;
import me.imdanix.wgtranslator.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public class RegionLister implements Callable<Integer> {

    private static final Logger log = Logger.getLogger(RegionLister.class.getCanonicalName());

    private final Actor sender;
    private final RegionManager manager;
    private final String world;
    private OwnerMatcher ownerMatcher;
    private String idFilter;
    private ProtectedRegion filterByIntersecting;
    private int page;
    private String playerName;
    private boolean nameOnly;

    public RegionLister(RegionManager manager, Actor sender, String world) {
        checkNotNull(manager);
        checkNotNull(sender);
        checkNotNull(world);

        this.manager = manager;
        this.sender = sender;
        this.world = world;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void filterByIntersecting(ProtectedRegion region) {
        this.filterByIntersecting = region;
    }

    public void filterOwnedByName(String name, boolean nameOnly) {
        this.playerName = name;
        this.nameOnly = nameOnly;
        if (nameOnly) {
            filterOwnedByName(name);
        } else {
            filterOwnedByProfile(name);
        }
    }

    private void filterOwnedByName(final String name) {
        ownerMatcher = new OwnerMatcher() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean isContainedWithin(DefaultDomain domain) {
                return domain.contains(name);
            }
        };
    }

    private void filterOwnedByProfile(final String name) {
        ownerMatcher = new OwnerMatcher() {
            private UUID uniqueId;

            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean isContainedWithin(DefaultDomain domain) throws CommandException {
                if (domain.contains(name)) {
                    return true;
                }

                if (uniqueId == null) {
                    Profile profile;

                    try {
                        profile = WorldGuard.getInstance().getProfileService().findByName(name);
                    } catch (IOException e) {
                        log.log(Level.WARNING, "Failed UUID lookup of '" + name + "'", e);
                        throw Msg.REGION__LIST__INFO__ERROR__UUID_IO.cmdException(name);
                    } catch (InterruptedException e) {
                        log.log(Level.WARNING, "Failed UUID lookup of '" + name + "'", e);
                        throw Msg.REGION__LIST__INFO__ERROR__UUID_INTERRUPT.cmdException(name);
                    }

                    if (profile == null) {
                        throw Msg.REGION__LIST__INFO__ERROR__DOESNT_EXIST.cmdException(name);
                    }

                    uniqueId = profile.getUniqueId();
                }

                return domain.contains(uniqueId);
            }
        };
    }

    public void filterIdByMatch(String idFilter) {
        this.idFilter = idFilter;
    }

    @Override
    public Integer call() throws Exception {
        Map<String, ProtectedRegion> regions = manager.getRegions();
        Collection<ProtectedRegion> iterableRegions = regions.values();

        if (filterByIntersecting != null) {
            iterableRegions = filterByIntersecting.getIntersectingRegions(iterableRegions);
        }

        // Build a list of regions to show
        List<RegionListEntry> entries = new ArrayList<>();

        for (ProtectedRegion rg : iterableRegions) {
            if (rg.getId().equals("__global__")) {
                continue;
            }
            final RegionListEntry entry = new RegionListEntry(rg);

            if (entry.matches(idFilter) && entry.matches(ownerMatcher)) {
                entries.add(entry);
            }
        }

        if (ownerMatcher == null) {
            Collections.sort(entries);
        }
        // insert global on top
        if (regions.containsKey("__global__")) {
            final RegionListEntry entry = new RegionListEntry(regions.get("__global__"));
            if (entry.matches(idFilter) && entry.matches(ownerMatcher)) {
                entries.add(0, entry);
            }
        }
        // unless we're matching owners, then sort by ownership
        if (ownerMatcher != null) {
            Collections.sort(entries);
        }

        RegionPermissionModel perms = sender.isPlayer() ? new RegionPermissionModel(sender) : null;
        Component title = ownerMatcher == null ? Msg.REGION__LIST__INFO__HEADER__ALL.text() : Msg.REGION__LIST__INFO__HEADER__PLAYER.text(ownerMatcher.getName());
        String cmd = "/rg list -w \"" + world + "\""
                + (playerName != null ? " -p " + playerName : "")
                + (nameOnly ? " -n" : "")
                + (filterByIntersecting != null ? " -s" : "")
                + (idFilter != null ? " -i " + idFilter : "")
                + " %page%";
        PaginationBox box = new RegionListBox(title, cmd, perms, entries, world);
        Messenger.plain(sender, box.create(page));

        return page;
    }

    private interface OwnerMatcher {
        String getName();

        boolean isContainedWithin(DefaultDomain domain) throws CommandException;
    }

    private static final class RegionListEntry implements Comparable<RegionListEntry> {
        private final ProtectedRegion region;
        private boolean isOwner;
        private boolean isMember;

        private RegionListEntry(ProtectedRegion rg) {
            this.region = rg;
        }

        public boolean matches(OwnerMatcher matcher) throws CommandException {
            return matcher == null
                    || (isOwner = matcher.isContainedWithin(region.getOwners()))
                    || (isMember = matcher.isContainedWithin(region.getMembers()));
        }

        public boolean matches(String idMatcher) {
            return idMatcher == null || region.getId().contains(idMatcher);
        }

        public ProtectedRegion getRegion() {
            return region;
        }

        public boolean isOwner() {
            return isOwner;
        }

        public boolean isMember() {
            return isMember;
        }

        @Override
        public int compareTo(RegionListEntry o) {
            if (isOwner != o.isOwner) {
                return isOwner ? -1 : 1;
            }
            if (isMember != o.isMember) {
                return isMember ? -1 : 1;
            }
            return region.getId().compareTo(o.region.getId());
        }
    }

    private static class RegionListBox extends PaginationBox {
        private final RegionPermissionModel perms;
        private final List<RegionListEntry> entries;
        private String world;

        RegionListBox(Component title, String cmd, RegionPermissionModel perms, List<RegionListEntry> entries, String world) {
            super(title, cmd);
            this.perms = perms;
            this.entries = entries;
            this.world = world;
        }

        @Override
        public Component getComponent(int number) {
            final RegionListEntry entry = entries.get(number);
            final TextComponent.Builder builder = Text.builder(number + 1 + ".").color(NamedTextColor.LIGHT_PURPLE);
            if (entry.isOwner()) {
                builder.append(Text.space()).append(Text.of("+", NamedTextColor.DARK_AQUA)
                        .hoverEvent(HoverEvent.showText(Text.of(Msg.REGION__LIST__INFO__OWNER.text(), NamedTextColor.GOLD))));
            } else if (entry.isMember()) {
                builder.append(Text.space()).append(Text.of("-", NamedTextColor.AQUA)
                        .hoverEvent(HoverEvent.showText(Text.of(Msg.REGION__LIST__INFO__MEMBER.text(), NamedTextColor.GOLD))));
            }
            builder.append(Text.space()).append(Text.of(entry.getRegion().getId(), NamedTextColor.GOLD));
            if (perms != null && perms.mayLookup(entry.region)) {
                builder.append(Text.space().append(Msg.REGION__LIST__INFO__ACTION__INFO.text(
                        "/rg info -w \"" + world + "\" " + entry.region.getId())));
            }
            final Location teleFlag = FlagValueCalculator.getEffectiveFlagOf(entry.region, Flags.TELE_LOC, perms != null && perms.getSender() instanceof RegionAssociable ? (RegionAssociable) perms.getSender() : null);
            if (perms != null && teleFlag != null && perms.mayTeleportTo(entry.region)) {
                builder.append(Text.space().append(Msg.REGION__LIST__INFO__ACTION__TP.text(
                        "/rg tp -w \"" + world + "\" " + entry.region.getId())));
            } else if (perms != null && perms.mayTeleportToCenter(entry.getRegion()) && entry.getRegion().isPhysicalArea()) {
                builder.append(Text.space().append(Msg.REGION__LIST__INFO__ACTION__TP_CENTER.text(
                        "/rg tp -c -w \"" + world + "\" " + entry.region.getId())));
            }
            return builder.build();
        }

        @Override
        public int getComponentsSize() {
            return entries.size();
        }
    }
}
