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
package me.tabinol.secuboid.permissionsflags;

import me.tabinol.secuboid.Secuboid;
import me.tabinol.secuboid.utilities.StringChanges;

/**
 * The Class Flag.
 */
public class Flag {

    /**
     * The flag type.
     */
    private final FlagType flagType;

    /**
     * The value.
     */
    private FlagValue value = null;

    /**
     * The inheritable.
     */
    private final boolean inheritable;

    /**
     * Instantiates a new land flag.
     *
     * @param flagType the flag type
     * @param value the value
     * @param inheritable the inheritable
     */
    public Flag(final FlagType flagType, final Object value, final boolean inheritable) {

	this.flagType = flagType;
	if (value instanceof FlagValue) {
	    this.value = (FlagValue) value;
	} else {
	    this.value = new FlagValue(value);
	}
	this.inheritable = inheritable;

	if (!flagType.isRegistered()) {
	    Secuboid.getThisPlugin().getPermissionsFlags().unRegisteredFlags.add(this);
	}
    }

    /**
     *
     * @return
     */
    public Flag copyOf() {

	return new Flag(flagType, value.copyOf(), inheritable);
    }

    /**
     * Equals.
     *
     * @param lf2 the lf2
     * @return true, if successful
     */
    public boolean equals(Flag lf2) {

	return flagType == lf2.getFlagType();
    }

    /**
     * Gets the flag type.
     *
     * @return the flag type
     */
    public final FlagType getFlagType() {

	return flagType;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public final FlagValue getValue() {

	return value;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    protected void setValue(FlagValue value) {

	this.value = value;
    }

    /**
     * Checks if is Inheritable.
     *
     * @return true, if is Inheritable
     */
    public boolean isInheritable() {

	return inheritable;
    }

    /**
     *
     * @return
     */
    public String toFileFormat() {

	if (!flagType.isRegistered()) {
	    return flagType.toString() + ":" + value.getValue() + ":" + inheritable;
	}

	if (value.getValue() instanceof Boolean) {
	    return flagType.toString() + ":" + value.getValueBoolean() + ":" + inheritable;
	}

	if (value.getValue() instanceof Double) {
	    return flagType.toString() + ":" + value.getValueDouble() + ":" + inheritable;
	}

	if (value.getValue() instanceof String) {
	    return flagType.toString() + ":" + StringChanges.toQuote(value.getValueString()) + ":" + inheritable;
	}

	if (value.getValue() instanceof String[]) {
	    StringBuilder sb = new StringBuilder();
	    for (String st : value.getValueStringList()) {
		sb.append(StringChanges.toQuote(st)).append(";");
	    }
	    return flagType.toString() + ":" + sb.toString() + ":" + inheritable;
	}

	return null;
    }
}
