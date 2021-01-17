package atom;

import battlecode.common.*;

public class Muckraker {

    public static Direction scoutDirection;
    public static boolean end = false;
    public static String role = "";

    public static void run(RobotController rc) throws GameActionException {
        int baseFlag = -1;
        if (rc.canGetFlag(Data.baseId)) {
            baseFlag = rc.getFlag(Data.baseId);
            if (!role.equals("100")) {
                role = Integer.toString(baseFlag);
            }
        }
        //System.out.println(role);

        Team enemy = rc.getTeam().opponent();
        if (rc.canSenseRobot(12)) {
            for (RobotInfo robot : rc.senseNearbyRobots(12, enemy)) {
                if (robot.type.canBeExposed()) {
                    if (rc.canExpose(robot.location)) {
                        rc.expose(robot.location);
                    }
                }
            }
        }

        if (role.equals("100")) { // scout
            scoutMode(rc);
        } else if (role.length() == 7) {
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
                        Direction nextDir = Pathfinding.basicBug(rc,
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
                        Direction nextDir = Pathfinding.basicBug(rc,
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
                        Direction nextDir = Pathfinding.basicBug(rc,
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
                        Direction nextDir = Pathfinding.basicBug(rc,
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

        Direction nextDir = Pathfinding.basicBug(rc, rc.getLocation().add(scoutDirection).add(scoutDirection));
        if (rc.canMove(nextDir)) {
            rc.move(nextDir);
            MapLocation currentLocation = rc.getLocation();
            int relx = currentLocation.x % 128;
            int rely = currentLocation.y % 128;
            int outMsg = Communication.coordEncoder("BEACON", relx, rely);
            if (rc.canSetFlag(outMsg)) {
                rc.setFlag(outMsg);
            }
        } else {
            Direction[] directions = Data.directions;
            for (Direction dir : directions) {
                if (rc.onTheMap(rc.getLocation().add(dir)) == false) {
                    scoutDirection = scoutDirection.opposite();
                    nextDir = Pathfinding.basicBug(rc, rc.getLocation().add(scoutDirection).add(scoutDirection));
                    if (rc.canMove(nextDir)) {
                        rc.move(nextDir);
                        MapLocation currentLocation = rc.getLocation();
                        int relx = currentLocation.x % 128;
                        int rely = currentLocation.y % 128;
                    }
                }
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
                } else if (robot.getTeam().equals(rc.getTeam().opponent())
                        && Integer.toString(rc.getFlag(rc.getID())).charAt(0) != '2') {
                    MapLocation robotLocation = robot.getLocation();
                    int relx = robotLocation.x % 128;
                    int rely = robotLocation.y % 128;
                    int outMsg = Communication.coordEncoder("WARN", relx, rely);
                    if (rc.canSetFlag(outMsg)) {
                        rc.setFlag(outMsg);
                    }
                }
            }
        }
    }

    public static void attackMode(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(30)) {
            RobotInfo[] robots = rc.senseNearbyRobots(30);
            for (RobotInfo robot : robots) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER && robot.getTeam() == rc.getTeam().opponent()) {
                    Direction nextDir = Pathfinding.basicBug(rc, robot.getLocation());
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

            int[] coords = Communication.relCoordDecoder(role);
            int[] distance = Pathfinding.getDistance(Data.relOriginPoint, coords);
            MapLocation target = Data.originPoint.translate(distance[0], distance[1]);
            Direction nextDir = Pathfinding.basicBug(rc, target);
            if (rc.canMove(nextDir)) {
                rc.move(nextDir);
            }
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
