package zgas.operador;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import zgas.operador.includes.Popup;
import zgas.operador.providers.RegistroProvider;

public class Login extends AppCompatActivity {

    private EditText etTelefono;
    private EditText etNumNomina;
    Button btnSend;
    private Popup mPopup;
    private ProgressBar pbLogin;

    String telefono;

    RegistroProvider registroProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        declaration();
        listenner();


        registroProvider = new RegistroProvider();
    }

    private void declaration() {
        /// MASK 10 DIGITOS
        etTelefono = findViewById(R.id.etTelefono);
        SimpleMaskFormatter smf = new SimpleMaskFormatter("NN NNNN NNNN");
        MaskTextWatcher mtw = new MaskTextWatcher(etTelefono, smf);
        etTelefono.addTextChangedListener(mtw);

        //

        //Popup - Error
        mPopup = new Popup(this, getApplicationContext(), findViewById(R.id.popupError));

        //Button Send
        btnSend = findViewById(R.id.btnSend);

        //Progress - bar
        pbLogin = findViewById(R.id.pbLogin);

        etNumNomina = findViewById(R.id.etNumNomina);

        etNumNomina.requestFocus();
    }
    boolean btnSendPress = false;

    private void listenner()
    {
        //Esconder Teclado
        LinearLayout titleLogin = findViewById(R.id.titleLogin);
        KeyboardVisibilityEvent.setEventListener(this, isOpen -> {
            if (isOpen) {
                titleLogin.setVisibility(View.GONE);
                //LinearDots.setVisibility(View.GONE);
            } else {
                titleLogin.setVisibility(View.VISIBLE);
                //LinearDots.setVisibility(View.VISIBLE);
            }
        });

        loadingVisible(false);

        btnSend.setOnClickListener(v -> {
            if(btnSendPress)
                return;
            else btnSendPress = true;

            runOnUiThread(() -> loadingVisible(true));

            telefono = etTelefono.getText().toString().replaceAll(" ", "");

            new Thread(() -> {
                UIUtil.hideKeyboard(Login.this); //ESCONDER TECLADO



                if (etNumNomina.length() == 0)
                {
                    runOnUiThread(() -> {
                        mPopup.setPopupError("Ingresa un número de nómina.");
                        btnSendPress = false;
                        runOnUiThread(() -> loadingVisible(false));
                    });
                }
                else if(!valNomina())
                {
                    runOnUiThread(() -> {
                        mPopup.setPopupError("El supervisor debe registrar tu número de nómina y teléfono.");
                        btnSendPress = false;
                        runOnUiThread(() -> loadingVisible(false));
                    });
                }
                else if (etTelefono.length() == 0)
                {
                    runOnUiThread(() -> {
                        mPopup.setPopupError("Ingresa un número de teléfono.");
                        btnSendPress = false;
                        runOnUiThread(() -> loadingVisible(false));
                    });
                }
                else if (etTelefono.length() != 12)
                {
                    runOnUiThread(() -> {
                        mPopup.setPopupError("El número debe contener 10 dígitos.");
                        btnSendPress = false;
                        runOnUiThread(() -> loadingVisible(false));
                    });
                }
                else
                {
                    new Thread(() -> {
                        Intent otpIntent = new Intent(Login.this , LoginSMS.class);
                        otpIntent.putExtra("telefono", telefono);
                        startActivity(otpIntent);
                        finish();
                    }).start();
                }
            }).start();
        });
    }



    public boolean b=false;

    private boolean valNomina() {
        b=false;

        registroProvider.getOperador(etNumNomina.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {


                    String telTEMP="12";
                    try {
                        telTEMP = snapshot.child("telefono").getValue().toString();
                        if(telefono.equals(telTEMP))
                            b = true;
                    }
                    catch (Exception ignored) {}

                    Log.d("DEP", "EXISTE"+telefono+telTEMP+b);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                runOnUiThread(() -> {
                    mPopup.setPopupError("Error desconocido.");
                    btnSendPress = false;
                    runOnUiThread(() -> loadingVisible(false));
                });
            }
        });






        for(int x=0; x<2; x++)
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Log.d("DEP", "VAR: "+b);
        return b;
    }

    private void loadingVisible(boolean b)
    {
        if(b)
        {
            btnSend.setVisibility(View.INVISIBLE);
            pbLogin.setVisibility(View.VISIBLE);
            etTelefono.setEnabled(false);
        }
        else
        {
            btnSend.setVisibility(View.VISIBLE);
            pbLogin.setVisibility(View.INVISIBLE);
            etTelefono.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }
}