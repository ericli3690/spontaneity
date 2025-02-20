package com.example.spontaneity;

// holds some global information that can't really be stored elsewhere

public class Globals {

    // upcoming notification
    public static String nextTitle = "Click Me!";
    public static String nextDesc = "It's time to check in on your habits.";

    // rounding to x decimal places, because java doesn't provide it
    public static double roundToPlaces(int places, double val) {
        double shift = Math.pow(10, places); // ex 10^2, 10^-1, how much the decimal should move
        return Math.round(shift * val) / shift; // shift decimal place, round off the rest, then remove the shift
    }

}
