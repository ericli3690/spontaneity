package com.example.spontaneity;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkRequest;

import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.spontaneity.databinding.ReminderDisplayListBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// reminderdisplayfrag
// contains the adapter, but also the floating button and checkstress button
// also handles random toast messages, creating/reading new reminders file and creating adapter
// activating notifications, showing menu buttons
// basically anything that happens once login/signup is done

public class ReminderDisplayFragment extends Fragment {

    public ReminderDisplayListBinding binding;
    public ReminderDisplayRecyclerViewAdapter adapter;

    // displaying random toasts services
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    final Handler handler = new Handler();
    // for display in random toasts
    private final String[] motivationalMessages = new String[] {
            "Have a good day today!",
            "Smile!",
            "Remember to breathe...",
            "You're the greatest!",
            "Hi! How are you?",
            "You can do it!",
            "Have confidence in yourself!",
            "Be bold!"
    };

    // no predictive animations, stops recyclerview from crashing when a lot of items are simultaneously removed
    private static class NPALinearLayoutManager extends LinearLayoutManager {
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }
        // implement constructors
        public NPALinearLayoutManager(Context context) {
            super(context);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = ReminderDisplayListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        RecyclerView list = binding.list;
        list.setLayoutManager(new NPALinearLayoutManager(root.getContext())); // use NPA

        FileManager fileManager = new FileManager(getContext(), "reminders.txt");

        adapter = new ReminderDisplayRecyclerViewAdapter(fileManager.readRemindersAsList(), getContext(), getActivity());
        list.setAdapter(adapter);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Spontaneity: Reminders");

        MenuHost menuHost = getActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                MenuItem deleteAllButton = menu.findItem(R.id.delete_all_button);
                MenuItem addDefaultReminders = menu.findItem(R.id.add_default_reminders);
                MenuItem settingsButton = menu.findItem(R.id.settings_button);
                if (deleteAllButton != null) {
                    deleteAllButton.setVisible(true);
                }
                if (addDefaultReminders != null) {
                    addDefaultReminders.setVisible(true);
                }
                if (settingsButton != null) {
                    settingsButton.setVisible(true);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        });

        // check stress button
        binding.checkStressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int runningTotalActivatedReminders = 0;
                double runningTotalUrgency = 0;
                for (int reminderNum = 0; reminderNum < adapter.reminders.size(); reminderNum++) {
                    Reminder currentReminder = adapter.reminders.get(reminderNum);
                    if (currentReminder.getChecked()) {
                        runningTotalActivatedReminders++;
                        runningTotalUrgency += Integer.parseInt(String.valueOf(currentReminder.getUrgency()));
                    }
                }

                double averageUrgency;
                if (runningTotalActivatedReminders == 0) {
                    // prevent division by zero
                    averageUrgency = 0;
                } else {
                    averageUrgency = runningTotalUrgency / runningTotalActivatedReminders;
                }
                double roundedAverageUrgency = Globals.roundToPlaces(1, averageUrgency);
                binding.stressOutput.setText(String.valueOf(roundedAverageUrgency));

                if (roundedAverageUrgency < 2) {
                    binding.stressMessage.setText("Super zen!");
                } else if (roundedAverageUrgency < 2.5) {
                    binding.stressMessage.setText("Looking calm!");
                } else if (roundedAverageUrgency < 3) {
                    binding.stressMessage.setText("Be confident!");
                } else if (roundedAverageUrgency < 3.5) {
                    binding.stressMessage.setText("You can do it!");
                } else if (roundedAverageUrgency < 4) {
                    binding.stressMessage.setText("Wow! Good luck!");
                } else { // implied 4+
                    binding.stressMessage.setText("Remember to breathe!");
                }
            }
        });

        // add new reminder
        binding.addNewReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // instantiating an empty reminder
                Reminder reminder = new Reminder(
                        "",
                        "",
                        3,
                        "Other",
                        "None",
                        true
                );
                EditAddMenu addMenu = new EditAddMenu(
                        "ADD REMINDER",
                        getContext(),
                        adapter.enclosingViewGroup,
                        reminder
                );
                // if deleted
                addMenu.binding.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addMenu.dialog.dismiss();
                    }
                });
                // if created
                addMenu.binding.doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addMenu.lockAnswers();
                        adapter.reminders.add(reminder);
                        adapter.reloadList();
                        adapter.reloadFile();
                        addMenu.dialog.dismiss();
                    }
                });

            }
        });
    }
}