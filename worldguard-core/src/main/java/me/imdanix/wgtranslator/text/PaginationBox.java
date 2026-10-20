package me.imdanix.wgtranslator.text;

import com.sk89q.minecraft.util.commands.CommandException;
import me.imdanix.wgtranslator.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.annotation.Nullable;

public abstract class PaginationBox extends MessageBox {
    private static final int IDEAL_ROWS_FOR_PLAYER = 8;

    private final String pageCommand;
    private int componentsPerPage = IDEAL_ROWS_FOR_PLAYER;
    private int currentPage = -1;

    protected PaginationBox(Component title, @Nullable String pageCommand) {
        super(title, new TextComponentProducer());

        if (pageCommand != null && !pageCommand.contains("%page%")) {
            throw new IllegalArgumentException("pageCommand must contain %page% if provided.");
        }
        this.pageCommand = pageCommand;
    }

    public abstract Component getComponent(int number);

    public abstract int getComponentsSize();

    public void setComponentsPerPage(int componentsPerPage) {
        this.componentsPerPage = componentsPerPage;
    }

    protected final int getCurrentPage() {
        return currentPage;
    }

    public Component create(int page) throws CommandException {
        if (page == 1 && getComponentsSize() == 0) {
            return getContents().reset().append(Msg.PAGINATION__EMPTY.text()).build();
        }
        int pageCount = (int) Math.ceil(getComponentsSize() / (double) componentsPerPage);
        if (page < 1 || page > pageCount) {
            throw new CommandException(Msg.PAGINATION__INVALID_PAGE.get());
        }
        currentPage = page;
        final int lastComp = Math.min(page * componentsPerPage, getComponentsSize());
        for (int i = (page - 1) * componentsPerPage; i < lastComp; i++) {
            getContents().append(getComponent(i));
            if (i + 1 != lastComp) {
                getContents().newline();
            }
        }
        if (pageCount == 1) {
            return super.build();
        }
        getContents().newline();
        Component pageNumberComponent = Msg.PAGINATION__PAGE.text(page, pageCount);
        if (pageCommand != null) {
            TextComponentProducer navProducer = new TextComponentProducer();
            Component navigate = Msg.PAGINATION__NAVIGATE.text();
            if (page > 1) {
                navProducer.append(Text.of("<<< ", NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.runCommand(pageCommand.replace("%page%", String.valueOf(page - 1))))
                        .hoverEvent(HoverEvent.showText(navigate)));
            }
            navProducer.append(pageNumberComponent);
            if (page < pageCount) {
                navProducer.append(Text.of(" >>>", NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.runCommand(pageCommand.replace("%page%", String.valueOf(page + 1))))
                        .hoverEvent(HoverEvent.showText(navigate)));
            }
            getContents().append(centerAndBorder(navProducer.build()));
        } else {
            getContents().append(centerAndBorder(pageNumberComponent));
        }
        return super.build();
    }

    @Override
    public TextComponent build() {
        throw new IllegalStateException("Pagination components must be created with a page");
    }
}
