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

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public final class DbUtils {

    @FunctionalInterface
    public static interface SqlBiConsumer<T, U> {
        void accept(T t, U u) throws SQLException;
    }

    @FunctionalInterface
    public static interface SqlFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    private DbUtils() {
    }

    public static void setUUID(final PreparedStatement stmt, final int parameterIndex, final UUID uuid)
            throws SQLException {
        final ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        stmt.setBytes(parameterIndex, bb.array());
    }

    public static UUID getUUID(final ResultSet rs, final String columnLabel) throws SQLException {
        final byte[] bytes = rs.getBytes(columnLabel);
        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        final Long high = byteBuffer.getLong();
        final Long low = byteBuffer.getLong();
        return new UUID(high, low);
    }

    public static <R> Optional<R> getOpt(final ResultSet rs, final String columnLabel,
            final SqlFunction<String, R> supplier) throws SQLException {
        final R r = supplier.apply(columnLabel);
        if (rs.wasNull()) {
            return null;
        }
        return Optional.of(r);
    }
}