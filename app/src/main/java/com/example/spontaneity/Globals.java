package com.example.spontaneity;

// holds some global information that can't really be stored elsewhere

public class Globals {

    // rounding to x decimal places, because java doesn't provide it
    public static double roundToPlaces(int places, double val) {
        double shift = Math.pow(10, places); // ex 10^2, 10^-1, how much the decimal should move
        return Math.round(shift * val) / shift; // shift decimal place, round off the rest, then remove the shift
    }

    // switch for urgency color
    public static int getUrgencyColor(int urgency) {
        switch (urgency) {
            case 5:
                return R.color.red;
            case 4:
                return R.color.orange;
            case 3:
                return R.color.yellow_2;
            case 2:
                return R.color.teal_200;
            case 1:
                return R.color.teal_700;
            default: // in case of failure
                return R.color.black;
        }
    }

    // switch for reminder color
    public static int getTextColor(String colorName) {
        switch (colorName) {
            case "Red":
                return R.color.red;
            case "Orange":
                return R.color.orange;
            case "Yellow":
                return R.color.yellow;
            case "Light Green":
                return R.color.light_green;
            case "Dark Green":
                return R.color.dark_green;
            case "Light Blue":
                return R.color.light_blue;
            case "Dark Blue":
                return R.color.dark_blue;
            case "Purple":
                return R.color.purple_500;
            case "Pink":
                return R.color.pink;
            case "None":
            default: // in case of failure, grey
                return R.color.grey;
        }
    }

}
