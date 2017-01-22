package me.tabinol.secuboid.lands.collisions;

import me.tabinol.secuboid.Secuboid;
import me.tabinol.secuboid.exceptions.SecuboidLandException;
import me.tabinol.secuboid.lands.InitLands;
import me.tabinol.secuboid.lands.Lands;
import me.tabinol.secuboid.lands.RealLand;
import me.tabinol.secuboid.lands.areas.CuboidArea;
import me.tabinol.secuboid.playercontainer.PlayerContainerNobody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static me.tabinol.secuboid.lands.InitLands.WORLD;
import static org.junit.Assert.fail;

/**
 * Test land collisions.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Secuboid.class)
public class CollisionsTest {

    private static Secuboid secuboid;
    private static Lands lands;

    @Before
    public void initCollisions() throws SecuboidLandException {
        InitLands initLands = new InitLands();
        secuboid = initLands.getSecuboid();
        lands = initLands.getLands();
        lands.createLand("land1", new PlayerContainerNobody(),
                new CuboidArea(WORLD, 0, 0, 0, 100, 255, 100));
    }

    private boolean isError(Collisions collisions, Collisions.LandError landError) {
        for (CollisionsEntry collisionEntry : collisions.getCollisions()) {
            if (collisionEntry.getError() == landError) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void landCollision() throws SecuboidLandException {
        Collisions collisions = new Collisions(secuboid, WORLD, "landT", null, Collisions.LandAction.LAND_ADD,
                0, new CuboidArea(WORLD, 10, 0, 10, 120, 255, 120),
                null, new PlayerContainerNobody(), true, false);
        collisions.doCollisionCheck();
        if (!isError(collisions, Collisions.LandError.COLLISION)) {
            fail("Land collision not detected");
        }
    }

    @Test
    public void landOutsideParent() throws SecuboidLandException {
        Collisions collisions = new Collisions(secuboid, WORLD, "landT", null, Collisions.LandAction.LAND_ADD,
                0, new CuboidArea(WORLD, 10, 0, 10, 120, 255, 120),
                lands.getLand("land1"), new PlayerContainerNobody(), true, false);
        collisions.doCollisionCheck();
        if (!isError(collisions, Collisions.LandError.OUT_OF_PARENT)) {
            fail("Land outside parent not detected");
        }
    }

    @Test
    public void landChildrenOutside() throws SecuboidLandException {
        RealLand land2 = lands.createLand("land2", new PlayerContainerNobody(),
                new CuboidArea(WORLD, 1000, 0, 1000, 1100, 255, 1100));
        lands.createLand("land3", new PlayerContainerNobody(),
                new CuboidArea(WORLD, 1000, 0, 1000, 1100, 255, 1100), land2);
        Collisions collisions = new Collisions(secuboid, WORLD, "land2", land2, Collisions.LandAction.AREA_MODIFY,
                1, new CuboidArea(WORLD, 10, 0, 10, 120, 255, 120),
                null, new PlayerContainerNobody(), true, false);
        collisions.doCollisionCheck();
        if (!isError(collisions, Collisions.LandError.CHILD_OUT_OF_BORDER)) {
            fail("Land has children outside not detected");
        }
    }

    @Test
    public void landHasChild() throws SecuboidLandException {
        RealLand land4 = lands.createLand("land4", new PlayerContainerNobody(),
                new CuboidArea(WORLD, 2000, 0, 2000, 2100, 255, 2100));
        lands.createLand("land5", new PlayerContainerNobody(),
                new CuboidArea(WORLD, 2000, 0, 2000, 2100, 255, 2100), land4);
        Collisions collisions = new Collisions(secuboid, WORLD, "land4", land4, Collisions.LandAction.LAND_REMOVE,
                0, null,
                null, new PlayerContainerNobody(), true, false);
        collisions.doCollisionCheck();
        if (!isError(collisions, Collisions.LandError.HAS_CHILDREN)) {
            fail("Land has children not detected");
        }
    }

    @Test
    public void landNameInUse() throws SecuboidLandException {
        lands.createLand("land6", new PlayerContainerNobody(),
                new CuboidArea(WORLD, 3000, 0, 3000, 3100, 255, 3100));
        Collisions collisions = new Collisions(secuboid, WORLD, "land6", null, Collisions.LandAction.LAND_ADD,
                0, new CuboidArea(WORLD, 10, 0, 10, 120, 255, 120),
                null, new PlayerContainerNobody(), true, false);
        collisions.doCollisionCheck();
        if (!isError(collisions, Collisions.LandError.NAME_IN_USE)) {
            fail("Land name in use not detected");
        }
    }

    @Test
    public void landMustHasOneArea() throws SecuboidLandException {
        Collisions collisions = new Collisions(secuboid, WORLD, "land1", lands.getLand("land1"), Collisions.LandAction.AREA_REMOVE,
                1, null,
                null, new PlayerContainerNobody(), true, false);
        collisions.doCollisionCheck();
        if (!isError(collisions, Collisions.LandError.MUST_HAVE_AT_LEAST_ONE_AREA)) {
            fail("Land must has at least one area not detected");
        }
    }
}