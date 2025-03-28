package com.example.spontaneity;

import static androidx.core.content.ContextCompat.getSystemService;

import static java.lang.Math.max;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.spontaneity.databinding.ActivityMainBinding;
import com.example.spontaneity.databinding.SignupBinding;

import java.util.concurrent.TimeUnit;

// PRIMARY ENTRY POINT
// only one activity in this project

// its binding contains content_main, which is a wrapper for the navhost
// navhostfrag is a navigation system for moving between screens
// it contains onboarding and reminderdisplayfrag

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // get binding
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // request notification permissions
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 33) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // create notification channels
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        NotificationChannel channel = new NotificationChannel("reminderNotifications", "SpontaneityChannel", NotificationManager.IMPORTANCE_HIGH);
        channel.setVibrationPattern(new long[] {0, 500});
        channel.enableVibration(true);
        notificationManager.createNotificationChannel(channel);

        NotificationChannel channelUrgent = new NotificationChannel("reminderNotificationsUrgent", "SpontaneityChannelUrgent", NotificationManager.IMPORTANCE_HIGH);
        channelUrgent.setVibrationPattern(new long[] {0, 500, 0, 500});
        channelUrgent.enableVibration(true);
        notificationManager.createNotificationChannel(channelUrgent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // when upper menu is being created
        // inflate the menu.xml
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // a menu item has been selected
        // detect what id
        // must use if instead of switch-case because ids can mutate and shouldnt be put in switches
        if (item.getItemId() == R.id.delete_all_button) {
            // delete all reminders
            // get the recyclerview
            ReminderDisplayFragment reminderDisplay = (ReminderDisplayFragment)
                     getSupportFragmentManager()
                        .getPrimaryNavigationFragment()
                        .getChildFragmentManager()
                        .getFragments()
                        .get(0);
            reminderDisplay.adapter.deleteAll(); // activate the delete all method in adapter
            return true; // handled
        } else if (item.getItemId() == R.id.add_default_reminders) {
            // append the defaults
            // get the recyclerview
            ReminderDisplayFragment reminderDisplay = (ReminderDisplayFragment)
                    getSupportFragmentManager()
                            .getPrimaryNavigationFragment()
                            .getChildFragmentManager()
                            .getFragments()
                            .get(0);
            reminderDisplay.adapter.addDefaults(); // activate the append defaults method in adapter
            return true; // handled
        } else if (item.getItemId() == R.id.settings_button) {
            // wants to edit settings
            // get the recyclerview so that can show things in its context
            ReminderDisplayFragment reminderDisplay = (ReminderDisplayFragment)
                    getSupportFragmentManager()
                            .getPrimaryNavigationFragment()
                            .getChildFragmentManager()
                            .getFragments()
                            .get(0);
            // make dialog box with default style
            Dialog dialog = new Dialog(MainActivity.this, R.style.DialogBoxStyle);
            // blur the rest of the screen
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.argb(100,0,0,0)));
            // use signup.xml as the layout, inflate at context and place in the same viewgroup as the adapter items; do not attach to parent
            SignupBinding dialogBinding = SignupBinding.inflate(
                    LayoutInflater.from(reminderDisplay.getContext()),
                    reminderDisplay.adapter.enclosingViewGroup,
                    false
            );
            // read user.txt
            FileManager fileManager = new FileManager(getApplicationContext(), "user.txt");
            String[] readFile = fileManager.readFile();
            // copy its contents to the binding fields
            dialogBinding.nameFieldSignup.setText(readFile[0]);
            dialogBinding.passwordFieldSignup.setText(readFile[1]);
            dialogBinding.realNameFieldSignup.setText(readFile[2]);
            dialogBinding.frequencyFieldSignup.setText(readFile[3]);
            // set some labels
            dialogBinding.signupScreenTitle.setText("Change Settings");
            dialogBinding.signupButton.setText("Save");
            // on submit
            dialogBinding.signupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // delete the file and recreate it using the binding's contents
                    fileManager.deleteFile();
                    fileManager.createFile(new String[] {
                            dialogBinding.nameFieldSignup.getText().toString(),
                            dialogBinding.passwordFieldSignup.getText().toString(),
                            dialogBinding.realNameFieldSignup.getText().toString(),
                            dialogBinding.frequencyFieldSignup.getText().toString()
                    });
                    dialog.dismiss();
                }
            });
            // set the binding as the dialog's contents, make it cancelable
            dialog.setContentView(dialogBinding.getRoot());
            dialog.setCancelable(true);
            dialog.show();
            return true; // handled
        } else {
            // not handled by this function, pass it up the callstack
            return super.onOptionsItemSelected(item);
        }
    }
}