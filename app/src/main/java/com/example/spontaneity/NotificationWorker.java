package com.example.spontaneity;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// TODO maybe not passing through constructor? move eveything into dowork?

public class NotificationWorker extends Worker {

    private final Context enclosingContext;
    private final Activity enclosingActivity;
    private final List<Reminder> reminders;
    private final NotificationManager notificationManager;

    private final Random random = new Random();

    private final String CHANNEL_ID = "10001";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams, Activity activity, List<Reminder> reminders) {
        super(context, workerParams);
        this.enclosingContext = context;
        this.enclosingActivity = activity;
        this.reminders = reminders;

        // make a new notification channel to send to
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
        );
        // make a manager for the channel
        this.notificationManager = getSystemService(enclosingContext, NotificationManager.class);
        // lock in the channel
        // does nothing if the channel with id CHANNEL_ID has already been created
        this.notificationManager.createNotificationChannel(notificationChannel);
    }

//    private int randomTime() {
//        FileManager fileManager = new FileManager(enclosingContext, enclosingActivity, "user.txt");
//        String[] readFile = fileManager.readFile();
//        int frequencyInSeconds = Integer.parseInt(readFile[3]);
//        double RANDOMNESS_PERCENTAGE = 0.3;
//        int randomPadding = random.nextInt((int) (RANDOMNESS_PERCENTAGE * frequencyInSeconds));
//        return frequencyInSeconds + randomPadding;
//    }

    private int getNextId() {
        // use an internal file holding an int
        FileManager fileManager = new FileManager(enclosingContext, enclosingActivity, "id.txt");
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

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d("doing work", "work");
            List<String> queuedNames = new ArrayList<String>();
            List<String> queuedDescriptions = new ArrayList<String>();

//            if (reminders.size() == 0) {
//                // there are no reminders! remind to add some new ones
//                sendNotif("No reminders!", "Add some reminders!");
//                return;
//            }

            // make a lottery of reminders to choose from
            // the urgency of a reminder is how many tickets it gets into the lottery, and is thus related to its likelihood
            List<Reminder> reminderLottery = new ArrayList<Reminder>();
            for (Reminder reminder : reminders) {
                // only do checked reminders
                if (reminder.getChecked()) {
                    if (reminder.getType().equals("Urgent")) {
                        // if its urgent just send it outright
                        queuedNames.add(reminder.getName());
                        queuedDescriptions.add(reminder.getType() + ": " + reminder.getDescription());
                    } else {
                        // otherwise add reminder [urgency] times to the lottery
                        for (int lotteryTicketNum = 0; lotteryTicketNum < reminder.getUrgency(); lotteryTicketNum++) {
                            reminderLottery.add(reminder);
                        }
                    }
                }
            }

            int lotteryWinnerIndex = random.nextInt(reminderLottery.size());
            Reminder lotteryWinner = reminderLottery.get(lotteryWinnerIndex);
            queuedNames.add(lotteryWinner.getName());
            queuedDescriptions.add(lotteryWinner.getType() + ": " + lotteryWinner.getDescription());

            for (int sendingReminder = 0; sendingReminder < queuedNames.size(); sendingReminder++) {
                Log.d("sending", "a");
                NotificationCompat.Builder builder = new NotificationCompat.Builder(enclosingContext, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_star_24)
                        .setContentTitle(queuedNames.get(sendingReminder))
                        .setContentText(queuedDescriptions.get(sendingReminder))
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setVibrate(new long[] {1000, 1000, 1000, 1000, 1000})
                        .setPriority(NotificationCompat.PRIORITY_MAX);
                // show notification with new id
                int nextId = getNextId();
                Log.d("notif id", String.valueOf(nextId)); // debug
                notificationManager.notify(nextId, builder.build()); // send
            }

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

}
