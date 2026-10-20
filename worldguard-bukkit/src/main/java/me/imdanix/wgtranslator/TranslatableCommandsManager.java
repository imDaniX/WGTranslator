/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.imdanix.wgtranslator;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.extension.platform.Actor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class TranslatableCommandsManager extends CommandsManager<Actor> {
    private FileConfiguration cfg;
    private File file;
    private boolean firstTime;
    private final Map<Command, Command> alternativeCommands = new HashMap<>();

    public void load(I18n translator) {
        file = new File(translator.getDirectory(), "commands.yml");
        firstTime = !file.exists();
        cfg = translator.getConfig(file, _ -> {});
    }

    public void clean() {
        cfg = null;
        file = null;
    }

    @Override
    public boolean hasPermission(Actor player, String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public List<Command> registerMethods(Class<?> cls, Method parent) {
        List<Command> commandsList = super.registerMethods(cls, parent);
        if (parent != null) {
            String parentName = parent.getAnnotation(Command.class).aliases()[0];

            for (Command nested : commandsList) {
                String nestedName = nested.aliases()[0];

                String nestedPath = parentName + ".nested." + nestedName;
                if (firstTime) {
                    cfg.set(nestedPath + ".usage", nested.usage());
                    cfg.set(nestedPath + ".description", nested.desc());
                    alternativeCommands.put(nested, nested);
                } else {
                    alternativeCommands.put(
                            nested,
                            new AltCommand(
                                    nested,
                                    cfg.getString(nestedPath + ".usage", nested.usage()),
                                    cfg.getString(nestedPath + ".description", nested.desc())
                            )
                    );
                }
            }
        } else {
            for (Command command : commandsList) {
                String commandName = command.aliases()[0];
                if (firstTime) {
                    cfg.set(commandName + ".usage", command.usage());
                    cfg.set(commandName + ".description.short", command.desc());
                    cfg.set(commandName + ".description.full", descs.get(commandName));
                    cfg.set(commandName + ".help", helpMessages.get(commandName).replace("\\n", "\n"));
                    alternativeCommands.put(command, command);
                } else {
                    if (cfg.isString(commandName + ".description.full")) descs.put(commandName, cfg.getString(commandName + ".description.full"));
                    if (cfg.isString(commandName + ".help")) for(String alias : command.aliases())
                            helpMessages.put(alias, cfg.getString(commandName + ".help"));
                    alternativeCommands.put(
                            command,
                            new AltCommand(
                                    command,
                                    cfg.getString(commandName + ".usage", command.usage()),
                                    cfg.getString(commandName + ".description.short", command.desc())
                            )
                    );
                }
            }
        }

        if (firstTime) {
            try {
                cfg.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return commandsList;
    }

    @Override
    protected String getUsage(String[] args, int level, Command cmd) {
        return super.getUsage(args, level, alternativeCommands.get(cmd));
    }

    @Override
    protected String getNestedUsage(String[] args, int level, Method method, Actor player) throws CommandException {
        StringBuilder command = new StringBuilder();

        command.append("/");

        for (int i = 0; i <= level; ++i) {
            command.append(args[i]).append(" ");
        }

        Map<String, Method> map = commands.get(method);
        boolean found = false;

        command.append("<");

        Set<String> allowedCommands = new HashSet<>();

        for (Map.Entry<String, Method> entry : map.entrySet()) {
            Method childMethod = entry.getValue();
            found = true;

            if (hasPermission(childMethod, player)) {
                Command childCmd = alternativeCommands.get(childMethod.getAnnotation(Command.class));

                allowedCommands.add(childCmd.aliases()[0]);
            }
        }

        if (!allowedCommands.isEmpty()) {
            command.append(StringUtil.joinString(allowedCommands, "|", 0));
        } else {
            if (!found) {
                command.append("?");
            } else {
                //command.append("action");
                throw new CommandPermissionsException();
            }
        }

        command.append(">");

        return command.toString();
    }

    private static class AltCommand implements Command {
        private final Command command;
        private final String usage;
        private final String desc;

        private AltCommand(Command command, String usage, String desc) {
            this.command = command;
            this.usage = Messenger.legacy(usage);
            this.desc = Messenger.legacy(desc);
        }

        @Override
        public String[] aliases() {
            return command.aliases();
        }

        @Override
        public String usage() {
            return usage;
        }

        @Override
        public String desc() {
            return desc;
        }

        @Override
        public int min() {
            return command.min();
        }

        @Override
        public int max() {
            return command.max();
        }

        @Override
        public String flags() {
            return command.flags();
        }

        @Override
        public String help() {
            return command.help();
        }

        @Override
        public boolean anyFlags() {
            return command.anyFlags();
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Command.class;
        }
    }
}
