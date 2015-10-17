/*
 Secuboid: Lands and Factions plugin for Minecraft server
 Copyright (C) 2014 Kaz00, Tabinol

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
package me.tabinol.secuboid.playerscache;

import java.util.concurrent.Callable;

import me.tabinol.secuboid.commands.CommandThreadExec;
import me.tabinol.secuboid.exceptions.SecuboidCommandException;

/**
 * The Class ReturnToCommand.
 */
public class ReturnToCommand implements Callable<Void> {

	/** The command exec. */
	private final CommandThreadExec commandExec;
	
	/** The player cache entry. */
	private final PlayerCacheEntry[] playerCacheEntry;
	
	/**
	 * Instantiates a new return to command.
	 *
	 * @param commandExec the command exec
	 * @param playerCacheEntry the player cache entry
	 */
	public ReturnToCommand(CommandThreadExec commandExec, PlayerCacheEntry[] playerCacheEntry) {
		
		this.commandExec = commandExec;
		this.playerCacheEntry = playerCacheEntry;
	}
	
	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Void call() {
		
		// Return the output of the request
		try {
			commandExec.commandThreadExecute(playerCacheEntry);
		} catch (SecuboidCommandException e) {
			// Empty
		}
		
		return null;
	}
}
