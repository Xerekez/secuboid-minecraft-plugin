/*
 Secuboid: Lands and Protection plugin for Minecraft server
 Copyright (C) 2015 Tabinol
 Forked from Factoid (Copyright (C) 2014 Kaz00, Tabinol)

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package me.tabinol.secuboid.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.tabinol.secuboid.Secuboid;
import me.tabinol.secuboid.commands.InfoCommand.CompletionMap;
import me.tabinol.secuboid.commands.executor.CommandExec;
import me.tabinol.secuboid.commands.executor.CommandHelp;
import me.tabinol.secuboid.config.players.PlayerConfEntry;
import me.tabinol.secuboid.exceptions.SecuboidCommandException;
import me.tabinol.secuboid.lands.Land;
import me.tabinol.secuboid.lands.types.Type;
import me.tabinol.secuboid.playercontainer.PlayerContainerType;
import me.tabinol.secuboid.utilities.StringChanges;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * The Class OnCommand.
 */
public final class CommandListener implements CommandExecutor, TabCompleter {

    private final Secuboid secuboid;
    private final Map<String, Class<? extends CommandExec>> commandToClass;
    private final List<String> consoleCommands;
    private final List<String> playerCommands;

    /**
     * Instantiates a new on command.
     *
     * @param secuboid secuboid instance
     */
    public CommandListener(Secuboid secuboid) {

        this.secuboid = secuboid;

        // Create Command list
        commandToClass = new TreeMap<>();
        consoleCommands = new ArrayList<>();
        playerCommands = new ArrayList<>();

        for (CommandClassList presentClass : CommandClassList.values()) {
            // Store commands information
            final InfoCommand infoCommand = presentClass.getCommandClass().getAnnotation(InfoCommand.class);
            final Class<? extends CommandExec> commandClass = presentClass.getCommandClass();
            addCommandToList(infoCommand, infoCommand.name().toLowerCase(), commandClass);
            for (String alias : infoCommand.aliases()) {
                addCommandToList(infoCommand, alias.toLowerCase(), commandClass);
            }
        }
    }

