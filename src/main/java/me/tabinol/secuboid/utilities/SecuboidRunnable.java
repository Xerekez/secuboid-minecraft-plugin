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
package me.tabinol.secuboid.utilities;

import me.tabinol.secuboid.Secuboid;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Schedule task in Secuboid.
 *
 * @author Tabinol
 */
public abstract class SecuboidRunnable extends BukkitRunnable {

    protected final Secuboid secuboid;

    /**
     * The task id.
     */
    private BukkitTask taskId = null;

    /**
     * Instantiates a new secuboid runnable.
     *
     * @param secuboid the secuboid instance
     */
    public SecuboidRunnable(Secuboid secuboid) {

        super();
        this.secuboid = secuboid;
        taskId = null;
    }

    /**
     * Run later.
     *
     * @param tick     the tick
     * @param multiple the multiple
     */
    public void runLater(Long tick, boolean multiple) {

        stopNextRun();

        if (multiple) {
            taskId = Bukkit.getServer().getScheduler().runTaskLater(secuboid, (Runnable) this, tick);

        } else {
            taskId = Bukkit.getServer().getScheduler().runTaskLater(secuboid, (Runnable) this, tick);
        }
    }

    /**
     * Checks if is active.
     *
     * @return true, if is active
     */
    public boolean isActive() {
        return taskId != null;
    }

    /**
     * Sets the one time done. IF IT IS NOT MULTIPLE RUN, YOU NEED TO SET DONE IN RUN() METHOD
     */
    protected void setOneTimeDone() {
        taskId = null;
    }

    /**
     * Stop next run.
     */
    public void stopNextRun() {
        if (taskId != null) {

            taskId.cancel();
            taskId = null;
        }
    }

}
