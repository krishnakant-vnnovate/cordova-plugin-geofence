package com.cowbell.cordova.geofence;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class RemoveGeofenceCommand extends AbstractGoogleServiceCommand {
    private Context context;
    private List<String> geofencesIds;

    public RemoveGeofenceCommand(Context context, List<String> geofencesIds) {
        super(context);
        this.context = context;
        this.geofencesIds = geofencesIds;
    }

    @Override
    protected void ExecuteCustomCode() {
        if (geofencesIds != null && geofencesIds.size() > 0) {
            logger.log(Log.DEBUG, "Removing geofences...");
            LocationServices.getGeofencingClient(context)
                    .removeGeofences(geofencesIds)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                logger.log(Log.DEBUG, "Geofences successfully removed");
                                CommandExecuted();
                            } else {
                                String message = "Removing geofences failed - " + task.getException().getMessage();
                                logger.log(Log.ERROR, message);
                                CommandExecuted(new Error(message));
                            }
                        }
                    });
        } else {
            logger.log(Log.DEBUG, "Tried to remove Geofences when there were none");
            CommandExecuted();
        }
    }
}
