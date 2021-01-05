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
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER:
                        // runEnlightenmentCenter();
                        EnlightenmentCenter.run(rc, turnCount);
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


    static void runSlanderer() throws GameActionException {

        if (turnCount < 2) {
            for (Direction dir : directions) {
                if (!rc.canMove(dir) && rc.getCooldownTurns() == 0) {
                    switch (dir) {
                        case NORTH:
                            if (rc.canSetFlag(1)) {
                                rc.setFlag(1);
                            }
                            break;
                        case EAST:
                            if (rc.canSetFlag(2)) {
                                rc.setFlag(2);
                            }
                            break;
                        case SOUTH:
                            if (rc.canSetFlag(3)) {
                                rc.setFlag(3);
                            }
                            break;
                        case WEST:
                            if (rc.canSetFlag(4)) {
                                rc.setFlag(4);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        int flag = rc.getFlag(rc.getID());
        // System.out.println(flag);
        if (flag == 1) {
            if (rc.canMove(Direction.SOUTH)) {
                rc.move(Pathfinder.chooseScoutNextStep(Direction.SOUTH, rc));
                System.out.println("I moved!");
            }
        } else if (flag == 2) {
            if (rc.canMove(Direction.WEST)) {
                rc.move(Pathfinder.chooseScoutNextStep(Direction.WEST, rc));
                System.out.println("I moved!");
            }
        } else if (flag == 3) {
            if (rc.canMove(Direction.NORTH)) {
                rc.move(Pathfinder.chooseScoutNextStep(Direction.NORTH, rc));
                System.out.println("I moved!");
            }
        } else if (flag == 4) {
            if (rc.canMove(Direction.EAST)) {
                rc.move(Pathfinder.chooseScoutNextStep(Direction.EAST, rc));
                System.out.println("I moved!");
            }
        }
        // if (tryMove(randomDirection()))
        // System.out.println("I moved!");
    }

    