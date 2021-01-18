package atomFinal;

import battlecode.common.*;

public class Politician {
    public static String role = "";
    public static MapLocation originPoint;
    public static int boundary = 25;

    public static boolean wasNearWall = false;
    public static Direction determinedDirection = Direction.CENTER;

    public static void run(RobotController rc) throws GameActionException {
        // if (rc.canGetFlag(Data.baseId)) {
        //     int baseFlag = rc.getFlag(Data.baseId);
        //     String baseFlagS = Integer.toString(baseFlag);
        //     if (baseFlagS.charAt(0) == '7' && role.charAt(0) != '7') {
        //         role = baseFlagS;
        //     }
        //     //System.out.println(role);
        // }

        if (role.length() > 0 && role.charAt(0) == '5') {
            takeoverLogic(rc);
        } else if (role.length() == 7 && role.charAt(0) == '2') {
            toTarget(rc);
        } else {
            logic(rc);
        }
    }

    public static void logic(RobotController rc) throws GameActionException {
        Team ally = rc.getTeam();
        Team enemy = ally.opponent();

        int nearbyAllies = 0;

        MapLocation thisLocation = rc.getLocation();
        int thisInfluence = rc.getInfluence();

        // if (role.charAt(0) == '7') {
        //     int[] coords = Communication.relCoordDecoder(role);
        //     int[] distance = Pathfinding.getDistance(Data.relOriginPoint, coords);
        //     MapLocation target = Data.originPoint.translate(distance[0], distance[1]);
        //     if (thisLocation.distanceSquaredTo(target) <= 6) {
        //         role = "";
        //     } else {
        //         Direction nextDir = Pathfinding.basicBug(rc, target);
        //         if (rc.canMove(nextDir)) {
        //             rc.move(nextDir);
        //         }
        //     }
        // }

        if (rc.canSenseRadiusSquared(25)) {
            RobotInfo[] robots = rc.senseNearbyRobots(25);
            boolean explode = false;
            boolean hasEnemy = false;
            for (RobotInfo robot : robots) {
                Team robotTeam = robot.getTeam();
                //System.out.println("TARGET:" + robotTeam + robot.getType());
                if ((robotTeam.equals(enemy) && robot.getType().equals(RobotType.MUCKRAKER)
                        || robotTeam.equals(Team.NEUTRAL))) {
                    int maxRadius = 0;
                    for (int i = 9; i > 0; i--) {
                        int neededInf = 10;
                        RobotInfo[] radius = rc.senseNearbyRobots(i);
                        for (RobotInfo rbt : radius) {
                            neededInf += rbt.getInfluence() + 1;
                            if (rbt.getTeam().equals(enemy)) {
                                hasEnemy = true;
                            }
                        }
                        if (neededInf < thisInfluence && radius.length != 0) {
                            maxRadius = i;
                            explode = true;
                            //System.out.println("NEWRADIUS" + maxRadius);
                            //System.out.println("NEEDEDINF" + neededInf);
                            break;
                        }
                    }
                    if (rc.canEmpower(maxRadius) && explode == true && hasEnemy == true) {
                        rc.empower(maxRadius);
                    } else if (thisInfluence > robot.getInfluence() + 10) {
                        Direction dir = thisLocation.directionTo(robot.getLocation());
                        if (rc.canMove(dir)) {
                            rc.move(dir);
                        }
                    }
                }
                if (robotTeam.equals(enemy) && thisLocation.distanceSquaredTo(robot.getLocation()) < 2
                        && rc.getRobotCount() > 200) {
                    if (rc.canEmpower(1)) {
                        rc.empower(1);
                    }
                } else if (robotTeam.equals(enemy) && thisLocation.distanceSquaredTo(robot.getLocation()) < 2
                        && rc.getRobotCount() > 300) {
                    if (rc.canEmpower(9)) {
                        rc.empower(9);
                    }
                } else if (thisLocation.distanceSquaredTo(robot.getLocation()) <= 9 && rc.getRobotCount() > 500) {
                    if (rc.canEmpower(9)) {
                        rc.empower(9);
                    }
                }
                if (robotTeam.equals(enemy) && robot.getType().equals(RobotType.POLITICIAN)
                        && thisInfluence < robot.getInfluence() + 10) {
                    if (rc.canMove(rc.getLocation().directionTo(robot.getLocation()).opposite())) {
                        rc.move(rc.getLocation().directionTo(robot.getLocation()).opposite());
                    }
                } else if (robotTeam.equals(enemy) && robot.getType().equals(RobotType.POLITICIAN)) {
                    if (thisLocation.isAdjacentTo(robot.getLocation())) {
                        if (rc.canEmpower(1)) {
                            rc.empower(1);
                        }
                    } else {
                        Direction dir = thisLocation.directionTo(robot.getLocation());
                        if (rc.canMove(dir)) {
                            rc.move(dir);
                        }
                    }
                }
                if (robot.getTeam().equals(Team.NEUTRAL)) {
                    if (thisInfluence - 10 > robot.getInfluence() / 10) {
                        if (thisLocation.isAdjacentTo(robot.getLocation())) {
                            if (rc.canEmpower(1)) {
                                rc.empower(1);
                            }
                        } else {
                            Direction dir = thisLocation.directionTo(robot.getLocation());
                            if (rc.canMove(dir)) {
                                rc.move(dir);
                            }
                        }
                    }
                }
                if (robotTeam.equals(rc.getTeam())) {
                    nearbyAllies++;
                    if (robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 4)) {
                        if (rc.canMove(rc.getLocation().directionTo(robot.getLocation()).opposite())) {
                            rc.move(rc.getLocation().directionTo(robot.getLocation()).opposite());
                        }
                    }
                }
            }
        }

