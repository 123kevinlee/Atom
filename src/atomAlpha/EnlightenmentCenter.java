package atomAlpha;

import battlecode.common.*;
import java.util.*;

public class EnlightenmentCenter {
    public static boolean scoutingPhase = true;
    public static int scoutCount = 0;
    public static boolean[] scoutReturn = { false, false, false, false }; // 0=N, 1=S, 2=E, 3=W
    public static Set<Integer> scoutIds = new TreeSet<Integer>();

    public static int[] coords = new int[4]; // 0=x1 1=y1 2=x2 3=y2
    public static int scoutLimit = 20;
    public static ArrayList<Direction> enemyDirections = new ArrayList<Direction>();

    public static void run(RobotController rc, int turnCount) throws GameActionException {
        if (scoutingPhase && scoutIds.size() < scoutLimit) {
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
            for (Integer id : scoutIds) {
                if (rc.canGetFlag(id)) {
                    int flag = rc.getFlag(id);
                    if (flag != 0) {
                        System.out.println("id: " + id + "msg:" + flag);
                    }
                } else {
                    scoutIds.remove(id);
                    System.out.println("DEAD");
                }
            }
        }
        System.out.println(scoutIds.size());
    }
}
