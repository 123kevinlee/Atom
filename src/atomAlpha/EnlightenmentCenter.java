package atomAlpha;

import battlecode.common.*;

public class EnlightenmentCenter {
    public static void run(RobotController rc, int turnCount) throws GameActionException {
        int influence = 50;
        System.out.println("Influence: " + rc.getInfluence());
        System.out.println(turnCount);

        if (turnCount < 50) {
            int dirI = Helper.getRandomInteger(0, 3);
            if (rc.canBuildRobot(RobotType.SLANDERER, Helper.directions[dirI * 2], influence)) {
                rc.buildRobot(RobotType.SLANDERER, Helper.directions[dirI * 2], influence);
            }
        }
    }
}
