package atomStorm;

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

        RobotInfo[] defensible = rc.senseNearbyRobots(defenseRadius, enemy);
        RobotInfo[] attackable = rc.senseNearbyRobots(sensorRadiusSquared, enemy);

        if (rc.canGetFlag(Data.baseId)) {
            String baseFlag = Integer.toString(rc.getFlag(Data.baseId));
            if (baseFlag.charAt(0) == '2' && baseFlag.length() == 7) {
                if (rc.canSetFlag(Integer.parseInt(baseFlag))) {
                    rc.setFlag(Integer.parseInt(baseFlag));
                    role = baseFlag;
                }
            }
        }

        /* if (rc.getRoundNum() > 200 && rc.getRoundNum() % 5 == 0) {
            Data.politicianDefenderBoundary += 30;
        } */

        RobotInfo[] robots = rc.senseNearbyRobots(-1);
        for (RobotInfo robot : robots) {
            if (robot.getTeam().equals(rc.getTeam().opponent())) {
                System.out.println("ENEMY:" + robot.getLocation().toString());
            }
        }
        for (RobotInfo robot : robots) {
            if (robot.getTeam().equals(Team.NEUTRAL) || robot.getTeam().equals(rc.getTeam().opponent())) {
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
            } else if (robot.getTeam().equals(rc.getTeam().opponent())) {
                System.out.println("Found Bot");
                RobotInfo[] allies = rc.senseNearbyRobots(-1, rc.getTeam());
                boolean taken = false;
                for (RobotInfo ally : allies) {
                    if (rc.canGetFlag(ally.getID()) && robot.getID() == rc.getFlag(ally.getID())) {
                        //taken = true;
                    }
                }

                if (taken == false && rc.getInfluence() > robot.getInfluence() + 11) {
                    if (rc.canSetFlag(robot.getID())) {
                        rc.setFlag(robot.getID());
                    }
                }

                if (robot.getLocation().isWithinDistanceSquared(Data.originPoint, 16)
                        && robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 9)) {
                    if (rc.canEmpower(9)) {
                        rc.empower(9);
                    }
                }
                // if (robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 2)) {
                //     System.out.println("EMPOWERING");
                //     if (rc.canEmpower(2)) {
                //         rc.empower(2);
                //     } else {
                //         System.out.println("HEADING TOWARDS BOT");
                //         Direction nextDir = Pathfinding.basicBugToBase(rc, robot.getLocation());
                //         if (rc.canMove(nextDir)) {
                //             rc.move(nextDir);
                //         }
                //         //mostly for when mucks are going to possible coords
                //         //and they are wrong, so they switch targets
                //         //System.out.println("NEW TARGET");
                //         MapLocation newTarget = robot.getLocation();
                //         int dx = newTarget.x - Data.originPoint.x;
                //         int dy = newTarget.y - Data.originPoint.y;
                //         if (dx < 65 && dy < 65) {
                //             int newFlag = Communication.coordEncoder("ENEMY", dx, dy);
                //             if (rc.canSetFlag(newFlag)) {
                //                 rc.setFlag(newFlag);
                //                 role = Integer.toString(newFlag);
                //             }
                //         }
                //         //track bot id
                //     }
                // }
            }
        }

        int thisId = rc.getFlag(rc.getID());
        if (Integer.toString(thisId).length() == 5) {
            if (!rc.canGetFlag(thisId)) {
                if (rc.canSetFlag(Integer.parseInt(role))) {
                    rc.setFlag(Integer.parseInt(role));
                    //Data.politicianDefenderBoundary = 60;
                    //Data.politicianBoundary = 125;
                }
            } else {
                int flag = rc.getFlag(rc.getID());
                if (rc.canSenseRobot(flag)) {
                    RobotInfo target = rc.senseRobot(flag);
                    if (target.getLocation().isWithinDistanceSquared(rc.getLocation(), 4)) {
                        System.out.println("EMPOWERING");
                        if (rc.canEmpower(4)) {
                            rc.empower(4);
                        }
                    } else {
                        System.out.println("HEADING TOWARDS BOT");
                        Direction nextDir = Pathfinding.basicBugToBase(rc, target.getLocation());
                        //Data.politicianDefenderBoundary = 1000000;
                        if (rc.canMove(nextDir)) {
                            rc.move(nextDir);
                        }
                    }
                }
            }
        }

        //logic for politicians that just converted from enemy politician or a slanderer
        else if (role.equals("")) {
            //logic for converted enemy politicians
            isConvertedEnemy(rc);

        } else if (role.equals("102")) {
            //isConvertedSlanderer(rc);
            isConvertedEnemy(rc);
        }
        // System.out.println(role);
        else if (role.length() == 7 && role.charAt(0) == '2') {
            if (rc.canSenseRadiusSquared(25)) {
                RobotInfo[] robotS = rc.senseNearbyRobots(25);
                // System.out.println(robots.toString());
                for (RobotInfo robot : robotS) {
                    if (robot.getTeam().equals(Team.NEUTRAL) || (robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)
                            && robot.getTeam().equals(rc.getTeam().opponent()))) {
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
                    if (robot.getTeam().equals(rc.getTeam())) {
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

                // System.out.println(priorityEnemy);

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
                    desiredActionRadius = 2;
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
                if (robot != null && robot.getTeam().equals(rc.getTeam())
                        && robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
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
        else if (role.length() == 4 && role.substring(0, 3).equals("112")) {
            scatterDefenderLogic(rc);
        } else if (role.length() == 3 && role.equals("113")) {
            idleAttacker(rc);
        }
    }

    public static void scatterDefenderLogic(RobotController rc) throws GameActionException {
        //only sense action radius
        if (rc.canSenseRadiusSquared(9)) {
            RobotInfo[] robots = rc.senseNearbyRobots(9, rc.getTeam().opponent());
            for (RobotInfo robot : robots) {
                if (robot.getType().equals(RobotType.MUCKRAKER)
                        && rc.getLocation().distanceSquaredTo(robot.getLocation()) <= 2) {
                    if (rc.canEmpower(2)) {
                        rc.empower(2);
                    }
                }
            }
        }

        if (rc.getLocation().distanceSquaredTo(Data.originPoint) > Data.politicianDefenderBoundary) {
            Direction dirBack = rc.getLocation().directionTo(Data.originPoint);
            if (rc.canMove(dirBack)) {
                rc.move(dirBack);
            }
        } else {
            Direction[] directions = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };
            Direction scatterDir = directions[Integer.parseInt(Character.toString((role.charAt(3))))];
            int boundary = Data.politicianBoundary;
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

    public static void idleAttacker(RobotController rc) throws GameActionException {

        if (rc.canSenseRadiusSquared(9)) {
            RobotInfo[] robots = rc.senseNearbyRobots(9, rc.getTeam().opponent());
            for (RobotInfo robot : robots) {
                if (robot.getType().equals(RobotType.MUCKRAKER)) {
                    if (rc.canEmpower(rc.getLocation().distanceSquaredTo(robot.getLocation()))) {
                        rc.empower(rc.getLocation().distanceSquaredTo(robot.getLocation()));
                    }
                }
            }
        }

        Direction randomDir = Data.directions[(int) (Math.random() * 8)];
        int boundary = Data.politicianBoundary;
        MapLocation origin = Data.originPoint;
        MapLocation currentLoc = rc.getLocation();

        if (rc.getLocation().distanceSquaredTo(origin) > boundary) {
            Direction towardsEC = Pathfinding.basicBugToBase(rc, origin);
            if (rc.canMove(towardsEC)) {
                rc.move(towardsEC);
            }
        } else {
            randomDir = Pathfinding.basicBugToBase(rc, currentLoc.add(randomDir));
            if (rc.canMove(randomDir)) {
                rc.move(randomDir);
            }
        }
    }

    public static void isConvertedSlanderer(RobotController rc) throws GameActionException {
        Direction safeDirection = Slanderer.safeDirection;
        if (safeDirection != null) {
            MapLocation safeTarget = Data.originPoint.add(safeDirection).add(safeDirection).add(safeDirection)
                    .add(safeDirection).add(safeDirection).add(safeDirection);
            safeDirection = Pathfinding.basicBugToBase(rc, safeTarget);
            if (rc.canMove(safeDirection)) {
                rc.move(safeDirection);
            }
        }
    }

    public static void isConvertedEnemy(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(25)) {
            RobotInfo robots[] = rc.senseNearbyRobots(25, rc.getTeam());
            for (RobotInfo robot : robots) {
                if (rc.canGetFlag(robot.getID()) && robot.getType().equals(RobotType.POLITICIAN)) {
                    int allyFlag = rc.getFlag(robot.getID());
                    if (rc.canSetFlag(allyFlag)) {
                        rc.setFlag(allyFlag);
                        role = Integer.toString(allyFlag);
                    }
                }
            }
        } else {
            Direction randomDirection = Data.directions[(int) (Math.random() * Data.directions.length)];
            if (rc.canMove(randomDirection)) {
                rc.move(randomDirection);
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
