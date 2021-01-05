package atomAlpha;

import battlecode.common.*;

public class Slanderer {
    static Direction scoutDirection;

    public static void run(RobotController rc, int turnCount) throws GameActionException {
        System.out.println(turnCount);
        if (turnCount < 2) {
            for (Direction dir : Helper.directions) {
                if (!rc.canMove(dir) && rc.getCooldownTurns() == 0) {
                    switch (dir) {
                        case NORTH:
                            scoutDirection = Direction.SOUTH;
                            break;
                        case EAST:
                            scoutDirection = Direction.WEST;
                            break;
                        case SOUTH:
                            scoutDirection = Direction.NORTH;
                            break;
                        case WEST:
                            scoutDirection = Direction.EAST;
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        if (rc.canMove(scoutDirection)) {
            rc.move(scoutDirection);
            // System.out.println("I moved!");
        }
    }
}
