package atomStorm;

import battlecode.common.*;

public class Data {
    public static int baseId = 0;
    public static MapLocation originPoint = new MapLocation(0, 0);
    public static int initRound = 0;

    public static Direction slandererConvertDirection = Direction.CENTER;

    public static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST,
            Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

    public static int slandererBoundary = 40;
    public static int politicianDefenderBoundary = 60;
    public static int politicianBoundary = 125;
    public static int muckrakerBoundary = 140;
}
