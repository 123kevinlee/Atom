package atomFinal;

import battlecode.common.*;

public class Muckraker {

    public static Direction scoutDirection;
    public static boolean end = false;
    public static String role = "";

    public static void run(RobotController rc) throws GameActionException {
        //System.out.println("HERE");
        Team enemy = rc.getTeam().opponent();
        MapLocation thisLocation = rc.getLocation();
        if (rc.canDetectRadiusSquared(30)) {
            RobotInfo[] closeRobots = rc.senseNearbyRobots(12, enemy);
            if (closeRobots.length > 0) {
                int maxInf = 0;
                MapLocation max = null;
                for (RobotInfo robot : closeRobots) {
                    //System.out.println("FOUND ROBOT");
                    if (robot.getType().canBeExposed() && robot.getInfluence() >= maxInf) {
                        max = robot.location;
                    }
                }
                if (max != null && rc.canExpose(max)) {
                    rc.expose(max);
                }
            }
            RobotInfo[] robots = rc.senseNearbyRobots(30, enemy);
            MapLocation max = null;
            int maxInf = 0;
            for (RobotInfo robot : robots) {
                if (robot.getType().canBeExposed() && robot.getInfluence() >= maxInf) {
                    max = robot.location;
                }
            }
            if (max != null) {
                Direction nextDir = Pathfinding.basicBug(rc, max);
                if (rc.canMove(nextDir)) {
                    rc.move(nextDir);
                }
            }

            // RobotInfo[] robots = rc.senseNearbyRobots(30, enemy);
            // MapLocation max;
            // for (RobotInfo robot : robots) {
            //     //System.out.println("FOUND ROBOT");
            //     if (robot.getType().canBeExposed()) {
            //         if (rc.canExpose(robot.location)) {
            //             //System.out.println("EXPOSING");
            //             rc.expose(robot.location);
            //         } else {
            //             Direction nextDir = Pathfinding.basicBug(rc, robot.getLocation());
            //             if (rc.canMove(nextDir)) {
            //                 rc.move(nextDir);
            //             }
            //         }
            //     }
            // }
        }

        if (role.equals("100")) { // scout
            scoutMode(rc);
        } else if (role.length() == 7) {
            attackMode(rc);
        } else {
            logic(rc);
        }
    }

