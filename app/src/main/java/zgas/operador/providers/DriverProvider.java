package zgas.operador.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import zgas.operador.models.Driver;


public class DriverProvider {

    DatabaseReference mDatabase;

    public DriverProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Usuarios").child("Operadores");
    }

    public Task<Void> create(Driver driver) {
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", driver.getNombre());
        map.put("apellido", driver.getApellido());
        map.put("telefono", driver.getTelefono());
        return mDatabase.child(driver.getId()).child("Datos").setValue(map);
    }

    public Task<Void> update(Driver driver) {
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", driver.getNombre());
        map.put("apellido", driver.getApellido());
        map.put("telefono", driver.getTelefono());
        //map.put("image", client.getImage());
        return mDatabase.child(driver.getId()).child("Datos").updateChildren(map);
    }

    public DatabaseReference getDriver(String idClient) {
        return mDatabase.child(idClient).child("Datos");
    }

}
