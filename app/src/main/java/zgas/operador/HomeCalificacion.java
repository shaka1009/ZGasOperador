package zgas.operador;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import zgas.operador.models.ClientBooking;
import zgas.operador.models.HistoryBooking;
import zgas.operador.providers.AuthProvider;
import zgas.operador.providers.ClientBookingProvider;
import zgas.operador.providers.HistoryBookingProvider;
import zgas.operador.providers.InServiceProvider;

public class HomeCalificacion extends AppCompatActivity {

    String idCliente;

    private TextView mTextViewPrice;
    private RatingBar mRatinBar;
    private Button mButtonCalification;

    private AuthProvider mAuthProvider;
    private ClientBookingProvider mClientBookingProvider;
    private HistoryBooking mHistoryBooking;
    private HistoryBookingProvider mHistoryBookingProvider;
    private InServiceProvider mInverviceProvider;

    private float mCalification = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_calificacion);

        mAuthProvider = new AuthProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mHistoryBookingProvider = new HistoryBookingProvider();
        mInverviceProvider = new InServiceProvider(mAuthProvider.getId());


        mRatinBar = findViewById(R.id.ratingbarCalification);
        mTextViewPrice = findViewById(R.id.tvTotal);
        mButtonCalification = findViewById(R.id.btnCalification);

        idCliente = getIntent().getStringExtra("idCliente");


        mRatinBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float calification, boolean b) {
                mCalification = calification;
            }
        });

        mButtonCalification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calificate();
            }
        });

        getClientBooking();

    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(idCliente).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ClientBooking clientBooking = dataSnapshot.getValue(ClientBooking.class);
                    mTextViewPrice.setText(clientBooking.getTotal());
                    mHistoryBooking = new HistoryBooking(clientBooking);
                    mHistoryBooking.setIdHistoryBooking(mHistoryBookingProvider.getKey());
                    mHistoryBooking.setStatus("terminate");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void calificate() {

        if (mCalification  > 0) {
            mHistoryBooking.setCalificationClient(mCalification);
            mHistoryBooking.setTimestamp(new Date().getTime());
            mHistoryBookingProvider.getHistoryBooking(mHistoryBooking.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        mHistoryBookingProvider.updateCalificactionClient(mHistoryBooking.getIdHistoryBooking(), mCalification).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(HomeCalificacion.this, "La calificacion se guardo correctamente", Toast.LENGTH_LONG).show();
                                mClientBookingProvider.updateStatus(idCliente, "finish", mHistoryBooking.getIdHistoryBooking());
                                mInverviceProvider.delete(idCliente);
                                finish();
                            }
                        });
                    }
                    else {
                        mHistoryBookingProvider.create(mHistoryBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(HomeCalificacion.this, "La calificacion se guardo correctamente", Toast.LENGTH_LONG).show();
                                mClientBookingProvider.updateStatus(idCliente, "finish", mHistoryBooking.getIdHistoryBooking());
                                mInverviceProvider.delete(idCliente);
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
        else {
            Toast.makeText(this, "Debes ingresar la calificacion", Toast.LENGTH_SHORT).show();
        }
    }
}