package atomAlpha;

import java.util.*;
import battlecode.common.*;

public class Pathfinding {
    public static MapLocation variableLoc;

    public static Direction chooseBestNextStep(RobotController rc, Direction dir) throws GameActionException {
        MapLocation myLoc = rc.getLocation();
        MapLocation testLoc;
        Map<Double, MapLocation> pass = new TreeMap<Double, MapLocation>();
        if (dir == Direction.NORTH) {
            for (int i = -1; i < 2; i++) {
                testLoc = new MapLocation(variableLoc.x + i, myLoc.y + 1);
                if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                    pass.put(rc.sensePassability(testLoc), testLoc);
                }
            }
        } else if (dir == Direction.EAST) {
            for (int i = -1; i < 2; i++) {
                testLoc = new MapLocation(myLoc.x + 1, variableLoc.y + i);
                if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                    pass.put(rc.sensePassability(testLoc), testLoc);
                }
            }
        } else if (dir == Direction.SOUTH) {
            for (int i = -1; i < 2; i++) {
                testLoc = new MapLocation(variableLoc.x + i, myLoc.y - 1);
                if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                    pass.put(rc.sensePassability(testLoc), testLoc);
                }
            }
        } else if (dir == Direction.WEST) {
            for (int i = -1; i < 2; i++) {
                testLoc = new MapLocation(myLoc.x - 1, variableLoc.y + i);
                if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                    pass.put(rc.sensePassability(testLoc), testLoc);
                }
            }
        } else if (dir == Direction.NORTHEAST) {
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                testLoc = new MapLocation(myLoc.x, myLoc.y + 1);
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                testLoc = new MapLocation(myLoc.x + 1, myLoc.y + 1);
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                testLoc = new MapLocation(myLoc.x + 1, myLoc.y);
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
        } else if (dir == Direction.SOUTHEAST) {
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                testLoc = new MapLocation(myLoc.x + 1, myLoc.y);
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                testLoc = new MapLocation(myLoc.x + 1, myLoc.y - 1);
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                testLoc = new MapLocation(myLoc.x, myLoc.y - 1);
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
        } else if (dir == Direction.SOUTHWEST) {
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                testLoc = new MapLocation(myLoc.x, myLoc.y - 1);
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                testLoc = new MapLocation(myLoc.x - 1, myLoc.y - 1);
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                testLoc = new MapLocation(myLoc.x - 1, myLoc.y);
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
        } else if (dir == Direction.NORTHWEST) {
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                testLoc = new MapLocation(myLoc.x - 1, myLoc.y);
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                testLoc = new MapLocation(myLoc.x - 1, myLoc.y + 1);
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                testLoc = new MapLocation(myLoc.x, myLoc.y + 1);
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
        }
        // System.out.println(pass.toString());
        Object[] keys = pass.keySet().toArray();
        if (keys.length != 0) {
            return (rc.getLocation().directionTo(pass.get(keys[keys.length - 1])));
        } else {
            return Direction.CENTER;
            // hit wall -- add logic later
        }

    }

    public static void findDefenseLocation(RobotController rc, MapLocation baseLoc) {
        MapLocation[] defenseSpots = {new MapLocation(baseloc.x + 3, baseLoc.y +3), new MapLocation(baseLoc.x + 3, baseLoc.y + 3), new MapLocation(baseLoc.x - 3, baseLoc.y - 3), new MapLocation(baseLoc.x - 3, baseLoc.y +3)};
        MapLocation targetLoc;
        for (int i = 0; i < 5; i++) {
            if (!rc.isLocationOccupied(defenseSpots[i])) {
                targetLoc = defenseSpots[i];
            }
        }
        if (rc.canMove(chooseBestNextStep(rc, rc.directionTo(rc.getLoction(), targetLoc)))) {
            rc.move(chooseBestNextStep(rc, rc.directionTo(rc.getLoction(), targetLoc)));
        }
    }

    public static void setStartLocation(RobotController rc) {
        variableLoc = rc.getLocation();
    }
}
