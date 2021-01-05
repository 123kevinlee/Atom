package atomAlpha;

import battlecode.common.*;

public class Slanderer {
    static Direction scoutDirection;

    public static void run(RobotController rc, int turnCount) throws GameActionException {
        // scout code
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
            rc.move(Pathfinding.chooseScoutNextStep(rc, scoutDirection));
        } else {
            switch (scoutDirection) {
                case NORTH:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.NORTH))) {
                        System.out.println("WALL!");
                        Helper.sendFlag(rc, 21);
                    }
                    break;
                case EAST:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.EAST))) {
                        System.out.println("WALL!");
                        Helper.sendFlag(rc, 21);
                    }
                    break;
                case SOUTH:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.SOUTH))) {
                        System.out.println("WALL!");
                        Helper.sendFlag(rc, 21);
                    }
                    break;
                case WEST:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.WEST))) {
                        System.out.println("WALL!");
                        Helper.sendFlag(rc, 21);
                    }
                    break;
            }
        }
    }

}
