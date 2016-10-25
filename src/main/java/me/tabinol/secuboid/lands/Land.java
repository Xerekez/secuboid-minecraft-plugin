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
package me.tabinol.secuboid.lands;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import me.tabinol.secuboid.Secuboid;
import me.tabinol.secuboid.events.LandModifyEvent;
import me.tabinol.secuboid.events.LandModifyEvent.LandModifyReason;
import me.tabinol.secuboid.events.PlayerContainerAddNoEnterEvent;
import me.tabinol.secuboid.events.PlayerContainerLandBanEvent;
import me.tabinol.secuboid.lands.areas.Area;
import me.tabinol.secuboid.lands.areas.AreaType;
import me.tabinol.secuboid.lands.types.Type;
import me.tabinol.secuboid.permissionsflags.FlagType;
import me.tabinol.secuboid.permissionsflags.FlagValue;
import me.tabinol.secuboid.permissionsflags.Flag;
import me.tabinol.secuboid.permissionsflags.Permission;
import me.tabinol.secuboid.permissionsflags.PermissionList;
import me.tabinol.secuboid.permissionsflags.PermissionType;
import me.tabinol.secuboid.playercontainer.PlayerContainer;
import me.tabinol.secuboid.playercontainer.PlayerContainerNobody;
import me.tabinol.secuboid.playercontainer.PlayerContainerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * The Class Land.
 */
public final class Land implements Comparable<Land> {

    /**
     * The Constant DEFAULT_PRIORITY.
     */
    public static final short DEFAULT_PRIORITY = 10;

    /**
     * The Constant MINIM_PRIORITY.
     */
    public static final short MINIM_PRIORITY = 0;

    /**
     * The Constant MAXIM_PRIORITY.
     */
    public static final short MAXIM_PRIORITY = 100;

    /**
     * The uuid.
     */
    private final UUID uuid;

    /**
     * The name.
     */
    private String name;

    /**
     * The type.
     */
    private Type type = null;

    /**
     * The areas.
     */
    private final Map<Integer, Area> areas = new TreeMap<Integer, Area>();

    /**
     * The children.
     */
    private final Map<UUID, Land> children = new TreeMap<UUID, Land>();

    /**
     * The priority.
     */
    private short priority = DEFAULT_PRIORITY; // Do not put more then 100000!!!!

    /**
     * The genealogy.
     */
    private int genealogy = 0; // 0 = first, 1 = child, 2 = child of child, ...

    /**
     * The parent.
     */
    private Land parent = null;

    /**
     * The world name. <br>
     * Why using the world string name, but not World instance or UUID? <br>
     * The uuid or the World instance can be changed if there is a land regen.
     */
    private final String worldName;

    /**
     * The owner.
     */
    private PlayerContainer owner;

    /**
     * The permissions.
     */
    private TreeMap<PlayerContainer, TreeMap<PermissionType, Permission>> permissions;

    /**
     * The flags.
     */
    private TreeMap<FlagType, Flag> flags;

    /**
     * The residents.
     */
    private Set<PlayerContainer> residents = new TreeSet<PlayerContainer>();

    /**
     * The banneds.
     */
    private final Set<PlayerContainer> banneds = new TreeSet<PlayerContainer>();

    /**
     * The auto save.
     */
    private boolean autoSave = true;

    /**
     * The money.
     */
    private double money = 0L;

    /**
     * The player notify.
     */
    private Set<PlayerContainerPlayer> playerNotify = new TreeSet<PlayerContainerPlayer>();

    /**
     * The players in land.
     */
    private final Set<Player> playersInLand = new HashSet<Player>();
    // Economy
    /**
     * The for sale.
     */
    private boolean forSale = false;

    /**
     * The for sale sign location
     */
    private Location forSaleSignLoc = null;

    /**
     * The sale price.
     */
    private double salePrice = 0;

    /**
     * The for rent.
     */
    private boolean forRent = false;

    /**
     * The for rent sign location
     */
    private Location forRentSignLoc = null;

    /**
     * The rent price.
     */
    private double rentPrice = 0;

    /**
     * The rent renew.
     */
    private int rentRenew = 0; // How many days before renew?

    /**
     * The rent auto renew.
     */
    private boolean rentAutoRenew = false;

    /**
     * The rented.
     */
    private boolean rented = false;

    /**
     * The tenant.
     */
    private PlayerContainerPlayer tenant = null;

    /**
     * The last payment.
     */
    private Timestamp lastPayment = new Timestamp(0);

