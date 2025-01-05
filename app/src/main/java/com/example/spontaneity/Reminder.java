package com.example.spontaneity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// data structure for reminders
// editing this will change the viewholder binding in adapter if notifyxchanged() is called on adapter

public class Reminder {
    // instance vars
    private String name;
    private String description;
    private int urgency;
    private String type;
    private String color;
    private boolean checked;

    // const
    public Reminder(String name, String description, int urgency, String type, String color, boolean checked) {
        this.name = name;
        this.description = description;
        this.urgency = urgency;
        this.type = type;
        this.color = color;
        this.checked = checked;
    }

    // getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUrgency() {
        return urgency;
    }

    public void setUrgency(int urgency) {
        this.urgency = urgency;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getShortDescription() {
        int SHORT_DESC_LEN = 32;
        if (description.length() <= SHORT_DESC_LEN) {
            return description;
        } else {
            return description.substring(0, SHORT_DESC_LEN) + "...";
        }
    }

    // some old getters before a better ui solution was discovered
    public String getShortName() {
        int SHORT_NAME_LEN = 15;
        if (name.length() <= SHORT_NAME_LEN) {
            return name;
        } else {
            return name.substring(0, SHORT_NAME_LEN) + "...";
        }
    }

    // static default reminders
    // using list, array list



    // constant
    final static int NUM_DEFAULTS = 7;

    // parallel arrays

    private static final String[] defaultNames = new String[] {
            "Drink More Water",
            "Love Yourself",
            "Be Grateful for Something",
            "Do Nothing for Ten Seconds",
            "Breathe",
            "Get Up and Walk Around",
            "Focus!"
    };

    private static final String[] defaultDescriptions = new String[] {
            "Consuming H2O is good for you, both physically and mentally",
            "You are beautiful, you are shining, you are destined for great things",
            "Gratefulness is the number one contributor to general happiness",
            "Sudden peace of mind",
            "In, out",
            "Exercise breaks are crucial, and even a five second stretch can help",
            "Just do it!"
    };

    private static final int[] defaultUrgencies = new int[] {1,2,3,4,5,3,3};

    private static final String[] defaultTypes = new String[] {
            "Health",
            "Positivity",
            "Quote",
            "Meditation",
            "Meditation",
            "Health",
            "Quote"
    };

    private static final String[] defaultColors = new String[] {
            "None",
            "Red",
            "Light Blue",
            "None",
            "Purple",
            "Yellow",
            "None"
    };

    private static final boolean[] defaultCheckeds = new boolean[] {true, true, true, true, false, false, false};

    // declare and initialize
    public static final List<Reminder> defaultReminders = new ArrayList<Reminder>();

    static {
        // array manipulation, add in all default reminders
        for (int i = 0; i < NUM_DEFAULTS; i++) {
            defaultReminders.add(new Reminder(
                    defaultNames[i],
                    defaultDescriptions[i],
                    defaultUrgencies[i],
                    defaultTypes[i],
                    defaultColors[i],
                    defaultCheckeds[i]
            ));
        }
    }

    // convert a list of reminders to a string perfect for file storage
    public static String[] getRemindersString(List<Reminder> list) {
        // convert default reminders to a string that can be saved
        String[] lines = new String[list.size()];
        // iterate through all default reminders
        for (int reminderNum = 0; reminderNum < list.size(); reminderNum++) {
            Reminder reminder = list.get(reminderNum);
            // convert those default reminders' data to strings, save them as lines with a delimiter
            lines[reminderNum] =    reminder.getName() + "%%%" +
                                    reminder.getDescription() + "%%%" +
                                    reminder.getUrgency() + "%%%" +
                                    reminder.getType() + "%%%" +
                                    reminder.getColor() + "%%%" +
                                    reminder.getChecked();
        }
        return lines;
    }

}
