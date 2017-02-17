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
package me.tabinol.secuboid.lands.areas;

import java.util.*;

/**
 * Represents a gride of point for roads.
 */
public class RegionMatrix {

    /**
     * points is represented: RegionX: RegionZ: chunkMatrix
     */
    private final Map<Integer, Map<Integer, ChunkMatrix>> points;

    /**
     * Creates a new region matrix for roads.
     */
    public RegionMatrix() {
        points = new HashMap<Integer, Map<Integer, ChunkMatrix>>();
    }

    /**
     * Creates a new region. Only for copyOf() and from save files.
     */
    RegionMatrix(Map<Integer, Map<Integer, ChunkMatrix>> points) {
        this.points = points;
    }

    Map<Integer, Map<Integer, ChunkMatrix>> getPoints() {
        return points;
    }

    /**
     * Adds a point in chunk matrix.
     *
     * @param x the x position
     * @param z the z position
     */
    public void addPoint(int x, int z) {
        addRemovePoint(true, x, z);
    }

    /**
     * Remove a point in chunk matrix.
     *
     * @param x the x position
     * @param z the z position
     */
    void removePoint(int x, int z) {
        addRemovePoint(false, x, z);
    }

    /**
     * Adds or removes a point.
     *
     * @param isAdd true = adds, flase = removes
     * @param x     the x
     * @param z     the z
     */
    private void addRemovePoint(boolean isAdd, int x, int z) {
        // From region X
        int chunkX = (int) Math.floor(x / 16);
        Map<Integer, ChunkMatrix> pRegionZ = points.get(chunkX);
        if (pRegionZ == null) {
            pRegionZ = new HashMap<Integer, ChunkMatrix>();
            points.put(chunkX, pRegionZ);
        }

        // From region Z
        int chunkZ = (int) Math.floor(z / 16);
        ChunkMatrix matrix = pRegionZ.get(chunkZ);
        if (matrix == null) {
            matrix = new ChunkMatrix();
            pRegionZ.put(chunkZ, matrix);
        }

        // Add to matrix
        byte posX = getChunkPos(x);
        byte posZ = getChunkPos(z);
        if (isAdd) {
            matrix.addPoint(posX, posZ);
        } else {
            matrix.removePoint(posX, posZ);
            if (matrix.isEmpty()) {
                pRegionZ.remove(chunkZ);
                if (pRegionZ.isEmpty()) {
                    points.remove(chunkX);
                }
            }
        }
    }

    /**
     * Gets de point value.
     *
     * @param x the x position
     * @param z the z position
     * @return boolean point value
     */
    boolean getPoint(int x, int z) {

        // From region X
        int chunkX = (int) Math.floor(x / 16);
        Map<Integer, ChunkMatrix> pRegionZ = points.get(chunkX);
        if (pRegionZ == null) {
            return false;
        }

        // From region Z
        int chunkZ = (int) Math.floor(z / 16);
        ChunkMatrix matrix = pRegionZ.get(chunkZ);
        if (matrix == null) {
            return false;
        }

        // Add to matrix
        byte posX = getChunkPos(x);
        byte posZ = getChunkPos(z);
        return matrix.getPoint(posX, posZ);
    }

    /**
     * Gets the x or the z and return the correct position in the chunk
     *
     * @param value the value (x or z)
     * @return the position (0-16)
     */
    private byte getChunkPos(int value) {
        int modul = value % 16;
        if (modul < 0) {
            return (byte) (16 + modul);
        } else {
            return (byte) modul;
        }
    }

    /**
     * Is the matrix empty?
     *
     * @return true or false
     */
    public boolean isEmpty() {
        return points.isEmpty();
    }

    /**
     * Count the number of points.
     *
     * @return the number of points
     */
    public long countPoints() {
        long nbPoints = 0;
        for (Map<Integer, ChunkMatrix> pRegionZ : points.values()) {
            for (ChunkMatrix matrix : pRegionZ.values()) {
                nbPoints += matrix.countPoints();
            }
        }
        return nbPoints;
    }

    public String toFileFormat() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Map<Integer, ChunkMatrix>> entryX : points.entrySet()) {
            for (Map.Entry<Integer, ChunkMatrix> entryZ : entryX.getValue().entrySet()) {
                sb.append(':').append(entryX.getKey()).append(':').append(entryZ.getKey()).append(':')
                        .append(entryZ.getValue().toFileFormat());
            }
        }
        return sb.toString();
    }

    public RegionMatrix copyOf() {
        Map<Integer, Map<Integer, ChunkMatrix>> newPoints = new HashMap<Integer, Map<Integer, ChunkMatrix>>();
        for (Map.Entry<Integer, Map<Integer, ChunkMatrix>> entryX : points.entrySet()) {
            Map<Integer, ChunkMatrix> newPointsZ = new HashMap<Integer, ChunkMatrix>();
            for (Map.Entry<Integer, ChunkMatrix> entryZ : entryX.getValue().entrySet()) {
                if (!entryZ.getValue().isEmpty()) {
                    newPointsZ.put(entryZ.getKey(), entryZ.getValue().copyOf());
                }
            }
            if (!newPointsZ.isEmpty()) {
                newPoints.put(entryX.getKey(), newPointsZ);
            }
        }
        return new RegionMatrix(newPoints);
    }
}