    /**
     * Instantiates a new land. <br>
     * IMPORTANT: Please use createLand in Lands class to create a Land or it will not be accessible.
     *
     * @param landName the land name
     * @param uuid the uuid
     * @param owner the owner
     * @param area the area
     * @param genealogy the genealogy
     * @param parent the parent
     * @param areaId the area id
     * @param type the type
     */
    public Land(String landName, UUID uuid, PlayerContainer owner,
	    Area area, int genealogy, Land parent, int areaId, Type type) {

	this.uuid = uuid;
	name = landName.toLowerCase();
	this.type = type;
	if (parent != null) {
	    this.parent = parent;
	    parent.addChild((Land) this);
	}
	this.owner = owner;
	this.genealogy = genealogy;
	permissions = new TreeMap<PlayerContainer, TreeMap<PermissionType, Permission>>();
	flags = new TreeMap<FlagType, Flag>();
	worldName = area.getWorldName();
	addArea(area, areaId);
    }

    /**
     * Sets the land default values
     */
    public void setDefault() {
	owner = new PlayerContainerNobody();
	residents = new TreeSet<PlayerContainer>();
	playerNotify = new TreeSet<PlayerContainerPlayer>();
	permissions = new TreeMap<PlayerContainer, TreeMap<PermissionType, Permission>>();
	flags = new TreeMap<FlagType, Flag>();
	doSave();
    }

    /**
     * Is a global land?
     *
     * @return True is the land is global
     */
    public boolean isGlobal() {
	return areas.get(1).getAreaType() == AreaType.INFINITE;
    }

    /**
     * Adds the area.
     *
     * @param area the area
     */
    public void addArea(Area area) {

	int nextKey = 0;

	if (areas.isEmpty()) {
	    nextKey = 1;
	} else {
	    for (int key : areas.keySet()) {

		if (nextKey < key) {
		    nextKey = key;
		}
	    }
	    nextKey++;
	}

	addArea(area, nextKey);
    }

    /**
     * Adds the area.
     *
     * @param area the area
     * @param price the price
     */
    public void addArea(Area area, double price) {

	if (price > 0) {
	    Secuboid.getThisPlugin().getLands().getPriceFromPlayer(worldName, owner, price);
	}
	addArea(area);
    }

    /**
     * Adds the area.
     *
     * @param area the area
     * @param key the key
     */
    public void addArea(Area area, int key) {

	((Area) area).setLand(this);
	areas.put(key, area);
	if (Secuboid.getThisPlugin() != null) {
	    Secuboid.getThisPlugin().getLands().addAreaToList(area);
	}
	doSave();

	// Start Event
	if (Secuboid.getThisPlugin() != null) {
	    Secuboid.getThisPlugin().getServer().getPluginManager().callEvent(
		    new LandModifyEvent(this, LandModifyReason.AREA_ADD, area));
	}
    }

    /**
     * Removes the area.
     *
     * @param key the key
     * @return true, if successful
     */
    public boolean removeArea(int key) {

	Area area;

	if ((area = areas.remove(key)) != null) {
	    Secuboid.getThisPlugin().getLands().removeAreaFromList(area);
	    doSave();

	    // Start Event
	    Secuboid.getThisPlugin().getServer().getPluginManager().callEvent(
		    new LandModifyEvent(this, LandModifyReason.AREA_REMOVE, area));

	    return true;
	}

	return false;
    }

    /**
     * Removes the area.
     *
     * @param area the area
     * @return true, if successful
     */
    public boolean removeArea(Area area) {

	Integer key = getAreaKey(area);

	if (key != null) {
	    return removeArea(key);
	}

	return false;
    }

    /**
     * Replace area.
     *
     * @param key the key
     * @param newArea the new area
     * @param price the price
     * @return true, if successful
     */
    public boolean replaceArea(int key, Area newArea, double price) {

	if (price > 0) {
	    Secuboid.getThisPlugin().getLands().getPriceFromPlayer(worldName, owner, price);
	}

	return replaceArea(key, newArea);
    }

    /**
     * Replace area.
     *
     * @param key the key
     * @param newArea the new area
     * @return true, if successful
     */
    public boolean replaceArea(int key, Area newArea) {

	Area area;

	if ((area = areas.remove(key)) != null) {
	    Secuboid.getThisPlugin().getLands().removeAreaFromList(area);
	    ((Area) newArea).setLand(this);
	    areas.put(key, newArea);
	    Secuboid.getThisPlugin().getLands().addAreaToList(newArea);
	    doSave();

	    // Start Event
	    Secuboid.getThisPlugin().getServer().getPluginManager().callEvent(
		    new LandModifyEvent(this, LandModifyReason.AREA_REPLACE, area));

	    return true;
	}

	return false;
    }

