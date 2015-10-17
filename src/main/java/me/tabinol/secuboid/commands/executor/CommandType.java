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
package me.tabinol.secuboid.commands.executor;

import me.tabinol.secuboid.Secuboid;
import me.tabinol.secuboid.commands.ChatPage;
import me.tabinol.secuboid.commands.CommandEntities;
import me.tabinol.secuboid.commands.CommandExec;
import me.tabinol.secuboid.commands.InfoCommand;
import me.tabinol.secuboid.config.Config;
import me.tabinol.secuboid.exceptions.SecuboidCommandException;
import me.tabinol.secuboidapi.SecuboidAPI;
import me.tabinol.secuboidapi.lands.types.IType;

import org.bukkit.ChatColor;

@InfoCommand(name="type", forceParameter=true)
public class CommandType extends CommandExec {
	
	public CommandType(CommandEntities entity) throws SecuboidCommandException {

        super(entity);
    }

    @Override
    public void commandExecute() throws SecuboidCommandException {

        checkSelections(true, null);
        checkPermission(true, false, null, null);
        
        String curArg = entity.argList.getNext();

        if (curArg.equalsIgnoreCase("list")) {
            
            StringBuilder stList = new StringBuilder();
            for (IType type : SecuboidAPI.iTypes().getTypes()) {
            	if (stList.length() != 0) {
            		stList.append(" ");
                }
                stList.append(ChatColor.WHITE).append(type.getName());
            stList.append(Config.NEWLINE);
            }
            new ChatPage("COMMAND.TYPES.LISTSTART", stList.toString(), entity.player, null).getPage(1);
        
        } else if(curArg.equals("remove")) {
        	
        	land.setType(null);
            entity.player.sendMessage(ChatColor.YELLOW + "[Secuboid] " + Secuboid.getThisPlugin().iLanguage().getMessage("COMMAND.TYPES.REMOVEISDONE", land.getName()));
            Secuboid.getThisPlugin().iLog().write("Land type removed: " + land.getName());
        
        } else { // Type change 
        	
        	IType type = SecuboidAPI.iTypes().getType(curArg);
        	
        	if(type == null) {
        		throw new SecuboidCommandException("Land Types", entity.player, "COMMAND.TYPES.INVALID");
        	}
        	
        	land.setType(type);
            entity.player.sendMessage(ChatColor.YELLOW + "[Secuboid] " + Secuboid.getThisPlugin().iLanguage().getMessage("COMMAND.TYPES.ISDONE", type.getName(), land.getName()));
            Secuboid.getThisPlugin().iLog().write("Land type: " + type.getName() + " for land: " + land.getName());
        }
    }
}