        //System.out.println("NEARBY ALLIES:" + nearbyAllies);
        //System.out.println("BOUNDARY: " + boundary);
        if (nearbyAllies > 8) {
            boundary += 4;
        } else if (nearbyAllies < 6 && boundary >= 27) {
            boundary -= 4;
        }

        if (!rc.onTheMap(thisLocation.translate(1, 0))) {
            System.out.println(thisLocation.translate(1, 0));
            //System.out.println("FOUND WALL EAST");
            determinedDirection = Direction.WEST;
        } else if (!rc.onTheMap(thisLocation.translate(-1, 0))) {
            //System.out.println("FOUND WALL WEST");
            determinedDirection = Direction.EAST;
        } else if (!rc.onTheMap(thisLocation.translate(0, 1))) {
            //System.out.println("FOUND WALL NORTH");
            determinedDirection = Direction.SOUTH;
        } else if (!rc.onTheMap(thisLocation.translate(0, -1))) {
            //System.out.println("FOUND WALL SOUTH");
            determinedDirection = Direction.NORTH;
        }

        if (determinedDirection != Direction.CENTER && thisLocation.distanceSquaredTo(Data.originPoint) < boundary) {
            //System.out.println("GOING IN DETERMINED DIRECTION:" + determinedDirection);
            //determinedDirection = Pathfinding.basicBug(rc, thisLocation.add(determinedDirection));
            if (rc.canMove(determinedDirection)) {
                //System.out.println("MOVED" + determinedDirection);
                rc.move(determinedDirection);
            }
        } else if (thisLocation.distanceSquaredTo(Data.originPoint) < boundary) {
            //System.out.println("MOVING TO BOUNDARY");
            if (rc.canMove(thisLocation.directionTo(Data.originPoint).opposite())) {
                rc.move(thisLocation.directionTo(Data.originPoint).opposite());
            }
        } else if (thisLocation.distanceSquaredTo(Data.originPoint) >= boundary) {
            // if (rc.canMove(thisLocation.directionTo(Data.originPoint).rotateRight())) {
            //     //System.out.println("MOVING TO DIAGONAL");
            //     rc.move(thisLocation.directionTo(Data.originPoint).rotateRight());
            // }
            //else {
            if (rc.canMove(thisLocation.directionTo(Data.originPoint))) {
                //System.out.println("MOVING BACK");
                rc.move(thisLocation.directionTo(Data.originPoint));
            }
            //}
        }