    /**
     * Gets the area.
     *
     * @param key the key
     * @return the area
     */
    public Area getArea(int key) {

	return areas.get(key);
    }

    /**
     * Gets the area key.
     *
     * @param area the area
     * @return the area key
     */
    public Integer getAreaKey(Area area) {

	for (Map.Entry<Integer, Area> entry : areas.entrySet()) {
	    if (entry.getValue() == area) {
		return entry.getKey();
	    }
	}

	return null;
    }

    /**
     * Gets the areas key.
     *
     * @return the areas key
     */
    public Set<Integer> getAreasKey() {

	return areas.keySet();
    }

    /**
     * Gets the ids and areas.
     *
     * @return the ids and areas
     */
    public Map<Integer, Area> getIdsAndAreas() {

	return areas;
    }

    /**
     * Gets the areas.
     *
     * @return the areas
     */
    public Collection<Area> getAreas() {

	return areas.values();
    }

    /**
     * Gets the world.
     *
     * @return the world
     */
    public World getWorld() {

	return Bukkit.getWorld(worldName);
    }

    /**
     * Gets the world name.
     *
     * @return the world name
     */
    public String getWorldName() {

	return worldName;
    }

    /**
     *
     * @param loc
     * @return
     */
    public boolean isLocationInside(Location loc) {

	return isLocationInside(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @return
     */
    public boolean isLocationInside(String world, int x, int y, int z) {

	for (Area area1 : areas.values()) {
	    if (area1.isLocationInside(world, x, y, z)) {
		return true;
	    }
	}

	return false;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {

	return name;
    }

    /**
     * Gets the uuid.
     *
     * @return the uuid
     */
    public UUID getUUID() {

	return uuid;
    }

    /**
     * Sets the name.
     *
     * @param newName the new name
     */
    protected void setName(String newName) {

	setAutoSave(false);
	Secuboid.getThisPlugin().getStorageThread().removeLand(this);
	this.name = newName;
	setAutoSave(true);
	doSave();

	// Start Event
	Secuboid.getThisPlugin().getServer().getPluginManager().callEvent(
		new LandModifyEvent(this, LandModifyReason.RENAME, name));
    }

    /**
     * Gets the owner.
     *
     * @return the owner
     */
    public PlayerContainer getOwner() {

	return owner;
    }

    /**
     * Checks if is owner.
     *
     * @param player the player
     * @return true, if is owner
     */
    public boolean isOwner(Player player) {

	return owner.hasAccess(player);
    }

    /**
     * Sets the owner.
     *
     * @param owner the new owner
     */
    public void setOwner(PlayerContainer owner) {

	this.owner = owner;
	doSave();

	// Start Event
	Secuboid.getThisPlugin().getServer().getPluginManager().callEvent(
		new LandModifyEvent(this, LandModifyReason.OWNER_CHANGE, owner));
    }

    /**
     * Adds the resident.
     *
     * @param resident the resident
     */
    public void addResident(PlayerContainer resident) {

	((PlayerContainer) resident).setLand(this);
	residents.add(resident);
	doSave();

	// Start Event
	Secuboid.getThisPlugin().getServer().getPluginManager().callEvent(
		new LandModifyEvent(this, LandModifyReason.RESIDENT_ADD, resident));
    }

    /**
     * Removes the resident.
     *
     * @param resident the resident
     * @return true, if successful
     */
    public boolean removeResident(PlayerContainer resident) {

	if (residents.remove(resident)) {
	    doSave();

	    // Start Event
	    Secuboid.getThisPlugin().getServer().getPluginManager().callEvent(
		    new LandModifyEvent(this, LandModifyReason.RESIDENT_REMOVE, resident));

	    return true;
	}

	return false;
    }

    /**
     * Gets the residents.
     *
     * @return the residents
     */
    public final Set<PlayerContainer> getResidents() {

	return residents;
    }

    /**
     * Checks if is resident.
     *
     * @param player the player
     * @return true, if is resident
     */
    public boolean isResident(Player player) {

	for (PlayerContainer resident : residents) {
	    if (resident.hasAccess(player)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Adds the banned.
     *
     * @param banned the banned
     */
    public void addBanned(PlayerContainer banned) {

	((PlayerContainer) banned).setLand(this);
	banneds.add(banned);
	doSave();

	// Start Event
	Secuboid.getThisPlugin().getServer().getPluginManager().callEvent(
		new PlayerContainerLandBanEvent(this, banned));
    }

    /**
     * Removes the banned.
     *
     * @param banned the banned
     * @return true, if successful
     */
    public boolean removeBanned(PlayerContainer banned) {

	if (banneds.remove(banned)) {
	    doSave();
	    return true;
	}

	return false;
    }

    /**
     * Gets the banneds.
     *
     * @return the banneds
     */
    public final Set<PlayerContainer> getBanneds() {

	return banneds;
    }

    /**
     * Checks if is banned.
     *
     * @param player the player
     * @return true, if is banned
     */
    public boolean isBanned(Player player) {

	for (PlayerContainer banned : banneds) {
	    if (banned.hasAccess(player)) {
		return true;
	    }
	}
	return false;
    }

    // Note : a child get the parent priority
    /**
     * Gets the priority.
     *
     * @return the priority
     */
    public short getPriority() {

	if (parent != null) {
	    return parent.getPriority();
	}

	return priority;
    }

    /**
     * Gets the genealogy.
     *
     * @return the genealogy
     */
    public int getGenealogy() {

	return genealogy;
    }

    /**
     * Sets the priority.
     *
     * @param priority the new priority
     */
    public void setPriority(short priority) {

	this.priority = priority;
	doSave();
    }

    /**
     * Gets the parent.
     *
     * @return the parent
     */
    public Land getParent() {

	return (Land) parent;
    }

    /**
     *
     * @param newParent
     */
    public void setParent(Land newParent) {

	// Remove files
	removeChildFiles();
	Secuboid.getThisPlugin().getStorageThread().removeLand(name, genealogy);

	// remove parent (if needed)
	if (parent != null) {
	    ((Land) parent).removeChild(uuid);
	    parent = null;
	    genealogy = 0;
	    Secuboid.getThisPlugin().getLog().write("remove parent from land: " + name);
	}

	// Add parent
	if (newParent != null) {
	    ((Land) newParent).addChild(this);
	    parent = newParent;
	    priority = parent.getPriority();
	    genealogy = parent.getGenealogy() + 1;
	    Secuboid.getThisPlugin().getLog().write("add parent " + parent.getName() + " to land: " + name);
	}

	// Save
	doSave();

	// Save children files
	saveChildFiles();
    }

    private void removeChildFiles() {

	for (Land child : children.values()) {
	    child.setAutoSave(false);
	    Secuboid.getThisPlugin().getStorageThread().removeLand((Land) child);
	    ((Land) child).removeChildFiles();
	}
    }

    private void saveChildFiles() {

	for (Land child : children.values()) {
	    child.setPriority(priority);
	    ((Land) child).genealogy = genealogy + 1;
	    child.setAutoSave(true);
	    child.forceSave();
	    ((Land) child).saveChildFiles();
	}
    }

    /**
     * Gets the ancestor.
     *
     * @param gen the gen
     * @return the ancestor
     */
    public Land getAncestor(int gen) { // 1 parent, 2 grand-parent, 3 ...

	Land ancestor = this;

	for (int t = 0; t < gen; t++) {
	    ancestor = ancestor.getParent();
	}

	return ancestor;
    }

    /**
     * Checks if is descendants.
     *
     * @param land the land
     * @return true, if is descendants
     */
    public boolean isDescendants(Land land) {

	if (land == this) {
	    return true;
	}

	for (Land landT : children.values()) {
	    if (landT.isDescendants(land) == true) {
		return true;
	    }
	}

	return false;
    }

    /**
     * Adds the child.
     *
     * @param land the land
     */
    private void addChild(Land land) {

	children.put(land.uuid, land);
	doSave();
    }

    /**
     * Removes the child.
     *
     * @param uuid the uuid
     */
    protected void removeChild(UUID uuid) {

	children.remove(uuid);
	doSave();
    }

    /**
     * Gets the child.
     *
     * @param uuid the uuid
     * @return the child
     */
    public Land getChild(UUID uuid) {

	return (Land) children.get(uuid);
    }

    /**
     * Gets the children.
     *
     * @return the children
     */
    public Collection<Land> getChildren() {

	return children.values();
    }

    /**
     * Sets the auto save.
     *
     * @param autoSave the new auto save
     */
    public void setAutoSave(boolean autoSave) {

	this.autoSave = autoSave;
    }

    /**
     * Force save.
     */
    public void forceSave() {

	if (Secuboid.getThisPlugin() != null) {
	    Secuboid.getThisPlugin().getStorageThread().saveLand(this);
	}
    }

    /* (non-Javadoc)
     * @see me.tabinol.secuboid.lands.DummyLand#doSave()
     */
    protected void doSave() {

	if (autoSave) {
	    forceSave();
	}
    }

    /**
     *
     * @param desLand
     */
    public void copyPermsFlagsTo(Land desLand) {

	// copy permissions
	for (Map.Entry<PlayerContainer, TreeMap<PermissionType, Permission>> pcEntry : permissions.entrySet()) {

	    TreeMap<PermissionType, Permission> perms = new TreeMap<PermissionType, Permission>();
	    for (Map.Entry<PermissionType, Permission> permEntry : pcEntry.getValue().entrySet()) {
		perms.put(permEntry.getKey(), permEntry.getValue().copyOf());
	    }
	    desLand.permissions.put(pcEntry.getKey(), perms);
	}

	// copy flags
	for (Map.Entry<FlagType, Flag> flagEntry : flags.entrySet()) {

	    desLand.flags.put(flagEntry.getKey(), flagEntry.getValue().copyOf());
	}
    }

    /* (non-Javadoc)
     * @see me.tabinol.secuboidapi.lands.ApiLand#addPermission(me.tabinol.secuboidapi.playercontainer.ApiPlayerContainer, me.tabinol.secuboidapi.parameters.ApiPermissionType, boolean, boolean)
     */
    /**
     *
     * @param pc
     * @param permType
     * @param value
     * @param inheritance
     */
    public void addPermission(PlayerContainer pc, PermissionType permType,
	    boolean value, boolean inheritance) {

	addPermission(pc, new Permission((PermissionType) permType, value, inheritance));
    }

    /* (non-Javadoc)
     * @see me.tabinol.secuboidapi.lands.ApiLand#addFlag(me.tabinol.secuboidapi.parameters.ApiFlagType, java.lang.Object, boolean)
     */
    /**
     *
     * @param flagType
     * @param value
     * @param inheritance
     */
    public void addFlag(FlagType flagType, Object value, boolean inheritance) {

	addFlag(new Flag((FlagType) flagType, value, inheritance));
    }

    /**
     * Adds the permission.
     *
     * @param pc the pc
     * @param perm the perm
     */
    public void addPermission(PlayerContainer pc, Permission perm) {

	TreeMap<PermissionType, Permission> permPlayer;

	pc.setLand(this);

	if (!permissions.containsKey(pc)) {
	    permPlayer = new TreeMap<PermissionType, Permission>();
	    permissions.put(pc, permPlayer);
	} else {
	    permPlayer = permissions.get(pc);
	}
	permPlayer.put(perm.getPermType(), perm);
	doSave();

	if (perm.getPermType() == PermissionList.LAND_ENTER.getPermissionType()
		&& perm.getValue() != perm.getPermType().getDefaultValue()) {

	    // Start Event for kick
	    Secuboid.getThisPlugin().getServer().getPluginManager().callEvent(
		    new PlayerContainerAddNoEnterEvent(this, pc));
	}

	// Start Event
	Secuboid.getThisPlugin().getServer().getPluginManager().callEvent(
		new LandModifyEvent(this, LandModifyReason.PERMISSION_SET, perm));
    }

    /**
     * Removes the permission.
     *
     * @param pc the pc
     * @param permType the perm type
     * @return true, if successful
     */
    public boolean removePermission(PlayerContainer pc,
	    PermissionType permType) {

	TreeMap<PermissionType, Permission> permPlayer;
	Permission perm;

	if (!permissions.containsKey(pc)) {
	    return false;
	}
	permPlayer = permissions.get(pc);
	perm = permPlayer.remove(permType);
	if (perm == null) {
	    return false;
	}

	// remove key for PC if it is empty
	if (permPlayer.isEmpty()) {
	    permissions.remove(pc);
	}

	doSave();

	if (this instanceof Land) {
	    // Start Event
	    Secuboid.getThisPlugin().getServer().getPluginManager().callEvent(
		    new LandModifyEvent(this, LandModifyReason.PERMISSION_UNSET, perm));
	}

	return true;
    }

    /**
     * Gets the sets the pc have permission.
     *
     * @return the sets the pc have permission
     */
    public final Set<PlayerContainer> getSetPCHavePermission() {

	return permissions.keySet();
    }

    /**
     * Gets the permissions for pc.
     *
     * @param pc the pc
     * @return the permissions for pc
     */
    public final Collection<Permission> getPermissionsForPC(PlayerContainer pc) {

	return permissions.get(pc).values();
    }

    /**
     * Check permission and inherit.
     *
     * @param player the player
     * @param pt the pt
     * @return the boolean
     */
    public boolean checkPermissionAndInherit(Player player,
	    PermissionType pt) {

	return checkPermissionAndInherit(player, pt, false);
    }

    /**
     * Check permission no inherit.
     *
     * @param player the player
     * @param pt the pt
     * @return the boolean
     */
    public boolean checkPermissionNoInherit(Player player,
	    PermissionType pt) {

	Boolean value = getPermission(player, pt, false);

	if (value != null) {
	    return value;
	} else {
	    return pt.getDefaultValue();
	}
    }

    /**
     * Check land permission and inherit.
     *
     * @param player the player
     * @param pt the pt
     * @param onlyInherit the only inherit
     * @return the boolean
     */
    private Boolean checkPermissionAndInherit(Player player, PermissionType pt, boolean onlyInherit) {

	Boolean permValue;

	if ((permValue = getPermission(player, pt, onlyInherit)) != null) {
	    return permValue;
	} else if (parent != null) {
	    return parent.checkPermissionAndInherit(player, pt, true);
	}

	return Secuboid.getThisPlugin().getLands().getPermissionInWorld(worldName, player, pt, true);
    }

    /**
     * Gets the land flag and inherit.
     *
     * @param ft the ft
     * @return the land flag value
     */
    public FlagValue getFlagAndInherit(FlagType ft) {

	return getFlagAndInherit(ft, true);
    }

    /**
     * Gets the land flag and inherit.
     *
     * @param ft the ft
     * @param onlyInherit the only inherit
     * @return the land flag value
     */
    private FlagValue getFlagAndInherit(FlagType ft, boolean onlyInherit) {

	FlagValue flagValue;

	if ((flagValue = getFlag(ft, onlyInherit)) != null) {
	    return flagValue;
	} else if (parent != null) {
	    return ((Land) parent).getFlagAndInherit(ft, true);
	}

	return Secuboid.getThisPlugin().getLands().getFlagInWorld(worldName, ft, true);
    }

    /**
     * Gets the permission.
     *
     * @param player the player
     * @param pt the pt
     * @param onlyInherit the only inherit
     * @return the permission
     */
    protected Boolean getPermission(Player player,
	    PermissionType pt, boolean onlyInherit) {

	return getPermission(player, pt, onlyInherit, null);
    }

    // Land parameter is only to paste to default parameters for a land
    private Boolean getPermission(Player player,
	    PermissionType pt, boolean onlyInherit, Land land) {

	for (Map.Entry<PlayerContainer, TreeMap<PermissionType, Permission>> permissionEntry : permissions.entrySet()) {
	    boolean value;
	    if (land != null) {
		value = permissionEntry.getKey().hasAccess(player, land);
	    } else {
		value = permissionEntry.getKey().hasAccess(player);
	    }
	    if (value) {
		Permission perm = permissionEntry.getValue().get(pt);

		// take the parent if the permission does not exist
		if (perm == null && pt.hasParent()) {
		    perm = permissionEntry.getValue().get(pt.getParent());
		}

		if (perm != null) {
		    Secuboid.getThisPlugin().getLog().write("Container: " + permissionEntry.getKey().toString() + ", "
			    + "PermissionType: " + perm.getPermType() + ", Value: " + perm.getValue() + ", Inheritable: " + perm.isInheritable());
		    if ((onlyInherit && perm.isInheritable()) || !onlyInherit) {
			return perm.getValue();
		    }
		}
	    }
	}

	// Check in default permissions
	return Secuboid.getThisPlugin().getLands().getDefaultConf(getType()).getPermission(player, pt, false, this);
    }

    /**
     * Adds the flag.
     *
     * @param flag the flag
     */
    public void addFlag(Flag flag) {

	flags.put(flag.getFlagType(), flag);
	doSave();

	if (this instanceof Land) {
	    // Start Event
	    Secuboid.getThisPlugin().getServer().getPluginManager().callEvent(
		    new LandModifyEvent((Land) this, LandModifyReason.FLAG_SET, flag));
	}
    }

    /**
     * Removes the flag.
     *
     * @param flagType the flag type
     * @return true, if successful
     */
    public boolean removeFlag(FlagType flagType) {

	Flag flag = flags.remove(flagType);

	if (flag == null) {
	    return false;
	}
	doSave();

	if (this instanceof Land) {
	    // Start Event
	    Secuboid.getThisPlugin().getServer().getPluginManager().callEvent(
		    new LandModifyEvent(this, LandModifyReason.FLAG_UNSET, flag));
	}

	return true;
    }

    /**
     * Gets the flags.
     *
     * @return the flags value or default
     */
    public Collection<Flag> getFlags() {

	return flags.values();
    }

    /**
     * Gets the flag no inherit.
     *
     * @param ft the ft
     * @return the flag value or default
     */
    public FlagValue getFlagNoInherit(FlagType ft) {

	FlagValue value = getFlag(ft, false);

	if (value != null) {
	    return value;
	} else {
	    return ft.getDefaultValue();
	}
    }

    /**
     * Gets the flag.
     *
     * @param ft the ft
     * @param onlyInherit the only inherit
     * @return the flag value
     */
    protected FlagValue getFlag(FlagType ft, boolean onlyInherit) {

	Flag flag = flags.get(ft);
	if (flag != null) {
	    Secuboid.getThisPlugin().getLog().write("Flag: " + flag.toString());

	    if ((onlyInherit && flag.isInheritable()) || !onlyInherit) {
		return flag.getValue();
	    }
	}

	// Check in default flags
	if (!onlyInherit) {

	    return (Secuboid.getThisPlugin().getLands()).getDefaultConf(getType()).getFlag(ft, false);
	}

	return null;
    }

    /**
     * Adds the money.
     *
     * @param money the money
     */
    public void addMoney(double money) {

	this.money += money;
	doSave();
    }

    /**
     * Substract money.
     *
     * @param money the money
     */
    public void substractMoney(double money) {

	this.money -= money;
	doSave();
    }

    /**
     * Gets the money.
     *
     * @return the money
     */
    public double getMoney() {

	return money;
    }

    /**
     * Adds the player notify.
     *
     * @param player the player
     */
    public void addPlayerNotify(PlayerContainerPlayer player) {

	playerNotify.add(player);
	doSave();
    }

    /**
     * Removes the player notify.
     *
     * @param player the player
     * @return true, if successful
     */
    public boolean removePlayerNotify(PlayerContainerPlayer player) {

	boolean ret = playerNotify.remove(player);
	doSave();

	return ret;
    }

    /**
     * Checks if is player notify.
     *
     * @param player the player
     * @return true, if is player notify
     */
    public boolean isPlayerNotify(PlayerContainerPlayer player) {

	return playerNotify.contains(player);
    }

    /**
     * Gets the players notify.
     *
     * @return the players notify
     */
    public Set<PlayerContainerPlayer> getPlayersNotify() {

	return playerNotify;
    }

    /**
     * Adds the player in land.
     *
     * @param player the player
     */
    public void addPlayerInLand(Player player) {

	playersInLand.add(player);
    }

    /**
     * Removes the player in land.
     *
     * @param player the player
     * @return true, if successful
     */
    public boolean removePlayerInLand(Player player) {

	return playersInLand.remove(player);
    }

    // No parent verify
    /**
     * Checks if is player in land.
     *
     * @param player the player
     * @return true, if is player in land
     */
    public boolean isPlayerInLand(Player player) {

	return playersInLand.contains(player);
    }

    /**
     * Checks if is playerin land no vanish.
     *
     * @param player the player
     * @param fromPlayer the from player
     * @return true, if is playerin land no vanish
     */
    public boolean isPlayerinLandNoVanish(Player player, Player fromPlayer) {

	if (playersInLand.contains(player)
		&& (!Secuboid.getThisPlugin().getPlayerConf().isVanished(player)
		|| Secuboid.getThisPlugin().getPlayerConf().get(fromPlayer).isAdminMod())) {
	    return true;
	}

	// Check Chidren
	for (Land landChild : children.values()) {
	    if (landChild.isPlayerinLandNoVanish(player, fromPlayer)) {
		return true;
	    }
	}

	return false;
    }

    /**
     * Gets the players in land.
     *
     * @return the players in land
     */
    public Set<Player> getPlayersInLand() {

	return playersInLand;
    }

    /**
     * Gets the players in land and children.
     *
     * @return the players in land and children
     */
    public Set<Player> getPlayersInLandAndChildren() {

	Set<Player> playLandChild = new HashSet<Player>();

	playLandChild.addAll(playersInLand);

	for (Land child : children.values()) {
	    playLandChild.addAll(child.getPlayersInLandAndChildren());
	}

	return playLandChild;
    }

    /**
     * Gets the players in land no vanish.
     *
     * @param fromPlayer the from player
     * @return the players in land no vanish
     */
    public Set<Player> getPlayersInLandNoVanish(Player fromPlayer) {

	Set<Player> playerList = new HashSet<Player>();

	for (Player player : playersInLand) {
	    if (!Secuboid.getThisPlugin().getPlayerConf().isVanished(player) || Secuboid.getThisPlugin().getPlayerConf().get(fromPlayer).isAdminMod()) {
		playerList.add(player);
	    }
	}
	for (Land landChild : children.values()) {
	    playerList.addAll(landChild.getPlayersInLandNoVanish(fromPlayer));
	}

	return playerList;
    }

    /**
     * Checks if is for sale.
     *
     * @return true, if is for sale
     */
    public boolean isForSale() {

	return forSale;
    }

    /**
     * Sets the for sale.
     *
     * @param isForSale the is for sale
     * @param salePrice the sale price
     * @param signLoc the sign location
     */
    public void setForSale(boolean isForSale, double salePrice, Location signLoc) {

	forSale = isForSale;
	if (forSale) {
	    this.salePrice = salePrice;
	    this.forSaleSignLoc = signLoc;
	    Secuboid.getThisPlugin().getLands().addForSale(this);
	} else {
	    this.salePrice = 0;
	    this.forSaleSignLoc = null;
	    Secuboid.getThisPlugin().getLands().removeForSale(this);
	}
	doSave();
    }

    /**
     *
     * @return
     */
    public Location getSaleSignLoc() {

	return forSaleSignLoc;
    }

    /**
     *
     * @param forSaleSignLoc
     */
    public void setSaleSignLoc(Location forSaleSignLoc) {

	this.forSaleSignLoc = forSaleSignLoc;
	doSave();
    }

    /**
     * Gets the sale price.
     *
     * @return the sale price
     */
    public double getSalePrice() {

	return salePrice;
    }

    /**
     * Checks if is for rent.
     *
     * @return true, if is for rent
     */
    public boolean isForRent() {

	return forRent;
    }

    /**
     * Sets the for rent.
     *
     * @param rentPrice the rent price
     * @param rentRenew the rent renew
     * @param rentAutoRenew the rent auto renew
     * @param signLoc the sign location
     */
    public void setForRent(double rentPrice, int rentRenew, boolean rentAutoRenew, Location signLoc) {

	forRent = true;
	this.rentPrice = rentPrice;
	this.rentRenew = rentRenew;
	this.rentAutoRenew = rentAutoRenew;
	this.forRentSignLoc = signLoc;
	Secuboid.getThisPlugin().getLands().addForRent(this);
	doSave();
    }

    /**
     *
     * @return
     */
    public Location getRentSignLoc() {

	return forRentSignLoc;
    }

    /**
     *
     * @param forRentSignLoc
     */
    public void setRentSignLoc(Location forRentSignLoc) {

	this.forRentSignLoc = forRentSignLoc;
	doSave();
    }

    /**
     * Un set for rent.
     */
    public void unSetForRent() {

	forRent = false;
	rentPrice = 0;
	rentRenew = 0;
	rentAutoRenew = false;
	forRentSignLoc = null;
	Secuboid.getThisPlugin().getLands().removeForRent(this);
	doSave();
    }

    /**
     * Gets the rent price.
     *
     * @return the rent price
     */
    public double getRentPrice() {

	return rentPrice;
    }

    /**
     * Gets the rent renew.
     *
     * @return the rent renew
     */
    public int getRentRenew() {

	return rentRenew;
    }

    /**
     * Gets the rent auto renew.
     *
     * @return the rent auto renew
     */
    public boolean getRentAutoRenew() {

	return rentAutoRenew;
    }

    /**
     * Checks if is rented.
     *
     * @return true, if is rented
     */
    public boolean isRented() {

	return rented;
    }

    /**
     * Sets the rented.
     *
     * @param tenant the new rented
     */
    public void setRented(PlayerContainerPlayer tenant) {

	rented = true;
	this.tenant = tenant;
	updateRentedPayment(); // doSave() done in this method
    }

    /**
     * Update rented payment.
     */
    public void updateRentedPayment() {

	lastPayment = new Timestamp(new Date().getTime());
	doSave();
    }

    /**
     * Un set rented.
     */
    public void unSetRented() {

	rented = false;
	tenant = null;
	lastPayment = new Timestamp(0);
	doSave();
    }

    /**
     * Gets the tenant.
     *
     * @return the tenant
     */
    public PlayerContainerPlayer getTenant() {

	return tenant;
    }

    /**
     * Checks if is tenant.
     *
     * @param player the player
     * @return true, if is tenant
     */
    public boolean isTenant(Player player) {

	return rented && tenant.hasAccess(player);
    }

    /**
     * Sets the last payment time.
     *
     * @param lastPayment the new last payment time
     */
    public void setLastPaymentTime(Timestamp lastPayment) {

	this.lastPayment = lastPayment;
	doSave();
    }

    /**
     * Gets the last payment time.
     *
     * @return the last payment time
     */
    public Timestamp getLastPaymentTime() {

	return lastPayment;
    }

    /**
     *
     * @return
     */
    public Type getType() {

	return type;
    }

    /**
     *
     * @param arg0
     */
    public void setType(Type arg0) {

	type = arg0;
	doSave();
    }

    @Override
    public int compareTo(Land t) {
	return name.compareTo(t.name);
    }
}
