package me.imdanix.wgtranslator.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class TextComponentProducer extends com.sk89q.worldedit.util.formatting.component.TextComponentProducer {
    private TextComponent.Builder builder = Component.text().content("");

    @Override
    public com.sk89q.worldedit.util.formatting.text.TextComponent.Builder getBuilder() {
        Class<?> type = com.sk89q.worldedit.util.formatting.text.TextComponent.Builder.class;
        return (com.sk89q.worldedit.util.formatting.text.TextComponent.Builder) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            com.sk89q.worldedit.util.formatting.text.TextComponent.Builder legacy = create().toBuilder();
            Object result;
            try {
                result = method.invoke(legacy, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
            Component adapted = LegacyComponents.adapt(legacy.build());
            builder = adapted instanceof TextComponent text ? text.toBuilder() : Component.text().append(adapted);
            return result == legacy ? proxy : result;
        });
    }

    public TextComponentProducer append(Component component) {
        builder.append(component);
        return this;
    }

    @Override
    public TextComponentProducer append(com.sk89q.worldedit.util.formatting.text.Component component) {
        return append(LegacyComponents.adapt(component));
    }

    @Override
    public TextComponentProducer append(String string) {
        builder.append(Component.text(string));
        return this;
    }

    @Override
    public TextComponentProducer newline() {
        builder.append(Component.newline());
        return this;
    }

    public TextComponent build() {
        return builder.build();
    }

    @Override
    public com.sk89q.worldedit.util.formatting.text.TextComponent create() {
        return LegacyComponents.adaptText(build());
    }

    @Override
    public TextComponentProducer reset() {
        builder = Component.text().content("");
        return this;
    }

    static TextComponent wrap(TextColor color, String... texts) {
        TextComponent.Builder result = Component.text().color(color);
        for (String text : texts) {
            result.append(Component.text(text));
        }
        return result.build();
    }

    static TextComponent wrap(TextColor color, Component... components) {
        return Component.text().color(color).append(components).build();
    }
}
