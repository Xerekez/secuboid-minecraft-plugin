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
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Can create a command and calculate the collisions in a thread.
 */
public abstract class CommandCollisionsThreadExec extends CommandExec {

    /**
     *
     */
    protected boolean addForApprove = false;

    /**
     *
     */
    protected Type type = null;

    /**
     *
     */
    protected Collisions.LandAction action = null;

    /**
     *
     */
    protected int removeId = 0;

    /**
     *
     */
    protected Area newArea = null;

    /**
     *
     */
    protected PlayerContainer owner = null;

    /**
     *
     */
    protected RealLand parent = null;

    /**
     * Instantiates a new command collisions thread exec.
     *
     * @param secuboid secuboid instance
     * @param infoCommand the info command
     * @param sender the sender
     * @param argList the arg list
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
     * Check collision. Why Land paramater? The land can be an other land, not the land stored here.
     *
     * @param landName the land name
     * @param land the land
     * @param type the type
     * @param action the action
     * @param removeId the remove id
     * @param newArea the new area
     * @param parent the parent
     * @param owner the owner of the land (PlayerContainer)
     * @param addForApprove the add for approve
     * @throws SecuboidCommandException the secuboid command exception
     */
    protected void checkCollision(String landName, RealLand land, Type type, Collisions.LandAction action,
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
	Collisions coll = new Collisions(secuboid, landName, land, action, removeId, newArea, parent,
		owner, isFree, !addForApprove);
	secuboid.getCollisionsManagerThread().lookForCollisions(this, coll);
    }

    /**
     * The returned method from thread
     *
     * @param collisions collisions
     * @throws SecuboidCommandException the secuboid command exception
     */
    public void commandThreadParentExecute(Collisions collisions) throws SecuboidCommandException {

	boolean allowApprove = collisions.getAllowApprove();

	if (collisions.hasCollisions()) {
	    sender.sendMessage(collisions.getPrints());

	    if (addForApprove) {
		if (secuboid.getConf().getAllowCollision() == Config.AllowCollisionType.APPROVE && allowApprove == true) {

		    sender.sendMessage(ChatColor.RED + "[Secuboid] " + secuboid.getLanguage().getMessage("COLLISION.GENERAL.NEEDAPPROVE", collisions.getLandName()));
		    secuboid.getLog().write("land " + collisions.getLandName() + " has collision and needs approval.");
		    secuboid.getLands().getApproveList().addApprove(new Approve(secuboid, collisions.getLandName(), type, action, removeId, newArea,
			    owner, parent, collisions.getPrice(), Calendar.getInstance()));
		    new CommandCancel(secuboid, infoCommand, sender, argList).commandExecute();

		} else if (secuboid.getConf().getAllowCollision() == Config.AllowCollisionType.FALSE || allowApprove == false) {

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
    protected boolean isPlayerMustPay() {
	// Is Economy?

	return !(secuboid.getPlayerMoney() == null
		|| !secuboid.getConf().useEconomy()
		|| playerConf.isAdminMode());
    }
}