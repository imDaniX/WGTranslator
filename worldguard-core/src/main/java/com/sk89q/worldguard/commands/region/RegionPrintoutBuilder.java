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

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.FlagValueCalculator;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.flags.registry.UnknownFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.profile.cache.ProfileCache;
import me.imdanix.wgtranslator.Messenger;
import me.imdanix.wgtranslator.Msg;
import me.imdanix.wgtranslator.text.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;

/**
 * Create a region printout, as used in /region info to show information about
 * a region.
 */
public class RegionPrintoutBuilder implements Callable<com.sk89q.worldedit.util.formatting.text.TextComponent> {

    private final String world;
    private final ProtectedRegion region;
    @Nullable
    private final ProfileCache cache;
    private TextComponentProducer builder = new TextComponentProducer();
    private final RegionPermissionModel perms;

    /**
     * Create a new instance with a region to report on.
     *
     * @param region the region
     * @param cache a profile cache, or {@code null}
     */
    public RegionPrintoutBuilder(String world, ProtectedRegion region, @Nullable ProfileCache cache, @Nullable Actor actor) {
        this.world = world;
        this.region = region;
        this.cache = cache;
        this.perms = actor != null && actor.isPlayer() ? new RegionPermissionModel(actor) : null;
    }

    @Deprecated
    public void newline() {
        builder.append(Text.newline());
    }

    @Deprecated
    public void appendBasics() {
        builder.append(Text.of("Region: ", NamedTextColor.BLUE));
        builder.append(regionId().color(NamedTextColor.YELLOW));
        builder.append(Text.of(" (type=", NamedTextColor.GRAY));
        builder.append(Text.of(region.getType().getName()));
        builder.append(Text.of(", priority=", NamedTextColor.GRAY));
        appendPriorityComponent(region);
        builder.append(Text.of(")", NamedTextColor.GRAY));
        newline();
    }

    @Deprecated
    public void appendVolume() {
        builder.append(Text.of("Volume: ", NamedTextColor.BLUE));
        builder.append(Text.of(String.valueOf(region.volume()), NamedTextColor.YELLOW));
        newline();
    }

    @Deprecated
    public void appendFlags() {
        builder.append(Text.of("Flags: ", NamedTextColor.BLUE));
        appendFlagsList(true);
        newline();
    }

    @Deprecated
    public void appendFlagsList(boolean useColors) {
        boolean hasFlags = false;

        for (Flag<?> flag : WorldGuard.getInstance().getFlagRegistry()) {
            Object val = region.getFlag(flag);

            // No value
            if (val == null) {
                continue;
            }

            if (hasFlags) {
                builder.append(Msg.REGION__INFO__FLAGS__SEPARATOR.text());
            }

            RegionGroupFlag groupFlag = flag.getRegionGroupFlag();
            Object group = null;
            if (groupFlag != null) {
                group = region.getFlag(groupFlag);
            }

            String flagName = group == null ? flag.getName() : flag.getName() + " -g " + group;

            NamedTextColor flagColor = NamedTextColor.WHITE;
            if (useColors) {
                // passthrough is ok on global
                if (FlagHelperBox.DANGER_ZONE.contains(flag)
                        && !(region.getId().equals(ProtectedRegion.GLOBAL_REGION) && flag == Flags.PASSTHROUGH)) {
                    flagColor = NamedTextColor.DARK_RED;
                } else if (Flags.INBUILT_FLAGS.contains(flag.getName())) {
                    flagColor = NamedTextColor.GOLD;
                } else if (flag instanceof UnknownFlag) {
                    flagColor = NamedTextColor.GRAY;
                } else {
                    flagColor = NamedTextColor.LIGHT_PURPLE;
                }
            }
            Component flagText = Msg.REGION__INFO__FLAGS__ENTRY.text(
                    NamedTextColor.NAMES.key(flagColor), Text.of(flagName), Text.of(String.valueOf(val)));
            if (!useColors) {
                flagText = Text.of(PlainTextComponentSerializer.plainText().serialize(flagText), NamedTextColor.WHITE);
            }
            if (perms != null && perms.maySetFlag(region, flag)) {
                flagText = flagText.hoverEvent(HoverEvent.showText(Msg.REGION__INFO__FLAGS__CLICK_TO_SET.text()))
                        .clickEvent(ClickEvent.suggestCommand("/rg flag -w \"" + world + "\" " + region.getId() + " " + flag.getName() + " "));
            }
            builder.append(flagText);

            hasFlags = true;
        }

        if (!hasFlags) {
            Component noFlags = Text.of(Msg.REGION__INFO__FLAGS__NONE.text(), useColors ? NamedTextColor.RED : NamedTextColor.WHITE);
            builder.append(noFlags);
        }

        if (perms != null && perms.maySetFlag(region)) {
            builder.append(Text.space())
                    .append(Msg.REGION__INFO__ACTION__FLAGS.text(
                            useColors ? "green" : "gray", "/rg flags -w \"" + world + "\" " + region.getId()
                    ));
        }
    }

