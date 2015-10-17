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

import me.tabinol.secuboid.parameters.FlagList;
import me.tabinol.secuboidapi.lands.ILand;
import me.tabinol.secuboidapi.playercontainer.EPlayerContainerType;
import me.tabinol.secuboidapi.playercontainer.IPlayerContainer;
import me.tabinol.secuboidapi.playercontainer.IPlayerContainerOwner;

import org.bukkit.entity.Player;


/**
 * The Class PlayerContainerOwner.
 */
public class PlayerContainerOwner extends PlayerContainer implements IPlayerContainerOwner {

    /** The land. */
    private ILand land;
    
    /**
     * Instantiates a new player container owner.
     *
     * @param land the land
     */
    public PlayerContainerOwner(ILand land) {
        
        super("", EPlayerContainerType.OWNER, false);
        this.land = land;
    }
    
    /* (non-Javadoc)
     * @see me.tabinol.secuboid.playercontainer.PlayerContainerInterface#equals(me.tabinol.secuboid.playercontainer.PlayerContainer)
     */
    @Override
    public boolean equals(IPlayerContainer container2) {
        
        return container2 instanceof PlayerContainerOwner &&
                land == ((PlayerContainerOwner)container2).land;
    }

    /* (non-Javadoc)
     * @see me.tabinol.secuboid.playercontainer.PlayerContainerInterface#copyOf()
     */
    @Override
    public PlayerContainer copyOf() {
        
        return new PlayerContainerOwner(land);
    }

    /* (non-Javadoc)
     * @see me.tabinol.secuboid.playercontainer.PlayerContainerInterface#hasAccess(org.bukkit.entity.Player)
     */
    @Override
    public boolean hasAccess(Player player) {
    	
    	return hasAccess(player, land);
    }
        
    @Override
    public boolean hasAccess(Player player, ILand land) {
        
        boolean value;
        ILand parent;
    	
    	if(land == null) {
    		return false;
    	}
        
    	value = land.getOwner().hasAccess(player);
    	
    	if(!value && (parent = land.getParent()) != null 
    			&& land.getFlagAndInherit(FlagList.INHERIT_OWNER.getFlagType()).getValueBoolean() == true) {
    		
    		return parent.getOwner().hasAccess(player);
    	}
    	
    	return value;
    }


    /**
     * Gets the land.
     *
     * @return the land
     */
    public ILand getLand() {
        
        return land;
    }

    /* (non-Javadoc)
     * @see me.tabinol.secuboid.playercontainer.PlayerContainerInterface#setLand(me.tabinol.secuboid.lands.Land)
     */
    @Override
    public void setLand(ILand land) {

        this.land = land;
    }
}
