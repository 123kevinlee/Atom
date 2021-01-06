package atomAlpha;

import battlecode.common.*;

public class Muckraker {

    public static Direction scoutDirection;
    public static MapLocation originPoint;
    public static boolean end = false;
    public static String role = "";

    public static void run(RobotController rc, int turnCount) throws GameActionException {
        if (role.equals("100")) {
            System.out.println("MuckRaker set to scout mode");
            System.out.println(scoutDirection);
            if (rc.canMove(Pathfinding.chooseBestNextStep(rc, scoutDirection))) {
                rc.move(Pathfinding.chooseBestNextStep(rc, scoutDirection));
                MapLocation currentLocation = rc.getLocation();
                int dx = currentLocation.x - originPoint.x;
                int dy = currentLocation.y - originPoint.y;
                int outMsg = Communication.coordEncoder("LIKELY", dx, dy);
                if (!end) {
                    Helper.sendFlag(rc, outMsg);
                }
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
                            end = true;

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
                            end = true;

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
                            end = true;

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
                            end = true;

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
                        end = true;
                    }
                }
            }
        } else {
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
                System.out.println("I moved!");
                int[] coords = Communication.coordDecoder(role);
                MapLocation currentLocation = rc.getLocation();
                coords[0] += currentLocation.x;
                coords[1] += currentLocation.y;
                System.out.println("ENEMY TARGET: " + coords[0] + "," + coords[1]);

                MapLocation targetLocation = new MapLocation(coords[0], coords[1]);
                Direction targetDirection = currentLocation.directionTo(targetLocation);

                if (rc.canMove(targetDirection)) {
                    rc.move(targetDirection);
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
                            System.out.println(role);
                            if (role.equals("100")) {
                                scoutDirection = rc.getLocation().directionTo(robot.getLocation()).opposite();
                                originPoint = robot.getLocation();
                            }
                        }
                    }
                }
            }
        }
    }
}
