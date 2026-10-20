package me.imdanix.wgtranslator;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class BukkitMessageBridge implements Messenger.Bridge {
    @Override
    public boolean send(Object receiver, Message.Place place, Component main, @Nullable Component sub) {
        Audience audience = audience(receiver);
        if (audience == null) {
            return false;
        }
        if (!(audience instanceof Player)) {
            audience.sendMessage(Messenger.combine(place, main, sub));
            return true;
        }
        switch (place) {
            case CHAT -> audience.sendMessage(main);
            case TITLE -> audience.showTitle(Title.title(main, sub == null ? Component.empty() : sub));
            case SUBTITLE -> audience.showTitle(Title.title(Component.empty(), sub == null ? main : sub));
            case ACTIONBAR -> audience.sendActionBar(main);
        }
        return true;
    }

    @Override
    public void broadcast(String permission, Component message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(message);
            }
        }
    }

    @Nullable
    private Audience audience(Object receiver) {
        if (receiver instanceof CommandSender sender) {
            return sender;
        }
        if (receiver instanceof Actor actor) {
            try {
                return WorldGuardPlugin.inst().unwrapActor(actor);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}
