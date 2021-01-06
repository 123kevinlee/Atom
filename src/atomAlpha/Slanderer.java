package atomAlpha;

import battlecode.common.*;

public class Slanderer {
    public static Direction scoutDirection;

    public static MapLocation originPoint;

    public static String role = "";

    public static void run(RobotController rc, int turnCount) throws GameActionException {
        // System.out.println(role);

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
        if (rc.canMove(Pathfinding.chooseBestNextStep(rc, scoutDirection))) {
            rc.move(Pathfinding.chooseBestNextStep(rc, scoutDirection));
            MapLocation currentLocation = rc.getLocation();
            int dx = currentLocation.x - originPoint.x;
            int dy = currentLocation.y - originPoint.y;
            int outMsg = Communication.coordEncoder("LIKELY", dx, dy);
            Helper.sendFlag(rc, outMsg);
        } else {
            switch (scoutDirection) {
                case NORTH:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.NORTH))) {
                        System.out.println("WALL!");
                        MapLocation currentLocation = rc.getLocation();
                        int dx = currentLocation.x - originPoint.x;
                        int dy = currentLocation.y - originPoint.y;
                        int outMsg = Communication.coordEncoder("WALL", dx, dy);
                        Helper.sendFlag(rc, outMsg);

                        scoutDirection = Direction.CENTER;
                    }
                    break;
                case EAST:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.EAST))) {
                        System.out.println("WALL!");
                        MapLocation currentLocation = rc.getLocation();
                        int dx = currentLocation.x - originPoint.x;
                        int dy = currentLocation.y - originPoint.y;
                        int outMsg = Communication.coordEncoder("WALL", dx, dy);
                        Helper.sendFlag(rc, outMsg);

                        scoutDirection = Direction.CENTER;
                    }
                    break;
                case SOUTH:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.SOUTH))) {
                        System.out.println("WALL!");
                        MapLocation currentLocation = rc.getLocation();
                        int dx = currentLocation.x - originPoint.x;
                        int dy = currentLocation.y - originPoint.y;
                        int outMsg = Communication.coordEncoder("WALL", dx, dy);
                        Helper.sendFlag(rc, outMsg);

                        scoutDirection = Direction.CENTER;
                    }
                    break;
                case WEST:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.WEST))) {
                        System.out.println("WALL!");
                        MapLocation currentLocation = rc.getLocation();
                        int dx = currentLocation.x - originPoint.x;
                        int dy = currentLocation.y - originPoint.y;
                        int outMsg = Communication.coordEncoder("WALL", dx, dy);
                        Helper.sendFlag(rc, outMsg);

                        scoutDirection = Direction.CENTER;
                    }
                    break;
                default:
                    break;
            }
        }
        int sensorRadius = rc.getType().sensorRadiusSquared;
        if (rc.canSenseRadiusSquared(sensorRadius)) {
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, rc.getTeam().opponent())) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    MapLocation baseLocation = robot.getLocation();
                    int dx = baseLocation.x - originPoint.x;
                    int dy = baseLocation.y - originPoint.y;
                    int outMsg = Communication.coordEncoder("ENEMY", dx, dy);
                    System.out.println("Found Enemy Base:" + outMsg);
                    Helper.sendFlag(rc, outMsg);
                }
            }
        }
    }

    public static void getRole(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(1)) {
            for (RobotInfo robot : rc.senseNearbyRobots(1, rc.getTeam())) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    if (rc.canGetFlag(robot.getID())) {
                        if (rc.canSetFlag(rc.getFlag(robot.getID()))) {
                            role = Integer.toString(rc.getFlag(robot.getID()));
                            originPoint = robot.getLocation();
                        }
                    }
                }
            }
        }
    }
}
