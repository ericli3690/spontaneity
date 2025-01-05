package com.example.spontaneity;

import static android.provider.Settings.Global.getString;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// creates notification channel, builds and sends notifications
// schedules and handles what notification to send logic

public class NotificationScheduler {

    // get enclosing
    private final Context enclosingContext;
    private final Activity enclosingActivity;

    // for scheduling
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final Random random = new Random(); // and at random times

    private final String CHANNEL_ID = "10001"; // arbitrary channel id

    // const
    public NotificationScheduler(Context enclosingContext, Activity enclosingActivity) {
        this.enclosingContext = enclosingContext;
        this.enclosingActivity = enclosingActivity;
    }

    // so that each notif has a unique id
    private int getNextId() {
        // use an internal file holding an int
        // add one to the int and rewrite it to the file, also return it
        FileManager fileManager = new FileManager(enclosingContext, enclosingActivity, "id.txt");
        int id;
        if (fileManager.wasCreated()) {
            // the file already exists
            // read its single value, then delete it
            // add one to the value
            String[] readFile = fileManager.readFile();
            id = Integer.parseInt(readFile[readFile.length - 1]);
            id++;
            fileManager.deleteFile();
        } else {
            // the file has not been created yet
            // the first value is 1
            id = 1;
        }
        // make a new file with the id as the single value
        fileManager.createFile(new String[] {String.valueOf(id)});
        return id; // return the id
    }

    // actually trigger a notif
    public void sendNotif(String title, String description) {
        // make a new notification channel to send to
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
        );
        // make a manager for the channel
        NotificationManager notificationManager = getSystemService(enclosingContext, NotificationManager.class);
        // lock in the channel
        // does nothing if the channel with id CHANNEL_ID has already been created
        notificationManager.createNotificationChannel(notificationChannel);

        // testing
//        notificationManager.deleteNotificationChannel("10002");
//        notificationManager.deleteNotificationChannel("10003");

        // aborted attempt at onclick
//        final Intent intent = new Intent(enclosingContext, ReminderDisplayFragment.class);
//        final PendingIntent pendingIntent = PendingIntent.getActivity(enclosingContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//        intent.putExtra()

        // build notification
        // sound and vibration don't work...
        NotificationCompat.Builder builder = new NotificationCompat.Builder(enclosingContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_star_24)
                .setContentTitle(title)
                .setContentText(description)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(new long[] {1000, 1000, 1000, 1000, 1000})
                .setPriority(NotificationCompat.PRIORITY_MAX);
        // show notification with new id
        int nextId = getNextId();
        Log.d("notif id", String.valueOf(nextId)); // debug
        notificationManager.notify(nextId, builder.build()); // send
    }

    // schedule a notification to fire
    public void scheduleNotif(int timeout, int randomness, List<Reminder> reminders) {
        int delay = random.nextInt(randomness) + timeout; // in the range of time randomness, plus minimum timeout
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                chooseNotif(timeout, randomness, reminders);
            }
        }, delay, TimeUnit.SECONDS);
    }

    // logic to choose what notification to send
    // takes in timeout and randomness simply to retrigger scheduleNotif, creating a cycle between these two methods
    // main input is reminders
    private void chooseNotif(int timeout, int randomness, List<Reminder> reminders) {
        scheduleNotif(timeout, randomness, reminders);
        if (reminders.size() == 0) {
            // there are no reminders! remind to add some new ones
            sendNotif("No reminders!", "Add some reminders!");
            return;
        }
        // else
        // make a lottery of reminders to choose from
        // the urgency of a reminder is how many tickets it gets into the lottery, and is thus related to its likelihood
        List<Reminder> reminderLottery = new ArrayList<Reminder>();
        for (Reminder reminder : reminders) {
            // only do checked reminders
            if (reminder.getChecked()) {
                if (reminder.getType().equals("Urgent")) {
                    // if its urgent just send it outright
                    sendNotif(reminder.getName(),
                            reminder.getType() + ": " + reminder.getDescription());
                } else {
                    // otherwise add reminder [urgency] times to the lottery
                    for (int lotteryTicketNum = 0; lotteryTicketNum < reminder.getUrgency(); lotteryTicketNum++) {
                        reminderLottery.add(reminder);
                    }
                }
            }
        }
        // pick a winner index
        int lotteryWinnerIndex = random.nextInt(reminderLottery.size());
        // get the winner
        Reminder lotteryWinner = reminderLottery.get(lotteryWinnerIndex);
        // send the winner
        sendNotif(lotteryWinner.getName(),
                lotteryWinner.getType() + ": " + lotteryWinner.getDescription());
    }

}
