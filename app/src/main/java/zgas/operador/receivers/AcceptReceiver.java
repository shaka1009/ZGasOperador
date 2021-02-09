package zgas.operador.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import zgas.operador.Home;
import zgas.operador.NotificationBookingActivity;
import zgas.operador.models.Driver;
import zgas.operador.providers.AuthProvider;
import zgas.operador.providers.ClientBookingProvider;
import zgas.operador.providers.GeofireProvider;
import zgas.operador.providers.InServiceProvider;


public class AcceptReceiver extends BroadcastReceiver {

    private ClientBookingProvider mClientBookingProvider;
    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;
    private InServiceProvider mInServiceProvider;


    String idServicio;
    int idNotificacion;

    @Override
    public void onReceive(Context context, Intent intent) {
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("active_drivers");
        //mGeofireProvider.removeLocation(mAuthProvider.getId());

        String idClient = intent.getExtras().getString("idClient");
        String searchById = intent.getExtras().getString("searchById");
        idServicio = intent.getExtras().getString("idServicio");
        mClientBookingProvider = new ClientBookingProvider();
        idNotificacion = intent.getExtras().getInt("idNotificacion", 100);
        mInServiceProvider = new InServiceProvider(mAuthProvider.getId());

        if (searchById.equals("true")) {
            mClientBookingProvider.updateStatus(idClient, "accept");
            mClientBookingProvider.updateData(idClient, Home.mDriver);

            Log.d("Dep", idServicio);

            Map<String, String> map = new HashMap<>();
            map.put("idServicio", idServicio); //YA
            map.put("idCliente", idClient); //YA
            mInServiceProvider.createService(idClient).setValue(map);
            try {
                NotificationBookingActivity.activity.finish();
            }catch (Exception ignored){}

/* HACER UN ADAPTADOR PARA TENER MUCHOS SERVICIOS
            Intent intent1 = new Intent(context, MapDriverBookingActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent1.setAction(Intent.ACTION_RUN);
            intent1.putExtra("idClient", idClient);
            context.startActivity(intent1);
*/

        }
        else {
            checkIfClientBookignWasAccept(idClient, context);
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(idNotificacion);

    }

    private void checkIfClientBookignWasAccept(final String idClient, final Context context) {
        mClientBookingProvider.getClientBooking(idClient).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("idDriver") && snapshot.hasChild("status")) {
                        String status = snapshot.child("status").getValue().toString();
                        String idDriver = snapshot.child("idDriver").getValue().toString();




                        if (status.equals("create") && idDriver.equals("")) {
                            mClientBookingProvider.updateStatusAndIdDriver(idClient, "accept", mAuthProvider.getId());

                            Log.d("Dep", idServicio);

                            Map<String, String> map = new HashMap<>();
                            map.put("idServicio", idServicio); //YA
                            map.put("idCliente", idClient); //YA
                            mInServiceProvider.createService(idClient).setValue(map);
                            try {
                                NotificationBookingActivity.activity.finish();
                            }catch (Exception ignored){}

                            /* HACER UN ADAPTADOR PARA TENER MUCHOS SERVICIOS
                            Intent intent1 = new Intent(context, MapDriverBookingActivity.class);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent1.setAction(Intent.ACTION_RUN);
                            intent1.putExtra("idClient", idClient);
                            context.startActivity(intent1);
                            */




                        }
                        else {
                            goToMapDriverActivity(context);
                        }
                    }
                    else {
                        goToMapDriverActivity(context);
                    }
                }
                else {
                    goToMapDriverActivity(context);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void goToMapDriverActivity(Context context) {
        Toast.makeText(context, "Otro conductor ya acepto el viaje", Toast.LENGTH_SHORT).show();

        try {
            NotificationBookingActivity.activity.finish();
        }catch (Exception ignored){}
        /*
        Intent intent1 = new Intent(context, Home.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        context.startActivity(intent1);*/
    }

}
