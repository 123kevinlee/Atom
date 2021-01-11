package atomAlpha;

import battlecode.common.*;

public class Politician {
    public static String role = "";
    public static MapLocation originPoint;

    public static void run(RobotController rc) throws GameActionException {

        boolean ecException = false;

        int sensorRadiusSquared = 25;
        int actionRadiusSquared = 9;

        int desiredActionRadius = 3;
        int defenseRadius = 9;

        Team enemy = rc.getTeam().opponent();

        RobotInfo[] attackable = rc.senseNearbyRobots(sensorRadiusSquared, enemy);

        for (RobotInfo robot : attackable) {
            if (robot.getTeam() == Team.NEUTRAL || (robot.getType() == RobotType.ENLIGHTENMENT_CENTER
                    && robot.getTeam() == rc.getTeam().opponent())) {
                if (robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 9)) {
                    if (rc.canEmpower(9)) {
                        rc.empower(9);
                    } else {
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
                }
            }
        }

        //logic for politicians that just converted from enemy politician or a slanderer
        if (role.equals("") || role.equals("1")) {
            if (rc.canSetFlag(1)) {
                rc.setFlag(1);
                role = "1";
            }

            //logic for converted enemy politicians
            if (Data.slandererConvertDirection.equals(Direction.CENTER)) {
                isConvertedEnemy(rc);
            } else {
                isConvertedSlanderer(rc);
            }
        }
        // System.out.println(role);
        else if (role.length() == 7) {
            if (rc.canSenseRadiusSquared(25)) {
                RobotInfo[] robots = rc.senseNearbyRobots(25);
                // System.out.println(robots.toString());
                for (RobotInfo robot : robots) {
                    if (robot.getTeam() == Team.NEUTRAL || (robot.getType() == RobotType.ENLIGHTENMENT_CENTER
                            && robot.getTeam() == rc.getTeam().opponent())) {
                        Direction nextDir = Pathfinding.basicBugToBase(rc, robot.getLocation());
                        if (rc.canMove(nextDir)) {
                            rc.move(nextDir);
                        }
                        // System.out.println("NEW TARGET");
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
                            if (allyFlag.charAt(0) == '2') {
                                String ending = Integer.toString(rc.getFlag(rc.getID())).substring(1);
                                if (!allyFlag.substring(1).equals(ending)) {
                                    role = allyFlag;
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
            if (attackable.length != 0) {
                // The int below discerns which enemy to attack first in the RobotInfo array
                int priorityEnemy = 0;

                for (int i = 0; i < attackable.length; i++) {
                    if (attackable[i].getType() == RobotType.ENLIGHTENMENT_CENTER) {
                        ecException = true;
                        priorityEnemy = i;
                        desiredActionRadius = 1;
                        break;
                    }
                }

                for (int i = 0; i < attackable.length; i++) {
                    if (attackable[i].getType() == RobotType.ENLIGHTENMENT_CENTER) {
                        ecException = true;
                        priorityEnemy = i;
                        desiredActionRadius = 1;
                        break;
                    }
                }

                if (ecException != true) {
                    for (int i = 0; i < attackable.length; i++) {
                        if ((rc.getInfluence() - attackable[i].getInfluence()) >= 11) {
                            priorityEnemy = i;
                            break;
                        } else {
                            priorityEnemy = -1;
                        }
                    }

                }

                System.out.println(priorityEnemy);

                if (priorityEnemy != -1) {
                    // System.out.println("TRACKING");

                    RobotInfo closeEnemy = attackable[priorityEnemy];

                    MapLocation myLoc = rc.getLocation();
                    MapLocation track = closeEnemy.getLocation();
                    int[] tracked = new int[2];

                    tracked[0] += track.x;
                    tracked[1] += track.y;

                    // System.out.println("ENEMY ROBOT: " + tracked[0] + "," + tracked[1]);

                    Direction toCloseEnemy = myLoc.directionTo(track);
                    if (myLoc.distanceSquaredTo(track) <= desiredActionRadius && rc.canEmpower(desiredActionRadius)) {
                        rc.empower(desiredActionRadius);
                        // System.out.println("Empowered");

                    } else if (rc.canMove(Pathfinding.basicBugToBase(rc, track))) {
                        rc.move(Pathfinding.basicBugToBase(rc, track));
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
            }

            int[] coords = Communication.coordDecoder(role);
            MapLocation currentLocation = rc.getLocation();
            coords[0] += Data.originPoint.x;
            coords[1] += Data.originPoint.y;
            // System.out.println("ENEMY TARGET: " + coords[0] + "," + coords[1]);
            MapLocation targetLocation = new MapLocation(coords[0], coords[1]);
            if (rc.canSenseLocation(targetLocation)) {
                RobotInfo robot = rc.senseRobotAtLocation(targetLocation);
                if (robot != null && robot.getTeam() == rc.getTeam()
                        && robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    // System.out.println(robot.getTeam());
                    // System.out.println("MISSING OR CONVERTED");
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

        //4 corner defender logic
        // else if (role.equals("111")) {
        //     cornerDefenderLogic(rc);
        // }

        //scatter defender logic
        else if (role.substring(0, 3).equals("112")) {
            scatterDefenderLogic(rc);
        }
    }

    public static void scatterDefenderLogic(RobotController rc) throws GameActionException {
        //only sense action radius
        if (rc.canSenseRadiusSquared(9)) {
            RobotInfo[] robots = rc.senseNearbyRobots(9, rc.getTeam().opponent());
            for (RobotInfo robot : robots) {
                if (robot.getType().equals(RobotType.MUCKRAKER) && rc.getInfluence() > robot.getInfluence() + 10) {
                    if (rc.canEmpower(rc.getLocation().distanceSquaredTo(robot.getLocation()))) {
                        rc.empower(rc.getLocation().distanceSquaredTo(robot.getLocation()));
                    }
                }
            }
        }

        if (rc.getLocation().distanceSquaredTo(Data.originPoint) > 24) {
            Direction dirBack = rc.getLocation().directionTo(Data.originPoint);
            if (rc.canMove(dirBack)) {
                rc.move(dirBack);
            }
        } else {
            Direction[] directions = new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH,
                    Direction.WEST };
            Direction scatterDir = directions[Integer.parseInt(Character.toString((role.charAt(3))))];
            //System.out.println(scatterDir);
            int boundary = 24;
            MapLocation target = Data.originPoint;
            for (int i = 0; i < boundary; i++) {
                target = target.add(scatterDir);
            }
            Direction nextDir = Pathfinding.basicBugToBase(rc, target);
            if (rc.canMove(nextDir)) {
                //System.out.println("Next Dir:" + nextDir);
                rc.move(nextDir);
            }
        }
    }

    public static void cornerDefenderLogic(RobotController rc) throws GameActionException {
        // if (Pathfinding.getDefenseReached() == false && rc.isReady()) {
        //     if (!rc.getLocation().equals(Data.originPoint.add(Direction.NORTHEAST))
        //             || !rc.getLocation().equals(Data.originPoint.add(Direction.NORTHWEST))
        //             || !rc.getLocation().equals(Data.originPoint.add(Direction.SOUTHEAST))
        //             || !rc.getLocation().equals(Data.originPoint.add(Direction.SOUTHWEST))) {
        //         // Pathfinding.chooseRandomDefenseLocation(rc, Data.originPoint);
        //     }
        //     Pathfinding.findDefenseLocation(rc, Data.originPoint);
        // }
        // // create a locking mechanism and chasing mechanism
        // if (rc.canSenseRadiusSquared(9) && defensible.length > 0 && rc.canEmpower(9)) {
        //     // System.out.println("EMPOWER");
        //     rc.empower(9);
        // }
    }

    public static void isConvertedSlanderer(RobotController rc) throws GameActionException {
        //for now, slanderer -> politician will join the defense
        if (!Data.originPoint.equals(new MapLocation(0, 0))) {
            int scatterDir = (int) (Math.random() * 3);
            String msg = "112" + Integer.toString(scatterDir);
            role = msg;
        }
        /* if (rc.canGetFlag(Data.baseId)) {
            String baseFlag = Integer.toString(rc.getFlag(Data.baseId));
            if (baseFlag.charAt(0) == '2' && baseFlag.length() == 7) {
                if (rc.canSetFlag(Integer.parseInt(baseFlag))) {
                    rc.setFlag(Integer.parseInt(baseFlag));
                    role = baseFlag;
                }
            }
            //if unit could not get attacking orders from home ec, it will try nearby allied units
            else if (rc.canSenseRadiusSquared(25)) {
                RobotInfo robots[] = rc.senseNearbyRobots(25, rc.getTeam());
                for (RobotInfo robot : robots) {
                    if (rc.canGetFlag(robot.getID())) {
                        String allyFlag = Integer.toString(rc.getFlag(robot.getID()));
                        if (allyFlag.charAt(0) == '2' && allyFlag.length() == 7) {
                            if (rc.canSetFlag(Integer.parseInt(allyFlag))) {
                                rc.setFlag(Integer.parseInt(allyFlag));
                                role = allyFlag;
                            }
                        } else {
                            if (rc.canSenseRobot(Data.baseId)) {
                                int scatterDir = (int) (Math.random() * 3);
                                String msg = "112" + Integer.toString(scatterDir);
                                role = msg;
                            }
                        }
                    }
                }
            }
        } */
    }

    public static void isConvertedEnemy(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(25)) {
            RobotInfo robots[] = rc.senseNearbyRobots(25, rc.getTeam());
            for (RobotInfo robot : robots) {
                if (rc.canGetFlag(robot.getID())) {
                    String allyFlag = Integer.toString(rc.getFlag(robot.getID()));
                    //attempts to get any attacking orders from other allied units
                    if ((allyFlag.charAt(0) == '2' & allyFlag.length() == 7)) {
                        if (rc.canSetFlag(Integer.parseInt(allyFlag))) {
                            rc.setFlag(Integer.parseInt(allyFlag));
                            role = allyFlag;
                        }
                    } else {
                        Direction randomDirection = Data.directions[(int) (Math.random() * Data.directions.length)];
                        if (rc.canMove(randomDirection)) {
                            rc.move(randomDirection);
                        }
                    }
                }
            }
        }
    }

    public static void init(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(3)) {
            for (RobotInfo robot : rc.senseNearbyRobots(3, rc.getTeam())) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    Data.baseId = robot.getID();
                    if (rc.canGetFlag(Data.baseId)) {
                        if (rc.canSetFlag(rc.getFlag(Data.baseId))) {
                            // Pathfinding.setStartLocation(rc);
                            role = Integer.toString(rc.getFlag(Data.baseId));
                            Data.originPoint = robot.getLocation();
                            Data.initRound = rc.getRoundNum();
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
