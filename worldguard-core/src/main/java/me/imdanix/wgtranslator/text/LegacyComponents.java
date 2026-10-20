package me.imdanix.wgtranslator.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public final class LegacyComponents {
    private LegacyComponents() { }

    public static com.sk89q.worldedit.util.formatting.text.Component adapt(Component component) {
        return com.sk89q.worldedit.util.formatting.text.serializer.gson.GsonComponentSerializer.INSTANCE.deserialize(
                GsonComponentSerializer.gson().serialize(component)
        );
    }

    public static com.sk89q.worldedit.util.formatting.text.TextComponent adaptText(Component component) {
        com.sk89q.worldedit.util.formatting.text.Component legacy = adapt(component);
        return legacy instanceof com.sk89q.worldedit.util.formatting.text.TextComponent text
                ? text
                : com.sk89q.worldedit.util.formatting.text.TextComponent.empty().append(legacy);
    }

    public static Component adapt(com.sk89q.worldedit.util.formatting.text.Component component) {
        return GsonComponentSerializer.gson().deserialize(
                com.sk89q.worldedit.util.formatting.text.serializer.gson.GsonComponentSerializer.INSTANCE.serialize(component)
        );
    }
}
