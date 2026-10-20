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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.Registry;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.registry.UnknownFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.imdanix.wgtranslator.Message;
import me.imdanix.wgtranslator.Msg;
import me.imdanix.wgtranslator.text.PaginationBox;
import me.imdanix.wgtranslator.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

class FlagHelperBox extends PaginationBox {

    private static final List<Flag<?>> FLAGS = WorldGuard.getInstance().getFlagRegistry().getAll().stream()
            .sorted((f1, f2) -> {
                if (f1 == f2) return 0;
                int idx1 = Flags.INBUILT_FLAGS.indexOf(f1.getName());
                int idx2 = Flags.INBUILT_FLAGS.indexOf(f2.getName());
                if (idx1 < 0 && idx2 >= 0) return 1;
                if (idx2 < 0 && idx1 >= 0) return -1;
                if (idx1 < 0) return f1.getName().compareTo(f2.getName());
                return idx1 < idx2 ? -1 : 1;
            })
            .collect(Collectors.toList());
    private static final int SIZE = FLAGS.size() == Flags.INBUILT_FLAGS.size() ? FLAGS.size() : FLAGS.size() + 1;
    private static final int PAD_PX_SIZE = 180;
    static final Set<Flag<?>> DANGER_ZONE = ImmutableSet.of(Flags.BUILD, Flags.PASSTHROUGH, Flags.BLOCK_PLACE, Flags.BLOCK_BREAK);

    private final World world;
    private final ProtectedRegion region;
    private final RegionPermissionModel perms;
    private boolean monoSpace;

    FlagHelperBox(World world, ProtectedRegion region, RegionPermissionModel perms) {
        super(Msg.REGION__FLAGS__TITLE.text(region.getId()), "/rg flags -w \"" + world.getName() + "\" -p %page% " + region.getId());
        this.world = world;
        this.region = region;
        this.perms = perms;
    }

    @Override
    public Component getComponent(int number) {
        if (number == Flags.INBUILT_FLAGS.size()) {
            return centerAndBorder(Text.of(Msg.REGION__FLAGS__THIRD_PARTY.text(), NamedTextColor.AQUA));
        } else if (number > Flags.INBUILT_FLAGS.size()) {
            number -= 1;
        }
        Flag<?> flag = FLAGS.get(number);
        return createLine(flag, number >= Flags.INBUILT_FLAGS.size());
    }

    @Override
    public int getComponentsSize() {
        return SIZE;
    }

    private Component createLine(Flag<?> flag, boolean thirdParty) {
        final TextComponent.Builder builder = Text.builder("");

        appendFlagName(builder, flag, flag instanceof UnknownFlag ? NamedTextColor.GRAY : thirdParty ? NamedTextColor.LIGHT_PURPLE : NamedTextColor.GOLD);
        appendFlagValue(builder, flag);
        return builder.build();
    }

    private void appendFlagName(TextComponent.Builder builder, Flag<?> flag, TextColor color) {
        final String name = flag.getName();
        int length = monoSpace ? name.length() : FlagFontInfo.getPxLength(name);
        builder.append(Text.of(name, color));
        if (flag.usesMembershipAsDefault()) {
            builder.append(Text.empty().append(Text.of("*", NamedTextColor.AQUA))
                    .hoverEvent(HoverEvent.showText(Msg.REGION__FLAGS__SPECIAL__MEMBER.text())));
            length += monoSpace ? 1 : FlagFontInfo.getPxLength('*');
        }
        if (flag == Flags.PASSTHROUGH) {
            builder.append(Text.empty().append(Text.of("*", NamedTextColor.AQUA))
                    .hoverEvent(HoverEvent.showText(Msg.REGION__FLAGS__SPECIAL__BUILD.text())));
            length += monoSpace ? 1 : FlagFontInfo.getPxLength('*');
        }
        int leftover = (monoSpace ? PAD_PX_SIZE / 3 : PAD_PX_SIZE) - length;
        builder.append(Text.space());
        leftover -= 4;
        if (leftover > 0) {
            builder.append(Text.of(Strings.repeat(".", monoSpace ? leftover : leftover / 2), NamedTextColor.DARK_GRAY));
        }
    }

