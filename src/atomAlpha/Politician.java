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

        RobotInfo[] defensible = rc.senseNearbyRobots(defenseRadius, enemy);
        RobotInfo[] attackable = rc.senseNearbyRobots(sensorRadiusSquared, enemy);
        // if (attackable.length != 0 && rc.canEmpower(desiredActionRadius)) {
        // RobotInfo[] attackable = rc.senseNearbyRobots(25, enemy);
        // // System.out.println(attackable.length);

        // if (attackable.length > 0) {
        // if (rc.canEmpower(9)) {
        // rc.empower(9);
        // }
        // }

        // if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
        // // System.out.println("empowering...");
        // rc.empower(desiredActionRadius);
        // // System.out.println("empowered");
        // return;
        // }

        // System.out.println(role);

        if (role.equals("")) { // this means it justconverted from slanderer
            if (rc.canSetFlag(1)) {
                rc.setFlag(1);
            }

            if (rc.canGetFlag(Data.baseId)) {
                // System.out.println("HERE0");
                String baseFlag = Integer.toString(rc.getFlag(Data.baseId));
                if (baseFlag.charAt(0) == '3') {
                    // System.out.println("HERE1");
                    role = baseFlag;
                } else if (rc.canSenseRadiusSquared(25)) {
                    // System.out.println("HERE2");
                    RobotInfo robots[] = rc.senseNearbyRobots(25, rc.getTeam());
                    for (RobotInfo robot : robots) {
                        if (rc.canGetFlag(robot.getID())) {
                            baseFlag = Integer.toString(rc.getFlag(robot.getID()));
                            if (baseFlag.charAt(0) == '3') {
                                role = baseFlag;
                            } else {
                                // System.out.println("HERE3");
                                if (Data.slandererConvertDirection != Direction.CENTER) {
                                    if (rc.canSenseRobot(Data.baseId)) {
                                        Data.slandererConvertDirection = Direction.CENTER;
                                    }
                                    if (rc.canMove(
                                            Pathfinding.chooseBestNextStep(rc, Data.slandererConvertDirection))) {
                                        rc.move(Pathfinding.chooseBestNextStep(rc, Data.slandererConvertDirection));
                                    }
                                } else {
                                    Direction randomDirection = Data.directions[(int) (Math.random()
                                            * Data.directions.length)];
                                    if (rc.canMove(Pathfinding.chooseBestNextStep(rc, randomDirection))) {
                                        rc.move(Pathfinding.chooseBestNextStep(rc, randomDirection));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // System.out.println(role);
        else if (role.length() == 7) {
            System.out.println("HERE");
            if (rc.canSenseRadiusSquared(25)) {
                System.out.println("HERE1");
                RobotInfo[] robots = rc.senseNearbyRobots(25);
                System.out.println(robots.toString());
                for (RobotInfo robot : robots) {
                    if (robot.getTeam() == rc.getTeam()) {
                        if (rc.canGetFlag(robot.getID())) {
                            String flag = Integer.toString(rc.getFlag(robot.getID()));
                            if (flag.charAt(0) == '3') {
                                String ending = Integer.toString(rc.getFlag(rc.getID())).substring(1);
                                if (flag.substring(1).equals(ending)) {
                                    if (rc.canSetFlag(Integer.parseInt(flag))) {
                                        rc.setFlag(Integer.parseInt(flag));
                                    }
                                }
                            }
                        }
                    }
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

            // if (rc.canSenseRadiusSquared(desiredActionRadius)) {
            // RobotInfo[] robot = rc.senseNearbyRobots(desiredActionRadius, enemy);
            // if (robot.length > 0 && rc.canEmpower(desiredActionRadius)) {
            // rc.empower(desiredActionRadius);
            // }
            // }

            // System.out.println(role);
            // System.out.println("I moved!");

            if (rc.canGetFlag(rc.getID())) {
                String flag = Integer.toString(rc.getFlag(rc.getID()));
                if (flag.charAt(0) == '3') {
                    Direction[] directions = Data.directions;
                    Direction randDirection = directions[(int) (Math.random() * directions.length)];
                    if (rc.canMove(randDirection)) {
                        System.out.println("Rand Dir:" + randDirection);
                        rc.move(randDirection);
                    } else {
                        for (int i = 0; i < 8; i++) {
                            if (rc.canMove(directions[i])) {
                                rc.move(directions[i]);
                            }
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
                    System.out.println(robot.getTeam());
                    System.out.println("MISSING OR CONVERTED");
                    String convertMsg = "3" + role.substring(1);
                    if (rc.canSetFlag(Integer.parseInt(convertMsg))) {
                        rc.setFlag(Integer.parseInt(convertMsg));
                    }
                }
            }
            // Direction targetDirection = currentLocation.directionTo(targetLocation);
            Direction nextDir = Pathfinding.basicBugToBase(rc, targetLocation);
            if (rc.canMove(nextDir)) {
                rc.move(nextDir);
            }
        }
        // if (role.equals("111") && Pathfinding.getDefenseReached() == false &&
        // rc.isReady()) {
        // Pathfinding.findDefenseLocation(rc, Data.originPoint);
        // }
        // // create a locking mechanism and chasing mechanism
        // if (rc.canSenseRadiusSquared(9) && defensible.length > 0 && rc.canEmpower(9)
        // && role.equals("111")) {
        // System.out.println("EMPOWER");
        // rc.empower(9);
        // }

        else if (role.equals("111")) {
            if (Pathfinding.getDefenseReached() == false && rc.isReady()) {
                if (!rc.getLocation().equals(Data.originPoint.add(Direction.NORTHEAST))
                        || !rc.getLocation().equals(Data.originPoint.add(Direction.NORTHWEST))
                        || !rc.getLocation().equals(Data.originPoint.add(Direction.SOUTHEAST))
                        || !rc.getLocation().equals(Data.originPoint.add(Direction.SOUTHWEST))) {
                    // Pathfinding.chooseRandomDefenseLocation(rc, Data.originPoint);
                }
                Pathfinding.findDefenseLocation(rc, Data.originPoint);
            }
            // create a locking mechanism and chasing mechanism
            if (rc.canSenseRadiusSquared(9) && defensible.length > 0 && rc.canEmpower(9)) {
                // System.out.println("EMPOWER");
                rc.empower(9);
            }
        }

        else if (role.length() != 7) {
            // sit around until get good role
        }
        // System.out.println("I moved!");
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