    private void addCommandToList(InfoCommand infoCommand, String command, Class<? extends CommandExec> commandClass) {
        commandToClass.put(command, commandClass);
        playerCommands.add(command);
        if (infoCommand.allowConsole()) {
            consoleCommands.add(command);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // Others commands then /secuboid, /claim and /fd will not be send.
        final ArgList argList = new ArgList(secuboid, args, sender);
        try {
            // Check the command to send
            getCommand(sender, cmd, argList);
            return true;
            // If error on command, send the message to the player
        } catch (SecuboidCommandException ex) {
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final String firstChars = args[args.length - 1];

        // First arguments
        if (args.length <= 1) {
            if (sender instanceof Player) {
                // Player connected
                return filterList(playerCommands, firstChars);
            }
            // Console?
            return filterList(consoleCommands, firstChars);
        }

        // Next arguments

        // Take command
        final Class<? extends CommandExec> cv = commandToClass.get(args[0]);
        if (cv == null) {
            return Collections.emptyList();
        }

        // Take the line without the command
        final String line = StringChanges.arrayToString(args, 1, args.length - 2);
        final InfoCommand ci = cv.getAnnotation(InfoCommand.class);

        // Find match with regex
        for (CompletionMap completionMap : ci.completion()) {
            Pattern pattern = Pattern.compile(completionMap.regex(), Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(line).matches()) {
                return makeArgList(sender, completionMap.completions(), firstChars);
            }
        }

        // No match found
        return Collections.emptyList();
    }

    private List<String> makeArgList(CommandSender sender, String[] completions, String firstChars) {
        List<String> argList = new ArrayList<>();
        for (String completion : completions) {
            switch (completion) {
            case "@approveLandList":
                argList.addAll(listApproveLandList(sender));
                break;
            case "@areaLand":
                argList.addAll(listAreaLand(sender));
                break;
            case "@boolean":
                argList.addAll(listBoolean());
                break;
            case "@command":
                argList.addAll(listCommand());
                break;
            case "@flag":
                argList.addAll(listFlag());
                break;
            case "@land":
                argList.addAll(listLand());
                break;
            case "@player":
                argList.addAll(listPlayer());
                break;
            case "@playerContainer":
                argList.addAll(listPlayerContainer());
                break;
            case "@permission":
                argList.addAll(listPermission());
                break;
            case "@type":
                argList.addAll(listType());
                break;
            default:
                argList.add(completion);
            }
        }
        return filterList(argList, firstChars);
    }

    private Set<String> listApproveLandList(CommandSender sender) {
        if (sender.hasPermission("secuboid.collisionapprove")) {
            return secuboid.getLands().getApproveList().getApproveList().keySet();
        }
        return Collections.emptySet();
    }

    private List<String> listAreaLand(CommandSender sender) {
        final PlayerConfEntry playerConf = secuboid.getPlayerConf().get(sender);
        if (playerConf.getSelection() != null && playerConf.getSelection().hasSelection()) {
            final Land land = playerConf.getSelection().getLand();
            final List<String> areasStrs = land.getAreas().stream().map(Object::toString).collect(Collectors.toList());
            return areasStrs;
        }
        return Collections.emptyList();
    }

    private List<String> listBoolean() {
        return Arrays.asList("false", "true");
    }

    private List<String> listCommand() {
        return playerCommands;
    }

    private Set<String> listFlag() {
        return secuboid.getPermissionsFlags().getFlagTypeNames();
    }

    private List<String> listLand() {
        final List<String> argList = new ArrayList<>();
        for (Land land : secuboid.getLands().getLands()) {
            argList.add(land.getName());
        }
        return argList;
    }

    private Set<String> listPlayer() {
        return secuboid.getPlayersCache().getPlayerNames();
    }

    private List<String> listPlayerContainer() {
        final List<String> argList = new ArrayList<>();
        argList.addAll(secuboid.getPlayersCache().getPlayerNames());
        for (PlayerContainerType pcType : PlayerContainerType.values()) {
            if (pcType != PlayerContainerType.PLAYERNAME) {
                argList.add(pcType.getPrint());
            }
        }
        return argList;
    }

    private Set<String> listPermission() {
        return secuboid.getPermissionsFlags().getPermissionTypeNames();
    }

    private List<String> listType() {
        return secuboid.getTypes().getTypes().stream().map(Type::getName).collect(Collectors.toList());
    }

    private List<String> filterList(List<String> matches, String firstChars) {
        return matches.stream().filter(match -> match.toLowerCase().startsWith(firstChars.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Gets command from args.
     *
     * @param sender  the sender
     * @param cmd     the command
     * @param argList arguments list
     * @throws SecuboidCommandException
     */
    private void getCommand(CommandSender sender, Command cmd, ArgList argList) throws SecuboidCommandException {

        try {
            // Show help if there is no arguments
            if (argList.isLast()) {
                new CommandHelp(secuboid, null, sender, null).commandExecute();
                return;
            }

            final String command = argList.getNext().toLowerCase();

            // take the name
            final Class<? extends CommandExec> cv = commandToClass.get(command.toLowerCase());

            // The command does not exist
            if (cv == null) {
                throw new SecuboidCommandException(secuboid, "Command not existing", sender, "COMMAND.NOTEXIST",
                        "SECUBOID");
            }

            // Remove page from memory if needed
            if (cv != commandToClass.get("page")) {
                secuboid.getPlayerConf().get(sender).setChatPage(null);
            }

            // Do the command
            final InfoCommand ci = cv.getAnnotation(InfoCommand.class);
            final CommandExec ce = cv
                    .getConstructor(Secuboid.class, InfoCommand.class, CommandSender.class, ArgList.class)
                    .newInstance(secuboid, ci, sender, argList);
            if (ce.isExecutable()) {
                ce.commandExecute();
            }

            // a huge number of Exception to catch!
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            throw new SecuboidCommandException(secuboid, "General Error on Command class find", sender,
                    "GENERAL.ERROR");
        } catch (InstantiationException ex) {
            ex.printStackTrace();
            throw new SecuboidCommandException(secuboid, "General Error on Command class find", sender,
                    "GENERAL.ERROR");
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            throw new SecuboidCommandException(secuboid, "General Error on Command class find", sender,
                    "GENERAL.ERROR");
        } catch (SecurityException ex) {
            ex.printStackTrace();
            throw new SecuboidCommandException(secuboid, "General Error on Command class find", sender,
                    "GENERAL.ERROR");
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            throw new SecuboidCommandException(secuboid, "General Error on Command class find", sender,
                    "GENERAL.ERROR");
        } catch (InvocationTargetException ex) {
            // Catched by SecuboidCommandException
        }
    }

    /**
     * Gets the info command.
     *
     * @param command the command
     * @return command information
     */
    public InfoCommand getInfoCommand(String command) {

        final Class<?> infoClass = commandToClass.get(command.toLowerCase());

        if (infoClass == null) {
            return null;
        }

        return infoClass.getAnnotation(InfoCommand.class);
    }
}
