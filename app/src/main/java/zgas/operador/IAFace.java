package zgas.operador;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class IAFace extends AppCompatActivity {

    Button btnVerificar;

    FloatingActionButton fabSelectImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ia_face);


        btnVerificar = findViewById(R.id.btnVerificar);
        fabSelectImage = findViewById(R.id.fabSelectImage);
        imvFoto1 = findViewById(R.id.imvFoto1);

        fabSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCamara(1);
            }
        });


        tvVer = findViewById(R.id.tvVer);


        btnVerificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                finish();




            }
        });



    }

    TextView tvVer;

    //IA DEC

    private double calculate_distance(float[][] ori_embedding, float[][] test_embedding) {
        double sum =0.0;
        for(int i=0;i<128;i++){
            sum=sum+Math.pow((ori_embedding[0][i]-test_embedding[0][i]),2.0);
        }
        return Math.sqrt(sum);
    }


    //

    private void abrirCamara(int code){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, code);
        }
    }

    private ImageView imvFoto1;
    Bitmap imgBitmap1;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imgBitmap1 = (Bitmap) extras.get("data");
            imvFoto1.setImageBitmap(imgBitmap1);

            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
        }
    }
}