    private void appendFlagValue(TextComponent.Builder builder, Flag<?> flag) {
        if (flag instanceof StateFlag) {
            appendStateFlagValue(builder, (StateFlag) flag);
        } else if (flag instanceof BooleanFlag) {
            appendBoolFlagValue(builder, ((BooleanFlag) flag));
        } else if (flag instanceof SetFlag) {
            appendSetFlagValue(builder, ((SetFlag<?>) flag));
        } else if (flag instanceof RegistryFlag) {
            appendRegistryFlagValue(builder, ((RegistryFlag<?>) flag));
        } else if (flag instanceof StringFlag) {
            appendStringFlagValue(builder, ((StringFlag) flag));
        } else if (flag instanceof LocationFlag) {
            appendLocationFlagValue(builder, ((LocationFlag) flag));
        } else if (flag instanceof NumberFlag) {
            appendNumericFlagValue(builder, (NumberFlag<? extends Number>) flag);
        } else {
            String display = String.valueOf(region.getFlag(flag));
            if (display.length() > 23) {
                display = display.substring(0, 20) + "...";
            }
            appendValueText(builder, flag, display, null);
        }
    }

    private <T> T getInheritedValue(ProtectedRegion region, Flag<T> flag) {
        ProtectedRegion parent = region.getParent();
        T val;
        while (parent != null) {
            val = parent.getFlag(flag);
            if (val != null) {
                return val;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private <V> void appendValueChoices(TextComponent.Builder builder, Flag<V> flag, Iterator<V> choices, @Nullable String suggestChoice) {
        V defVal = flag.getDefault();
        V currVal = region.getFlag(flag);
        boolean inherited = false;
        if (currVal == null) {
            currVal = getInheritedValue(region, flag);
            if (currVal != null) {
                inherited = true;
            }
        }
        while (choices.hasNext()) {
            V choice = choices.next();
            boolean isExplicitSet = currVal == choice && !inherited;

            boolean maySet = perms.maySetFlag(region, flag, isExplicitSet ? null : String.valueOf(choice));

            TextColor col = isExplicitSet ? NamedTextColor.WHITE : inherited && currVal == choice ? NamedTextColor.GRAY : NamedTextColor.DARK_GRAY;
            Set<TextDecoration> styles = choice == defVal ? ImmutableSet.of(TextDecoration.UNDERLINED) : Collections.emptySet();

            Component choiceComponent = Text.empty().append(Text.of(capitalize(String.valueOf(choice)), col, styles));

            List<Component> hoverTexts = new ArrayList<>();
            if (maySet) {
                if (isExplicitSet) {
                    hoverTexts.add(Text.of(Msg.REGION__FLAGS__UNSET.text(), NamedTextColor.GOLD));
                } else if (DANGER_ZONE.contains(flag) && !(ProtectedRegion.GLOBAL_REGION.equals(region.getId()) && flag == Flags.PASSTHROUGH)) {
                    hoverTexts.add(Msg.REGION__FLAGS__CONSEQUENCES.text());
                } else {
                    hoverTexts.add(Text.of(Msg.REGION__FLAGS__SET.text(), NamedTextColor.GOLD));
                }
            }
            Component valType = getToolTipHint(defVal, choice, inherited);
            if (valType != null) {
                hoverTexts.add(valType);
            }

            if (!hoverTexts.isEmpty()) {
                TextComponent.Builder hoverBuilder = Text.builder("");
                for (Iterator<Component> hovIt = hoverTexts.iterator(); hovIt.hasNext(); ) {
                    hoverBuilder.append(hovIt.next());
                    if (hovIt.hasNext()) {
                        hoverBuilder.append(Text.newline());
                    }
                }
                choiceComponent = choiceComponent.hoverEvent(
                        HoverEvent.showText(hoverBuilder.build()));
            }

            if (maySet && (isExplicitSet || !DANGER_ZONE.contains(flag))) {
                builder.append(choiceComponent.clickEvent(ClickEvent.runCommand(makeCommand(flag, isExplicitSet ? "" : choice))));
            } else {
                builder.append(choiceComponent);
            }
            builder.append(Text.space());
        }
        if (suggestChoice != null && perms.maySetFlag(region, flag)) {
            builder.append(Text.of(suggestChoice, NamedTextColor.DARK_GRAY)
                    .hoverEvent(HoverEvent.showText(Text.of(Msg.REGION__FLAGS__SET_CUSTOM.text(), NamedTextColor.GOLD)))
                    .clickEvent(ClickEvent.suggestCommand(makeCommand(flag, ""))));
        }
    }

    private <V> void appendValueText(TextComponent.Builder builder, Flag<V> flag, String display, @Nullable Component hover) {
        V defVal = flag.getDefault();
        V currVal = region.getFlag(flag);
        boolean inherited = false;
        if (currVal == null) {
            currVal = getInheritedValue(region, flag);
            if (currVal != null) {
                inherited = true;
            }
        }

        boolean isExplicitSet = currVal != null && !inherited;

        boolean maySet = !(flag instanceof UnknownFlag) && perms.maySetFlag(region, flag);

        TextColor col = isExplicitSet ? NamedTextColor.WHITE : inherited ? NamedTextColor.GRAY : NamedTextColor.DARK_GRAY;
        Set<TextDecoration> styles = currVal == defVal ? ImmutableSet.of(TextDecoration.UNDERLINED) : Collections.emptySet();

        Component displayComponent = Text.empty().append(Text.of(display, col, styles));

        List<Component> hoverTexts = new ArrayList<>();
        if (maySet) {
            if (isExplicitSet) {
                hoverTexts.add(Text.of(Msg.REGION__FLAGS__CHANGE.text(), NamedTextColor.GOLD));
            } else {
                hoverTexts.add(Text.of(Msg.REGION__FLAGS__SET.text(), NamedTextColor.GOLD));
            }
        }
        Component valType = getToolTipHint(defVal, currVal, inherited);
        if (valType != null) {
            hoverTexts.add(valType);
        }

        if (!hoverTexts.isEmpty()) {
            TextComponent.Builder hoverBuilder = Text.builder("");
            for (Iterator<Component> hovIt = hoverTexts.iterator(); hovIt.hasNext(); ) {
                hoverBuilder.append(hovIt.next());
                if (hovIt.hasNext()) {
                    hoverBuilder.append(Text.newline());
                }
            }
            if (hover != null) {
                hoverBuilder.append(Text.newline());
                hoverBuilder.append(hover);
            }
            displayComponent = displayComponent.hoverEvent(
                    HoverEvent.showText(hoverBuilder.build()));
        }

        if (maySet) {
            builder.append(displayComponent.clickEvent(ClickEvent.suggestCommand(makeCommand(flag, ""))));
        } else {
            builder.append(displayComponent);
        }
        builder.append(Text.space());
    }

    private String makeCommand(Flag<?> flag, Object choice) {
        return "/rg flag -w \"" + world.getName() + "\" -h " + getCurrentPage()
                + " " + region.getId() + " " + flag.getName() + " " + choice;
    }

    private String capitalize(String value) {
        if (value.isEmpty()) return value;
        value = value.toLowerCase();
        return value.length() > 1
                ? Character.toUpperCase(value.charAt(0)) + value.substring(1)
                : String.valueOf(Character.toUpperCase(value.charAt(0)));
    }

    @Nullable
    private <V> Component getToolTipHint(V defVal, V currVal, boolean inherited) {
        Component valType;
        if (inherited) {
            if (currVal == defVal) {
                valType = Msg.REGION__FLAGS__INHERITED__INHERITED_DEFAULT.text();
            } else {
                valType = Msg.REGION__FLAGS__INHERITED__INHERITED.text();
            }
        } else {
            if (currVal == defVal) {
                valType = Msg.REGION__FLAGS__INHERITED__DEFAULT.text();
            } else {
                valType = null;
            }
        }
        return valType;
    }

    private void appendStateFlagValue(TextComponent.Builder builder, StateFlag flag) {
        final Iterator<StateFlag.State> choices = Iterators.forArray(StateFlag.State.values());
        appendValueChoices(builder, flag, choices, null);
    }

    private void appendBoolFlagValue(TextComponent.Builder builder, BooleanFlag flag) {
        final Iterator<Boolean> choices = Iterators.forArray(Boolean.TRUE, Boolean.FALSE);
        appendValueChoices(builder, flag, choices, null);
    }

    private <V> void appendSetFlagValue(TextComponent.Builder builder, SetFlag<V> flag) {
        Flag<V> subType = flag.getType();
        Class<?> clazz = subType.getClass();
        String subName;
        subName = clazz.isAssignableFrom(RegistryFlag.class)
                ? ((RegistryFlag<?>) subType).getRegistry().getName()
                : subType.getClass().getSimpleName().replace("Flag", "");
        Set<V> currVal = region.getFlag(flag);
        if (currVal == null) {
            currVal = getInheritedValue(region, flag);
        }
        String display = (currVal == null ? "" : currVal.size() + "x ") + subName;
        final String stringValue = currVal == null ? ""
                : currVal.stream().map(String::valueOf).collect(Collectors.joining(","));
        TextComponent hoverComp = Text.of("");
        if (currVal != null) {
            hoverComp = hoverComp.append(Msg.REGION__FLAGS__VALUES.text())
                    .append(Text.newline()).append(Text.of(stringValue));
        }
        appendValueText(builder, flag, display, hoverComp);
    }

    private void appendRegistryFlagValue(TextComponent.Builder builder, RegistryFlag<?> flag) {
        final Registry<?> registry = flag.getRegistry();
        String regName = registry.getName();
        Keyed currVal = region.getFlag(flag);
        if (currVal == null) {
            currVal = getInheritedValue(region, flag);
        }
        String display = currVal == null ? regName : currVal.id();
        appendValueText(builder, flag, display, null);
    }

    private void appendLocationFlagValue(TextComponent.Builder builder, LocationFlag flag) {
        Location currVal = region.getFlag(flag);
        if (currVal == null) {
            currVal = getInheritedValue(region, flag);
        }
        if (currVal == null) {
            final Location defVal = flag.getDefault();
            if (defVal == null) {
                appendValueText(builder, flag, Msg.REGION__FLAGS__VALUE__LOCATION__UNSET.get(), null);
            } else {
                appendValueText(builder, flag, defVal.toString(), Msg.REGION__FLAGS__VALUE__LOCATION__DEFAULT.text()
                        .append(Text.newline()).append(Text.of(defVal.toString())));
            }
        } else {
            appendValueText(builder, flag, currVal.toString(), Msg.REGION__FLAGS__VALUE__LOCATION__CURRENT.text()
                    .append(Text.newline()).append(Text.of(currVal.toString())));
        }
    }

    private <V extends Number> void appendNumericFlagValue(TextComponent.Builder builder, NumberFlag<V> flag) {
        Number currVal = region.getFlag(flag);
        if (currVal == null) {
            currVal = getInheritedValue(region, flag);
        }
        Number defVal = flag.getDefault();
        Number[] suggested = flag.getSuggestedValues();
        SortedSet<Number> choices = new TreeSet<>(Comparator.comparing(Number::doubleValue));
        if (currVal != null) {
            choices.add(currVal);
        }
        if (defVal != null) {
            choices.add(defVal);
        }
        if (suggested.length > 0) {
            choices.addAll(Arrays.asList(suggested));
        }
        //noinspection unchecked
        appendValueChoices(builder, flag, (Iterator<V>) choices.iterator(), choices.isEmpty() ? Msg.REGION__FLAGS__VALUE__NUMERIC__UNSET.get() : Msg.REGION__FLAGS__VALUE__NUMERIC__CUSTOM.get());
    }

    private void appendStringFlagValue(TextComponent.Builder builder, StringFlag flag) {
        String currVal = region.getFlag(flag);
        if (currVal == null) {
            currVal = getInheritedValue(region, flag);
        }
        if (currVal == null) {
            final String defVal = flag.getDefault();
            if (defVal == null) {
                appendValueText(builder, flag, Msg.REGION__FLAGS__VALUE__STRING__UNSET.get(), null);
            } else {
                final Component defComp = Message.Serializer.fromLegacy(defVal);
                String display = reduceToText(defComp);
                display = display.replace("\n", "\\n");
                if (display.length() > 23) {
                    display = display.substring(0, 20) + "...";
                }
                appendValueText(builder, flag, display, Msg.REGION__FLAGS__VALUE__STRING__DEFAULT.text()
                        .append(Text.newline()).append(defComp));
            }
        } else {
            Component currComp = Message.Serializer.fromLegacy(currVal);
            String display = reduceToText(currComp);
            display = display.replace("\n", "\\n");
            if (display.length() > 23) {
                display = display.substring(0, 20) + "...";
            }
            appendValueText(builder, flag, display, Msg.REGION__FLAGS__VALUE__STRING__CURRENT.text()
                    .append(Text.newline()).append(currComp));
        }
    }

    private static String reduceToText(Component component) {
        StringBuilder text = new StringBuilder();
        appendTextTo(text, component);
        return text.toString();
    }

    private static void appendTextTo(StringBuilder builder, Component component) {
        if (component instanceof TextComponent) {
            builder.append(((TextComponent) component).content());
        } else if (component instanceof TranslatableComponent) {
            builder.append(((TranslatableComponent) component).key());
        }
        for (Component child : component.children()) {
            appendTextTo(builder, child);
        }
    }

    void tryMonoSpacing() {
        this.monoSpace = true;
    }

    private static final class FlagFontInfo {
        static int getPxLength(char c) {
            switch (c) {
                case 'i':
                case ':':
                    return 1;
                case 'l':
                    return 2;
                case '*':
                case 't':
                    return 3;
                case 'f':
                case 'k':
                    return 4;
                default:
                    return 5;
            }
        }

        static int getPxLength(String string) {
            return string.chars().reduce(0, (p, i) -> p + getPxLength((char) i) + 1);
        }
    }
}