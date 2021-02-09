package zgas.operador;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import zgas.operador.includes.Popup;
import zgas.operador.includes.Toolbar;
import zgas.operador.models.Driver;
import zgas.operador.providers.AuthProvider;
import zgas.operador.providers.DriverProvider;
import zgas.operador.providers.PriceProvider;
import zgas.operador.tools.Validacion;

public class HomePerfil extends AppCompatActivity {
    private AuthProvider mAuthProvider;
    private DriverProvider mDriverProvider;
    private EditText etNombre;
    private EditText etApellido;
    private Popup mPopup;
    private CircleImageView ivPerfil;
    boolean isNew;
    private CoordinatorLayout snackbar;
    private ConnectivityManager cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_perfil);
        Toolbar.show(this, true);
        mAuthProvider = new AuthProvider();
        mDriverProvider = new DriverProvider();
        cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        mPopup = new Popup(this, getApplicationContext(), findViewById(R.id.popupError));
        pressUpdate = false;

        isNew = getIntent().getBooleanExtra("isNew", false);

        declaration();
        listenner();

    }

    private void listenner() {
        KeyboardVisibilityEvent.setEventListener(this, isOpen -> {
            if (isOpen) {
                ivPerfil.setVisibility(View.GONE);
            } else {
                ivPerfil.setVisibility(View.VISIBLE);
            }
        });
    }

    private void declaration() {
        /// MASK 10 DIGITOS
        TextView tvTelefono = findViewById(R.id.tvTelefono);
        SimpleMaskFormatter smf1 = new SimpleMaskFormatter("+NN NN NNNN NNNN");
        MaskTextWatcher mtw1 = new MaskTextWatcher(tvTelefono, smf1);
        tvTelefono.addTextChangedListener(mtw1);
        tvTelefono.setText(mAuthProvider.getPhone());
        //

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        ivPerfil = findViewById(R.id.ivPerfil);

        if(!Home.mDriver.getNombre().equals(""))
            etNombre.setText(Home.mDriver.getNombre());
        if(!Home.mDriver.getApellido().equals(""))
            etApellido.setText(Home.mDriver.getApellido());

        snackbar = findViewById(R.id.snackbar_layout);
    }

    ///////////TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_check_ok, menu); //MOSTRAR
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.btnUpdate:
                UIUtil.hideKeyboard(HomePerfil.this); //ESCONDER TECLADO
                valDatos();
                break;

            case android.R.id.home:
                backPress();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    //

    private boolean pressUpdate;

    private void valDatos() {
        if(pressUpdate)
            return;
        else pressUpdate = true;

        Validacion mVal = new Validacion();

        if((etNombre.getText().toString()).equals("") || mVal.isOnlySpace(etNombre.getText().toString()))
        {
            //mPopup.setPopupError("El nombre es requerido.");
            Snackbar.make(snackbar, "El nombre es requerido.", Snackbar.LENGTH_LONG)
                    .setActionTextColor(getResources().getColor(R.color.white)).show();
            etNombre.requestFocus();
            SleepButton();
        }
        else if((etNombre.getText().toString()).trim().equals(Home.mDriver.getNombre()) && (etApellido.getText().toString()).trim().equals(Home.mDriver.getApellido())){
            finish();
        }
        else if(!isConnected())
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar snackbar1;
                    snackbar1 = Snackbar.make(snackbar, "No hay conexión a internet, no puedes actualizar tus datos personales.", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar1.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.rojoAlert));
                    snackbar1.show();
                }
            });
            SleepButton();
        }
        else
        {
            Home.mDriver = new Driver(mAuthProvider.getId(), etNombre.getText().toString().trim(), etApellido.getText().toString().trim(), mAuthProvider.getPhone());
            mDriverProvider.update(Home.mDriver).addOnCompleteListener(taskCreate -> {
                if (taskCreate.isSuccessful()) {
                    if(isNew)
                    {
                        Driver.setIsLoad(true);
                        Intent intent = new Intent(HomePerfil.this, Home.class);
                        startActivity(intent);
                        runOnUiThread(() -> {
                            Home.tvNombre.setText(Home.mDriver.getNombre());
                            Home.tvApellido.setText(Home.mDriver.getApellido());
                        });
                        guardar_datos();
                    }
                    else
                    {
                        runOnUiThread(() -> {
                            Home.tvNombre.setText(Home.mDriver.getNombre());
                            Home.tvApellido.setText(Home.mDriver.getApellido());
                        });
                        guardar_datos();
                    }
                        finish();
                }
                else {
                    mPopup.setPopupError("No se pudo registrar los datos, intentalo más tarde.");
                    pressUpdate = false;
                }
            });
        }
    }

    private boolean isConnected(){
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void guardar_datos()
    {

        try{
            //DELETE FILE
            try{
                try{
                    deleteFile("Acc_App");
                }catch(Exception ignored){}

                File f = new File("Acc_App");
                f.delete();
            }catch(Exception ignored){}
            //

            java.util.Date Data = new Date();
            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            FileOutputStream conf = openFileOutput("Acc_App", Context.MODE_PRIVATE);
            String cadena =
                    dateFormat.format(Data) + "\n" +
                            Home.mDriver.getNombre() + "\n" +
                            Home.mDriver.getApellido() + "\n";

            conf.write(cadena.getBytes());
            conf.close();
        }
        catch(Exception ignored){}
    }


    private void SleepButton()
    {
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pressUpdate = false;
        }).start();
    }

    //BACK PRESS

    @Override
    public void onBackPressed() {
        backPress();
    }

    //DEP
    private void backPress() {
        if(isNew)
            this.moveTaskToBack(true); //Minimizar
        else
            finish();
    }
    //BACK PRESS
}