package zgas.operador.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import zgas.operador.NotificationBookingActivity;
import zgas.operador.providers.AuthProvider;
import zgas.operador.providers.ClientBookingProvider;
import zgas.operador.providers.DriversFoundProvider;


public class CancelReceiver extends BroadcastReceiver {
    private ClientBookingProvider mClientBookingProvider;
    private DriversFoundProvider mDriversFoundProvider;
    private AuthProvider mAuthProvider;

    int idNotificacion;

    @Override
    public void onReceive(Context context, Intent intent) {
        String idClient = intent.getExtras().getString("idClient");
        String searchById = intent.getExtras().getString("searchById");
        mClientBookingProvider = new ClientBookingProvider();
        mDriversFoundProvider = new DriversFoundProvider(idClient);
        mAuthProvider = new AuthProvider();

        idNotificacion = intent.getExtras().getInt("idNotificacion", 100);

        if (searchById.equals("true")) {
            mClientBookingProvider.updateStatus(idClient, "cancel");
        }
        mDriversFoundProvider.delete(mAuthProvider.getId());

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(idNotificacion);

        try {
            NotificationBookingActivity.activity.finish();
        }catch (Exception ignored){}
    }
}
