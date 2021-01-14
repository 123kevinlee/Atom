package atom;

import java.nio.charset.CoderResult;

import battlecode.common.*;

public class Muckraker {

    public static Direction scoutDirection;
    public static boolean end = false;
    public static String role = "";

    public static void run(RobotController rc) throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        if (rc.canSenseRobot(12)) {
            for (RobotInfo robot : rc.senseNearbyRobots(12, enemy)) {
                if (robot.type.canBeExposed()) {
                    if (rc.canExpose(robot.location)) {
                        rc.expose(robot.location);
                        return;
                    }
                }
            }
        }

        if (role.equals("100")) { // scout
            scoutMode(rc);
        } else {
            attackMode(rc);
        }
    }

    public static void scoutMode(RobotController rc) throws GameActionException {
        if (!rc.onTheMap(rc.getLocation().add(scoutDirection))) {
            switch (scoutDirection) {
                case NORTH:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.NORTH))) {
                        MapLocation currentLocation = rc.getLocation();
                        int relx = currentLocation.x % 128;
                        int rely = currentLocation.y % 128;
                        int outMsg = Communication.coordEncoder("WALL", relx, rely);
                        if (rc.canSetFlag(outMsg)) {
                            rc.setFlag(outMsg);
                        }
                        scoutDirection = scoutDirection.opposite();
                        Direction nextDir = Pathfinding.basicBugToBase(rc,
                                rc.getLocation().add(scoutDirection).add(scoutDirection));
                        if (rc.canMove(nextDir)) {
                            rc.move(nextDir);
                        }
                    }
                    break;
                case EAST:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.EAST))) {
                        MapLocation currentLocation = rc.getLocation();
                        int relx = currentLocation.x % 128;
                        int rely = currentLocation.y % 128;
                        int outMsg = Communication.coordEncoder("WALL", relx, rely);
                        if (rc.canSetFlag(outMsg)) {
                            rc.setFlag(outMsg);
                        }
                        scoutDirection = scoutDirection.opposite();
                        Direction nextDir = Pathfinding.basicBugToBase(rc,
                                rc.getLocation().add(scoutDirection).add(scoutDirection));
                        if (rc.canMove(nextDir)) {
                            rc.move(nextDir);
                        }
                    }
                    break;
                case SOUTH:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.SOUTH))) {
                        MapLocation currentLocation = rc.getLocation();
                        int relx = currentLocation.x % 128;
                        int rely = currentLocation.y % 128;
                        int outMsg = Communication.coordEncoder("WALL", relx, rely);
                        if (rc.canSetFlag(outMsg)) {
                            rc.setFlag(outMsg);
                        }
                        scoutDirection = scoutDirection.opposite();
                        Direction nextDir = Pathfinding.basicBugToBase(rc,
                                rc.getLocation().add(scoutDirection).add(scoutDirection));
                        if (rc.canMove(nextDir)) {
                            rc.move(nextDir);
                        }
                    }
                    break;
                case WEST:
                    if (!rc.onTheMap(rc.getLocation().add(Direction.WEST))) {
                        MapLocation currentLocation = rc.getLocation();
                        int relx = currentLocation.x % 128;
                        int rely = currentLocation.y % 128;
                        int outMsg = Communication.coordEncoder("WALL", relx, rely);
                        if (rc.canSetFlag(outMsg)) {
                            rc.setFlag(outMsg);
                        }
                        scoutDirection = scoutDirection.opposite();
                        Direction nextDir = Pathfinding.basicBugToBase(rc,
                                rc.getLocation().add(scoutDirection).add(scoutDirection));
                        if (rc.canMove(nextDir)) {
                            rc.move(nextDir);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        Direction nextDir = Pathfinding.basicBugToBase(rc, rc.getLocation().add(scoutDirection).add(scoutDirection));
        if (rc.canMove(nextDir)) {
            rc.move(nextDir);
            MapLocation currentLocation = rc.getLocation();
            int relx = currentLocation.x % 128;
            int rely = currentLocation.y % 128;
            int outMsg = Communication.coordEncoder("BEACON", relx, rely);
            if (rc.canSetFlag(outMsg)) {
                rc.setFlag(outMsg);
            }
        }

        int sensorRadius = rc.getType().sensorRadiusSquared;
        if (rc.canSenseRadiusSquared(sensorRadius)) {
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius)) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER && robot.getTeam() == rc.getTeam().opponent()) {
                    MapLocation robotLocation = robot.getLocation();
                    int relx = robotLocation.x % 128;
                    int rely = robotLocation.y % 128;
                    int outMsg = Communication.coordEncoder("ENEMY", relx, rely);
                    if (rc.canSetFlag(outMsg)) {
                        rc.setFlag(outMsg);
                    }
                } else if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER && robot.getTeam() == Team.NEUTRAL) {
                    MapLocation robotLocation = robot.getLocation();
                    int relx = robotLocation.x % 128;
                    int rely = robotLocation.y % 128;
                    int outMsg = Communication.coordEncoder("NEUTRAL", relx, rely);
                    if (rc.canSetFlag(outMsg)) {
                        rc.setFlag(outMsg);
                    }
                } else if (robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)
                        && robot.getTeam().equals(rc.getTeam()) && robot.getID() != Data.baseId) {
                    MapLocation robotLocation = robot.getLocation();
                    int relx = robotLocation.x % 128;
                    int rely = robotLocation.y % 128;
                    int outMsg = Communication.coordEncoder("ALLY", relx, rely);
                    if (rc.canSetFlag(outMsg)) {
                        rc.setFlag(outMsg);
                    }
                }
            }
        }
    }

    public static void attackMode(RobotController rc) throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;

        if (rc.canSenseRadiusSquared(actionRadius)) {
            for (RobotInfo robot : rc.senseNearbyRobots(actionRadius)) {
                if (robot.type.canBeExposed() && robot.getTeam().equals(enemy)) {
                    // It's a slanderer
                    if (rc.canExpose(robot.location)) {
                        rc.expose(robot.location);
                        return;
                    }
                }
            }
        }
        if (role.length() == 7) {
            if (rc.canSenseRadiusSquared(30)) {
                RobotInfo[] robots = rc.senseNearbyRobots(30);
                for (RobotInfo robot : robots) {
                    if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER
                            && robot.getTeam() == rc.getTeam().opponent()) {
                        Direction nextDir = Pathfinding.basicBugToBase(rc, robot.getLocation());
                        if (rc.canMove(nextDir)) {
                            rc.move(nextDir);
                        }

                        //mostly for when mucks are going to possible coords
                        //and they are wrong, so they switch targets
                        //System.out.println("NEW TARGET");
                        MapLocation currentLocation = rc.getLocation();
                        int relx = currentLocation.x % 128;
                        int rely = currentLocation.y % 128;
                        int newFlag = Communication.coordEncoder("ENEMY", relx, rely);
                        if (rc.canSetFlag(newFlag)) {
                            rc.setFlag(newFlag);
                            role = Integer.toString(newFlag);
                        }
                    }
                    if (robot.getTeam() == rc.getTeam()) {
                        if (rc.canGetFlag(robot.getID())) {
                            String allyFlag = Integer.toString(rc.getFlag(robot.getID()));
                            String thisFlag = Integer.toString(rc.getFlag(rc.getID()));
                            //if a ally's flag has the same target coords but the prefix is 3, that means the ec has been converted
                            if (allyFlag.charAt(0) == '3') {
                                String ending = thisFlag.substring(1);
                                if (allyFlag.substring(1).equals(ending)) {
                                    if (rc.canSetFlag(Integer.parseInt(allyFlag))) {
                                        rc.setFlag(Integer.parseInt(allyFlag));
                                        role = allyFlag;
                                    }
                                }
                            }
                            //if a ally's flag has diff target coords and this unit has already converted its target, it will switch targets
                            //if (allyFlag.charAt(0) == '2' && thisFlag.charAt(0) == '3') {
                            if (allyFlag.charAt(0) == '2') {
                                String ending = Integer.toString(rc.getFlag(rc.getID())).substring(1);
                                if (!allyFlag.substring(1).equals(ending)) {
                                    if (rc.canSetFlag(Integer.parseInt(allyFlag))) {
                                        rc.setFlag(Integer.parseInt(allyFlag));
                                        role = allyFlag;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //if unit's ec has already been converted, make movement random 
            //***in the future it should run away and find another target***
            // if (role.charAt(0) == '3') {
            //     Direction[] directions = Data.directions;
            //     Direction randDirection = directions[(int) (Math.random() * directions.length)];
            //     if (rc.canMove(randDirection)) {
            //         rc.move(randDirection);
            //     } else {
            //         for (int i = 0; i < 8; i++) {
            //             if (rc.canMove(directions[i])) {
            //                 rc.move(directions[i]);
            //             }
            //         }
            //     }
            // } else if (role.charAt(0) == '2') {
            //     int[] coords = Communication.coordDecoder(role);
            //     coords[0] += Data.originPoint.x;
            //     coords[1] += Data.originPoint.y;
            //     // System.out.println("ENEMY TARGET: " + coords[0] + "," + coords[1]);

            //     MapLocation targetLocation = new MapLocation(coords[0], coords[1]);
            //     if (rc.canSenseLocation(targetLocation)) {
            //         RobotInfo robot = rc.senseRobotAtLocation(targetLocation);
            //         if (robot != null && robot.getTeam() == rc.getTeam()
            //                 && robot.getType() == RobotType.ENLIGHTENMENT_CENTER) { // System.out.println("MISSING OR CONVERTED");
            //             String convertMsg = "3" + role.substring(1);
            //             if (rc.canSetFlag(Integer.parseInt(convertMsg))) {
            //                 rc.setFlag(Integer.parseInt(convertMsg));
            //                 role = convertMsg;
            //             }
            //         }
            //     }
            //     Direction nextDir = Pathfinding.basicBugToBase(rc, targetLocation);
            //     if (rc.canMove(nextDir)) {
            //         rc.move(nextDir);
            //     }
            // }
        }
    }

    public static void init(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(3)) {
            for (RobotInfo robot : rc.senseNearbyRobots(3, rc.getTeam())) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    if (rc.canGetFlag(robot.getID())) {
                        Data.baseId = robot.getID();
                        Data.originPoint = robot.getLocation();
                        Data.relOriginPoint[0] = Data.originPoint.x % 128;
                        Data.relOriginPoint[1] = Data.originPoint.y % 128;
                        Data.initRound = rc.getRoundNum();
                        role = Integer.toString(rc.getFlag(robot.getID()));
                        if (rc.canSetFlag(rc.getFlag(robot.getID()))) {
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
