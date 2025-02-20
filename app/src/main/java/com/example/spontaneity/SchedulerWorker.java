package com.example.spontaneity;

import static java.lang.Math.max;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SchedulerWorker extends Worker {

    private final Context enclosingContext;
    private final Random random = new Random();

    public SchedulerWorker(@NonNull Context context,
                           @NonNull WorkerParameters workerParams) {

        super(context, workerParams);
        this.enclosingContext = context;
    }

    private int randomTime() {
        FileManager fileManager = new FileManager(enclosingContext, "user.txt");
        String[] readFile = fileManager.readFile();
        int frequencyInMinutes = max(Integer.parseInt(readFile[3]), 15); // must be at least 15 min
        return random.nextInt((int) (frequencyInMinutes * 0.6));
    }

    @NonNull
    @Override
    public Result doWork() {
        try {

            // make a lottery of reminders to choose from
            // the urgency of a reminder is how many tickets it gets into the lottery, and is thus related to its likelihood
            List<Reminder> reminderLottery = new ArrayList<Reminder>();
            FileManager fileManager = new FileManager(enclosingContext, "reminders.txt");
            List<Reminder> reminders = fileManager.readRemindersAsList();
            for (Reminder reminder : reminders) {
                // only do checked reminders
                if (reminder.getChecked()) {
                    if (reminder.getType().equals("Urgent")) {
                        // if its urgent just send it outright
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(enclosingContext, "reminderNotifications")
                                .setSmallIcon(R.drawable.ic_baseline_star_24)
                                .setContentTitle(reminder.getName())
                                .setContentText(reminder.getDescription())
                                .setVibrate(new long[] {0, 500, 0, 500})
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(enclosingContext);
                        notificationManager.notify(fileManager.getNextId(), builder.build());
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
            Globals.nextTitle = lotteryWinner.getName();
            Globals.nextDesc = lotteryWinner.getType() + ": " + lotteryWinner.getDescription();
            Globals.nextColor = lotteryWinner.getColor();

            int randomTime = randomTime();

            OneTimeWorkRequest notifyRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInitialDelay(randomTime, TimeUnit.MINUTES)
                    .build();

            WorkManager
                    .getInstance(enclosingContext)
                    .enqueueUniqueWork(
                            "notifyRequest",
                            ExistingWorkPolicy.KEEP,
                            notifyRequest
                    );

            Log.d("SchedulerWorker", "Did work! The notification will arrive in " + randomTime + " minutes.");

            return Result.success();

        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

}
