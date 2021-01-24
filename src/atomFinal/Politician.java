package atomFinal;

import battlecode.common.*;

public class Politician {
    public static String role = "";
    public static MapLocation originPoint;
    public static int boundary = 25;
    public static boolean adjusted = false;
    public static int followID = 0;

    public static boolean wasNearWall = false;
    public static Direction determinedDirection = Direction.CENTER;

    public static Direction weightedDirection = Direction.CENTER;

    public static void run(RobotController rc) throws GameActionException {
        System.out.println(role);
        System.out.println(weightedDirection);
        int flag = -1;
        if (rc.canGetFlag(rc.getID())) {
            flag = rc.getFlag(rc.getID());
        }
        if (role.length() > 0 && role.charAt(0) == '4' && flag == 0) {
            if (rc.canSetFlag(Integer.parseInt(role))) {
                rc.setFlag(Integer.parseInt(role));
            }
        }

        if (rc.canSenseRadiusSquared(25)) {
            RobotInfo[] robots = rc.senseNearbyRobots(25, rc.getTeam().opponent());
            if (robots.length > 0) {
                if (rc.canSetFlag(666)) {
                    rc.setFlag(666);
                }
            } else {
                if (role.equals("0") || role.equals("")) {
                    RobotInfo[] team = rc.senseNearbyRobots(25, rc.getTeam());
                    for (RobotInfo robot : team) {
                        if (robot.getType().equals(RobotType.POLITICIAN)) {
                            if (rc.canGetFlag(robot.getID())) {
                                int flagT = rc.getFlag(robot.getID());
                                String flagTS = Integer.toString(flagT);
                                if (flagTS.length() > 0 && flagTS.charAt(0) == '4') {
                                    role = flagTS;
                                }
                            }
                        }
                    }
                }
                if (role.length() > 0 && role.charAt(0) == '4') {
                    if (rc.canSetFlag(Integer.parseInt(role))) {
                        rc.setFlag(Integer.parseInt(role));
                    }
                } else if (rc.canSetFlag(0)) {
                    rc.setFlag(0);
                }
            }
        }

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
        double currentEmpowerFactor = rc.getEmpowerFactor(ally, 0);

        if (rc.canSenseRadiusSquared(25)) {
            RobotInfo[] robots = rc.senseNearbyRobots(25);
            for (RobotInfo robot : robots) {
                Team robotTeam = robot.getTeam();
                boolean explode = false;
                //System.out.println("TARGET:" + robotTeam + robot.getType());
                if ((robotTeam.equals(enemy) && robot.getType().equals(RobotType.MUCKRAKER)
                        || robotTeam.equals(Team.NEUTRAL))) {
                    int maxRadius = 0;
                    for (int i = 9; i > 0; i--) {
                        boolean hasEnemy = false;
                        boolean hasPoly = false;
                        boolean nearFarmer = false;
                        RobotInfo[] radius = rc.senseNearbyRobots(i);
                        int maxInf = 0;
                        int numOfUnits = radius.length;
                        for (RobotInfo rbt : radius) {
                            int rbtInfluence = rbt.getInfluence();
                            if (rbt.getTeam().equals(enemy)) {
                                if (rbt.getType().equals(RobotType.POLITICIAN)) {
                                    hasPoly = true;
                                } else if (rbt.getType().equals(RobotType.SLANDERER)
                                        && rbt.getTeam().equals(rc.getTeam())) {
                                    nearFarmer = true;
                                }
                                hasEnemy = true;
                                //System.out.println(rbt.ID + ":" + rbtInfluence);
                                if (rbtInfluence > maxInf) {
                                    maxInf = rbtInfluence;
                                    //System.out.println(maxInf);
                                }
                            }
                        }
                        if (numOfUnits != 0 && hasEnemy == true
                                && maxInf < ((thisInfluence - 10) * currentEmpowerFactor) / numOfUnits) {
                            if (((thisInfluence * currentEmpowerFactor - 11) / numOfUnits) - maxInf < 10
                                    || numOfUnits > 2 || hasPoly || nearFarmer) {
                                maxRadius = i;
                                explode = true;
                                break;
                            }
                        }
                    }
                    if (rc.canEmpower(maxRadius) && explode == true) {
                        rc.empower(maxRadius);
                    } else if ((thisInfluence - 10) * currentEmpowerFactor > robot.getInfluence()) {
                        Direction nextDir = Pathfinding.basicBug(rc, robot.getLocation());
                        nextDir = Pathfinding.smartNav(rc, robot.getLocation());
                        if (rc.canMove(nextDir)) {
                            rc.move(nextDir);
                        }
                    }
                }
                if (robotTeam.equals(enemy) && robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
                    if (thisLocation.distanceSquaredTo(robot.getLocation()) < 10) {
                        if (rc.canEmpower(9)) {
                            rc.empower(9);
                        }
                    } else {
                        Direction nextDir = Pathfinding.basicBug(rc, robot.getLocation());
                        nextDir = Pathfinding.smartNav(rc, robot.getLocation());
                        if (rc.canMove(nextDir)) {
                            rc.move(nextDir);
                        }
                    }
                }
                if (robotTeam.equals(enemy) && rc.getRobotCount() > 300) {
                    if (thisLocation.distanceSquaredTo(robot.getLocation()) < 2) {
                        if (rc.canEmpower(1)) {
                            rc.empower(1);
                        }
                    } else {
                        Direction nextDir = Pathfinding.basicBug(rc, robot.getLocation());
                        nextDir = Pathfinding.smartNav(rc, robot.getLocation());
                        if (rc.canMove(nextDir)) {
                            rc.move(nextDir);
                        }
                    }
                }
                if (robotTeam.equals(enemy) && thisLocation.distanceSquaredTo(robot.getLocation()) <= 9
                        && rc.getRobotCount() > 500) {
                    if (rc.canEmpower(9)) {
                        rc.empower(9);
                    }
                }

                if (robotTeam.equals(enemy) && robot.getType().equals(RobotType.POLITICIAN)
                        && (thisInfluence - 10) * currentEmpowerFactor < robot.getInfluence()) {
                    Direction nextDir = Pathfinding.basicBug(rc,
                            rc.getLocation().directionTo(robot.getLocation()).opposite());
                    nextDir = Pathfinding.smartNav(rc, rc.getLocation().directionTo(robot.getLocation()).opposite());
                    if (rc.canMove(nextDir)) {
                        rc.move(nextDir);
                        //System.out.println("RUNNING FROM ENEMY:" + dir);
                    }
                } else if (robotTeam.equals(enemy) && robot.getType().equals(RobotType.POLITICIAN)
                        && (thisInfluence - 10) * currentEmpowerFactor > robot.getInfluence()) {
                    if (thisLocation.isAdjacentTo(robot.getLocation())) {
                        if (rc.canEmpower(1)) {
                            rc.empower(1);
                        }
                    } else {
                        Direction dir = Pathfinding.basicBug(rc, robot.getLocation());
                        dir = Pathfinding.smartNav(rc, robot.getLocation());
                        if (rc.canMove(dir)) {
                            rc.move(dir);
                            //System.out.println("CHASING:" + dir);
                        }
                    }
                }
                if (robot.getTeam().equals(Team.NEUTRAL)) {
                    if (thisInfluence * currentEmpowerFactor - 10 > robot.getInfluence() / 10
                            || robot.getInfluence() <= 100) {
                        if (thisLocation.isAdjacentTo(robot.getLocation())) {
                            if (rc.canEmpower(1)) {
                                rc.empower(1);
                            }
                        } else {
                            Direction dir = Pathfinding.basicBug(rc, robot.getLocation());
                            dir = Pathfinding.smartNav(rc, robot.getLocation());
                            if (rc.canMove(dir)) {
                                rc.move(dir);
                                //System.out.println("CHASING:" + dir);
                            }
                        }
                    }
                }
                if (robotTeam.equals(rc.getTeam())) {
                    nearbyAllies++;
                    if (robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 10)
                            && thisLocation.distanceSquaredTo(Data.originPoint) >= boundary - 4) {
                        // if (robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 2)) {
                        Direction nextDir = Pathfinding.basicBug(rc,
                                rc.getLocation().directionTo(robot.getLocation()).opposite());

                        nextDir = Pathfinding.smartNav(rc,
                                rc.getLocation().directionTo(robot.getLocation()).opposite());

                        if (rc.canMove(nextDir)) {
                            rc.move(nextDir);
                            //System.out.println("RUNNING FROM TEAMMATE:" + dir);
                        }
                    }
                }
            }
        }

