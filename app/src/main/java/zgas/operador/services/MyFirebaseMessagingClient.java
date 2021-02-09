package zgas.operador.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import zgas.operador.Home;
import zgas.operador.NotificationBookingActivity;
import zgas.operador.R;
import zgas.operador.channel.NotificationHelper;
import zgas.operador.providers.AuthProvider;
import zgas.operador.providers.ClientBookingProvider;
import zgas.operador.providers.GeofireProvider;
import zgas.operador.providers.GoogleApiProvider;
import zgas.operador.receivers.AcceptReceiver;
import zgas.operador.receivers.CancelReceiver;

public class MyFirebaseMessagingClient extends FirebaseMessagingService {

    private GoogleApiProvider mGoogleApiProvider;
    private AuthProvider mAuthProvider;
    private ClientBookingProvider mClientBookingProvider;
    private GeofireProvider mGeofireProvider;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification = remoteMessage.getNotification();

        Log.d("DEP", "NOTIFICACION");

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("active_drivers");
        mGoogleApiProvider = new GoogleApiProvider(MyFirebaseMessagingClient.this);
        mClientBookingProvider = new ClientBookingProvider();

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");           //YA
        String body = data.get("body"); //YA

        Random random = new Random();
        int idNotification = random.nextInt(100000);

        if (title != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (title.contains("Solicitud de servicio")) {
                    ///Distancia

                    String idClient = data.get("idClient"); //YA
                    String cantidad30kg = data.get("cantidad30kg");
                    String cantidad20kg = data.get("cantidad20kg");
                    String cantidad10kg = data.get("cantidad10kg");
                    String cantidad = data.get("cantidad");
                    String destination  = data.get("destination"); //YA
                    String Slatitud  = data.get("latitud"); //YA
                    String Slongitud  = data.get("longitud"); //YA

                    LatLng latLngCliente = new LatLng(Double.parseDouble(Slatitud), Double.parseDouble(Slongitud));


                    mGeofireProvider.getDriverLocation(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists())
                            {
                                double latitud = (double) snapshot.child("0").getValue();
                                double longitud = (double) snapshot.child("1").getValue(); ;
                                LatLng latLngDriver = new LatLng(latitud, longitud);

                                mGoogleApiProvider.getDirections(latLngCliente, latLngDriver).enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(response.body());
                                            JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                            JSONObject route = jsonArray.getJSONObject(0);
                                            JSONArray legs =  route.getJSONArray("legs");
                                            JSONObject leg = legs.getJSONObject(0);
                                            JSONObject distance = leg.getJSONObject("distance");
                                            JSONObject duration = leg.getJSONObject("duration");
                                            String distanceText = distance.getString("text");
                                            String durationText = duration.getString("text");

                                            String title = "Solicitud de servicio a "+ durationText +" de tu ubicación.";
                                            String body = "Un cliente esta solicitando un servicio a una distancia de " + distanceText + "\n" +
                                                    "En el domicilio: " + destination + "\n" +
                                                    cantidad;

                                            Log.d("DEP", "id client: " + idClient);
                                            String origin = "";

                                            String searchById = data.get("searchById"); //ya

                                            String idServicio = data.get("idServicio"); //ya
                                            showNotificationApiOreoActions(title, body, idClient, searchById, idNotification, idServicio);
                                            showNotificationActivity(idClient, origin, destination, durationText, distanceText, searchById, cantidad30kg, cantidad20kg, cantidad10kg, idNotification, idServicio);

                                        }
                                        catch (Exception e) {
                                            Log.d("DEP", "error: AQUI SI ENTRA");
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });




                }