    @Deprecated
    public void appendParents() {
        appendParentTree(true);
    }

    @Deprecated
    public void appendParentTree(boolean useColors) {
        if (region.getParent() != null) {
            appendParentLines(useColors);
            newline();
        }
    }

    @Deprecated
    public void appendDomain() {
        builder.append(Text.of("Owners: ", NamedTextColor.BLUE));
        addDomainString(region.getOwners(),
                perms != null && perms.mayAddOwners(region) ? "addowner" : null,
                perms != null && perms.mayRemoveOwners(region) ? "removeowner" : null);
        newline();
        builder.append(Text.of("Members: ", NamedTextColor.BLUE));
        addDomainString(region.getMembers(),
                perms != null && perms.mayAddMembers(region) ? "addmember" : null,
                perms != null && perms.mayRemoveMembers(region) ? "removemember" : null);
        newline();
    }

    @Deprecated
    public void appendBounds() {
        builder.append(Text.of("Bounds: ", NamedTextColor.BLUE));
        appendBoundsValue();
        newline();
    }

    public Component flagsList(boolean useColors) {
        return capture(() -> appendFlagsList(useColors));
    }

    public Component parentTree(boolean useColors) {
        return region.getParent() == null ? Component.empty() : capture(() -> appendParentLines(useColors));
    }

    private void appendParentLines(boolean useColors) {
        List<ProtectedRegion> inheritance = new ArrayList<>();

        ProtectedRegion r = region;
        inheritance.add(r);
        while (r.getParent() != null) {
            r = r.getParent();
            inheritance.add(r);
        }

        ListIterator<ProtectedRegion> it = inheritance.listIterator(
                inheritance.size());

        ProtectedRegion last = null;
        int indent = 0;
        while (it.hasPrevious()) {
            ProtectedRegion cur = it.previous();

            if (indent != 0) {
                newline();
            }

            StringBuilder namePrefix = new StringBuilder();

            // Put symbol for child
            if (indent != 0) {
                for (int i = 0; i < indent; i++) {
                    namePrefix.append(" ");
                }
                namePrefix.append("\u2937"); //⤷
            }

            // Put name
            builder.append(Text.of(namePrefix.toString(), useColors ? NamedTextColor.GREEN : NamedTextColor.WHITE));
            if (perms != null && perms.mayLookup(cur)) {
                builder.append(Text.of(cur.getId(), useColors ? NamedTextColor.GREEN : NamedTextColor.WHITE)
                    .hoverEvent(HoverEvent.showText(Msg.REGION__INFO__PARENT__CLICK_INFO.text()))
                    .clickEvent(ClickEvent.runCommand("/rg info -w \"" + world + "\" " + cur.getId())));
            } else {
                builder.append(Text.of(cur.getId(), useColors ? NamedTextColor.GREEN : NamedTextColor.WHITE));
            }

            // Put (parent)
            if (!cur.equals(region)) {
                builder.append(Msg.REGION__INFO__PARENT__SUFFIX.text(capture(() -> appendPriorityComponent(cur))));
            }
            if (last != null && cur.equals(region) && perms != null && perms.maySetParent(cur, last)) {
                builder.append(Text.space());
                builder.append(Msg.REGION__INFO__ACTION__UNLINK.text("/rg setparent -w \"" + world + "\" " + cur.getId()));
            }

            last = cur;
            indent++;
        }
    }

    private void addDomainString(DefaultDomain domain, String addCommand, String removeCommand) {
        if (domain.size() == 0) {
            builder.append(ErrorFormat.wrap(Msg.REGION__INFO__DOMAIN__NONE.text()));
        } else {
            if (perms != null) {
                builder.append(LegacyComponents.adapt(domain.toUserFriendlyComponent(cache)));
            } else {
                builder.append(LabelFormat.wrap(domain.toUserFriendlyString(cache)));
            }
        }
        if (addCommand != null) {
            builder.append(Text.space().append(Msg.REGION__INFO__ACTION__ADD.text(
                    "/rg " + addCommand + " -w \"" + world + "\" " + region.getId() + " ")));
        }
        if (removeCommand != null && domain.size() > 0) {
            builder.append(Text.space().append(Msg.REGION__INFO__ACTION__REMOVE.text(
                    "/rg " + removeCommand + " -w \"" + world + "\" " + region.getId() + " ")));
            builder.append(Text.space().append(Msg.REGION__INFO__ACTION__CLEAR.text(
                    "/rg " + removeCommand + " -w \"" + world + "\" -a " + region.getId())));
        }
    }

