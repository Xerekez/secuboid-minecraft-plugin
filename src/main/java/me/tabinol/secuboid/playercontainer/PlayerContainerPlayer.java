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
package me.tabinol.secuboid.playercontainer;

import java.util.UUID;
import me.tabinol.secuboid.Secuboid;
import me.tabinol.secuboid.lands.RealLand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Represents a player.
 *
 * @author tabinol
 */
public class PlayerContainerPlayer implements PlayerContainer {

    private final Secuboid secuboid;
    private final UUID minecraftUUID;
    private final String name;

    public PlayerContainerPlayer(Secuboid secuboid, UUID minecraftUUID) {
	this.secuboid = secuboid;
	name = "ID-" + minecraftUUID.toString();
	this.minecraftUUID = minecraftUUID;
    }

    @Override
    public boolean hasAccess(Player player) {
		return player != null && minecraftUUID.equals(player.getUniqueId());
	}

    @Override
    public boolean hasAccess(Player player, RealLand land) {
	return hasAccess(player);
    }

    @Override
    public String getPrint() {

	StringBuilder sb = new StringBuilder();
	String playerName = getPlayerName();

	sb.append(ChatColor.DARK_RED).append("P:");

	if (playerName != null) {
	    sb.append(ChatColor.WHITE).append(playerName);
	} else {
	    // Player never connected on the server, show UUID
	    sb.append(ChatColor.DARK_GRAY).append(name);
	}

	return sb.toString();
    }

    public String getPlayerName() {

	String playerName;

	// Pass 1 get in Online players
	Player player = Bukkit.getPlayer(minecraftUUID);
	if (player != null) {
	    return player.getName();
	}

	// Pass 2 get from Secuboid cache
	playerName = secuboid.getPlayersCache().getNameFromUUID(minecraftUUID);
	if (playerName != null) {
	    return playerName;
	}

	// Pass 3 get from offline players
	OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(minecraftUUID);
	if (offlinePlayer != null) {
	    return offlinePlayer.getName();
	}

	return null;
    }

    @Override
    public void setLand(RealLand land) {
    }

    public UUID getMinecraftUUID() {
	return minecraftUUID;
    }

    public Player getPlayer() {
	return Bukkit.getPlayer(minecraftUUID);
    }

    public OfflinePlayer getOfflinePlayer() {
	return Bukkit.getOfflinePlayer(minecraftUUID);
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public PlayerContainerType getContainerType() {
	return PlayerContainerType.PLAYER;
    }

    @Override
    public String toFileFormat() {
	return PlayerContainerType.PLAYER + ":" + name;
    }

    @Override
    public RealLand getLand() {
	return null;
    }

    @Override
    public int compareTo(PlayerContainer t) {
	int result = PlayerContainerType.PLAYER.compareTo(t.getContainerType());
	if (result == 0) {
	    return result;
	}
	return minecraftUUID.compareTo(((PlayerContainerPlayer) t).minecraftUUID);
    }
}
