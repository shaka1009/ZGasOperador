package zgas.operador.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import zgas.operador.models.DriverFound;


public class DriversFoundProvider {

    DatabaseReference mDatabase;


    /*
    public DriversFoundProvider() {
        // LE ENVIAMOS LA SOLITUD DE VIAJE
        mDatabase = FirebaseDatabase.getInstance().getReference().child("DriversFound");
    }
*/
    public DriversFoundProvider(String idCliente) {
        // LE ENVIAMOS LA SOLITUD DE VIAJE
        mDatabase = FirebaseDatabase.getInstance().getReference().child("DriversFound").child(idCliente);
    }

    public Task<Void> create(DriverFound driverFound) {
        return mDatabase.child(driverFound.getIdDriver()).setValue(driverFound);
    }

    /*
     * SI UN CONDCUTOR ESTA RECIBIENDO LA NOTIFICACION
     */
    public Query getDriverFoundByIdDriver(String idDriver) {
        return mDatabase.orderByChild("idDriver").equalTo(idDriver);
    }

    public Task<Void> delete(String idDriver) {
        return mDatabase.child(idDriver).removeValue();
    }

}
