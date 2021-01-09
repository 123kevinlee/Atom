package atomAlpha;

import battlecode.common.*;

public class Politician {
    public static String role = "";
    public static MapLocation originPoint;

    public static void run(RobotController rc) throws GameActionException {

        MapLocation myLoc = rc.getLocation();
        int detectionRadiusSquared = 25;
        int actionRadiusSquared = 9;

        int chaseCount = 0;
        int actionRadius = 1;
        int defenseRadius = 9;

        Team enemy = rc.getTeam().opponent();

        RobotInfo[] defensible = rc.senseNearbyRobots(defenseRadius, enemy);
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        // if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
        // // System.out.println("empowering...");
        // rc.empower(actionRadius);
        // // System.out.println("empowered");
        // return;
        // }

        // System.out.println(role);

        if (role.equals("")) { // this means it justconverted from slanderer
            if (rc.canSetFlag(1)) {
                rc.setFlag(1);
            }
            if (attackable.length != 0 && chaseCount != -1) {
                // System.out.println("TRACKING");

                // The int below discerns which enemy to attack first in the RobotInfo array
                int priorityEnemy = 0;

                for (int i = 0; i < attackable.length; i++) {
                    if (attackable[i].getType() == RobotType.ENLIGHTENMENT_CENTER) {
                        priorityEnemy = i;
                        break;
                    }
                }

                RobotInfo closeEnemy = attackable[priorityEnemy];
                MapLocation track = closeEnemy.getLocation();
                int[] tracked = new int[2];
                tracked[0] += track.x;
                tracked[1] += track.y;

                // System.out.println("ENEMY ROBOT: " + tracked[0] + "," + tracked[1]);
                Direction toCloseEnemy = myLoc.directionTo(track);

                if (myLoc.distanceSquaredTo(track) <= actionRadiusSquared && rc.canEmpower(actionRadiusSquared)) {
                    rc.empower(actionRadiusSquared);
                    // System.out.println("Empowered");

                } else if (rc.canMove(Pathfinding.chooseBestNextStep(rc, toCloseEnemy))) {
                    rc.move(Pathfinding.chooseBestNextStep(rc, toCloseEnemy));
                    chaseCount++;
                }
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
                                        chaseCount++;
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

            // /*
            // * create a locking mechanism and chasing mechanism marks the last known
            // * location of the closest enemy bot, no flags needed moves in the direction
            // of
            // * this location, if within sensor radius, blows up if the enemy bot is
            // already
            // * destroyed, the bot must resume trying to blow up the enlightment center.
            // */
            // if (attackable.length != 0 && chaseCount != -1) {
            // System.out.println("TRACKING");

            // // The int below discerns which enemy to attack first in the RobotInfo array
            // int priorityEnemy = 0;

            // for (int i = 0; i < attackable.length; i++) {
            // if (attackable[i].getType() == RobotType.ENLIGHTENMENT_CENTER) {
            // priorityEnemy = i;
            // break;
            // }
            // }

            // RobotInfo closeEnemy = attackable[priorityEnemy];
            // MapLocation track = closeEnemy.getLocation();
            // int[] tracked = new int[2];
            // tracked[0] += track.x;
            // tracked[1] += track.y;

            // System.out.println("ENEMY ROBOT: " + tracked[0] + "," + tracked[1]);
            // Direction toCloseEnemy = myLoc.directionTo(track);

            // if (myLoc.distanceSquaredTo(track) <= actionRadiusSquared &&
            // rc.canEmpower(actionRadiusSquared)) {
            // rc.empower(actionRadiusSquared);
            // System.out.println("Empowered");

            // } else if (rc.canMove(Pathfinding.chooseBestNextStep(rc, toCloseEnemy))) {
            // rc.move(Pathfinding.chooseBestNextStep(rc, toCloseEnemy));
            // chaseCount++;
            // }
            // }

            // // System.out.println("I moved!");
            // int[] coords = Communication.coordDecoder(role); // coords of enemy base or
            // whatever target
            // MapLocation origin = Data.originPoint;
            // coords[0] += origin.x;
            // coords[1] += origin.y;
            // System.out.println("ENEMY TARGET: " + coords[0] + "," + coords[1]);
            // MapLocation targetLocation = new MapLocation(coords[0], coords[1]);
            // Direction targetDirection = origin.directionTo(targetLocation);
            // if (rc.canMove(Pathfinding.chooseBestNextStep(rc, targetDirection))) {
            // rc.move(Pathfinding.chooseBestNextStep(rc, targetDirection));
            // }
            // reverted
            if (rc.canSenseRadiusSquared(actionRadius)) {
                RobotInfo[] robot = rc.senseNearbyRobots(actionRadius, enemy);
                if (robot.length > 0 && rc.canEmpower(actionRadius)) {
                    rc.empower(actionRadius);
                }
            }

            // System.out.println(role);
            // System.out.println("I moved!");
            int[] coords = Communication.coordDecoder(role);
            MapLocation currentLocation = rc.getLocation();
            coords[0] += Data.originPoint.x;
            coords[1] += Data.originPoint.y;
            // System.out.println("ENEMY TARGET: " + coords[0] + "," + coords[1]);

            MapLocation targetLocation = new MapLocation(coords[0], coords[1]);
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
