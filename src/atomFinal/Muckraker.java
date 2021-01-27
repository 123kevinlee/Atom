package atomFinal;

import battlecode.common.*;

public class Muckraker {

    public static Direction scoutDirection = null;
    public static boolean end = false;
    public static String role = "";

    public static void run(RobotController rc) throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        MapLocation thisLocation = rc.getLocation();
        if (rc.canDetectRadiusSquared(30)) {
            RobotInfo[] closeRobots = rc.senseNearbyRobots(12, enemy);
            if (closeRobots.length > 0) {
                int maxInf = 0;
                MapLocation max = null;
                for (RobotInfo robot : closeRobots) {
                    if (robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)
                            && rc.getLocation().isAdjacentTo(robot.getLocation())) {
                        return;
                    }
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
                //nextDir = Pathfinding.smartNav(rc, max);
                if (rc.canMove(nextDir)) {
                    rc.move(nextDir);
                }
            }
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
        Direction[] directions = Data.directions;
        if (scoutDirection == null) {
            scoutDirection = directions[(int) (Math.random() * 8)];
        } else {
            if (rc.canSenseRadiusSquared(30)) {
                RobotInfo[] robots = rc.senseNearbyRobots(30);
                for (RobotInfo robot : robots) {
                    if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER
                            && robot.getTeam() == rc.getTeam().opponent()) {
                        Direction nextDir = Pathfinding.basicBug(rc, robot.getLocation());
                        //nextDir = Pathfinding.smartNav(rc, robot.getLocation());
                        if (rc.canMove(nextDir)) {
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
                Direction nextDir = Pathfinding.basicBug(rc, scoutDirection);
                if (!rc.onTheMap(rc.getLocation().add(scoutDirection))) {
                    scoutDirection = scoutDirection.opposite().rotateRight();
                } else if (rc.canMove(nextDir)) {
                    rc.move(nextDir);
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
                    //nextDir = Pathfinding.smartNav(rc, robot.getLocation());
                    if (rc.canMove(nextDir) && rc.getLocation().distanceSquaredTo(robot.getLocation()) > 4) {
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
        //nextDir = Pathfinding.smartNav(rc, target);
        if (rc.canSenseLocation(target)) {
            RobotInfo robot = rc.senseRobotAtLocation(target);
            if (!robot.getTeam().equals(rc.getTeam())) {
                RobotInfo[] robotsEE = rc.senseNearbyRobots(30);
                for (RobotInfo robotEE : robotsEE) {
                    if (robotEE.getType() == RobotType.ENLIGHTENMENT_CENTER
                            && robotEE.getTeam() == rc.getTeam().opponent()) {
                        robot = robotEE;
                    }
                }
            }
            if (robot != null && robot.getTeam().equals(rc.getTeam())) {
                role = "";
            }
            Direction[] around = Data.directions;
            Direction openSpot = null;
            boolean surrounded = true;
            for (Direction dir : around) {
                RobotInfo robotE = rc.senseRobotAtLocation(target.add(dir));
                if (robotE != null && !robotE.getTeam().equals(rc.getTeam())) {
                    surrounded = false;
                    openSpot = dir;
                    break;
                }
            }
            if (surrounded) {
                role = "";
            }
            if (openSpot != null) {
                MapLocation spot = target.add(openSpot);
                int relx = spot.x % 128;
                int rely = spot.y % 128;
                int newFlag = Communication.coordEncoder("ENEMY", relx, rely);
                if (rc.canSetFlag(newFlag)) {
                    rc.setFlag(newFlag);
                    role = Integer.toString(newFlag);
                }
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
