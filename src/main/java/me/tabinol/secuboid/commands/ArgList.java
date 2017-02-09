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

import me.tabinol.secuboid.Secuboid;
import me.tabinol.secuboid.exceptions.SecuboidCommandException;
import me.tabinol.secuboid.permissionsflags.Flag;
import me.tabinol.secuboid.permissionsflags.FlagType;
import me.tabinol.secuboid.permissionsflags.FlagValue;
import me.tabinol.secuboid.permissionsflags.Permission;
import me.tabinol.secuboid.permissionsflags.PermissionType;
import me.tabinol.secuboid.playercontainer.PlayerContainer;
import me.tabinol.secuboid.playercontainer.PlayerContainerType;
import org.bukkit.command.CommandSender;

/**
 * The Class ArgList. Works with command arguments.
 */
public class ArgList {

    private final Secuboid secuboid;

    /**
     * The arg.
     */
    private final String[] arg;

    /**
     * The iterator.
     */
    private int iterator;

    /**
     * The player.
     */
    private final CommandSender player;

    /**
     * Instantiates a new arg list.
     *
     * @param secuboid secuboid instance
     * @param arg      the arg
     * @param player   the player
     */
    public ArgList(Secuboid secuboid, String[] arg, CommandSender player) {

        this.secuboid = secuboid;
        this.arg = arg;
        this.player = player;
        iterator = -1;
    }

    /**
     * Gets the next.
     *
     * @return the next
     */
    public String getNext() {

        iterator++;
        return getCur();
    }

    /**
     * Gets the cur.
     *
     * @return the cur
     */
    private String getCur() {

        if (iterator >= arg.length) {
            return null;
        }
        if (iterator < 0) {
            iterator = 0;
        }

        return arg[iterator];
    }

    /**
     * Sets the iterator to zero position.
     *
     */
    public void setPosZero() {

        this.iterator = 0;
    }

    /**
     * Checks if is last.
     *
     * @return true, if is last
     */
    public boolean isLast() {

        return iterator == arg.length - 1;
    }

    /**
     * Length.
     *
     * @return the int
     */
    public int length() {

        return iterator;
    }

    /**
     * Gets the next to end.
     *
     * @return the next to end
     */
    private String getNextToEnd() {

        StringBuilder result = new StringBuilder();
        String cur;

        while ((cur = getNext()) != null) {
            if (result.length() != 0) {
                result.append(" ");
            }
            result.append(cur);
        }

        return result.toString();
    }

    /**
     * Gets the flag type from arg.
     *
     * @param isAdminmode the is adminmode
     * @param isOwner     the is owner
     * @return the flag type from arg
     * @throws SecuboidCommandException the secuboid command exception
     */
    public FlagType getFlagTypeFromArg(boolean isAdminmode, boolean isOwner) throws SecuboidCommandException {

        String curArg = getNext();
        FlagType flagType;

        if (curArg == null) {
            throw new SecuboidCommandException(secuboid, "Flag error", player, "COMMAND.FLAGS.FLAGNULL");
        }

        flagType = secuboid.getPermissionsFlags().getFlagType(curArg.toUpperCase());
        if (flagType == null) {
            throw new SecuboidCommandException(secuboid, "Flag error", player, "COMMAND.FLAGS.FLAGNULL");
        }

        if (!isAdminmode && !(isOwner && secuboid.getConf().getOwnerConfigFlag().contains(flagType))) {
            throw new SecuboidCommandException(secuboid, "Flag error", player, "GENERAL.MISSINGPERMISSION");
        }

        return flagType;
    }

    /**
     * Gets the flag from arg.
     *
     * @param isAdminmob the is adminmob
     * @param isOwner    the is owner
     * @return the flag from arg
     * @throws SecuboidCommandException the secuboid command exception
     */
    public Flag getFlagFromArg(boolean isAdminmob, boolean isOwner) throws SecuboidCommandException {

        FlagType flagType = getFlagTypeFromArg(isAdminmob, isOwner);

        if (isLast()) {
            throw new SecuboidCommandException(secuboid, "Flag error", player, "GENERAL.MISSINGINFO");
        }

        FlagValue flagValue = secuboid.getNewInstance().getFlagValueFromFileFormat(getNextToEnd(), flagType);

        if (flagValue != null) {
            return secuboid.getPermissionsFlags().newFlag(flagType, flagValue, true);
        } else {
            return null;
        }
    }

