package com.example.spontaneity;

import android.app.Activity;
import android.content.Context;

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
    private final Activity activity; // need the activity to send snackbar errors

    // constructor so that a filemanager can be generated for each file
    public FileManager(Context context, Activity activity, String filepath) {
        this.file = new File(context.getFilesDir(), filepath);
        this.activity = activity;
    }

    // if a file error occurs for some reason
    // should never fire...
    private void onError() {
        // display an error snackbar
        Snackbar.make(
                activity.findViewById(android.R.id.content),
                "FILE ERROR: Please try again later.",
                BaseTransientBottomBar.LENGTH_SHORT
        ).show();
    }

    // check if createFile has been run
    public boolean wasCreated() { // check if the file exists already, ie createFile was already run for this filepath
        return file.isFile();
    }

    // reverse createFile
    public boolean deleteFile() { // undo createFile, will not destroy this manager
        return file.delete();
    }

    // create the file
    public void createFile(String[] contents) {
        try {
            // make a file, then write to it
            // even if the file already exists, just overwrite it
            // check wasCreated() beforehand to ensure overwrites dont happen
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            // write every line in contents to the file, separated with breaks
            for (String content : contents) {
                writer.write(content + "\n");
            }
            writer.close(); // close writer
        } catch (IOException e) {
            // sometimes filewriter crashes
            this.onError();
        }
    }

    // get all the contents in the file as an array, with each line of the file in a slot of the array
    public String[] readFile() {
        try {
            List<String> output = new ArrayList<String>();
            Scanner scanner = new Scanner(file);
            // while loop and scanner, array manipulation, indexing
            // read each line and push it to the output array
            while (scanner.hasNextLine()) {
                output.add(scanner.nextLine());
            }
            scanner.close();
            // return as a string array, array manipulation
            return output.toArray(new String[0]);
        } catch (IOException e) {
            // return nothing in case of failure for whatever reason
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

}
