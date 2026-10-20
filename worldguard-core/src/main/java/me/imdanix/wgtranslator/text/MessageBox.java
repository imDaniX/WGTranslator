package me.imdanix.wgtranslator.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import static com.google.common.base.Preconditions.checkNotNull;

public class MessageBox extends TextComponentProducer {
    private static final int GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH = 47;

    private final TextComponentProducer contents;

    public MessageBox(String title, TextComponentProducer contents) {
        this(Component.text(title), contents);
    }

    @SuppressWarnings("this-escape")
    public MessageBox(Component title, TextComponentProducer contents) {
        checkNotNull(title);

        append(centerAndBorder(title)).newline();
        this.contents = contents;
    }

    public static Component centerAndBorder(Component text) {
        TextComponentProducer line = new TextComponentProducer();
        int leftOver = GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH - PlainTextComponentSerializer.plainText().serialize(text).length();
        int side = (int) Math.floor(leftOver / 2.0);
        if (side > 0) {
            if (side > 1) {
                line.append(createBorder(side - 1));
            }
            line.append(Component.space());
        }
        line.append(text);
        if (side > 0) {
            line.append(Component.space());
            if (side > 1) {
                line.append(createBorder(side - 1));
            }
        }
        return line.build();
    }

    private static TextComponent createBorder(int count) {
        return Component.text("-".repeat(count), NamedTextColor.YELLOW, TextDecoration.STRIKETHROUGH);
    }

    public TextComponentProducer getContents() {
        return contents;
    }

    @Override
    public TextComponent build() {
        append(contents.build());
        return super.build();
    }
}
