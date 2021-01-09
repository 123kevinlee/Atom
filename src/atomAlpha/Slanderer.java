package atomAlpha;

import battlecode.common.*;

public class Slanderer {
    public static Direction scoutDirection;
    public static boolean end = false;
    public static String role = "";

    public static void run(RobotController rc) throws GameActionException {
        if (role.equals("100")) { // scout
            scoutMode(rc);
        } else if (role.length() == 7) {
            farmMode(rc);
        }
    }

    public static void scoutMode(RobotController rc) throws GameActionException {
        // System.out.println("Slanderer set to scout mode");
        // System.out.println(scoutDirection);

        Direction nextDir = Pathfinding.chooseBestNextStep(rc, scoutDirection);

        if (rc.canMove(nextDir)) {
            rc.move(nextDir);
            MapLocation currentLocation = rc.getLocation();
            int dx = currentLocation.x - Data.originPoint.x;
            int dy = currentLocation.y - Data.originPoint.y;
            int outMsg = Communication.coordEncoder("LIKELY", dx, dy);
            if (!end) {
                if (rc.canSetFlag(outMsg)) {
                    rc.setFlag(outMsg);
                }
            }
        } else {
            switch (scoutDirection) {
                case NORTH:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.NORTH))) {
                        // System.out.println("WALL!");
                        MapLocation currentLocation = rc.getLocation();
                        int dx = currentLocation.x - Data.originPoint.x;
                        int dy = currentLocation.y - Data.originPoint.y;
                        int outMsg = Communication.coordEncoder("WALL", dx, dy);
                        if (rc.canSetFlag(outMsg)) {
                            rc.setFlag(outMsg);
                        }
                        end = true;
                        turnRight(rc);
                    }
                    break;
                case EAST:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.EAST))) {
                        // System.out.println("WALL!");
                        MapLocation currentLocation = rc.getLocation();
                        int dx = currentLocation.x - Data.originPoint.x;
                        int dy = currentLocation.y - Data.originPoint.y;
                        int outMsg = Communication.coordEncoder("WALL", dx, dy);
                        if (rc.canSetFlag(outMsg)) {
                            rc.setFlag(outMsg);
                        }
                        end = true;
                        turnRight(rc);
                    }
                    break;
                case SOUTH:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.SOUTH))) {
                        // System.out.println("WALL!");
                        MapLocation currentLocation = rc.getLocation();
                        int dx = currentLocation.x - Data.originPoint.x;
                        int dy = currentLocation.y - Data.originPoint.y;
                        int outMsg = Communication.coordEncoder("WALL", dx, dy);
                        if (rc.canSetFlag(outMsg)) {
                            rc.setFlag(outMsg);
                        }
                        end = true;
                        turnRight(rc);
                    }
                    break;
                case WEST:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.WEST))) {
                        // System.out.println("WALL!");
                        MapLocation currentLocation = rc.getLocation();
                        int dx = currentLocation.x - Data.originPoint.x;
                        int dy = currentLocation.y - Data.originPoint.y;
                        int outMsg = Communication.coordEncoder("WALL", dx, dy);
                        if (rc.canSetFlag(outMsg)) {
                            rc.setFlag(outMsg);
                        }
                        end = true;
                        turnRight(rc);
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
                    int dx = baseLocation.x - Data.originPoint.x;
                    int dy = baseLocation.y - Data.originPoint.y;
                    int outMsg = Communication.coordEncoder("ENEMY", dx, dy);
                    // System.out.println("Found Enemy Base:" + outMsg);
                    if (rc.canSetFlag(outMsg)) {
                        rc.setFlag(outMsg);
                    }
                    end = true;
                }
            }
        }
    }

    public static void farmMode(RobotController rc) throws GameActionException {
        // System.out.println("I moved!");
        int[] coords = Communication.coordDecoder(role); // coords of safe corner
        coords[0] += Data.originPoint.x;
        coords[1] += Data.originPoint.y;
        //System.out.println("SAFE CORNER: " + coords[0] + "," + coords[1]);
        MapLocation targetLocation = new MapLocation(coords[0], coords[1]);
        Direction targetDirection = Data.originPoint.directionTo(targetLocation);
        if (Data.slandererConvertDirection == Direction.CENTER) {
            Data.slandererConvertDirection = targetDirection.opposite();
        }
        //System.out.println(rc.canMove(Pathfinding.chooseBestNextStep(rc, targetDirection)));
        if (rc.canMove(Pathfinding.chooseBestNextStep(rc, targetDirection))) {
            rc.move(Pathfinding.chooseBestNextStep(rc, targetDirection));
        }
    }

    public static void turnRight(RobotController rc) throws GameActionException {
        Pathfinding.setStartLocation(rc);
        scoutDirection = scoutDirection.rotateRight().rotateRight();
        Direction nextMove = Pathfinding.chooseBestNextStep(rc, scoutDirection);
        if (rc.canMove(nextMove)) {
            rc.move(nextMove);
        }
    }

    public static void init(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(3)) {
            for (RobotInfo robot : rc.senseNearbyRobots(3, rc.getTeam())) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    Data.baseId = robot.getID();
                    if (rc.canGetFlag(Data.baseId)) {
                        if (rc.canSetFlag(rc.getFlag(Data.baseId))) {
                            role = Integer.toString(rc.getFlag(Data.baseId));
                            Data.originPoint = robot.getLocation();
                            Data.initRound = rc.getRoundNum();
                            if (role.equals("100")) {
                                scoutDirection = rc.getLocation().directionTo(robot.getLocation()).opposite();
                            } else {
                                if (rc.canSetFlag(Integer.parseInt(role))) {
                                    rc.setFlag(Integer.parseInt(role));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
