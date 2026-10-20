package me.imdanix.wgtranslator.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Set;

public final class Text {
    private Text() { }

    public static TextComponent of(String content) {
        return Component.text(content);
    }

    public static TextComponent of(String content, TextColor color) {
        return Component.text(content, color);
    }

    public static TextComponent of(String content, TextColor color, Set<TextDecoration> decorations) {
        return Component.text(content, color, decorations);
    }

    public static Component of(Component content, TextColor color) {
        return content.colorIfAbsent(color);
    }

    public static Component of(Component content, TextColor color, Set<TextDecoration> decorations) {
        Component result = content.colorIfAbsent(color);
        for (TextDecoration decoration : decorations) {
            result = result.decoration(decoration, true);
        }
        return result;
    }

    public static TextComponent.Builder builder() {
        return Component.text();
    }

    public static TextComponent.Builder builder(String content) {
        return Component.text().content(content);
    }

    public static TextComponent empty() {
        return Component.empty();
    }

    public static TextComponent space() {
        return Component.space();
    }

    public static TextComponent newline() {
        return Component.newline();
    }
}
