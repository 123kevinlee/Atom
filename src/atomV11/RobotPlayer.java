package atomV11;

import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    public static void run(RobotController rc) throws GameActionException {
        RobotPlayer.rc = rc;
        switch (rc.getType()) {
            case POLITICIAN:
                Politician.init(rc);
                break;
            case SLANDERER:
                Slanderer.init(rc);
                break;
            case MUCKRAKER:
                Muckraker.init(rc);
            case ENLIGHTENMENT_CENTER:
                EnlightenmentCenter.init(rc);
                break;
        }

        while (true) {
            try {
                switch (rc.getType()) {

                    case ENLIGHTENMENT_CENTER:
                        EnlightenmentCenter.run(rc);
                        break;
                    case POLITICIAN:
                        Politician.run(rc);
                        break;
                    case SLANDERER:
                        Slanderer.run(rc);
                        break;
                    case MUCKRAKER:
                        Muckraker.run(rc);
                        break;

                }
                Clock.yield();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
