package com.example.spontaneity;

// holds some useful global functtions

public class Globals {
    public static boolean notificationsStarted = false; // makes sure multiple notif schedulers don't run simultaneously

    // rounding to x decimal places, because java doesn't provide it
    public static double roundToPlaces(int places, double val) {
        double shift = Math.pow(10, places); // ex 10^2, 10^-1, how much the decimal should move
        return Math.round(shift * val) / shift; // shift decimal place, round off the rest, then remove the shift
    }
}
