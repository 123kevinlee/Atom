package atomAlpha;

import battlecode.common.*;

public class Data {
    public static int baseId = 0;
    public static MapLocation originPoint = new MapLocation(0, 0);
    public static int initRound = 0;

    public static Direction slandererConvertDirection = Direction.CENTER;

    static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };
}
