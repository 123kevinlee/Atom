package atomAlpha;

import battlecode.common.*;
import java.util.*;

public class EnlightenmentCenter {
    public static boolean scoutingPhase = true;
    public static boolean setGuard = false;
    public static boolean rushPhase = false;
    public static int scoutCount = 0;
    public static int guardCount = 0;
    public static boolean[] scoutReturn = { false, false, false, false }; // 0=N, 1=S, 2=E, 3=W
    public static Set<Integer> scoutIds = new TreeSet<Integer>();

    public static int[] coords = new int[4]; // 0=x1 1=y1 2=x2 3=y2
    public static int scoutLimit = 20;
    public static ArrayList<Direction> enemyDirections = new ArrayList<Direction>();

    public static void run(RobotController rc, int turnCount) throws GameActionException {
        if (scoutingPhase && scoutIds.size() < scoutLimit) {
            if (rc.canSetFlag(100)) {
                rc.setFlag(100);
                // scout slanderer
            }

            int dirIndex = scoutCount % 4;
            System.out.println(scoutCount);
            int influence = 1;
            if (rc.canBuildRobot(RobotType.SLANDERER, Helper.directions[dirIndex * 2], influence)) {
                rc.buildRobot(RobotType.SLANDERER, Helper.directions[dirIndex * 2], influence);
                scoutCount++;
                if (rc.canSenseRadiusSquared(1)) {
                    for (RobotInfo robot : rc.senseNearbyRobots(1, rc.getTeam())) {
                        scoutIds.add(robot.getID());
                    }
                }
            }
            int removeId = 0;
            for (Integer id : scoutIds) {
                if (rc.canGetFlag(id)) {
                    int flag = rc.getFlag(id);
                    if (flag != 0) {
                        System.out.println("id: " + id + "msg:" + flag);
                    }
                } else {
                    removeId = id;
                    System.out.println("DEAD");
                }
            }
            if (removeId != 0) {
                scoutIds.remove(removeId);
            }
        }

        System.out.println(scoutIds.size());

        if (setGuard == true) {
            if (rc.canSetFlag(111)) {
                rc.setFlag(111);
                // defender politician
            }
            int influence = 10;
            int dirIndex = guardCount % 4;
            if (rc.canBuildRobot(RobotType.POLITICIAN, Helper.directions[dirIndex * 2 + 1], influence)) {
                rc.buildRobot(RobotType.POLITICIAN, Helper.directions[dirIndex * 2 + 1], influence);
                guardCount++;
            }
        }
    }
}
