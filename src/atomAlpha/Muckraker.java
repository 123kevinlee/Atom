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
            //if scout can't move, it means it hit a wall
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
                        // end = true;
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
                        // end = true;
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
                        // end = true;
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
                        // end = true;
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
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius)) {
                // System.out.println(robot.toString());
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER && robot.getTeam() == rc.getTeam().opponent()) {
                    MapLocation baseLocation = robot.getLocation();
                    int dx = baseLocation.x - Data.originPoint.x;
                    int dy = baseLocation.y - Data.originPoint.y;
                    int outMsg = Communication.coordEncoder("ENEMY", dx, dy);
                    // System.out.println("Found Enemy Base:" + outMsg);
                    if (rc.canSetFlag(outMsg)) {
                        rc.setFlag(outMsg);
                    }
                    // end = true;
                } else if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER && robot.getTeam() == Team.NEUTRAL) {
                    // System.out.println("NUETRAL");
                    MapLocation baseLocation = robot.getLocation();
                    int dx = baseLocation.x - Data.originPoint.x;
                    int dy = baseLocation.y - Data.originPoint.y;
                    int outMsg = Communication.coordEncoder("NEUTRAL", dx, dy);
                    // System.out.println("Found Enemy Base:" + outMsg);
                    if (rc.canSetFlag(outMsg)) {
                        rc.setFlag(outMsg);
                    }
                    // end = true;
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
                        MapLocation newTarget = robot.getLocation();
                        int dx = newTarget.x - Data.originPoint.x;
                        int dy = newTarget.y - Data.originPoint.y;
                        if (dx < 65 && dy < 65) {
                            int newFlag = Communication.coordEncoder("ENEMY", dx, dy);
                            if (rc.canSetFlag(newFlag)) {
                                rc.setFlag(newFlag);
                                role = Integer.toString(newFlag);
                            }
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
                            if (allyFlag.charAt(0) == '2' && thisFlag.charAt(0) == '3') {
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
            if (role.charAt(0) == '3') {
                Direction[] directions = Data.directions;
                Direction randDirection = directions[(int) (Math.random() * directions.length)];
                if (rc.canMove(randDirection)) {
                    rc.move(randDirection);
                } else {
                    for (int i = 0; i < 8; i++) {
                        if (rc.canMove(directions[i])) {
                            rc.move(directions[i]);
                        }
                    }
                }
            } else if (role.charAt(0) == '2') {
                int[] coords = Communication.coordDecoder(role);
                coords[0] += Data.originPoint.x;
                coords[1] += Data.originPoint.y;
                // System.out.println("ENEMY TARGET: " + coords[0] + "," + coords[1]);

                MapLocation targetLocation = new MapLocation(coords[0], coords[1]);
                if (rc.canSenseLocation(targetLocation)) {
                    RobotInfo robot = rc.senseRobotAtLocation(targetLocation);
                    if (robot != null && robot.getTeam() == rc.getTeam()
                            && robot.getType() == RobotType.ENLIGHTENMENT_CENTER) { // System.out.println("MISSING OR CONVERTED");
                        String convertMsg = "3" + role.substring(1);
                        if (rc.canSetFlag(Integer.parseInt(convertMsg))) {
                            rc.setFlag(Integer.parseInt(convertMsg));
                            role = convertMsg;
                        }
                    }
                }
                Direction nextDir = Pathfinding.basicBugToBase(rc, targetLocation);
                if (rc.canMove(nextDir)) {
                    rc.move(nextDir);
                }
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
