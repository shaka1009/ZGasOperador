package zgas.operador.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InServiceProvider {

    DatabaseReference mDatabase;

    public InServiceProvider(String id) {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("inService").child(id);
    }

    public DatabaseReference createService(String idClient) {
        return mDatabase.child(idClient);
    }

    public DatabaseReference getInService() {
        return mDatabase;
    }

    public DatabaseReference loadServices(String idClientBooking) {
        return mDatabase.child(idClientBooking);
    }


    public Task<Void> delete(String idClient) {
        return mDatabase.child(idClient).removeValue();
    }




}