                else if (title.contains("VIAJE CANCELADO")) {
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    // eliminando la notificacion de solicitud de viaje
                    manager.cancel(idNotification);
                    //showNotificationApiOreo(title, body);
                }
                else {
                    showNotificationApiOreo(title, body);
                }
            }
            else {
                if (title.contains("Solicitud de servicio")) {
                    String idClient = data.get("idClient");
                    String origin = data.get("origin");
                    String destination  = data.get("destination");
                    String min = data.get("min");
                    String distance = data.get("distance");
                    String searchById = data.get("searchById");
                    String cantidad30kg = data.get("cantidad30kg");
                    String cantidad20kg = data.get("cantidad20kg");
                    String cantidad10kg = data.get("cantidad10kg");
                    String idServicio = data.get("idServicio"); //ya
                    showNotificationActions(title, body, idClient, searchById, idNotification, idServicio);
                    showNotificationActivity(idClient, origin, destination, min, distance, searchById, cantidad30kg, cantidad20kg, cantidad10kg, idNotification, idServicio);
                }
                else if (title.contains("VIAJE CANCELADO")) {
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(idNotification);
                    //showNotification(title, body);
                }
                else {
                    showNotification(title, body);
                }
            }
        }
    }

    private void showNotificationActivity(String idClient, String origin, String destination, String min, String distance, String searchById, String cantidad30kg, String cantidad20kg, String cantidad10kg, int idNotificacion, String idServicio) {
        PowerManager pm = (PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if (!isScreenOn) {
            PowerManager.WakeLock wakeLock = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                             PowerManager.ACQUIRE_CAUSES_WAKEUP |
                             PowerManager.ON_AFTER_RELEASE,
                             "AppName:MyLock"
            );
            wakeLock.acquire(10000);
        }
        Intent intent = new Intent(getBaseContext(), NotificationBookingActivity.class);
        intent.putExtra("idClient", idClient);
        intent.putExtra("origin", origin);
        intent.putExtra("destination", destination);
        intent.putExtra("min", min);
        intent.putExtra("distance", distance);
        intent.putExtra("searchById", searchById);

        intent.putExtra("cantidad30kg", cantidad30kg);
        intent.putExtra("cantidad20kg", cantidad20kg);
        intent.putExtra("cantidad10kg", cantidad10kg);

        intent.putExtra("idNotificacion", idNotificacion);
        intent.putExtra("idServicio", idServicio);




        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showNotification(String title, String body) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldAPI(title, body, intent, sound);
        notificationHelper.getManager().notify(1, builder.build());
    }

    private void showNotificationActions(String title, String body, String idClient, String searchById, int idNotification, String idServicio) {

        // ACEPTAR
        Intent acceptIntent = new Intent(this, AcceptReceiver.class);
        acceptIntent.putExtra("idClient", idClient);
        acceptIntent.putExtra("searchById", searchById);
        acceptIntent.putExtra("idNotificacion", idNotification);
        acceptIntent.putExtra("idServicio", idServicio);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, idNotification, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptPendingIntent
        ).build();

        // CANCELAR

        Intent cancelIntent = new Intent(this, CancelReceiver.class);
        cancelIntent.putExtra("idClient", idClient);
        cancelIntent.putExtra("searchById", searchById);
        acceptIntent.putExtra("idNotificacion", idNotification);
        acceptIntent.putExtra("idServicio", idServicio);

        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, idNotification, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action cancelAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build();

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldAPIActions(title, body, sound, acceptAction, cancelAction);
        notificationHelper.getManager().notify(idNotification, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreo(String title, String body) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotification(title, body, intent, sound);
        notificationHelper.getManager().notify(1, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreoActions(String title, String body, String idClient, String searchById, int idNotification, String idServicio) {

        Intent acceptIntent = new Intent(this, AcceptReceiver.class);
        acceptIntent.putExtra("idClient", idClient);
        acceptIntent.putExtra("searchById", searchById);
        acceptIntent.putExtra("idNotificacion", idNotification);
        acceptIntent.putExtra("idServicio", idServicio);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, idNotification, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action acceptAction= new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptPendingIntent
        ).build();

        Intent cancelIntent = new Intent(this, CancelReceiver.class);
        cancelIntent.putExtra("idClient", idClient);
        cancelIntent.putExtra("searchById", searchById);
        acceptIntent.putExtra("idNotificacion", idNotification);
        acceptIntent.putExtra("idServicio", idServicio);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, idNotification, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action cancelAction= new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build();

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotificationActions(title, body, sound, acceptAction, cancelAction);
        notificationHelper.getManager().notify(idNotification, builder.build());
    }
}
