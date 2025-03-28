package com.example.spontaneity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {

    private final Context enclosingContext;

    public NotificationWorker(@NonNull Context context,
                              @NonNull WorkerParameters workerParams) {

        super(context, workerParams);
        this.enclosingContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        String nextTitle = "Click Me!";
        String nextDesc = "It's time to check in on your habits.";
        String nextColor = "None";

        FileManager queueFileManager = new FileManager(enclosingContext, "queue.txt");
        String[] readFile = queueFileManager.readFile();
        if (queueFileManager.wasCreated() && readFile.length == 3) {
            nextTitle = readFile[0];
            nextDesc = readFile[1];
            nextColor = readFile[2];
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(enclosingContext, "reminderNotifications")
                .setSmallIcon(R.drawable.ic_baseline_star_24)
                .setColor(ContextCompat.getColor(enclosingContext, Globals.getTextColor(nextColor)))
                .setContentTitle(nextTitle)
                .setContentText(nextDesc)
                .setVibrate(new long[] {0, 500})
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Intent intent = new Intent(enclosingContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pending = PendingIntent.getActivity(enclosingContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pending);
        builder.setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(enclosingContext);

        FileManager idFileManager = new FileManager(enclosingContext, "id.txt");
        int nextId = idFileManager.getNextId();
        notificationManager.notify(nextId, builder.build());
        Log.d("NotificationWorker", "Sent notification! Used ID: " + nextId);
        return Result.success();
    }
}
