package atom;

import java.text.*;
import battlecode.common.RobotType;

public class Communication {
    public static int coordEncoder(String type, int relx, int rely) {
        String outMsg = "";
        switch (type) {
            case "ENEMY": // ec in and out
                outMsg += "2";
                break;
            case "BEACON": // ec in
                outMsg += "3"; // also conversion
                break;
            case "WALL": // ec in
                outMsg += "4";
                break;
            case "NEUTRAL": // ec out
                outMsg += "5";
                break;
            case "ALLY":
                outMsg += "6";
                break;
        }
        DecimalFormat df = new DecimalFormat("000");

        outMsg += df.format(relx) + df.format(rely);
        return Integer.parseInt(outMsg);
    }

    public static int[] relCoordDecoder(String msg) {
        msg = msg.substring(1);
        int[] temp = new int[] { Integer.parseInt(msg.substring(0, 3)), Integer.parseInt(msg.substring(3)) };
        return temp;
    }
}
