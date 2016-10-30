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
package me.tabinol.secuboid.config.players;

// Entries for each player
import me.tabinol.secuboid.Secuboid;
import me.tabinol.secuboid.commands.ChatPage;
import me.tabinol.secuboid.commands.ConfirmEntry;
import me.tabinol.secuboid.lands.Land;
import me.tabinol.secuboid.playercontainer.PlayerContainerPlayer;
import me.tabinol.secuboid.selection.PlayerSelection;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The Class PlayerConfEntry.
 */
public class PlayerConfEntry {

    /**
     * The player (or sender).
     */
    private final CommandSender sender;

    /**
     *  // The player (if is not console).
     */
    private final Player player;

    /**
     * Player Lands, areas and visual selections
     */
    private final PlayerSelection playerSelection;

    /**
     * If the player is in Admin Mod
     */
    private boolean adminMode = false;

    /**
     * secuboid confirm command
     */
    private ConfirmEntry confirm = null;

    /**
     * pages for /secuboid page command
     */
    private ChatPage chatPage = null;

    /**
     * Time of lastupdate for PlayerEvents
     */
    private long lastMoveUpdate = 0;

    /**
     * Last Land for player
     */
    private Land lastLand = null;

    /**
     * Present location
     */
    private Location lastLoc = null;

    /**
     * If the player has a teleportation cacelled
     */
    private boolean tpCancel = false;

    /**
     * Auto cancel selection system
     */
    private PlayerAutoCancelSelect cancelSelect = null;

    /**
     * PlayerContainerPlayer for this player
     */
    private final PlayerContainerPlayer pcp;

    /**
     * Instantiates a new player conf entry.
     *
     * @param sender the sender
     */
    PlayerConfEntry(CommandSender sender) {

	this.sender = sender;
	if (sender instanceof Player) {
	    player = (Player) sender;
	    playerSelection = new PlayerSelection(this);
	    pcp = new PlayerContainerPlayer(player.getUniqueId());
	} else {
	    player = null;
	    playerSelection = null;
	    pcp = null;
	}
    }

    /**
     *
     * @return
     */
    public PlayerContainerPlayer getPlayerContainer() {

	return pcp;
    }

    /**
     *
     * @return
     */
    public CommandSender getSender() {

	return sender;
    }

    /**
     *
     * @return
     */
    public Player getPlayer() {

	return player;
    }

    /**
     * Gets the selection.
     *
     * @return the selection
     */
    public PlayerSelection getSelection() {

	return playerSelection;
    }

    /**
     *
     * @return
     */
    public boolean isAdminMode() {

	// Security for adminmode
	if (adminMode == true && !sender.hasPermission("secuboid.adminmod")) {
	    adminMode = false;
	    return false;
	}

	return adminMode;
    }

    /**
     * Sets the admin mod.
     *
     * @param value the new admin mod
     */
    public void setAdminMode(boolean value) {

	adminMode = value;
    }

    /**
     * Gets the confirm.
     *
     * @return the confirm
     */
    public ConfirmEntry getConfirm() {

	return confirm;
    }

    /**
     * Sets the confirm.
     *
     * @param entry the new confirm
     */
    public void setConfirm(ConfirmEntry entry) {

	confirm = entry;
    }

    /**
     * Gets the chat page.
     *
     * @return the chat page
     */
    public ChatPage getChatPage() {

	return chatPage;
    }

    /**
     * Sets the chat page.
     *
     * @param page the new chat page
     */
    public void setChatPage(ChatPage page) {

	chatPage = page;
    }

    /**
     *
     * @return
     */
    public long getLastMoveUpdate() {

	return lastMoveUpdate;
    }

    /**
     * Sets the last move update.
     *
     * @param lastMove the new last move update
     */
    public void setLastMoveUpdate(Long lastMove) {

	lastMoveUpdate = lastMove;
    }

    /**
     *
     * @return
     */
    public Land getLastLand() {

	return lastLand;
    }

    /**
     * Sets the last land.
     *
     * @param land the new last land
     */
    public void setLastLand(Land land) {

	lastLand = land;
    }

    /**
     *
     * @return
     */
    public Location getLastLoc() {

	return lastLoc;
    }

    /**
     * Sets the last loc.
     *
     * @param loc the new last loc
     */
    public void setLastLoc(Location loc) {

	lastLoc = loc;
    }

    /**
     * Checks for tp cancel.
     *
     * @return true, if successful
     */
    public boolean hasTpCancel() {

	return tpCancel;
    }

    /**
     * Sets the tp cancel.
     *
     * @param tpCancel the new tp cancel
     */
    public void setTpCancel(boolean tpCancel) {

	this.tpCancel = tpCancel;
    }

    // Set auto cancel select
    /**
     * Sets the auto cancel select.
     *
     * @param value the new auto cancel select
     */
    public void setAutoCancelSelect(boolean value) {

	Long timeTick = Secuboid.getThisPlugin().getConf().getSelectAutoCancel();

	if (timeTick == 0) {
	    return;
	}

	if (cancelSelect == null && value == true) {
	    cancelSelect = new PlayerAutoCancelSelect(this);
	}

	if (cancelSelect == null) {
	    return;
	}

	if (value == true) {

	    // Schedule task
	    cancelSelect.runLater(timeTick, false);
	} else {

	    // Stop!
	    cancelSelect.stopNextRun();
	}
    }
}
