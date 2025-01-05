package com.example.spontaneity;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spontaneity.databinding.EditMenuBinding;
import com.example.spontaneity.databinding.ReminderDisplayBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// ADAPTER
// crucial class that governs how both the collection of reminders as a whole works
// and also how individual reminders work

public class ReminderDisplayRecyclerViewAdapter extends RecyclerView.Adapter<ReminderDisplayRecyclerViewAdapter.ViewHolder> {

    // public list of reminders
    // each element is a reminder
    // if these are edited anywhere in the code and adapter.notifyx is called, the screen will update
    public final List<Reminder> reminders;

    // get enclosings
    private final Context enclosingContext;
    private final Activity enclosingActivity;
    public ViewGroup enclosingViewGroup;

    // const
    public ReminderDisplayRecyclerViewAdapter(List<Reminder> reminders, Context context, Activity activity) {
        // sort reminders alphabetically
        Collections.sort(reminders, Comparator.comparing(Reminder::getName));
        this.reminders = reminders;
        this.enclosingContext = context;
        this.enclosingActivity = activity;
    }

    // delete this
    private void deleteThis(Reminder reminder) { // user has confirmed a delete
        // is used twice: onlongpress, and at edit.deletebutton
        // update view
        notifyItemRemoved(reminders.indexOf(reminder));
        notifyItemRangeChanged(reminders.indexOf(reminder), reminders.size());
        // remove the reminder
        reminders.remove(reminder);
        // save this change
        reloadFile();
    }

    // RELOADS

    // ui reload
    public void reloadList() {
        // sort alphabetically
        Collections.sort(reminders, Comparator.comparing(Reminder::getName));
        // notify the ui
        notifyDataSetChanged();
    }

    // save reload
    public void reloadFile() {
        // write to file
        // could do this in a really complex way by finding the exact line and changing it
        // but completely overwriting the file, while more inefficient, is faster
        FileManager fileManager = new FileManager(enclosingContext, enclosingActivity, "reminders.txt");
        fileManager.deleteFile(); // delete and recreate
        fileManager.createFile(Reminder.getRemindersString(reminders)); // use Reminders format method
    }

    // MENU ACTIONS

    // delete every single reminder
    public void deleteAll() {
        // delete all reminders in file
        FileManager fileManager = new FileManager(enclosingContext, enclosingActivity, "reminders.txt");
        fileManager.deleteFile();
        fileManager.createFile(new String[] {""}); // file must always exist, but add empty marker
        // delete everything in reminders
        int remindersSize = reminders.size();
        reminders.clear();
        // notify the ui
        notifyItemRangeChanged(0, remindersSize);
    }

    // add default reminders
    public void addDefaults() {
        // append reminders to file
        FileManager fileManager = new FileManager(enclosingContext, enclosingActivity, "reminders.txt");
        fileManager.appendFile(Reminder.getRemindersString(Reminder.defaultReminders));
        // add to the data
        reminders.addAll(Reminder.defaultReminders);
        // notify ui
        reloadList();
    }

    // when a viewholder is created, set its listeners
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // save the viewgroup for use
        enclosingViewGroup = parent;
        // get the binding by inflating it with the xml provided by the parent element it will be attached to
        // also pass in the parent and not to attach it to the app root
        ReminderDisplayBinding binding = ReminderDisplayBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);
        // attach the binding so the viewholder can get the child views of the main element
        // henceforth viewHolder will be used instead of binding to make it clear what is being referred to
        ViewHolder viewHolder = new ViewHolder(binding);

        // add an event listener for when any element is clicked, open edit menu
        binding.element.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the clicked reminder
                Reminder reminder = reminders.get(viewHolder.getBindingAdapterPosition());
                // make a new menu using EditAddMenu, with title, deps, and the reminder to populate it with
                EditAddMenu editMenu = new EditAddMenu(
                        "EDIT REMINDER",
                        enclosingContext,
                        parent,
                        reminder
                );
                // set the delete button's functions
                editMenu.binding.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // delete the reminder and dismiss the edit prompt
                        deleteThis(reminder);
                        editMenu.dialog.dismiss();
                    }
                });
                // set the done button's functions
                editMenu.binding.doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // set the reminder's display data
                        editMenu.lockAnswers();
                        // apply changes, then dismiss the edit prompt
                        reloadList();
                        reloadFile();
                        editMenu.dialog.dismiss();
                    }
                });
                // old testing toast
//                Toast.makeText(parent.getContext(), binding.elementNameField.getText(), Toast.LENGTH_SHORT).show();
            }
        });

        // add an event listener for when any element is long clicked, for deleting
        binding.element.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // get the clicked reminder
                Reminder reminder = reminders.get(viewHolder.getBindingAdapterPosition());
                // get confirmation for delete
                Snackbar deleteConfirmation = Snackbar.make(
                        view,
                        "Delete?",
                        BaseTransientBottomBar.LENGTH_LONG
                );
                deleteConfirmation.setAction("Yes", new View.OnClickListener() {
                    // confirmation recieved
                    @Override
                    public void onClick(View view) {
                        deleteThis(reminder);
                    }
                });
                deleteConfirmation.show();
                return true; // mark event as handled
            }
        });

        // if the switch is toggled, update the data for the reminder
        binding.elementSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // get the clicked reminder
                Reminder reminder = reminders.get(viewHolder.getBindingAdapterPosition());
                // isChecked is whether it is checked or not
                reminder.setChecked(isChecked);
                reloadFile();
            }
        });

        // return a new viewholder object, defined below using the viewholder class, and thus allows multiple instances
        // ie the multiple elements of the list
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // when a viewholder is binded to the view, set its characteristics using the data set
        // this is where the dataset is cast to the binding
        holder.nameView.setText(reminders.get(position).getName()); // set name
        holder.descriptionView.setText(reminders.get(position).getDescription()); // set description

        // urgency
        int urgency = reminders.get(position).getUrgency(); // get urgency
        holder.urgencyView.setText(String.valueOf(urgency)); // set urgency text
        // set urgency color
        holder.urgencyView.setTextColor( // set urgency color
            ContextCompat.getColor( // need to use a color id, then need to pass in context
                enclosingContext, // pass in context
                getUrgencyColor(urgency) // get color id using method below
            )
        );

        // color
        String color = reminders.get(position).getColor(); // get color
        holder.nameView.setTextColor( // set name's color using the color field, as above
            ContextCompat.getColor(
                enclosingContext,
                getTextColor(color)
            )
        );

        holder.typeView.setText(reminders.get(position).getType()); // set type
        holder.switchView.setChecked(reminders.get(position).getChecked()); // set checked
    }

    // mandatory get size
    @Override
    public int getItemCount() {
        return reminders.size();
    }

    // basically a fancy container for the binding
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameView;
        public final TextView descriptionView;
        public final TextView urgencyView;
        public final TextView typeView;
        public final SwitchCompat switchView;

        public ViewHolder(ReminderDisplayBinding binding) {
            // get the binding's stuff
            super(binding.getRoot());
            this.nameView = binding.elementNameField;
            this.descriptionView = binding.elementDescriptionField;
            this.urgencyView = binding.elementUrgencyField;
            this.typeView = binding.elementTypeField;
            this.switchView = binding.elementSwitch;
        }
    }

    // switch for urgency color
    public int getUrgencyColor(int urgency) {
        // switch, conditionals
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
    public int getTextColor(String colorName) {
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