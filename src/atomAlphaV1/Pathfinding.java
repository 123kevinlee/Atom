package atomAlphaV1;

import java.util.*;
import battlecode.common.*;

public class Pathfinding {
    public static MapLocation variableLoc = null;
    public static boolean defenseLocReached = false;
    public static MapLocation targetLoc = null;

    public static Direction chooseBestNextStep(RobotController rc, Direction dir) throws GameActionException {
        MapLocation myLoc = rc.getLocation();
        MapLocation testLoc = null;

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
            testLoc = new MapLocation(myLoc.x, myLoc.y + 1);
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            testLoc = new MapLocation(myLoc.x + 1, myLoc.y + 1);
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            testLoc = new MapLocation(myLoc.x + 1, myLoc.y);
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
        } else if (dir == Direction.SOUTHEAST) {
            testLoc = new MapLocation(myLoc.x + 1, myLoc.y);
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            testLoc = new MapLocation(myLoc.x + 1, myLoc.y - 1);
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            testLoc = new MapLocation(myLoc.x, myLoc.y - 1);
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
        } else if (dir == Direction.SOUTHWEST) {
            testLoc = new MapLocation(myLoc.x, myLoc.y - 1);
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            testLoc = new MapLocation(myLoc.x - 1, myLoc.y - 1);
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            testLoc = new MapLocation(myLoc.x - 1, myLoc.y);
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
        } else if (dir == Direction.NORTHWEST) {
            testLoc = new MapLocation(myLoc.x - 1, myLoc.y);
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            testLoc = new MapLocation(myLoc.x - 1, myLoc.y + 1);
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
                pass.put(rc.sensePassability(testLoc), testLoc);
            }
            testLoc = new MapLocation(myLoc.x, myLoc.y + 1);
            if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc) && rc.getLocation().isAdjacentTo(testLoc)) {
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

    public static void findDefenseLocation(RobotController rc, MapLocation baseLoc) throws GameActionException {
        Map<MapLocation, MapLocation> spots = new TreeMap<MapLocation, MapLocation>() {
            {
                put(baseLoc.translate(1, 1), baseLoc.translate(3, 3));
                put(baseLoc.translate(1, -1), baseLoc.translate(3, -3));
                put(baseLoc.translate(-1, -1), baseLoc.translate(-3, -3));
                put(baseLoc.translate(-1, 1), baseLoc.translate(-3, 3));
            }
        };
        Object[] keys = spots.keySet().toArray();
        if (targetLoc == null) {
            for (int i = 0; i < keys.length; i++) {
                if (rc.getLocation().equals(keys[i])) {
                    targetLoc = spots.get(keys[i]);
                }
            }
        }
        // System.out.println(targetLoc);
        if (targetLoc != null) {
            if (rc.canMove(rc.getLocation().directionTo(targetLoc))) {
                // System.out.println("TRY TO MOVE");
                rc.move(rc.getLocation().directionTo(targetLoc));
            } else if (rc.canMove(chooseBestNextStep(rc, rc.getLocation().directionTo(targetLoc)))) {
                rc.move(chooseBestNextStep(rc, rc.getLocation().directionTo(targetLoc)));
            }
        }
        if (rc.getLocation().equals(targetLoc)) {
            defenseLocReached = true;
            // System.out.println("REACHED TARGET LOCATION");
        }
    }

    public static Direction basicBugToBase(RobotController rc, MapLocation target) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(target);
        // System.out.println(dir);
        if (dir == null) {
            return Direction.CENTER;
        }
        if (rc.getLocation().distanceSquaredTo(target) == 1) {
            // do something
        } else if (rc.canMove(dir)) {
            return dir;
        } else {
            Direction attemptDir = Direction.CENTER;
            for (int i = 1; i < 8; i++) {
                switch (i) {
                    case 1:
                        attemptDir = dir.rotateRight();
                        break;
                    case 2:
                        attemptDir = dir.rotateLeft();
                        break;
                    case 3:
                        attemptDir = dir.rotateRight().rotateRight();
                        break;
                    case 4:
                        attemptDir = dir.rotateLeft().rotateLeft();
                        break;
                    case 5:
                        attemptDir = dir.opposite().rotateRight();
                        break;
                    case 6:
                        attemptDir = dir.opposite().rotateLeft();
                        break;
                    case 7:
                        attemptDir = dir.opposite();
                        break;
                    default:
                        break;
                }
                if (rc.canMove(attemptDir)) {
                    return attemptDir;
                }
            }
        }
        return Direction.CENTER;
    }

    public static boolean getDefenseReached() throws GameActionException {
        return defenseLocReached;
    }

    public static void setStartLocation(RobotController rc) throws GameActionException {
        variableLoc = rc.getLocation();
    }
}
