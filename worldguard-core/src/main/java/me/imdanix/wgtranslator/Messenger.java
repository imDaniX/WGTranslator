package me.imdanix.wgtranslator;

import com.sk89q.worldedit.extension.platform.Actor;
import net.kyori.adventure.text.Component;

import javax.annotation.Nullable;

public final class Messenger {
    private static volatile Bridge bridge;
    private static volatile Message.Serializer defaultSerializer = Message.Serializer.LEGACY_AMPERSAND;

    private Messenger() { }

    public static void setBridge(@Nullable Bridge bridge) {
        Messenger.bridge = bridge;
    }

    public static Message.Serializer defaultSerializer() {
        return defaultSerializer;
    }

    public static void setDefaultSerializer(Message.Serializer serializer) {
        defaultSerializer = serializer;
    }

    public static String legacy(String text) {
        return Message.Serializer.toLegacy(defaultSerializer.deserialize(text));
    }

    public static void send(Object receiver, Message.Type type, Component message) {
        send(receiver, Message.Place.CHAT, type.apply(message), null);
    }

    public static void send(Object receiver, Message.Place place, Component main, @Nullable Component sub) {
        Bridge current = bridge;
        if (current != null && current.send(receiver, place, main, sub)) {
            return;
        }
        if (receiver instanceof Actor actor) {
            actor.printRaw(Message.Serializer.toLegacy(combine(place, main, sub)));
        }
    }

    public static Component combine(Message.Place place, Component main, @Nullable Component sub) {
        if (sub == null) {
            return main;
        }
        return place == Message.Place.SUBTITLE ? sub : main.appendNewline().append(sub);
    }

    public static void plain(Object receiver, Component message) {
        send(receiver, Message.Type.PLAIN, message);
    }

    public static void broadcast(String permission, Component message) {
        Bridge current = bridge;
        if (current != null) {
            current.broadcast(permission, message);
        }
    }

    public interface Bridge {
        boolean send(Object receiver, Message.Place place, Component main, @Nullable Component sub);

        void broadcast(String permission, Component message);
    }
}
