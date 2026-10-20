package me.imdanix.wgtranslator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class MsgTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        '&0Black' | '§0Black'
        '&aGreen' | '§aGreen'
        '&lBold' | '§lBold'
        '&rReset' | '§rReset'
        '&#abcdefMixed hex' | '§x§a§b§c§d§e§fMixed hex'
        '&#123456Dark hex' | '§x§1§2§3§4§5§6Dark hex'
        'Normal text without codes' | 'Normal text without codes'
        '&Invalid code' | '&Invalid code'
        '&' | '&'
        '' | ''
        '&&kDouble ampersand' | '&§kDouble ampersand'
        '&#FF0000Uppercase hex' | '§x§f§f§0§0§0§0Uppercase hex'
        '&#12Short hex' | '&#12Short hex'
        '&#ghijklNon hex' | '&#ghijklNon hex'
        'Text ending with &' | 'Text ending with &'
        '&#ffffff&#000000Two hex back to back' | '§x§f§f§f§f§f§f§x§0§0§0§0§0§0Two hex back to back'
        """)
    public void ampersandTest(String input, String expected) {
        assertEquals(
                Message.Serializer.LEGACY_SECTION.deserialize(expected).compact(),
                Message.Serializer.LEGACY_AMPERSAND.deserialize(input).compact()
        );
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', quoteCharacter = '"', textBlock = """
        break   | stone    | "You can't break stone."
        {type}  | {action} | "You can't {type} {action}."
        <red>&c | stone    | "You can't <red>&c stone."
        """)
    public void placeholdersTest(String action, String type, String expected) {
        assertEquals(expected, plain(Msg.BLACKLIST__PUNISHMENT__KICK__DEFAULT.text(action, type)));
    }

    @Test
    public void legacyStringTest() {
        assertEquals("§cThe [Lock] sign must be a sign post, not a wall sign.", Msg.CHEST_LOCK__LOCK_POST.get("[Lock]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/rg addowner -w \"my world\" spawn ", "/say it's", "/say back\\slash"})
    public void miniMessageClickTest(String command) {
        assertEquals(command, clickValue(Msg.REGION__INFO__ACTION__ADD.text(command)));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        '<b>stone' | '<b>stone'
        '§cred' | 'red'
        """)
    public void miniMessageArgumentTest(String argument, String expected) {
        assertEquals("Click to teleport\n" + expected, plain(hoverText(Msg.REGION__INFO__ACTION__TELEPORT.text("/rg tp spawn", argument))));
        assertTrue(plain(Msg.REGION__COMMANDS__NEW_USER_INFO.text(argument)).endsWith("Use " + expected + ")"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"gold", "dark_red"})
    public void miniMessageTagArgumentTest(String color) {
        Component component = Msg.REGION__INFO__FLAGS__ENTRY.text(color, Component.text("pvp"), Component.text("deny"));
        Component colored = null;
        for (Component node : component.iterable(ComponentIteratorType.DEPTH_FIRST)) {
            if (colored == null && node.color() != null) {
                colored = node;
            }
        }
        assertEquals(NamedTextColor.NAMES.value(color), colored.color());
    }

    @Test
    public void layoutTest() {
        Component component = Msg.REGION__INFO__LAYOUT.text(
                Component.text("=== Info ==="), Component.text("spawn"), "cuboid", Component.text("5"),
                Component.text("pvp: deny"), Component.empty(), Component.text("me"), Component.empty(),
                Component.text("10"), Component.text("0 -> 1"), Component.empty());
        assertEquals("""
                === Info ===
                Region: spawn (type=cuboid, priority=5)
                Flags: pvp: deny
                Owners: me
                Members:\s
                Volume: 10
                Bounds: 0 -> 1""",
                plain(component)
        );
    }

    @Test
    public void titleTest() {
        Msg msg = Msg.GOD__ENABLED;
        RecordingBridge bridge = new RecordingBridge();
        Messenger.setBridge(bridge);
        try {
            msg.setMessage(new Message("Title\nSubtitle", null, Message.Serializer.LEGACY_SECTION, Message.Place.TITLE));
            msg.info("receiver");
            assertEquals(Message.Place.TITLE, bridge.place);
            assertEquals("Title", plain(bridge.main));
            assertEquals("Subtitle", plain(bridge.sub));
        } finally {
            Messenger.setBridge(null);
            msg.setMessage(null);
        }
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        , red
        DEBUG, gray
        """)
    public void typeTest(Message.Type type, String color) {
        Msg msg = Msg.GOD__ENABLED;
        RecordingBridge bridge = new RecordingBridge();
        Messenger.setBridge(bridge);
        try {
            msg.setMessage(new Message("text", type, null, Message.Place.CHAT));
            msg.error("receiver");
            assertEquals(NamedTextColor.NAMES.value(color), bridge.main.color());
        } finally {
            Messenger.setBridge(null);
            msg.setMessage(null);
        }
    }

    @ParameterizedTest
    @CsvSource({"MINIMESSAGE, <green>Hi", "LEGACY_AMPERSAND, &aHi"})
    public void defaultSerializerTest(Message.Serializer serializer, String text) {
        Msg msg = Msg.GOD__ENABLED;
        try {
            Messenger.setDefaultSerializer(serializer);
            msg.setMessage(Message.of(text));
            assertEquals(NamedTextColor.GREEN, msg.text().color());
        } finally {
            Messenger.setDefaultSerializer(Message.Serializer.LEGACY_AMPERSAND);
            msg.setMessage(null);
        }
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static String clickValue(Component component) {
        for (Component node : component.iterable(ComponentIteratorType.DEPTH_FIRST)) {
            if (node.clickEvent() != null) {
                return ((ClickEvent.Payload.Text) node.clickEvent().payload()).value();
            }
        }
        return fail("no click event");
    }

    private static Component hoverText(Component component) {
        for (Component node : component.iterable(ComponentIteratorType.DEPTH_FIRST)) {
            if (node.hoverEvent() != null) {
                return (Component) node.hoverEvent().value();
            }
        }
        return fail("no hover event");
    }

    private static final class RecordingBridge implements Messenger.Bridge {
        private Message.Place place;
        private Component main;
        private Component sub;

        @Override
        public boolean send(Object receiver, Message.Place place, Component main, Component sub) {
            this.place = place;
            this.main = main;
            this.sub = sub;
            return true;
        }

        @Override
        public void broadcast(String permission, Component message) { }
    }

    @Test
    public void sectionsTest() {
        for (Msg msgLeft : Msg.values()) {
            String[] sectionsLeft = msgLeft.name().split("__");
            for (Msg msgRight : Msg.values()) {
                if (msgLeft == msgRight) continue;
                String[] sectionRight = msgRight.name().split("__");
                if (sectionsLeft.length > sectionRight.length) continue;

                if (isPrefix(sectionsLeft, sectionRight)) {
                    fail("'" + msgRight.name() + "' contains '" + msgLeft.name() + "'");
                }
            }
        }
    }

    private boolean isPrefix(String[] shorter, String[] longer) {
        for (int i = 0; i < shorter.length; i++) {
            if (!shorter[i].equals(longer[i])) {
                return false;
            }
        }
        return true;
    }
}