    /**
     * Gets the player container from arg.
     *
     * @param bannedPCTList the banned pct list
     * @return the player container from arg
     * @throws SecuboidCommandException the secuboid command exception
     */
    public PlayerContainer getPlayerContainerFromArg(PlayerContainerType[] bannedPCTList) throws SecuboidCommandException {

        String curArg = getNext();
        String param = null;
        PlayerContainer pc;

        if (curArg == null) {
            throw new SecuboidCommandException(secuboid, "PlayerContainer Error", player, "COMMAND.CONTAINERTYPE.TYPENULL");
        }

        PlayerContainerType pcType = PlayerContainerType.getFromString(curArg);

        if (pcType == null) {
            // Type player if it is the player directly
            pcType = PlayerContainerType.PLAYER;
            param = curArg;
        }

        if (bannedPCTList != null) {
            for (PlayerContainerType bPCT : bannedPCTList) {
                if (pcType == bPCT) {
                    throw new SecuboidCommandException(secuboid, "PlayerContainer Error", player, "COMMAND.CONTAINERTYPE.NOTPERMITTED");
                }
            }
        }

        if (pcType.hasParameter()) {
            if (param == null) {
                param = getNext();
            }
            if (param == null) {
                throw new SecuboidCommandException(secuboid, "PlayerContainer Error", player, "COMMAND.CONTAINER.CONTAINERNULL");
            }
            pc = secuboid.getNewInstance().createPlayerContainer(pcType, param);
        } else {
            pc = secuboid.getNewInstance().createPlayerContainer(pcType, "");
        }

        if (pcType == PlayerContainerType.PLAYER && pc == null) {

            // this player doesn't exist
            throw new SecuboidCommandException(secuboid, "Player not exist Error", player, "COMMAND.CONTAINER.PLAYERNOTEXIST");
        }

        return pc;
    }

    /**
     * Gets the permission type from arg.
     *
     * @param isAdminmode the is adminmod
     * @param isOwner     the is owner
     * @return the permission type from arg
     * @throws SecuboidCommandException the secuboid command exception
     */
    public PermissionType getPermissionTypeFromArg(boolean isAdminmode, boolean isOwner) throws SecuboidCommandException {

        String curArg = getNext();
        PermissionType pt;

        if (curArg == null) {
            throw new SecuboidCommandException(secuboid, "Permission Error", player, "COMMAND.PERMISSIONTYPE.TYPENULL");
        }

        pt = secuboid.getPermissionsFlags().getPermissionType(curArg.toUpperCase());
        if (pt == null) {
            throw new SecuboidCommandException(secuboid, "Permission Error", player, "COMMAND.PERMISSIONTYPE.INVALID");
        }

        if (!isAdminmode && !(isOwner && secuboid.getConf().getOwnerConfigPerm().contains(pt))) {
            throw new SecuboidCommandException(secuboid, "Permission Error", player, "GENERAL.MISSINGPERMISSION");
        }

        return pt;
    }

    /**
     * Gets the permission from arg.
     *
     * @param isAdminmode the is adminmod
     * @param isOwner     the is owner
     * @return the permission from arg
     * @throws SecuboidCommandException the secuboid command exception
     */
    public Permission getPermissionFromArg(boolean isAdminmode, boolean isOwner) throws SecuboidCommandException {

        PermissionType pt = getPermissionTypeFromArg(isAdminmode, isOwner);
        String curArg = getNext();

        if (curArg == null) {
            throw new SecuboidCommandException(secuboid, "Permission Error", player, "COMMAND.PERMISSIONVALUE.VALUENULL");
        }

        return secuboid.getPermissionsFlags().newPermission(pt, Boolean.parseBoolean(curArg), true);
    }
}
