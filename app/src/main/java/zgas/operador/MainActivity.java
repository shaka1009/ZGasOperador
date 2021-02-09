package zgas.operador;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Objects;
import java.util.StringTokenizer;

import zgas.operador.models.Driver;
import zgas.operador.models.Price;
import zgas.operador.providers.AuthProvider;
import zgas.operador.providers.GeofireProvider;
import zgas.operador.providers.PriceProvider;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static Activity activity;

    private AuthProvider mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        mAuthProvider = new AuthProvider();

        Home.mPrice = new Price();
        Home.mDriver = new Driver();
    }


    @Override
    protected void onStart() {
        super.onStart();

        if(mAuthProvider.existSession())
        {
            //load_data();
            Intent intent = new Intent(MainActivity.this, Home.class);
            startActivity(intent);
        }
        ///* EN PRUEBA, NO QUITAR
        else
        {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
        }
    }


}