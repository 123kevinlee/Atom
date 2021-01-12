package atomAlpha;

import battlecode.common.*;

public class Slanderer {
    public static Direction scoutDirection;
    public static String cornerRole = "";
    public static boolean end = false;
    public static String role = "";

    public static void run(RobotController rc) throws GameActionException {
        if (role.equals("100")) { // scout
            scoutMode(rc);
        } else if (role.length() == 7) {
            farmMode(rc);
        } else if (role.equals("102")) {
            nearFarm(rc);
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
        cornerRole = role;

        Team enemy = rc.getTeam().opponent();
        Team home = rc.getTeam();

        boolean muckrakerThreat = false;
        boolean getAwayEC = false;

        // System.out.print("can sense radius 20" + rc.canSenseRadiusSquared(20));
        RobotInfo[] fleeRaker = rc.senseNearbyRobots(20, enemy);
        RobotInfo[] awayEC = rc.senseNearbyRobots(15, home);
        int priorityEnemy = -1;

        for (int i = 0; i < fleeRaker.length; i++) {
            if (fleeRaker[i].getType() == RobotType.MUCKRAKER) {
                priorityEnemy = i;
                muckrakerThreat = true;
                break;
            }
        }

        if (muckrakerThreat == false) {
            for (int i = 0; i < awayEC.length; i++) {
                if (awayEC[i].getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    priorityEnemy = i;
                    getAwayEC = true;
                    break;
                }
            }
        }

        if (priorityEnemy > -1) {
            MapLocation myLoc = rc.getLocation();
            MapLocation destination = myLoc;

            if (muckrakerThreat) {
                // FLEE THE MUCKRAKER

                RobotInfo closeObject = fleeRaker[priorityEnemy];
                MapLocation track = closeObject.getLocation();

                // System.out.println("FLEEING MUCKRAKER");

                // int[] tracked = new int[2];
                // tracked[0] += track.x;
                // tracked[1] += track.y;

                // System.out.println("ENEMY MUCKRAKER: " + tracked[0] + "," + tracked[1]);

                Direction toObject = myLoc.directionTo(track);
                Direction away = toObject.opposite();

                for (int i = 0; i < 2; i++) {
                    destination = destination.add(away);
                }

                // System.out.println("direction to run: " + away);

            } else if (getAwayEC) {
                // Move away from ec at the opposite and rotated 45 direction

                RobotInfo closeObject = awayEC[priorityEnemy];
                MapLocation track = closeObject.getLocation();

                // System.out.println("MOVING AWAY FROM HOME BASE");

                // int[] tracked = new int[2];
                // tracked[0] += track.x;
                // tracked[1] += track.y;

                // System.out.println("Home Base: " + tracked[0] + "," + tracked[1]);

                Direction toObject = myLoc.directionTo(track);
                Direction awayObject = toObject.opposite();
                Direction away = awayObject.rotateRight();

                //System.out.print("to base: " + toObject + " Away from base: " + awayObject + " w rotation: " + away);

                for (int i = 0; i < 2; i++) {
                    destination = destination.add(away);
                }
            }

            //System.out.print(destination.x + ", " + destination.y);

            if (rc.canMove(Pathfinding.basicBugToBase(rc, destination))) {
                rc.move(Pathfinding.basicBugToBase(rc, destination));
            }

        } else {

            // System.out.println("the stored corner is in role: " + cornerRole);
            int[] coords = Communication.coordDecoder(role); // coords of safe corner
            coords[0] += Data.originPoint.x;
            coords[1] += Data.originPoint.y;
            // System.out.println("SAFE CORNER: " + coords[0] + "," + coords[1]);
            MapLocation targetLocation = new MapLocation(coords[0], coords[1]);
            Direction targetDirection = Data.originPoint.directionTo(targetLocation);
            if (Data.slandererConvertDirection == Direction.CENTER) {
                Data.slandererConvertDirection = targetDirection.opposite();
            }
            //System.out.println(rc.canMove(Pathfinding.basicBugToBase(rc, targetLocation)));
            if (rc.canMove(Pathfinding.basicBugToBase(rc, targetLocation))) {
                rc.move(Pathfinding.basicBugToBase(rc, targetLocation));
            }
        }

    }

    public static void nearFarm(RobotController rc) throws GameActionException {
        //only sense action radius
        Direction scatterDir = Data.directions[(int) (Math.random() * 8)];
        //System.out.println(scatterDir);
        int boundary = 4;
        MapLocation target = Data.originPoint;
        for (int i = 0; i < boundary; i++) {
            target = target.add(scatterDir);
        }
        if (rc.canSenseRadiusSquared(20)) {
            RobotInfo[] robots = rc.senseNearbyRobots(20, rc.getTeam().opponent());
            for (RobotInfo robot : robots) {
                if (robot.getType().equals(RobotType.MUCKRAKER)) {
                    Direction nextDir = Pathfinding.basicBugToBase(rc,
                            rc.getLocation().add(rc.getLocation().directionTo(robot.getLocation()).opposite()));
                    if (rc.canMove(nextDir)) {
                        rc.move(nextDir);
                    }
                }
            }
        }

        Direction nextDir = Pathfinding.basicBugToBase(rc, target);
        if (rc.canMove(nextDir)) {
            rc.move(nextDir);
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
