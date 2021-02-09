package zgas.operador.providers;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import zgas.operador.Home;
import zgas.operador.models.Price;

public class PriceProvider {

    DatabaseReference mDatabase;

    public PriceProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Precio");
    }

    public void getPriceSnapshot(@NonNull DataSnapshot snapshot)
    {
        if (snapshot.exists())
        {
            double thermogas = (double) snapshot.child("thermogas").getValue();
            double multigas = (double) snapshot.child("multigas").getValue();
            double gaslicuado = (double) snapshot.child("gaslicuado").getValue();
            Home.mPrice = new Price(thermogas, multigas, gaslicuado);
            Price.setIsLoad(true);
        }
    }

    public DatabaseReference getPrice() {
        return mDatabase;
    }

}
