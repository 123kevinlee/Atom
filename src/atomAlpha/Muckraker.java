package atomAlpha;

import battlecode.common.*;

public class Muckraker {

    public static Direction scoutDirection;
    public static boolean end = false;
    public static String role = "";

    public static void run(RobotController rc) throws GameActionException {
        if (role.equals("100")) { // scout
            scoutMode(rc);
        } else {
            attackMode(rc);
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

    public static void scoutMode(RobotController rc) throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        if (rc.canSenseRobot(12)) {
            for (RobotInfo robot : rc.senseNearbyRobots(12, enemy)) {
                if (robot.type.canBeExposed()) {
                    // It's a slanderer... go get them!
                    if (rc.canExpose(robot.location)) {
                        // System.out.println("e x p o s e d");
                        rc.expose(robot.location);
                        return;
                    }
                }
            }
        }

        // System.out.println("MuckRaker set to scout mode");
        // System.out.println(scoutDirection);

        Direction nextDir = Pathfinding.chooseBestNextStep(rc, scoutDirection);

        if (rc.canMove(nextDir)) {
            // System.out.println(scoutDirection);
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
                        // System.out.println(scoutDirection + " " + currentLocation.toString());
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
                        // System.out.println(scoutDirection + " " + currentLocation.toString());
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
                        // System.out.println(scoutDirection + " " + currentLocation.toString());
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
                        // System.out.println(scoutDirection + " " + currentLocation.toString());
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

    public static void attackMode(RobotController rc) throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;

        if (rc.canSenseRadiusSquared(actionRadius)) {
            for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
                if (robot.type.canBeExposed()) {
                    // It's a slanderer... go get them!
                    if (rc.canExpose(robot.location)) {
                        // System.out.println("e x p o s e d");
                        rc.expose(robot.location);
                        return;
                    }
                }
            }
        }
        if (role.length() == 7) {
            // System.out.println(role);
            // System.out.println("I moved!");
            int[] coords = Communication.coordDecoder(role);
            MapLocation currentLocation = rc.getLocation();
            coords[0] += Data.originPoint.x;
            coords[1] += Data.originPoint.y;
            // System.out.println("ENEMY TARGET: " + coords[0] + "," + coords[1]);

            MapLocation targetLocation = new MapLocation(coords[0], coords[1]);
            Direction targetDirection = currentLocation.directionTo(targetLocation);

            if (rc.canMove(targetDirection)) {
                rc.move(targetDirection);
            }
        }
    }

    public static void init(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(3)) {
            for (RobotInfo robot : rc.senseNearbyRobots(3, rc.getTeam())) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    if (rc.canGetFlag(robot.getID())) {
                        if (rc.canSetFlag(rc.getFlag(robot.getID()))) {
                            role = Integer.toString(rc.getFlag(robot.getID()));
                            // System.out.println(role);
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
