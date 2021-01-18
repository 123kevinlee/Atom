package atomFinal;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import java.util.*;
import battlecode.common.*;

public class Pathfinding {
    public static Direction basicBug(RobotController rc, MapLocation target) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(target);
        // System.out.println(dir);
        if (dir == null) {
            return Direction.CENTER;
        }
        if (rc.getLocation().distanceSquaredTo(target) == 1) {
            // do something
            return Direction.CENTER;
        } else if (rc.canMove(dir)) {
            return dir;
        } else {
            Direction attemptDir = Direction.CENTER;
            Direction returnDirection = Direction.CENTER;
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
                    returnDirection = attemptDir;
                }
            }
            return returnDirection;
        }
    }

    public static int[] getDistance(int[] ref, int[] target) {
        int[] temp = new int[2];

        if (target[0] >= ref[0] && target[0] < ref[0] + 64) {
            temp[0] = target[0] - ref[0];
        } else if (target[0] > ref[0] && target[0] > ref[0] + 64) {
            temp[0] = ref[0] - (127 - target[0]);
        } else if (target[0] < ref[0] && target[0] > ref[0] - 64) {
            temp[0] = target[0] - ref[0];
        } else if (target[0] < ref[0] && target[0] < ref[0] - 64) {
            temp[0] = (127 - ref[0]) + target[0];
        }

        if (target[1] >= ref[1] && target[1] < ref[1] + 64) {
            temp[1] = target[1] - ref[1];
        } else if (target[1] > ref[1] && target[1] > ref[1] + 64) {
            temp[1] = ref[1] - (127 - target[1]);
        } else if (target[1] < ref[1] && target[1] > ref[1] - 64) {
            temp[1] = target[1] - ref[1];
        } else if (target[1] < ref[1] && target[1] < ref[1] - 64) {
            temp[1] = (127 - ref[1]) + target[1];
        }
        return temp;
    }
}
