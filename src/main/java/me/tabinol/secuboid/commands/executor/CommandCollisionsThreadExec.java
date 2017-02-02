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
package me.tabinol.secuboid.commands.executor;

import java.util.Calendar;

import me.tabinol.secuboid.Secuboid;
import me.tabinol.secuboid.commands.ArgList;
import me.tabinol.secuboid.commands.InfoCommand;
import me.tabinol.secuboid.config.Config;
import me.tabinol.secuboid.exceptions.SecuboidCommandException;
import me.tabinol.secuboid.lands.RealLand;
import me.tabinol.secuboid.lands.approve.Approve;
import me.tabinol.secuboid.lands.areas.Area;
import me.tabinol.secuboid.lands.collisions.Collisions;
import me.tabinol.secuboid.lands.types.Type;
import me.tabinol.secuboid.playercontainer.PlayerContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Can create a command and calculate the collisions in a thread.
 */
public abstract class CommandCollisionsThreadExec extends CommandExec {

    private static final long STATUS_FIRST_NB_TICKS = 40;
    private static final long STATUS_NEXT_NB_TICKS = 400;


    private boolean addForApprove = false;
    private Collisions.LandAction action = null;
    private BukkitTask statusTask = null;
    Type type = null;
    int removeId = 0;
    Area newArea = null;
    PlayerContainer owner = null;
    RealLand parent = null;

    private class CollisionThreadStatus extends BukkitRunnable {

        private final Player player;
        private final Collisions collisions;
        private long nbTick;

        CollisionThreadStatus(Player player, Collisions collisions) {
            this.player = player;
            this.collisions = collisions;
            nbTick = STATUS_FIRST_NB_TICKS;
        }

        @Override
        public void run() {
            if (player.isOnline()) {
                player.sendMessage(ChatColor.DARK_GRAY + "[Secuboid] "
                        + secuboid.getLanguage().getMessage("COLLISION.GENERAL.PERCENT", collisions.getPercentDone() + ""));
            }
            secuboid.getLog().info("Collision manger is running and takes " + nbTick + " ticks.");
            nbTick += STATUS_NEXT_NB_TICKS;
        }
    }

    /**
     * Instantiates a new command collisions thread exec.
     *
     * @param secuboid    secuboid instance
     * @param infoCommand the info command
     * @param sender      the sender
     * @param argList     the arg list
     * @throws SecuboidCommandException the secuboid command exception
     */
    public CommandCollisionsThreadExec(Secuboid secuboid, InfoCommand infoCommand, CommandSender sender, ArgList argList)
            throws SecuboidCommandException {

        super(secuboid, infoCommand, sender, argList);
    }

    /**
     * Command thread execute.
     *
     * @param collisions collisions
     * @throws SecuboidCommandException the secuboid command exception
     */
    public abstract void commandThreadExecute(Collisions collisions)
            throws SecuboidCommandException;

    /**
     * Check collision. Why Land parameter? The land can be an other land, not the land stored here.
     * @param worldName the world name
     * @param landName      the land name
     * @param land          the land
     * @param type          the type
     * @param action        the action
     * @param removeId      the remove id
     * @param newArea       the new area
     * @param parent        the parent
     * @param owner         the owner of the land (PlayerContainer)
     * @param addForApprove the add for approve
     * @throws SecuboidCommandException the secuboid command exception
     */
    void checkCollision(String worldName, String landName, RealLand land, Type type, Collisions.LandAction action,
                        int removeId, Area newArea, RealLand parent, PlayerContainer owner,
                        boolean addForApprove) throws SecuboidCommandException {

        // allowApprove: false: The command can absolutely not be done if there is error!
        this.addForApprove = addForApprove;
        this.type = type;
        this.action = action;
        this.removeId = removeId;
        this.newArea = newArea;
        this.owner = owner;
        this.parent = parent;
        boolean isFree = !isPlayerMustPay();
        Collisions coll = new Collisions(secuboid, worldName, landName, land, action, removeId, newArea, parent,
                owner, isFree, !addForApprove);
        secuboid.getCollisionsManagerThread().lookForCollisions(this, coll);
        CollisionThreadStatus collisionThreadStatus = new CollisionThreadStatus(player, coll);
        statusTask = Bukkit.getScheduler().runTaskTimer(secuboid, (Runnable) collisionThreadStatus,
                STATUS_FIRST_NB_TICKS, STATUS_NEXT_NB_TICKS);
    }

    /**
     * The returned method from thread
     *
     * @param collisions collisions
     * @throws SecuboidCommandException the secuboid command exception
     */
    public void commandThreadParentExecute(Collisions collisions) throws SecuboidCommandException {

        boolean allowApprove = collisions.getAllowApprove();

        if (statusTask != null) {
            statusTask.cancel();
        }

        if (collisions.hasCollisions()) {
            sender.sendMessage(collisions.getPrints());

            if (addForApprove) {
                if (secuboid.getConf().getAllowCollision() == Config.AllowCollisionType.APPROVE && allowApprove) {

                    sender.sendMessage(ChatColor.RED + "[Secuboid] " + secuboid.getLanguage().getMessage("COLLISION.GENERAL.NEEDAPPROVE", collisions.getLandName()));
                    secuboid.getLands().getApproveList().addApprove(new Approve(secuboid, collisions.getLandName(), type, action, removeId, newArea,
                            owner, parent, collisions.getPrice(), Calendar.getInstance()));
                    new CommandCancel(secuboid, null, sender, argList).commandExecute();

                } else if (secuboid.getConf().getAllowCollision() == Config.AllowCollisionType.FALSE || !allowApprove) {

                    throw new SecuboidCommandException(secuboid, "Land collision", sender, "COLLISION.GENERAL.CANNOTDONE");
                }
            }
        }

        commandThreadExecute(collisions);
    }

    /**
     * Checks if is player must pay.
     *
     * @return true, if is player must pay
     */
    private boolean isPlayerMustPay() {
        // Is Economy?

        return !(secuboid.getPlayerMoney() == null
                || !secuboid.getConf().useEconomy()
                || playerConf.isAdminMode());
    }
}
