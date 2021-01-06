package atomAlpha;

import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;
    static int turnCount;

    /**
     * run() is the method that is called when a robot is instantiated in the
     * Battlecode world. If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this
        // robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        turnCount = 0;
        // System.out.println("I'm a " + rc.getType() + " and I just got created!");
        switch (rc.getType()) {
            case POLITICIAN:
                Politician.getRole(rc);
                Pathfinding.setStartLocation(rc);
                break;

            case SLANDERER:
                Slanderer.getRole(rc);
                Pathfinding.setStartLocation(rc);
                break;
            case MUCKRAKER:
                Muckraker.getRole(rc);
                Pathfinding.setStartLocation(rc);
            default:
                break;
        }

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER:
                        // runEnlightenmentCenter();
                        EnlightenmentCenter.run(rc, turnCount);
                        // System.out.println("Bytecode Left:" + Clock.getBytecodesLeft());
                        break;
                    case POLITICIAN:
                        Politician.run(rc, turnCount);
                        // runPolitician();
                        break;
                    case SLANDERER:
                        Slanderer.run(rc, turnCount);
                        // runSlanderer();
                        break;
                    case MUCKRAKER:
                        Muckraker.run(rc, turnCount);
                        // runMuckraker();
                        break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform
                // this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }
}
