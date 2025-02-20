package com.example.spontaneity;

import android.content.Context;
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(enclosingContext, "reminderNotifications")
                .setSmallIcon(R.drawable.ic_baseline_star_24)
                .setColor(ContextCompat.getColor(enclosingContext, Globals.getTextColor(Globals.nextColor)))
                .setContentTitle(Globals.nextTitle)
                .setContentText(Globals.nextDesc)
                .setVibrate(new long[] {0, 500})
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(enclosingContext);
        FileManager fileManager = new FileManager(enclosingContext, "id.txt");
        int nextId = fileManager.getNextId();
        notificationManager.notify(nextId, builder.build());
        Log.d("NotificationWorker", "Sent notification! Used ID: " + nextId);
        return Result.success();
    }
}
