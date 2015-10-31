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

package me.tabinol.secuboid.flycreative;

import me.tabinol.secuboid.Secuboid;
import me.tabinol.secuboid.config.Config;
import me.tabinol.secuboid.listeners.FlyCreativeListener;
import me.tabinol.secuboidapi.ApiSecuboidSta;
import me.tabinol.secuboidapi.events.PlayerLandChangeEvent;
import me.tabinol.secuboidapi.lands.ApiDummyLand;
import me.tabinol.secuboidapi.parameters.ApiPermissionType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;

public class Creative {

    public final static String CREATIVE_IGNORE_PERM = "secuboid.flycreative.ignorecreative";
    public final static String OVERRIDE_NODROP_PERM = "secuboid.flycreative.override.nodrop";
    public final static String OVERRIDE_NOOPENCHEST_PERM = "secuboid.flycreative.override.noopenchest";
    public final static String OVERRIDE_NOBUILDOUTSIDE_PERM = "secuboid.flycreative.override.nobuildoutside";
    public final static String OVERRIDE_BANNEDITEMS_PERM = "secuboid.flycreative.override.allowbanneditems";
    private final Config conf;
    private final ApiPermissionType permissionType;
    private final FlyCreativeListener flyCreativeListener;

    public Creative() {

        conf = Secuboid.getThisPlugin().getConf();

        // Register flags
        permissionType = ApiSecuboidSta.getParameters().registerPermissionType("CREATIVE", false);

        flyCreativeListener = Secuboid.getThisPlugin().getFlyCreativeListener();
    }

    public boolean creative(Event event, Player player, ApiDummyLand dummyLand) {

        if (!player.hasPermission(CREATIVE_IGNORE_PERM)) {
            if (askCreativeFlag(player, dummyLand)) {
                if (player.getGameMode() != GameMode.CREATIVE) {
                    flyCreativeListener.addIgnoredGMPlayers(player);
                	player.setGameMode(GameMode.CREATIVE);
                }
            } else {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    if (player.isFlying() && event instanceof PlayerLandChangeEvent
                            && !((PlayerLandChangeEvent) event).isTp()) {
                        // Return the player in the last cuboid if he is flying.
                        ((PlayerLandChangeEvent) event).setCancelled(true);
                    } else {
                        flyCreativeListener.addIgnoredGMPlayers(player);
                        player.setGameMode(GameMode.SURVIVAL);
                    }
                }
            }
        }

        return player.getGameMode() == GameMode.CREATIVE;
    }

    public void setGM(Player player, GameMode gm) {

        if (!player.hasPermission(CREATIVE_IGNORE_PERM)) {
            flyCreativeListener.addIgnoredGMPlayers(player);
            player.setGameMode(gm);
        }
    }

    public boolean dropItem(PlayerDropItemEvent event, Player player) {

        if (conf.isCreativeNoDrop()
                && !player.hasPermission(OVERRIDE_NODROP_PERM)) {

            return true;
        }
        
        return false;
    }

    public void invOpen(InventoryOpenEvent event, HumanEntity player) {

        if (conf.isCreativeNoOpenChest()
                && !player.hasPermission(OVERRIDE_NOOPENCHEST_PERM)) {

            InventoryType it = event.getView().getType();

            if (it == InventoryType.CHEST || it == InventoryType.DISPENSER
                    || it == InventoryType.DROPPER || it == InventoryType.ENDER_CHEST
                    || it == InventoryType.FURNACE || it == InventoryType.HOPPER) {

                event.setCancelled(true);
            }
        }
    }

    // Return «true» if the events must be cancelled
    public boolean build(Event event, Player player) {

        Location blockLoc;

        if (conf.isCreativeNoBuildOutside()
                && !player.hasPermission(OVERRIDE_NOBUILDOUTSIDE_PERM)) {

            if (event instanceof BlockBreakEvent) {
                blockLoc = ((BlockBreakEvent) event).getBlock().getLocation();
            } else {
                blockLoc = ((BlockPlaceEvent) event).getBlockPlaced().getLocation();
            }
            if (!askCreativeFlag(player, ApiSecuboidSta.getLands().getLandOrOutsideArea(blockLoc))) {

                return true;
            }
        }
        return false;
    }

    public void checkBannedItems(InventoryCloseEvent event, HumanEntity player) {

        if (!player.hasPermission(OVERRIDE_BANNEDITEMS_PERM)) {

            for(Material mat : conf.getCreativeBannedItems()) {
                event.getPlayer().getInventory().remove(mat);
            }
        }
    }

    private boolean askCreativeFlag(Player player, ApiDummyLand dummyLand) {

        return dummyLand.checkPermissionAndInherit(player, permissionType);
    }
}
