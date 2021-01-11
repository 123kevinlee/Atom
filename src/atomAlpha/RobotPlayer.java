package atomAlpha;

import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    /**
     * run() is the method that is called when a robot is instantiated in the
     * Battlecode world. If this method returns, the robot dies!
     **/
    // OUI OUI BAGUETTE
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        RobotPlayer.rc = rc;
        switch (rc.getType()) {
            case POLITICIAN:
                Politician.init(rc);
                Pathfinding.setStartLocation(rc);
                break;

            case SLANDERER:
                Slanderer.init(rc);
                Pathfinding.setStartLocation(rc);
                break;
            case MUCKRAKER:
                Muckraker.init(rc);
                Pathfinding.setStartLocation(rc);
            case ENLIGHTENMENT_CENTER:
                System.out.println("ATOM V1.2: Good Luck!");
                EnlightenmentCenter.init(rc);
                break;
            default:
                break;
        }

        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                switch (rc.getType()) {

                    case ENLIGHTENMENT_CENTER:
                        // runEnlightenmentCenter();
                        EnlightenmentCenter.run(rc);
                        // System.out.println("Influence:" + rc.getInfluence());
                        // System.out.println(Clock.getBytecodesLeft());
                        // System.out.println("Bytecode Left:" + Clock.getBytecodesLeft());
                        break;
                    case POLITICIAN:
                        Politician.run(rc);
                        // System.out.println(Clock.getBytecodesLeft());
                        // runPolitician();
                        break;
                    case SLANDERER:
                        Slanderer.run(rc);
                        // System.out.println(Clock.getBytecodesLeft());
                        // runSlanderer();
                        break;
                    case MUCKRAKER:
                        Muckraker.run(rc);
                        // System.out.println(Clock.getBytecodesLeft());
                        // runMuckraker();
                        break;

                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform
                // this loop again
                Clock.yield();

            } catch (Exception e) {
                //System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }
}