    private void appendBoundsValue() {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        TextComponent bound = Text.of(min + " -> " + max, NamedTextColor.YELLOW);
        if (perms != null && perms.maySelect(region)) {
            bound = bound
                    .hoverEvent(HoverEvent.showText(Msg.REGION__INFO__BOUNDS__CLICK_SELECT.text()))
                    .clickEvent(ClickEvent.runCommand("/rg select " + region.getId()));
        }
        builder.append(bound);
        final Location teleFlag = FlagValueCalculator.getEffectiveFlagOf(region, Flags.TELE_LOC, perms != null && perms.getSender() instanceof RegionAssociable ? (RegionAssociable) perms.getSender() : null);
        if (teleFlag != null && perms != null && perms.mayTeleportTo(region)) {
            builder.append(Text.space().append(Msg.REGION__INFO__ACTION__TELEPORT.text(
                    "/rg tp -w \"" + world + "\" " + region.getId(),
                    teleFlag.getBlockX() + ", " + teleFlag.getBlockY() + ", " + teleFlag.getBlockZ())));
        } else if (perms != null && perms.mayTeleportToCenter(region) && region.isPhysicalArea()) {
            builder.append(Text.space().append(Msg.REGION__INFO__ACTION__CENTER_TELEPORT.text(
                    "/rg tp -c -w \"" + world + "\" " + region.getId())));
        }
    }

    private void appendPriorityComponent(ProtectedRegion rg) {
        final String content = String.valueOf(rg.getPriority());
        if (perms != null && perms.maySetPriority(rg)) {
            builder.append(Text.of(content, NamedTextColor.GOLD)
                    .hoverEvent(HoverEvent.showText(Msg.REGION__INFO__PRIORITY__CLICK_CHANGE.text()))
                    .clickEvent(ClickEvent.suggestCommand("/rg setpriority -w \"" + world + "\" " + rg.getId() + " ")));
        } else {
            builder.append(Text.of(content, NamedTextColor.WHITE));
        }
    }

    private Component capture(Runnable action) {
        TextComponentProducer previous = builder;
        builder = new TextComponentProducer();
        try {
            action.run();
            return builder.build();
        } finally {
            builder = previous;
        }
    }

    private Component domain(DefaultDomain domain, String addCommand, String removeCommand) {
        return capture(() -> addDomainString(domain, addCommand, removeCommand));
    }

    private Component regionId() {
        return Text.of(region.getId()).clickEvent(ClickEvent.runCommand("/rg info -w \"" + world + "\" " + region.getId()));
    }

    @Deprecated
    @Override
    public com.sk89q.worldedit.util.formatting.text.TextComponent call() {
        return LegacyComponents.adaptText(component());
    }

    @Deprecated
    public void send(Actor sender) {
        Messenger.plain(sender, builder.build());
    }

    @Deprecated
    public com.sk89q.worldedit.util.formatting.component.TextComponentProducer append(String str) {
        return builder.append(Text.of(str));
    }

    @Deprecated
    public com.sk89q.worldedit.util.formatting.component.TextComponentProducer append(com.sk89q.worldedit.util.formatting.text.TextComponent component) {
        return builder.append(component);
    }

    @Deprecated
    public com.sk89q.worldedit.util.formatting.text.TextComponent toComponent() {
        return builder.create();
    }

    @Override
    public String toString() {
        return PlainTextComponentSerializer.plainText().serialize(builder.build()).trim();
    }

    public Component component() {
        Component lastSeen = cache != null && perms == null
                ? Text.newline().append(SubtleFormat.wrap(Msg.REGION__INFO__LAST_SEEN.text()))
                : Component.empty();
        return Msg.REGION__INFO__LAYOUT.text(
                MessageBox.centerAndBorder(Msg.REGION__INFO__TITLE.text()),
                regionId(),
                Text.of(region.getType().getName()),
                capture(() -> appendPriorityComponent(region)),
                capture(() -> appendFlagsList(true)),
                region.getParent() == null ? Component.empty() : Text.newline().append(capture(() -> appendParentLines(true))),
                domain(region.getOwners(),
                        perms != null && perms.mayAddOwners(region) ? "addowner" : null,
                        perms != null && perms.mayRemoveOwners(region) ? "removeowner" : null),
                domain(region.getMembers(),
                        perms != null && perms.mayAddMembers(region) ? "addmember" : null,
                        perms != null && perms.mayRemoveMembers(region) ? "removemember" : null),
                Text.of(String.valueOf(region.volume())),
                capture(this::appendBoundsValue),
                lastSeen
        );
    }
}