        //System.out.println("NEARBY ALLIES:" + nearbyAllies);
        //System.out.println("BOUNDARY: " + boundary);
        if (nearbyAllies > 4) {
            boundary += 6;
        } else if (nearbyAllies < 6 && boundary >= 27) {
            boundary -= 9;
        }

        if (!rc.onTheMap(thisLocation.translate(1, 0))) {
            // System.out.println(thisLocation.translate(1, 0));
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
                //System.out.println("MOVED AWAY FROM WALL" + determinedDirection);
                rc.move(determinedDirection);
            }
        } else if (thisLocation.distanceSquaredTo(Data.originPoint) < boundary) {
            //System.out.println("MOVING TO BOUNDARY");\
            if (role.length() > 0 && role.charAt(0) == '4' && weightedDirection.equals(Direction.CENTER)) {
                int[] coords = Communication.relCoordDecoder(role);
                int[] distance = Pathfinding.getDistance(Data.relOriginPoint, coords);
                MapLocation target = Data.originPoint.translate(distance[0], distance[1]);
                System.out.println(target.toString());
                Direction toTarget = thisLocation.directionTo(target);
                System.out.println(toTarget);
                Direction[] possibles = new Direction[5];
                possibles[0] = toTarget;
                possibles[1] = toTarget.rotateLeft();
                possibles[2] = toTarget.rotateRight();
                possibles[3] = toTarget.rotateLeft().rotateLeft();
                possibles[4] = toTarget.rotateRight().rotateRight();
                weightedDirection = possibles[(int) (Math.random() * 5)];
            }
            Direction nextDir = Pathfinding.smartNav(rc, thisLocation.directionTo(Data.originPoint).opposite());
            if (!weightedDirection.equals(Direction.CENTER)) {
                nextDir = Pathfinding.smartNav(rc, weightedDirection);
            }
            //nextDir = Pathfinding.smartNav(rc, thisLocation.directionTo(Data.originPoint).opposite());
            if (rc.canMove(nextDir)) {
                rc.move(nextDir);
            }

            // if (rc.canMove(thisLocation.directionTo(Data.originPoint).opposite())) {
            //     rc.move(thisLocation.directionTo(Data.originPoint).opposite());
            //     //System.out.println("MOVED TO BOUNDARY:" + thisLocation.directionTo(Data.originPoint).opposite());
            // }

        } else if (thisLocation.distanceSquaredTo(Data.originPoint) >= boundary) {
            // if (rc.canMove(thisLocation.directionTo(Data.originPoint).rotateRight())) {
            //     //System.out.println("MOVING TO DIAGONAL");
            //     rc.move(thisLocation.directionTo(Data.originPoint).rotateRight());
            // }
            //else {
            Direction nextDir = Pathfinding.smartNav(rc, thisLocation.directionTo(Data.originPoint));
            if (rc.canMove(nextDir)) {
                rc.move(nextDir);
            }

            if (rc.canMove(thisLocation.directionTo(Data.originPoint))) {
                //System.out.println("MOVING BACK");
                rc.move(thisLocation.directionTo(Data.originPoint));
                //System.out.println("MOVED BACK FROM BOUNDARY:" + thisLocation.directionTo(Data.originPoint));
            }
            //}
        }

        Direction[] directions = Data.directions;
        for (Direction direction : directions) {
            if (rc.canMove(direction)) {
                rc.move(direction);
                //System.out.println("MOVE RANDOM:" + direction);
            }
        }
    }

    public static void takeoverLogic(RobotController rc) throws GameActionException {
        Team ally = rc.getTeam();
        Team enemy = rc.getTeam().opponent();
        double empowerFactor = rc.getEmpowerFactor(rc.getTeam(), 0);
        MapLocation thisLocation = rc.getLocation();
        int thisInfluence = rc.getInfluence();

        if (rc.canSenseRadiusSquared(9)) {
            RobotInfo[] robots = rc.senseNearbyRobots(2);
            // System.out.println(robots.toString());
            for (RobotInfo robot : robots) {
                if (robot.getTeam().equals(Team.NEUTRAL)) {
                    int totalRobotsAround = 0;
                    if (rc.canSenseRadiusSquared(2)) {
                        RobotInfo[] robotS = rc.senseNearbyRobots(2);
                        totalRobotsAround = robotS.length;
                    }
                    if (totalRobotsAround > 0 && robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)
                            && ((rc.getInfluence() - 10) * empowerFactor) / totalRobotsAround > robot.getInfluence()) {
                        //if (rc.getLocation().distanceSquaredTo(robot.getLocation()) == 1) {
                        if (rc.canEmpower(2)) {
                            rc.empower(2);
                        }
                        //}
                        Direction nextDir = Pathfinding.basicBug(rc, robot.getLocation());
                        nextDir = Pathfinding.smartNav(rc, robot.getLocation());
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
                } else if ((robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)
                        && robot.getTeam().equals(rc.getTeam().opponent()))) {
                    int maxRadius = 0;
                    boolean explode = false;
                    for (int i = 9; i > 0; i--) {
                        boolean hasEnemyEC = false;
                        RobotInfo[] radius = rc.senseNearbyRobots(i);
                        int maxInf = 0;
                        int numOfUnits = radius.length;
                        for (RobotInfo rbt : radius) {
                            int rbtInfluence = rbt.getInfluence();
                            if (rbt.getTeam().equals(enemy)) {
                                //System.out.println(rbt.ID + ":" + rbtInfluence);
                                if (rbtInfluence > maxInf) {
                                    maxInf = rbtInfluence;
                                    //System.out.println(maxInf);
                                }
                                if (rbt.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
                                    hasEnemyEC = true;
                                }
                            }
                        }
                        if (numOfUnits != 0 && hasEnemyEC == true
                                && maxInf < ((thisInfluence - 10) * empowerFactor) / numOfUnits) {
                            maxRadius = i;
                            explode = true;
                            break;
                        }
                    }
                    if (rc.canEmpower(maxRadius) && explode == true) {
                        rc.empower(maxRadius);
                    }
                }
            }
        }
        int[] coords = Communication.relCoordDecoder(role);
        //System.out.println(role);
        int[] distance = Pathfinding.getDistance(Data.relOriginPoint, coords);
        MapLocation target = Data.originPoint.translate(distance[0], distance[1]);
        //System.out.println(target.toString());
        Direction nextDir = Pathfinding.basicBug(rc, target);
        nextDir = Pathfinding.smartNav(rc, target);
        if (rc.canMove(nextDir)) {
            rc.move(nextDir);
        }
    }

    public static void toTarget(RobotController rc) throws GameActionException {
        Team ally = rc.getTeam();
        Team enemy = ally.opponent();
        int thisInfluence = rc.getInfluence();
        double currentEmpowerFactor = rc.getEmpowerFactor(ally, 0);

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
                        if (neededInf < (thisInfluence - 10) * currentEmpowerFactor) {
                            maxRadius = i;
                            //System.out.println("NEWRADIUS" + maxRadius);
                            //System.out.println("NEEDEDINF" + neededInf);
                        } else {
                            break;
                        }
                    }
                    if (rc.canEmpower(maxRadius)) {
                        rc.empower(maxRadius);
                    }
                } else if (robot.getTeam().equals(enemy)
                        && robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 25)
                        && (thisInfluence - 10) * currentEmpowerFactor > robot.getInfluence()) {
                    Direction dir = Pathfinding.basicBug(rc, robot.getLocation());
                    dir = Pathfinding.smartNav(rc, robot.getLocation());
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
        nextDir = Pathfinding.smartNav(rc, target);
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
