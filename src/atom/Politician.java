package atom;

import javax.swing.BoundedRangeModel;

import battlecode.common.*;

public class Politician {
    public static String role = "";
    public static MapLocation originPoint;
    public static int boundary = 25;

    public static boolean wasNearWall = false;
    public static Direction staticToBase = Direction.CENTER;

    public static void run(RobotController rc) throws GameActionException {
        int baseFlag = -1;
        if (rc.canGetFlag(Data.baseId)) {
            baseFlag = rc.getFlag(Data.baseId);
            if (role.length() > 0 && role.charAt(0) != '5' && Integer.toString(baseFlag).charAt(0) == '7') {
                //rc.setFlag(baseFlag);
                role = Integer.toString(baseFlag);
            }
        }
        System.out.println(role);

        Team enemy = rc.getTeam().opponent();

        if (role.length() > 0 && role.charAt(0) == '5') {
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
        } else {
            if (rc.canSenseRadiusSquared(25)) {
                for (RobotInfo robot : rc.senseNearbyRobots(25)) {
                    if (robot.getTeam().equals(Team.NEUTRAL) || (robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)
                            && robot.getTeam().equals(enemy))) {
                        if (robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 9)) {
                            if (rc.canEmpower(9)) {
                                rc.empower(9);
                            } else {
                                Direction nextDir = Pathfinding.basicBug(rc, robot.getLocation());
                                if (rc.canMove(nextDir)) {
                                    rc.move(nextDir);
                                }
                                // //mostly for when mucks are going to possible coords
                                // //and they are wrong, so they switch targets
                                // //System.out.println("NEW TARGET");
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
                }
            }

            //logic for politicians that just converted from enemy politician or a slanderer
            if (!Data.wasAlly || Data.wasSlanderer) {
                logic(rc);
                //isConvertedEnemy(rc);
            } else if (role.length() == 7 && role.charAt(0) == '2') {
                toTarget(rc);
            } else if (role.length() == 7 && role.charAt(0) == '7') {
                toDanger(rc);
            } else {
                logic(rc);
            }
        }
    }

    public static void logic(RobotController rc) throws GameActionException {
        Team ally = rc.getTeam();
        Team enemy = ally.opponent();

        int nearbyAllies = 0;

        MapLocation thisLocation = rc.getLocation();
        if (!rc.onTheMap(thisLocation.translate(1, 0)) || !rc.onTheMap(thisLocation.translate(-1, 0))
                || !rc.onTheMap(thisLocation.translate(0, 1)) || !rc.onTheMap(thisLocation.translate(0, -1))) {
            wasNearWall = true;
        }

        if (rc.canSenseRadiusSquared(-1)) {
            RobotInfo[] robots = rc.senseNearbyRobots(-1);
            for (RobotInfo robot : robots) {
                if ((robot.getTeam().equals(enemy) || robot.getTeam().equals(Team.NEUTRAL))
                        && robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 2)
                        && rc.getInfluence() > robot.getInfluence() + 11) {
                    if (rc.canEmpower(2)) {
                        rc.empower(2);
                    }
                } else if (robot.getTeam().equals(enemy) || robot.getTeam().equals(Team.NEUTRAL)
                        && robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 25)
                        && rc.getInfluence() + 11 > robot.getInfluence()) {
                    Direction dir = rc.getLocation().directionTo(robot.getLocation());
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                } else if (robot.getTeam().equals(enemy) && robot.getType().equals(RobotType.POLITICIAN)
                        && rc.getInfluence() < robot.getInfluence() + 10) {
                    if (rc.canMove(rc.getLocation().directionTo(robot.getLocation()).opposite())) {
                        rc.move(rc.getLocation().directionTo(robot.getLocation()).opposite());
                    }
                }
                if (robot.getTeam().equals(rc.getTeam())) {
                    nearbyAllies++;
                    if (robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 11)) {
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
            boundary += 6;
        } else if (nearbyAllies < 6 && boundary >= 27) {
            boundary -= 6;
        }

        if (thisLocation.distanceSquaredTo(Data.originPoint) < boundary) {
            //System.out.println("MOVING TO BOUNDARY");
            if (wasNearWall) {
                if (staticToBase.equals(Direction.CENTER)) {
                    staticToBase = thisLocation.directionTo(Data.originPoint);
                } else if (rc.canMove(staticToBase)) {
                    rc.move(staticToBase);
                }
            }
            if (rc.canMove(thisLocation.directionTo(Data.originPoint).opposite())) {
                rc.move(thisLocation.directionTo(Data.originPoint).opposite());
            }
        } else if (thisLocation.distanceSquaredTo(Data.originPoint) >= boundary) {
            if (rc.canMove(thisLocation.directionTo(Data.originPoint).rotateRight())) {
                //System.out.println("MOVING TO DIAGONAL");
                rc.move(thisLocation.directionTo(Data.originPoint).rotateRight());
            } else {
                if (rc.canMove(thisLocation.directionTo(Data.originPoint))) {
                    //System.out.println("MOVING BACK");
                    rc.move(thisLocation.directionTo(Data.originPoint));
                }
            }
        }

        Direction[] directions = Data.directions;
        for (Direction direction : directions) {
            if (rc.canMove(direction)) {
                rc.move(direction);
            }
        }

    }

    public static void toDanger(RobotController rc) throws GameActionException {
        Team ally = rc.getTeam();
        Team enemy = ally.opponent();

        if (rc.canSenseRadiusSquared(-1)) {
            RobotInfo[] robots = rc.senseNearbyRobots(-1);
            for (RobotInfo robot : robots) {
                if (robot.getTeam().equals(enemy) && robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 2)
                        && rc.getInfluence() > robot.getInfluence() + 11) {
                    if (rc.canEmpower(2)) {
                        rc.empower(2);
                    }
                } else if (robot.getTeam().equals(enemy)
                        && robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 25)
                        && rc.getInfluence() + 11 > robot.getInfluence()) {
                    Direction dir = rc.getLocation().directionTo(robot.getLocation());
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                } else if (robot.getTeam().equals(rc.getTeam())) {
                    if (robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 11)) {
                        if (rc.canMove(rc.getLocation().directionTo(robot.getLocation()).opposite())) {
                            rc.move(rc.getLocation().directionTo(robot.getLocation()).opposite());
                        }
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

    public static void toTarget(RobotController rc) throws GameActionException {
        Team ally = rc.getTeam();
        Team enemy = ally.opponent();

        if (rc.canSenseRadiusSquared(-1)) {
            RobotInfo[] robots = rc.senseNearbyRobots(-1);
            for (RobotInfo robot : robots) {
                if (robot.getTeam().equals(enemy) && robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 2)
                        && rc.getInfluence() > robot.getInfluence() + 11) {
                    if (rc.canEmpower(2)) {
                        rc.empower(2);
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

    public static void isConvertedEnemy(RobotController rc) throws GameActionException {
        Team ally = rc.getTeam();
        Team enemy = ally.opponent();

        if (rc.canSenseRadiusSquared(-1)) {
            RobotInfo[] robots = rc.senseNearbyRobots(-1);
            for (RobotInfo robot : robots) {
                if (robot.getTeam().equals(enemy) && robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 2)
                        && rc.getInfluence() > robot.getInfluence() + 11) {
                    if (rc.canEmpower(2)) {
                        rc.empower(2);
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
                    role = Integer.toString(rc.getFlag(Data.baseId));
                    Data.originPoint = robot.getLocation();
                    Data.relOriginPoint[0] = Data.originPoint.x % 128;
                    Data.relOriginPoint[1] = Data.originPoint.y % 128;
                    Data.initRound = rc.getRoundNum();
                    Data.wasAlly = true;
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
