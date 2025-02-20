package com.example.spontaneity;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

// separate class for file management
// for reusability
// handles opening, reading, and writing files

public class FileManager {

    private final File file;
    private final Context context;

    public FileManager(Context context, String filepath) {
        this.file = new File(context.getFilesDir(), filepath);
        this.context = context;
    }

    // if a file error occurs for some reason
    private void onError() {
        Toast.makeText(context,
                "FILE ERROR: Please try again later.",
                Toast.LENGTH_SHORT).show();
    }

    public boolean wasCreated() { // check if the file exists already, ie createFile was already run for this filepath
        return file.isFile();
    }

    public boolean deleteFile() { // undo createFile, will not destroy this manager
        return file.delete();
    }

    public void createFile(String[] contents) {
        try {
            // make a file, then write to it
            // even if the file already exists, just overwrite it
            // check wasCreated() beforehand to ensure overwrites dont happen
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            for (String content : contents) {
                writer.write(content + "\n");
            }
            writer.close();
        } catch (IOException e) {
            this.onError();
        }
    }

    public String[] readFile() {
        try {
            List<String> output = new ArrayList<String>();
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                output.add(scanner.nextLine());
            }
            scanner.close();
            // return as a string array, array manipulation
            return output.toArray(new String[0]);
        } catch (IOException e) {
            // return nothing in case of failure
            this.onError();
            return new String[0];
        }
    }

    public void appendFile(String[] newLines) {
        // read the file, add the new lines to be added to that read-array, then rewrite the array to file
        List<String> workingFile = new ArrayList<String>(Arrays.asList(this.readFile()));
        Collections.addAll(workingFile, newLines);
        // may be unable to delete
        // but does not need to be caught here
        // createFile, which checks if a file is about to be overwritten, will fire instead
        // strictly speaking file.delete() isnt necessary but will be included just in case
        file.delete();
        this.createFile(workingFile.toArray(new String[0])); // return as string array
    }

    public List<Reminder> readRemindersAsList() {
        // get the list of items to display
        // either from defaults, or using internal storage
        List<Reminder> listElements = new ArrayList<Reminder>();
        FileManager remindersFile = new FileManager(context, "reminders.txt");
        if (remindersFile.wasCreated()) {
            String[] readFile = remindersFile.readFile();
            for (String line : readFile) {
                if (!line.equals("")) {
                    // if all lines are empty, there will be no reminders
                    String[] splitLine = line.split("%%%");
                    listElements.add(new Reminder(
                            splitLine[0],
                            splitLine[1],
                            Integer.parseInt(splitLine[2]),
                            splitLine[3],
                            splitLine[4],
                            Boolean.parseBoolean(splitLine[5])
                    ));
                }
            }
        } else {
            // file has not been created yet, just use the defaults
            // the file SHOULD always always exist, but just in case...
            listElements = Reminder.defaultReminders;
            remindersFile.createFile(Reminder.getRemindersString(Reminder.defaultReminders));
        }
        return listElements;
    }

    public int getNextId() {
        // use an internal file holding an int
        FileManager fileManager = new FileManager(context, "id.txt");
        int id;
        if (fileManager.wasCreated()) {
            String[] readFile = fileManager.readFile();
            id = Integer.parseInt(readFile[readFile.length - 1]);
            id++;
            fileManager.deleteFile();
        } else {
            id = 1;
        }
        // make a new file with the id as the single value
        fileManager.createFile(new String[] {String.valueOf(id)});
        return id;
    }

}
