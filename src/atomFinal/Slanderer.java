package atomFinal;

import battlecode.common.*;

public class Slanderer {
    public static Direction safeDirection = Direction.CENTER;
    public static String role = "";
    public static int boundary = 14;

    public static void run(RobotController rc) throws GameActionException {
        logic(rc);
    }

    public static void logic(RobotController rc) throws GameActionException {
        MapLocation thisLocation = rc.getLocation();
        Team thisTeam = rc.getTeam();
        Team enemy = thisTeam.opponent();

        if (rc.canSenseRadiusSquared(20)) {
            RobotInfo[] robots = rc.senseNearbyRobots(20);
            for (RobotInfo robot : robots) {
                if (robot.getTeam().equals(enemy)) {
                    Direction nextDir = Pathfinding.farmerBug(rc,
                            thisLocation.directionTo(robot.getLocation()).opposite());
                    if (rc.canMove(nextDir)) {
                        rc.move(nextDir);
                    }
                } else if (robot.getTeam().equals(thisTeam) && rc.canGetFlag(robot.getID())) {
                    int flag = rc.getFlag(robot.getID());
                    if (flag == 666) {
                        Direction nextDir = Pathfinding.farmerBug(rc,
                                thisLocation.directionTo(robot.getLocation()).opposite());
                        if (rc.canMove(nextDir)) {
                            rc.move(nextDir);
                        }
                    }
                } else if (robot.getTeam().equals(thisTeam) && thisLocation.distanceSquaredTo(Data.originPoint) > robot
                        .getLocation().distanceSquaredTo(Data.originPoint)) {
                    Direction nextDir = Pathfinding.farmerBug(rc, thisLocation.directionTo(Data.originPoint));
                    if (rc.canMove(nextDir)) {
                        rc.move(nextDir);
                    }
                }
            }
        }

        if (rc.getInfluence() == 130 && rc.getRoundNum() - Data.initRound < 5) {
            if (rc.canMove(Direction.SOUTH)) {
                rc.move(Direction.SOUTH);
            }
            boundary = 8;
        }

        if (thisLocation.distanceSquaredTo(Data.originPoint) < boundary) {
            Direction nextDir = Pathfinding.farmerBug(rc, thisLocation.directionTo(Data.originPoint).opposite());
            ;
            if (rc.canMove(nextDir)) {
                rc.move(nextDir);
                return;
            }
        } else if (thisLocation.distanceSquaredTo(Data.originPoint) > boundary) {
            Direction nextDir = Pathfinding.farmerBug(rc, thisLocation.directionTo(Data.originPoint));
            if (rc.canMove(nextDir)) {
                rc.move(nextDir);
                return;
            }
        }

        Direction randomDir = Data.directions[(int) (Math.random() * 8)];

        Direction nextDir = Pathfinding.basicBug(rc, rc.getLocation().add(randomDir));
        if (rc.canMove(nextDir)) {
            rc.move(nextDir);
        }
    }

    public static void init(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(2)) {
            for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    Data.baseId = robot.getID();
                    if (rc.canGetFlag(Data.baseId)) {
                        role = Integer.toString(rc.getFlag(Data.baseId));
                        Data.originPoint = robot.getLocation();
                        Data.relOriginPoint[0] = Data.originPoint.x % 128;
                        Data.relOriginPoint[1] = Data.originPoint.y % 128;
                        Data.initRound = rc.getRoundNum();
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
