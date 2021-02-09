package zgas.operador.providers;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import zgas.operador.models.ClientBooking;
import zgas.operador.models.Driver;


public class ClientBookingProvider {

    private DatabaseReference mDatabase;


    public ClientBookingProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("ClientBooking");
    }

    public Task<Void> create(String id, Map<String, Object> data) {
        return mDatabase.child(id).setValue(data);
    }

    public Task<Void> create(ClientBooking clientBooking) {
        return mDatabase.child(clientBooking.getIdCliente()).setValue(clientBooking);
    }

    public Task<Void> updateStatus(String idClientBooking, String status) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        return mDatabase.child(idClientBooking).updateChildren(map);
    }

    public Task<Void> updateStatus(String idClientBooking, String status, String idHistory) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("idHistoryBooking", idHistory);
        return mDatabase.child(idClientBooking).updateChildren(map);
    }

    public Task<Void> updateData(String idClientBooking, Driver driver) {
        Map<String, Object> map = new HashMap<>();
        map.put("nombreOperador", driver.getNombre());
        map.put("apellidoOperador", driver.getApellido());
        map.put("unidadOperador", driver.getUnidad());
        return mDatabase.child(idClientBooking).updateChildren(map);
    }

    public Task<Void> updateIdHistoryBooking(String idClientBooking) {
        String idPush = mDatabase.push().getKey();
        Map<String, Object> map = new HashMap<>();
        map.put("idHistoryBooking", idPush);
        return mDatabase.child(idClientBooking).updateChildren(map);
    }
    public Task<Void> updateStatusAndIdDriver(String idClientBooking, String status, String idDriver) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("idDriver", idDriver);
        return mDatabase.child(idClientBooking).updateChildren(map);
    }

    public DatabaseReference getStatus(String idClientBooking) {
        return mDatabase.child(idClientBooking).child("status");
    }

    public DatabaseReference getClientBooking(String idClientBooking) {
        return mDatabase.child(idClientBooking);
    }

    public Task<Void> delete(String idClientBooking) {
        return mDatabase.child(idClientBooking).removeValue();
    }
}
