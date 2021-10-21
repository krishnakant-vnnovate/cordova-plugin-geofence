package com.cowbell.cordova.geofence;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.apache.cordova.LOG;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddGeofenceCommand extends AbstractGoogleServiceCommand {
    private Context context;
    private List<Geofence> geofencesToAdd;
    private PendingIntent pendingIntent;

    public AddGeofenceCommand(Context context, PendingIntent pendingIntent,
                              List<Geofence> geofencesToAdd) {
        super(context);
        this.context = context;
        this.geofencesToAdd = geofencesToAdd;
        this.pendingIntent = pendingIntent;
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> geofencesToAdd) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofencesToAdd);
        return builder.build();
    }

    @Override
    public void ExecuteCustomCode() {
        logger.log(Log.DEBUG, "Adding new geofences... now");
        if (geofencesToAdd != null && geofencesToAdd.size() > 0) try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.getGeofencingClient(context)
                    .addGeofences(getGeofencingRequest(geofencesToAdd), pendingIntent)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                logger.log(Log.DEBUG, "Geofences successfully added");
                                CommandExecuted();
                            } else try {
                                Map<Integer, String> errorCodeMap = new HashMap<Integer, String>();
                                errorCodeMap.put(GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE, GeofencePlugin.ERROR_GEOFENCE_NOT_AVAILABLE);
                                errorCodeMap.put(GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES, GeofencePlugin.ERROR_GEOFENCE_LIMIT_EXCEEDED);


//                                Integer statusCode = status.getStatusCode();
                                String message = "Adding geofences failed - SystemCode: " + task.getException().getMessage();
                                JSONObject error = new JSONObject();
                                error.put("message", message);

//                                if (statusCode == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
//                                    error.put("code", GeofencePlugin.ERROR_GEOFENCE_NOT_AVAILABLE);
//                                } else if (statusCode == GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES) {
//                                    error.put("code", GeofencePlugin.ERROR_GEOFENCE_LIMIT_EXCEEDED);
//                                } else {
//                                    error.put("code", GeofencePlugin.ERROR_UNKNOWN);
//                                }

                                logger.log(Log.ERROR, message);
                                CommandExecuted(error);
                            } catch (JSONException exception) {
                                CommandExecuted(exception);
                            }
                        }
                    });
        } catch (Exception exception) {
            logger.log(LOG.ERROR, "Exception while adding geofences");
            exception.printStackTrace();
            CommandExecuted(exception);
        }
    }
}
