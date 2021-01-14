package atom;

import battlecode.common.*;

public class Slanderer {
    public static Direction safeDirection = Direction.CENTER;
    public static String role = "";

    public static void run(RobotController rc) throws GameActionException {
        int baseFlag = -1;
        if (rc.canGetFlag(Data.baseId)) {
            baseFlag = rc.getFlag(Data.baseId);
        }
        if (baseFlag != -1) {
            if (baseFlag > 0 && baseFlag <= 7) {
                safeDirection = Data.directions[baseFlag].opposite();
            }
        }
        logic(rc);
    }

    public static void logic(RobotController rc) throws GameActionException {
        Direction randomDir = Data.directions[(int) (Math.random() * 8)];
        int boundary = 14;
        if (rc.getInfluence() == 150) {
            boundary = 6;
        }

        if (rc.canSenseRadiusSquared(20)) {
            RobotInfo[] robots = rc.senseNearbyRobots(20, rc.getTeam().opponent());
            for (RobotInfo robot : robots) {
                if (robot.getType().equals(RobotType.MUCKRAKER)) {
                    Direction nextDir = Pathfinding.basicBug(rc,
                            rc.getLocation().add(rc.getLocation().directionTo(robot.getLocation()).opposite()));
                    if (rc.canMove(nextDir)) {
                        rc.move(nextDir);
                        return;
                    }
                }
            }
        }

        if (safeDirection != Direction.CENTER) {
            Direction nextDir = Pathfinding.basicBug(rc, Data.originPoint.add(safeDirection).add(safeDirection)
                    .add(safeDirection).add(safeDirection).add(safeDirection).add(safeDirection));
            if (rc.canMove(nextDir)) {
                rc.move(nextDir);
                return;
            }
        }

        if (rc.getLocation().distanceSquaredTo(Data.originPoint) < 4) {
            Direction nextDir = Pathfinding.basicBug(rc,
                    rc.getLocation().add(rc.getLocation().directionTo(Data.originPoint).opposite()));
            if (rc.canMove(nextDir)) {
                rc.move(nextDir);
                return;
            }
        } else if (rc.getLocation().distanceSquaredTo(Data.originPoint) > boundary) {
            Direction nextDir = Pathfinding.basicBug(rc,
                    rc.getLocation().add(rc.getLocation().directionTo(Data.originPoint)));
            if (rc.canMove(nextDir)) {
                rc.move(nextDir);
                return;
            }
        }

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