        Direction[] directions = Data.directions;
        for (Direction direction : directions) {
            if (rc.canMove(direction)) {
                rc.move(direction);
            }
        }
    }

    public static void takeoverLogic(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(25)) {
            RobotInfo[] robots = rc.senseNearbyRobots(25);
            // System.out.println(robots.toString());
            for (RobotInfo robot : robots) {
                if (robot.getTeam().equals(Team.NEUTRAL) || (robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)
                        && robot.getTeam().equals(rc.getTeam().opponent()))) {
                    int totalRobotsAround = 0;
                    if (rc.canSenseRadiusSquared(1)) {
                        RobotInfo[] robotS = rc.senseNearbyRobots(1);
                        totalRobotsAround = robotS.length;
                    }
                    if (totalRobotsAround > 0 && robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)
                            && (rc.getInfluence() - 10) / totalRobotsAround > robot.getInfluence()) {
                        if (rc.getLocation().distanceSquaredTo(robot.getLocation()) == 1) {
                            if (rc.canEmpower(1)) {
                                rc.empower(1);
                            }
                        }
                        Direction nextDir = Pathfinding.basicBug(rc, robot.getLocation());
                        if (rc.canMove(nextDir)) {
                            rc.move(nextDir);
                        }
                        // System.out.println("NEW TARGET");
                        int relx = robot.getLocation().x % 128;
                        int rely = robot.getLocation().y % 128;
                        int newFlag = Communication.coordEncoder("NEUTRAL", relx, rely);
                        if (rc.canSetFlag(newFlag)) {
                            rc.setFlag(newFlag);
                            role = Integer.toString(newFlag);
                        }
                    }
                }
            }
        }
        int[] coords = Communication.relCoordDecoder(role);
        System.out.println(role);
        int[] distance = Pathfinding.getDistance(Data.relOriginPoint, coords);
        MapLocation target = Data.originPoint.translate(distance[0], distance[1]);
        System.out.println(target.toString());
        Direction nextDir = Pathfinding.basicBug(rc, target);
        if (rc.canMove(nextDir)) {
            rc.move(nextDir);
        }
    }

    public static void toTarget(RobotController rc) throws GameActionException {
        Team ally = rc.getTeam();
        Team enemy = ally.opponent();
        int thisInfluence = rc.getInfluence();

        if (rc.canSenseRadiusSquared(25)) {
            RobotInfo[] robots = rc.senseNearbyRobots(25);
            for (RobotInfo robot : robots) {
                Team robotTeam = robot.getTeam();
                if ((robotTeam.equals(enemy) || robotTeam.equals(Team.NEUTRAL))) {
                    int maxRadius = 0;
                    for (int i = 0; i < 10; i++) {
                        int neededInf = 10;
                        RobotInfo[] radius = rc.senseNearbyRobots(i);
                        for (RobotInfo rbt : radius) {
                            neededInf += rbt.getInfluence() + 1;
                        }
                        if (neededInf < thisInfluence) {
                            maxRadius = i;
                            System.out.println("NEWRADIUS" + maxRadius);
                            System.out.println("NEEDEDINF" + neededInf);
                        } else {
                            break;
                        }
                    }
                    if (rc.canEmpower(maxRadius)) {
                        rc.empower(maxRadius);
                    }
                } else if (robot.getTeam().equals(enemy)
                        && robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 25)
                        && rc.getInfluence() + 11 > robot.getInfluence()) {
                    Direction dir = rc.getLocation().directionTo(robot.getLocation());
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
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

    public static void init(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(3)) {
            for (RobotInfo robot : rc.senseNearbyRobots(3, rc.getTeam())) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    Data.baseId = robot.getID();
                    role = Integer.toString(rc.getFlag(Data.baseId));
                    Data.originPoint = robot.getLocation();
                    Data.relOriginPoint[0] = Data.originPoint.x % 128;
                    Data.relOriginPoint[1] = Data.originPoint.y % 128;
                    Data.initRound = rc.getRoundNum();
                    if (rc.canGetFlag(Data.baseId)) {
                        if (rc.canSetFlag(rc.getFlag(Data.baseId))) {
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
