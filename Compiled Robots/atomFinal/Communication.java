package atomFinal;

import java.text.*;

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
            case "WARN":
                outMsg += "7";
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

    public static int neutralCoordEncoder(int influence, int relx, int rely) {
        String outMsg = "1";
        if (influence % 100 == 0) {
            influence /= 100;
        } else {
            influence += 100;
            influence /= 100;
        }
        outMsg += Integer.toString(influence);
        DecimalFormat df = new DecimalFormat("000");

        outMsg += df.format(relx) + df.format(rely);
        return Integer.parseInt(outMsg);
    }
}
