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
        }
        // System.out.println(pass.toString());
        Object[] keys = pass.keySet().toArray();
        return (rc.getLocation().directionTo(pass.get(keys[keys.length - 1])));
    }

    public static void setStartLocation(RobotController rc) {
        variableLoc = rc.getLocation();
    }
}
