package me.imdanix.wgtranslator;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class I18n implements CommandExecutor {
    private static final Component BRAND = Message.Serializer.LEGACY_AMPERSAND.deserialize("&6&lWGTranslator &f&oby imDaniX");

    private final File directory;
    private final File configFile;
    private final File messagesFile;

    public I18n(Plugin plugin) {
        Bukkit.getLogger().info("[WGTranslator] Initializing translation from the files.");
        this.directory = new File(plugin.getDataFolder(), "translator");
        this.configFile = new File(directory, "config.yml");
        this.messagesFile = new File(directory, "messages.yml");
        reload();
    }

    public File getDirectory() {
        return directory;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(BRAND);
        if (!reload()) {
            sender.sendMessage(ChatColor.YELLOW + "Translation is reloaded, but something went wrong. " +
                    "Please check the console.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Translation was successfully reloaded.");
        }
        return true;
    }

    private boolean reload() {
        boolean valid = reloadSettings(getConfig(configFile, cfg -> cfg.set("serializer", Message.Serializer.LEGACY_AMPERSAND.name())));
        FileConfiguration messages = getConfig(messagesFile, cfg -> {});
        if (messages != null) {
            addMissing(messages);
        }
        return reloadMessages(messages) && valid;
    }

    private void addMissing(FileConfiguration cfg) {
        int added = 0;
        for (Msg msg : Msg.values()) {
            if (cfg.contains(msg.asSection())) {
                continue;
            }
            if (msg.isMiniMessage()) {
                cfg.set(msg.asSection() + ".text", msg.getDefault());
                cfg.set(msg.asSection() + ".serializer", Message.Serializer.MINIMESSAGE.name());
            } else {
                cfg.set(msg.asSection(), msg.getDefault());
            }
            added++;
        }
        if (added == 0) {
            return;
        }
        try {
            cfg.save(messagesFile);
            Bukkit.getLogger().info("[WGTranslator] Added " + added + " missing messages to messages.yml.");
        } catch (IOException e) {
            Bukkit.getLogger().warning("[WGTranslator] Could not save missing messages to messages.yml.");
            e.printStackTrace();
        }
    }

    private boolean reloadSettings(@Nullable FileConfiguration cfg) {
        Message.Serializer serializer = Message.Serializer.LEGACY_AMPERSAND;
        boolean valid = cfg != null;
        if (cfg != null) {
            String value = cfg.getString("serializer");
            try {
                serializer = value == null ? serializer : Message.Serializer.valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                valid = false;
                Bukkit.getLogger().warning("[WGTranslator] Unknown default serializer '" + value + "' in config.yml. " +
                        "Using " + serializer + ".");
            }
        }
        Messenger.setDefaultSerializer(serializer);
        return valid;
    }

    private boolean reloadMessages(@Nullable FileConfiguration cfg) {
        if (cfg == null) {
            return false;
        }
        List<String> errors = new ArrayList<>();
        for (Msg msg : Msg.values()) {
            Message message = null;
            try {
                message = parse(cfg, msg.asSection());
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("[WGTranslator] Invalid message '" + msg.asSection() + "': " + e.getMessage());
            }
            if (!msg.setMessage(message)) {
                errors.add(msg.asSection());
            }
        }
        Logger log = Bukkit.getLogger();
        if (errors.isEmpty()) {
            log.info("[WGTranslator] Successfully reloaded all the messages.");
            return true;
        } else {
            log.warning("[WGTranslator] Some messages in messages.yml are invalid. " +
                    "Using default ones for: " + String.join(", ", errors) + ".");
            return false;
        }
    }

    @Nullable
    private Message parse(FileConfiguration cfg, String path) {
        if (cfg.isString(path)) {
            return Message.of(cfg.getString(path));
        }
        ConfigurationSection section = cfg.getConfigurationSection(path);
        if (section == null || !section.isString("text")) {
            return null;
        }
        Message.Type type = parse(Message.Type.class, section.getString("type"), null);
        Message.Serializer serializer = parse(Message.Serializer.class, section.getString("serializer"), null);
        Message.Place place = parse(Message.Place.class, section.getString("place"), Message.Place.CHAT);
        return new Message(section.getString("text"), type, serializer, place);
    }

    private static <E extends Enum<E>> E parse(Class<E> type, @Nullable String value, E fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unknown " + type.getSimpleName() + " '" + value + "'");
        }
    }

    @Nullable
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public FileConfiguration getConfig(File file, Consumer<FileConfiguration> defaults) {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                defaults.accept(cfg);
                cfg.save(file);
                return cfg;
            } catch (Exception e) {
                Bukkit.getLogger().warning("[WGTranslator] Something went wrong during creation of " + file.getName() + ".");
                e.printStackTrace();
                return null;
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }
}
