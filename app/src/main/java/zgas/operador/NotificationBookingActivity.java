package zgas.operador;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import zgas.operador.models.Driver;
import zgas.operador.providers.AuthProvider;
import zgas.operador.providers.ClientBookingProvider;
import zgas.operador.providers.DriversFoundProvider;
import zgas.operador.providers.GeofireProvider;
import zgas.operador.providers.InServiceProvider;

public class NotificationBookingActivity extends AppCompatActivity {

    public static Activity activity;

    private TextView mTextViewDestination;
    private TextView mTextViewMin;
    private TextView mTextViewDistance;
    private TextView textViewCantidad;
    private TextView mTextViewCounter;
    private Button mbuttonAccept;
    private Button mbuttonCancel;

    private ClientBookingProvider mClientBookingProvider;
    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;
    private InServiceProvider mInServiceProvider;

    private String mExtraIdClient;
    private String mExtraOrigin;
    private String mExtraDestination;
    private String mExtraMin;
    private String mExtraDistance;
    private String mExtraSearchById;
    private String mExtraidServicio;

    private MediaPlayer mMediaPlayer;

    private int mCounter = 120;
    private Handler mHandler;

    private DriversFoundProvider mDriversFoundProvider;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mCounter = mCounter -1;
            mTextViewCounter.setText(String.valueOf(mCounter));
            if (mCounter > 0) {
                initTimer();
            }
            else {
                cancelBooking();
            }
        }
    };
    private ValueEventListener mListener;

    private void initTimer() {
        mHandler = new Handler();
        mHandler.postDelayed(runnable, 1000);
    }

    int mExtraCantidad30kg, mExtraCantidad20kg, mExtraCantidad10kg;
    int mExtraIdNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_booking);
        activity = this;

        mTextViewDestination = findViewById(R.id.textViewDestination);
        mTextViewMin = findViewById(R.id.textViewMin);
        mTextViewDistance = findViewById(R.id.textViewDistance);
        mTextViewCounter = findViewById(R.id.textViewCounter);
        mbuttonAccept = findViewById(R.id.btnAcceptBooking);
        mbuttonCancel = findViewById(R.id.btnCancelBooking);
        textViewCantidad = findViewById(R.id.textViewCantidad);

        mAuthProvider = new AuthProvider();

        mInServiceProvider = new InServiceProvider(mAuthProvider.getId());

        mExtraIdClient = getIntent().getStringExtra("idClient");
        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");
        mExtraMin = getIntent().getStringExtra("min");
        mExtraDistance = getIntent().getStringExtra("distance");
        mExtraSearchById = getIntent().getStringExtra("searchById");
        mExtraidServicio = getIntent().getStringExtra("idServicio");

        mDriversFoundProvider = new DriversFoundProvider(mExtraIdClient);


        mExtraCantidad30kg = Integer.parseInt(getIntent().getStringExtra("cantidad30kg"));
        mExtraCantidad20kg = Integer.parseInt(getIntent().getStringExtra("cantidad20kg"));
        mExtraCantidad10kg = Integer.parseInt(getIntent().getStringExtra("cantidad10kg"));

        mExtraIdNotification = getIntent().getIntExtra("idNotificacion", 0);

        mTextViewDestination.setText(mExtraDestination);
        mTextViewMin.setText(mExtraMin);
        mTextViewDistance.setText(mExtraDistance);


        String cantidad="";
        if(mExtraCantidad30kg>=1)
            cantidad = "Cilindro(s) de 30 Kg: " + mExtraCantidad30kg + "\n";
        if(mExtraCantidad20kg>=1)
            cantidad = cantidad + "Cilindro(s) de 20 Kg: " + mExtraCantidad20kg + "\n";;
        if(mExtraCantidad10kg>=1)
            cantidad = cantidad + "Cilindro(s) de 10 Kg: " + mExtraCantidad10kg + "\n";;

        textViewCantidad.setText(cantidad);

        mMediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mMediaPlayer.setLooping(true);

        mClientBookingProvider = new ClientBookingProvider();

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        initTimer();

        checkIfClientCancelBooking();

        mbuttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptBooking();
            }
        });

        mbuttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelBooking();
            }
        });


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
                            mClientBookingProvider.updateData(idClient, Home.mDriver);
                            Intent intent1 = new Intent(context, Home.class); //CAMBIAR
                            //intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            //intent1.setAction(Intent.ACTION_RUN);
                            //intent1.putExtra("idClient", idClient);

                            Map<String, String> map = new HashMap<>();
                            map.put("idServicio", mExtraidServicio); //YA
                            map.put("idCliente", idClient); //YA
                            mInServiceProvider.createService(idClient).setValue(map);


                            context.startActivity(intent1);
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
        /*
        Intent intent1 = new Intent(context, Home.class); //CAMBIAR MapDriverActivity.class
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        context.startActivity(intent1);*/
        finish();
    }

    private void checkIfClientCancelBooking() {
        mListener = mClientBookingProvider.getClientBooking(mExtraIdClient).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    goToMapDriverActivity();
                }
                // SIGNIFICA QUE EL CLIENT BOOKING SI EXISTE
                else if (dataSnapshot.hasChild("idDriver") && dataSnapshot.hasChild("status")){
                    String idDriver = dataSnapshot.child("idDriver").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();

                    if ((status.equals("accept") || status.equals("cancel")) && !idDriver.equals(mAuthProvider.getId())) {
                        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.cancel(mExtraIdNotification);
                        goToMapDriverActivity();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void goToMapDriverActivity() {
        Toast.makeText(NotificationBookingActivity.this, "El cliente ya no esta disponible", Toast.LENGTH_LONG).show();
        if (mHandler != null) mHandler.removeCallbacks(runnable);
        //Intent intent = new Intent(NotificationBookingActivity.this, Home.class); //Cambiar MapDriverActivity
        //startActivity(intent);
        finish();
    }

    private void cancelBooking() {
        if (mHandler != null) mHandler.removeCallbacks(runnable);

        if (mExtraSearchById.equals("true")) {
            mClientBookingProvider.updateStatus(mExtraIdClient, "cancel");
        }
        Log.d("CLIENTE", "ID: " + mExtraIdClient);

        mDriversFoundProvider.delete(mAuthProvider.getId());

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(mExtraIdNotification);
        //Intent intent = new Intent(NotificationBookingActivity.this, Home.class); //Cambiar MapDriverActivity
        //startActivity(intent);
        finish();
    }

    private void acceptBooking() {
        if (mHandler != null) mHandler.removeCallbacks(runnable);
        mGeofireProvider = new GeofireProvider("active_drivers");
        //mGeofireProvider.removeLocation(mAuthProvider.getId());
        mClientBookingProvider = new ClientBookingProvider();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(mExtraIdNotification);

        if (mExtraSearchById.equals("true")) {
            mClientBookingProvider.updateStatus(mExtraIdClient, "accept");
            mClientBookingProvider.updateData(mExtraIdClient, Home.mDriver);
            //Intent intent1 = new Intent(NotificationBookingActivity.this, Home.class); // Home MapDriverBookingActivity
            //intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //intent1.setAction(Intent.ACTION_RUN);
            //intent1.putExtra("idClient", mExtraIdClient);
            //startActivity(intent1);
            finish();
        }
        else {
            checkIfClientBookignWasAccept(mExtraIdClient, NotificationBookingActivity.this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.release();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMediaPlayer != null) {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) mHandler.removeCallbacks(runnable);

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
        if (mListener != null) {
            mClientBookingProvider.getClientBooking(mExtraIdClient).removeEventListener(mListener);
        }
    }
}