    public static void logic(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(25)) {
            RobotInfo[] robots = rc.senseNearbyRobots(25, rc.getTeam().opponent());
            if (robots.length > 0 && (rc.getFlag(rc.getID()) == 0 || rc.getFlag(rc.getID()) == 666)) {
                if (rc.canSetFlag(666)) {
                    rc.setFlag(666);
                }
            } else {
                if (rc.canSetFlag(0)) {
                    rc.setFlag(0);
                }
            }
        }
        //System.out.println("HERE");
        Direction[] directions = Data.directions;
        if (scoutDirection == null) {
            scoutDirection = directions[(int) (Math.random() * 8)];
            //System.out.println("SCOUTDIRECTION:" + scoutDirection);
        } else {
            if (rc.canSenseRadiusSquared(30)) {
                RobotInfo[] robots = rc.senseNearbyRobots(30);
                for (RobotInfo robot : robots) {
                    if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER
                            && robot.getTeam() == rc.getTeam().opponent()) {
                        Direction nextDir = Pathfinding.basicBug(rc, robot.getLocation());
                        if (rc.canMove(nextDir) && rc.getLocation().distanceSquaredTo(robot.getLocation()) > 2) {
                            rc.move(nextDir);
                        }

                        //mostly for when mucks are going to possible coords
                        //and they are wrong, so they switch targets
                        //System.out.println("NEW TARGET");
                        int relx = robot.getLocation().x % 128;
                        int rely = robot.getLocation().y % 128;
                        int newFlag = Communication.coordEncoder("ENEMY", relx, rely);
                        if (rc.canSetFlag(newFlag)) {
                            rc.setFlag(newFlag);
                            role = Integer.toString(newFlag);
                        }
                    } else if (robot.getTeam().equals(rc.getTeam())
                            && rc.getLocation().distanceSquaredTo(robot.getLocation()) < 4) {
                        Direction nextDir = Pathfinding.basicBug(rc,
                                rc.getLocation().directionTo(robot.getLocation()).opposite());
                        if (rc.canMove(nextDir)) {
                            rc.move(nextDir);
                        }
                    }
                }
                if (!rc.onTheMap(rc.getLocation().add(scoutDirection))) {
                    scoutDirection = scoutDirection.opposite().rotateRight();
                } else if (rc.canMove(scoutDirection)) {
                    rc.move(scoutDirection);
                    //System.out.println("MOVED SCOUTDIRECTION:" + scoutDirection);
                } else {
                    for (Direction dir : directions) {
                        if (rc.canMove(dir)) {
                            rc.move(dir);
                        }
                    }
                }
            }
        }
    }

    public static void scoutMode(RobotController rc) throws GameActionException {
        if (!rc.onTheMap(rc.getLocation().add(scoutDirection))) {
            MapLocation currentLocation = rc.getLocation();
            int relx = currentLocation.x % 128;
            int rely = currentLocation.y % 128;
            int outMsg = Communication.coordEncoder("WALL", relx, rely);
            if (rc.canSetFlag(outMsg)) {
                rc.setFlag(outMsg);
            }
            scoutDirection = scoutDirection.opposite();
            Direction nextDir = Pathfinding.basicBug(rc, scoutDirection);
            if (rc.canMove(nextDir)) {
                rc.move(nextDir);
            }
        }

        Direction nextDir = Pathfinding.scoutBug(rc, scoutDirection);
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
                    int influence = robot.getInfluence();
                    int outMsg = Communication.neutralCoordEncoder(influence, relx, rely);
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
                // else if (robot.getTeam().equals(rc.getTeam().opponent())
                //         && Integer.toString(rc.getFlag(rc.getID())).charAt(0) != '2') {
                //     MapLocation robotLocation = robot.getLocation();
                //     int relx = robotLocation.x % 128;
                //     int rely = robotLocation.y % 128;
                //     int outMsg = Communication.coordEncoder("WARN", relx, rely);
                //     if (rc.canSetFlag(outMsg)) {
                //         rc.setFlag(outMsg);
                //     }
                // }
            }
        }
    }

    public static void attackMode(RobotController rc) throws GameActionException {
        //System.out.println("HERE");
        if (rc.canSenseRadiusSquared(30)) {
            RobotInfo[] robots = rc.senseNearbyRobots(30);
            for (RobotInfo robot : robots) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER && robot.getTeam() == rc.getTeam().opponent()) {
                    Direction nextDir = Pathfinding.basicBug(rc, robot.getLocation());
                    if (rc.canMove(nextDir) && rc.getLocation().distanceSquaredTo(robot.getLocation()) > 2) {
                        rc.move(nextDir);
                    }

                    //mostly for when mucks are going to possible coords
                    //and they are wrong, so they switch targets
                    //System.out.println("NEW TARGET");
                    int relx = robot.getLocation().x % 128;
                    int rely = robot.getLocation().y % 128;
                    int newFlag = Communication.coordEncoder("ENEMY", relx, rely);
                    if (rc.canSetFlag(newFlag)) {
                        rc.setFlag(newFlag);
                        role = Integer.toString(newFlag);
                    }
                }
            }
        }
        int[] coords = Communication.relCoordDecoder(role);
        int[] distance = Pathfinding.getDistance(Data.relOriginPoint, coords);
        MapLocation target = Data.originPoint.translate(distance[0], distance[1]);
        Direction nextDir = Pathfinding.basicBug(rc, target);
        if (rc.canSenseLocation(target)) {
            RobotInfo robot = rc.senseRobotAtLocation(target);
            if (robot.getTeam().equals(rc.getTeam())) {
                role = "";
            }

        }
        if (rc.canMove(nextDir)) {
            rc.move(nextDir);
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
