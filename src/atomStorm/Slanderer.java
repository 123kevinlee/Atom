package atomStorm;

import battlecode.common.*;

public class Slanderer {
    public static Direction safeDirection;
    public static String cornerRole = "";
    public static boolean end = false;
    public static String role = "";

    public static void run(RobotController rc) throws GameActionException {
        String baseFlag = Integer.toString(rc.getFlag(Data.baseId));
        if (rc.canGetFlag(Data.baseId) && baseFlag.length() == 4) {
            if (baseFlag.substring(0, 3).equals("112")) {
                Direction[] directionsS = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };
                safeDirection = directionsS[Integer.parseInt(Character.toString((baseFlag.charAt(3))))].opposite();
            }
        }
        urgentMoves(rc);
        if (safeDirection != null) {
            MapLocation safeTarget = Data.originPoint.add(safeDirection).add(safeDirection).add(safeDirection)
                    .add(safeDirection).add(safeDirection).add(safeDirection);
            safeDirection = Pathfinding.basicBugToBase(rc, safeTarget);
            if (rc.canMove(safeDirection)) {
                rc.move(safeDirection);
            }
        }
        if (role.length() == 7 && role.charAt(0) == '5') {
            relocate(rc);
        } else if (role.equals("102")) {
            nearFarm(rc);
        }
    }

    public static void urgentMoves(RobotController rc) throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        Team home = rc.getTeam();
        boolean muckrakerThreat = false;
        boolean getAwayEC = false;
        RobotInfo[] fleeRaker = rc.senseNearbyRobots(20, enemy);
        RobotInfo[] awayEC = rc.senseNearbyRobots(5, home);
        int priorityEnemy = -1;

        for (int i = 0; i < fleeRaker.length; i++) {
            if (fleeRaker[i].getType().equals(RobotType.MUCKRAKER)
                    && fleeRaker[i].getType().equals(RobotType.POLITICIAN)) {
                priorityEnemy = i;
                muckrakerThreat = true;
                break;
            }
        }

        if (muckrakerThreat == false) {
            for (int i = 0; i < awayEC.length; i++) {
                if (awayEC[i].getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
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
                MapLocation enemyLoc = fleeRaker[priorityEnemy].getLocation();
                Direction awayFromEnemy = myLoc.directionTo(enemyLoc).opposite();

                for (int i = 0; i < 2; i++) {
                    destination = destination.add(awayFromEnemy);
                }
            } else if (getAwayEC) {
                // Move away from ec at the opposite and rotated 45 direction
                MapLocation ecLoc = awayEC[priorityEnemy].getLocation();
                Direction awayFromEc = myLoc.directionTo(ecLoc).opposite();
                for (int i = 0; i < 2; i++) {
                    destination = destination.add(awayFromEc);
                }
            }

            if (rc.canMove(Pathfinding.basicBugToBase(rc, destination))) {
                rc.move(Pathfinding.basicBugToBase(rc, destination));
            }
        }
    }

    public static void nearFarm(RobotController rc) throws GameActionException {
        Direction randomDir = Data.directions[(int) (Math.random() * 8)];
        int boundary = Data.slandererBoundary;
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

    public static void relocate(RobotController rc) throws GameActionException {
        int[] coords = Communication.coordDecoder(role);
        coords[0] += Data.originPoint.x;
        coords[1] += Data.originPoint.y;
        MapLocation targetLocation = new MapLocation(coords[0], coords[1]);
        if (rc.canMove(Pathfinding.basicBugToBase(rc, targetLocation))) {
            rc.move(Pathfinding.basicBugToBase(rc, targetLocation));
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
