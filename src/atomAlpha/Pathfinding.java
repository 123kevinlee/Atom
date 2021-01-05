package atomAlpha;


import java.util.*;
import battlecode.common.*;

public class Pathfinding {

    public static Direction chooseScoutNextStep(Direction dir, RobotController rc) {
        MapLocation myLoc = new MapLocation(getLocation());
        MapLocation testLoc;
        Map<Double, MapLocation> pass = new TreeMap<Double, MapLocation>();
        if (dir == Direction.NORTH) {
            for (int i = -1; i < 2; i++) {
                testLoc = new MapLocation(myLoc.x+i, myLoc.y+1);
                if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc)) {
                    pass.add(rc.sensePassability(testLoc), testLoc);
                }
            }
        } else if (dir == Direction.EAST) {
            for (int i = -1; i < 2; i++) {
                testLoc = new MapLocation(myLoc.x+1, myLoc.y+i);
                if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc)) {
                    pass.add(rc.sensePassability(testLoc), testLoc);
                }
            }
        } else if (dir == Direction.SOUTH) {
            for (int i = -1; i < 2; i++) {
                testLoc = new MapLocation(myLoc.x+i, myLoc.y-1);
                if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc)) {
                    pass.add(rc.sensePassability(testLoc), testLoc);
                }
            }
        } else if (dir == Direction.WEST) {
            for (int i = -1; i < 2; i++) {
                testLoc = new MapLocation(myLoc.x-1, myLoc.y+i);
                if (rc.onTheMap(testLoc) && !rc.isLocationOccupied(testLoc)) {
                    pass.add(rc.sensePassability(testLoc), testLoc);
                }
            }
        }
        return (rc.getLocation().directionTo(pass.keySet().toArray()[0]));
    }
}
