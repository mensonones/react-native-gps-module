package modules;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.PermissionListener;

import java.util.function.Consumer;

public class MyGPSModule extends ReactContextBaseJavaModule implements PermissionListener, LocationListener {

    private final ReactApplicationContext reactContext;

    public static final int REQUEST_CODE_PERMISSIONS = 101;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    private static final String ACTIVITY_DOES_NOT_EXIST = "ACTIVITY_DOES_NOT_EXIST";
    private static final String PERMISSION_DENIED = "PERMISSION_DENIED";

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public MyGPSModule(ReactApplicationContext context) {
        super(context);
        this.reactContext = context;
    }

    @NonNull
    @Override
    public String getName() {
        return "MyGPSModule";
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(MyGPSModule.this);
        }
    }

    /**
     * Function to get latitude
     * */

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    public void showSettingsAlert() {
        final Activity activity = getCurrentActivity();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);

        // Setting Dialog Title
        alertDialog.setTitle("GPS desativado");

        // Setting Dialog Message
        alertDialog.setMessage("GPS não está ativado. Você deseja ir nas configurações e ativar?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Configurações", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    @SuppressLint("NewApi")
    @ReactMethod
    public void getCoordinatesByGPS(final Promise promise) {
        Log.d("getCoordinatesByGPS", "CALLED getCoordinatesByGPS");
        Context context = getReactApplicationContext();
        final Activity activity = getCurrentActivity();

        if (context == null) {
            promise.reject(ACTIVITY_DOES_NOT_EXIST, "Context doesn't exist");
            return;
        }

        int hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

        locationManager = (LocationManager) reactContext.getSystemService(Context.LOCATION_SERVICE);

        // getting GPS status
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            Log.d("getCoordinatesByGPS", "hasPermission == PackageManager.PERMISSION_GRANTED");
            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d("getCoordinatesByGPS", "!isGPSEnabled && !isNetworkEnabled");
                this.showSettingsAlert();
            }
            if (isGPSEnabled && isNetworkEnabled) {
                Log.d("getCoordinatesByGPS", "isGPSEnabled: " + isGPSEnabled);

                if (isNetworkEnabled) {
                    //check the network permission
                    if (ActivityCompat.checkSelfPermission(reactContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(reactContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) getCurrentActivity(), new String[] {
                                android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                        }, REQUEST_CODE_PERMISSIONS);
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            double lat = (double)(latitude);
                            double lng = (double)(longitude);

                            WritableMap map = Arguments.createMap();
                            map.putDouble("latitude", lat);
                            map.putDouble("longitude", lng);

                            promise.resolve(map);
                        }
                    }
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        //check the network permission
                        if (ActivityCompat.checkSelfPermission(reactContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(reactContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) getCurrentActivity(), new String[] {
                                    android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                            }, REQUEST_CODE_PERMISSIONS);
                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        Log.d("GPS Enabled", "GPS Enabled");
            /* if (locationManager != null) {
                location = locationManager
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER); */

                        locationManager.getCurrentLocation(
                                LocationManager.GPS_PROVIDER,
                                null,
                                reactContext.getMainExecutor(),
                                new Consumer < Location > () {
                                    @Override
                                    public void accept(Location location) {
                                        Log.d("GPS ENABLED", "isGpsEnabled");
                                        // code
                                        location = location;

                                        if (location != null) {
                                            latitude = location.getLatitude();
                                            longitude = location.getLongitude();

                                            double lat = (double)(latitude);
                                            double lng = (double)(longitude);

                                            WritableMap map = Arguments.createMap();
                                            map.putDouble("latitude", lat);
                                            map.putDouble("longitude", lng);

                                            promise.resolve(map);
                                        }
                                    }
                                });
                    }
                }
            }
        } else {
            Log.d("getCoordinatesByGPS", "ELSE hasPermission == PackageManager.PERMISSION_GRANTED");
            promise.reject(PERMISSION_DENIED, "Permission was not granted");
            ActivityCompat.requestPermissions(activity,
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions,
                                              int[] grantResults) {
        Log.d("MyModuleGPS", "onRequestPermissionsResult");
        onRequestPermissionsResult(requestCode, permissions, grantResults);
        final Activity activity = getCurrentActivity();
        if (grantResults[0] == PermissionChecker.PERMISSION_DENIED && grantResults[1] == PermissionChecker.PERMISSION_DENIED) {
            // user denies
            Log.d("MyModuleGPS", "onRequestPermissionsResult user denies PERMISSION_DENIED");
        }
        if (grantResults[0] == PermissionChecker.PERMISSION_DENIED && grantResults[1] == PermissionChecker.PERMISSION_GRANTED) {
            // user allow while using
            Log.d("MyModuleGPS", "onRequestPermissionsResult user allow while using GRANTED");
        }
        if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED && grantResults[1] == PermissionChecker.PERMISSION_GRANTED) {
            // user allow all the time
            Log.d("MyModuleGPS", "onRequestPermissionsResult user allow all the time GRANTED");
        }
        return true;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double lat = (double)(location.getLatitude());
        double lng = (double)(location.getLongitude());
        Log.d("ONLOCATION LAT", String.valueOf(lat));
        Log.d("ONLOCATION LNG", String.valueOf(lng));
    }
}