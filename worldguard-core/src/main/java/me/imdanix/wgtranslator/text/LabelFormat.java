package me.imdanix.wgtranslator.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class LabelFormat {
    private LabelFormat() { }

    public static TextComponent wrap(String... texts) {
        return TextComponentProducer.wrap(NamedTextColor.YELLOW, texts);
    }

    public static TextComponent wrap(Component... components) {
        return TextComponentProducer.wrap(NamedTextColor.YELLOW, components);
    }
}
