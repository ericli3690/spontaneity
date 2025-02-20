package com.example.spontaneity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.spontaneity.databinding.EditMenuBinding;

import java.util.Arrays;

// makes a menu that shows as a popup on screen
// used for both editing reminders and creating reminders

public class EditAddMenu {

    public Dialog dialog;
    public EditMenuBinding binding;

    private final Reminder reminder;

    // spinner presets
    private final String[] typeSpinnerOptions = new String[] {
            "Health",
            "Positivity",
            "Quote",
            "Urgent",
            "Meditation",
            "Other"
    };

    private final String[] colorSpinnerOptions = new String[] {
            "None",
            "Red",
            "Orange",
            "Yellow",
            "Light Green",
            "Dark Green",
            "Light Blue",
            "Dark Blue",
            "Purple",
            "Pink"
    };

    private final String[] urgencySpinnerOptions = new String[] {"1", "2", "3", "4", "5"};

    // factory method for quickly making spinners
    private void makeSpinnerAdapter(Context context, Spinner spinner, String[] values, String defaultValue) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context,
                android.R.layout.simple_spinner_item,
                values
        );
        // set the dropdown visual to the default
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(Arrays.asList(values).indexOf(defaultValue)); // set the selected one using index
    }

    public EditAddMenu(String title, Context context, ViewGroup parent, Reminder reminderTemplate) {
        // open dialog
        this.dialog = new Dialog(context, R.style.DialogBoxStyle);
        // lower the opacity of the rest of the window to 100/255
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100,0,0,0)));
        // inflate the menu's xml, set it as the dialog's root
        this.binding = EditMenuBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        this.reminder = reminderTemplate;
        // set menu values
        binding.editBanner.setText(title);
        binding.nameField.setText(reminder.getName());
        binding.descriptionField.setText(reminder.getDescription());
        makeSpinnerAdapter(
                context,
                binding.typeField, // spinner
                typeSpinnerOptions, // options
                reminder.getType() // default
        );
        makeSpinnerAdapter(
                context,
                binding.colorField,
                colorSpinnerOptions,
                reminder.getColor()
        );
        makeSpinnerAdapter(
                context,
                binding.urgencyField,
                urgencySpinnerOptions,
                String.valueOf(reminder.getUrgency())
        );
        dialog.setContentView(binding.getRoot());
        dialog.setCancelable(true);
        dialog.show();
    }

    public void lockAnswers() {
        // whenever the user presses submit
        // transfer the data in the bindings to the data obj
        reminder.setName(binding.nameField.getText().toString());
        reminder.setDescription(binding.descriptionField.getText().toString());
        reminder.setUrgency(Integer.parseInt(binding.urgencyField.getSelectedItem().toString()));
        reminder.setType(binding.typeField.getSelectedItem().toString());
        reminder.setColor(binding.colorField.getSelectedItem().toString());
    }
}
