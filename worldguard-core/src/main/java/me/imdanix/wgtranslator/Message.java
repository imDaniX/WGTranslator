package me.imdanix.wgtranslator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import javax.annotation.Nullable;

public record Message(String text, @Nullable Type type, @Nullable Serializer serializer, Place place) {
    public static Message of(String text) {
        return new Message(text, null, null, Place.CHAT);
    }

    public static Message of(String text, Serializer serializer) {
        return new Message(text, null, serializer, Place.CHAT);
    }

    public enum Place {
        CHAT,
        TITLE,
        SUBTITLE,
        ACTIONBAR
    }

    public enum Serializer {
        LEGACY_AMPERSAND {
            @Override
            public Component deserialize(String text) {
                return AMPERSAND.deserialize(text);
            }
        },
        LEGACY_SECTION {
            @Override
            public Component deserialize(String text) {
                return SECTION.deserialize(text);
            }
        },
        MINIMESSAGE {
            @Override
            public Component deserialize(String text) {
                return MINI.deserialize(text);
            }
        };

        private static final LegacyComponentSerializer AMPERSAND = LegacyComponentSerializer.builder()
                .character('&')
                .hexCharacter('#')
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .extractUrls()
                .build();
        private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer.builder()
                .character('§')
                .hexCharacter('#')
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .extractUrls()
                .build();
        private static final MiniMessage MINI = MiniMessage.miniMessage();

        public abstract Component deserialize(String text);

        public static Component deserializeMiniMessage(String text, TagResolver resolver) {
            return MINI.deserialize(text, resolver);
        }

        public static String toLegacy(Component component) {
            return SECTION.serialize(component);
        }

        public static Component fromLegacy(String text) {
            return SECTION.deserialize(text);
        }
    }

    public enum Type {
        PLAIN(null),
        INFO(NamedTextColor.LIGHT_PURPLE),
        DEBUG(NamedTextColor.GRAY),
        ERROR(NamedTextColor.RED),
        SUBTLE(NamedTextColor.GRAY),
        LABEL(NamedTextColor.YELLOW),
        CODE(NamedTextColor.AQUA);

        private final TextColor color;

        Type(@Nullable TextColor color) {
            this.color = color;
        }

        public Component apply(Component component) {
            return color == null ? component : component.colorIfAbsent(color);
        }
    }
}
