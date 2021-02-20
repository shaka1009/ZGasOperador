package zgas.operador;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import zgas.operador.includes.Popup;
import zgas.operador.includes.Toolbar;
import zgas.operador.models.ClientBooking;
import zgas.operador.models.Driver;
import zgas.operador.models.Price;
import zgas.operador.providers.AuthProvider;
import zgas.operador.providers.ClientBookingProvider;
import zgas.operador.providers.GeofireProvider;
import zgas.operador.providers.GoogleApiProvider;
import zgas.operador.providers.InServiceProvider;
import zgas.operador.adapters.ServiciosListAdapter;
import zgas.operador.providers.TokenProvider;
import zgas.operador.services.ForegroundService;
import zgas.operador.tools.CarMoveAnim;
import zgas.operador.tools.DecodePoints;


@SuppressLint("SetTextI18n")
public class Home extends AppCompatActivity implements OnMapReadyCallback {

    //Clases
    private AuthProvider mAuthProvider;
    private DriverProvider mDriverProvider;
    private GeofireProvider mGeofireProvider;
    private ClientBookingProvider mClientBookingProvider;
    private GoogleApiProvider mGoogleApiProvider;
    private List mPolylineList;
    private PolylineOptions mPolylineOptions;

    //Variable Global
    public static Driver mDriver;
    public static Price mPrice;
    private boolean pressButton = false;

    //MAP
    private final static int REQUEST_CHECK_SETTINGS = 0x1;
    private final static int LOCATION_REQUEST_CODE = 1;
    private FusedLocationProviderClient mFusedLocation;
    private LocationRequest mLocationRequest;
    private LocationManager mLocationManager;
    private GoogleApiClient mGoogleApiClient;
    public static GoogleMap mMap;
    public static GoogleMap mMapRoute;
    private Marker mMarker;
    private boolean mIsStartLocation = false;
    private LatLng mCurrentLatLng;
    private LatLng mStartLatLng;
    private LatLng mEndLatLng;

    //Button Connect
    private Button mButtonConnect;
    private boolean mIsConnect;

    //Conexión a internet
    private ConnectivityManager cm; //Método  isConnected() - true: tiene internet, false no tiene
    private TokenProvider mTokenProvider; //Generación de token de usuario

    //Drawer
    private DrawerLayout drawer, drawerRight;
    private Popup mPopup;

    //Servicios
    private RecyclerView rvServicios;
    InServiceProvider mInServiceProvider;
    private ChildEventListener mListener;
    private List<ClientBooking> mServicios;
    public static boolean inService=false;
    public static LatLng destino;
    private Marker mMarkerDestino;

    //Diseño
    public static TextView tvTelefono;
    public static TextView tvNombre;
    public static TextView tvApellido;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ///SE NECESITAN CARGAR DATOS PERSONALES DESDE HOME
        //CARGA UNA SOLA VEZ AL DÍA :)
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar.showHome(this, true);
        mPopup = new Popup(this, getApplicationContext(), findViewById(R.id.popupError));

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("active_drivers");
        mGoogleApiProvider = new GoogleApiProvider(this);
        mInServiceProvider = new InServiceProvider(mAuthProvider.getId());
        mClientBookingProvider = new ClientBookingProvider();
        mDriverProvider = new DriverProvider();
        cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        rvServicios = findViewById(R.id.rvServicios);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvServicios.setLayoutManager(linearLayoutManager);

        drawerMain();

        generateToken();
        mapCreate();
        buttonConnect();
        load_first_day();


        iniciarListener();
    }



    ////////////CARGA DE DATOS
    private void load_first_day() {
        //Load Perfil
        new Thread(new Runnable(){
            @Override
            public void run()
            {
                load_datos();
            }
        }).start();
    }

    private void load_datos()
    {
        try {
            FileInputStream read = openFileInput("Acc_App");
            int size = read.available();
            byte[] buffer = new byte[size];
            read.read(buffer);
            read.close();
            String text = new String(buffer);
            StringTokenizer token = new StringTokenizer(text, "\n");
            String fecha = token.nextToken();

            java.util.Date Data = new Date();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            if(fecha.equals(dateFormat.format(Data)))
            {

                Home.mDriver.setTelefono(token.nextToken());
                Home.mDriver.setNombre(token.nextToken());
                Home.mDriver.setApellido(token.nextToken());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvTelefono.setText(mDriver.getTelefono());
                        tvNombre.setText(mDriver.getNombre());
                        tvApellido.setText(mDriver.getApellido());
                    }
                });
                Driver.setIsLoad(true);
            }
            else
                guardar_datos();
        }
        catch(Exception e){
            guardar_datos();
        }
    }

    private void guardar_datos()
    {
        new Thread(() -> mDriverProvider.getDriver(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String id = mAuthProvider.getId();
                    String nombre = Objects.requireNonNull(snapshot.child("nombre").getValue()).toString();
                    String apellido = Objects.requireNonNull(snapshot.child("apellido").getValue()).toString();
                    String telefono = mAuthProvider.getPhone();
                    Home.mDriver = new Driver(id, nombre, apellido, telefono);
                    Driver.setIsLoad(true);

                    Log.d("DEP", "Datos en variable.");

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
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                        FileOutputStream conf = openFileOutput("Acc_App", Context.MODE_PRIVATE);
                        String cadena =
                                dateFormat.format(Data) + "\n" +
                                        Home.mDriver.getTelefono() + "\n" +
                                        Home.mDriver.getNombre() + "\n" +
                                        Home.mDriver.getApellido() + "\n";

                        conf.write(cadena.getBytes());
                        conf.close();
                    }
                    catch(Exception ignored){}

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvTelefono.setText(mDriver.getTelefono());
                            tvNombre.setText(mDriver.getNombre());
                            tvApellido.setText(mDriver.getApellido());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        })).start();
    }



    Polyline polylineFinal;
    private void drawRoute(final LatLng latLng) {
        mGoogleApiProvider.getDirections(mCurrentLatLng, latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
/*
                try {
                    mMarkerDestino = mMapRoute.addMarker(
                            new MarkerOptions()
                                    .position(latLng)
                                    .flat(true)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_house)));
                    mMarkerDestino.remove();
                }
                catch (Exception ignored){}*/

                try {polylineFinal.remove();} catch(Exception ignored){}
                try {
                    assert response.body() != null;
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    mPolylineList = DecodePoints.decodePoly(points);
                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(Color.DKGRAY);
                    mPolylineOptions.width(13f);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mPolylineList);
                    polylineFinal = mMapRoute.addPolyline(mPolylineOptions);
                    /*
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");*/
                    if(!inService)
                        try {polylineFinal.remove();} catch(Exception ignored){ }


                } catch (Exception e) {
                    // AQUI DEBES TENER EL TOAST PARA VERIFICAR EL ERROR
                    runOnUiThread(() -> {
                        Log.d("Error", "Error encontrado " + e.getMessage());
                        Toast.makeText(Home.this, "Error al trazar la ruta " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Toast.makeText(Home.this, "Conductor Lat: " + mCurrentLatLng.latitude, Toast.LENGTH_SHORT).show();
                        Toast.makeText(Home.this, "Conductor Lng: " + mCurrentLatLng.longitude, Toast.LENGTH_SHORT).show();
                        Toast.makeText(Home.this, "Lugar de recogida Lat: " + latLng.latitude, Toast.LENGTH_SHORT).show();
                        Toast.makeText(Home.this, "Lugar de recogida Lng: " + latLng.longitude, Toast.LENGTH_SHORT).show();
                    });
                }
            }
            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {}
        });
    }




    @Override
    protected void onResume() {
        stopServiceSegundoPlano(); //Segundo plano
        if(mIsConnect)
        {
            stopLocation();
            removeLocation();
            pararListener();
            startLocation();
        }

        iniciarListener();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        stopLocation();
        removeLocation();
        pararListener();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if(mIsConnect)
            startServiceSegundoPlano(); //Segundo plano
        stopLocation();
        removeLocation();
        pararListener();
        super.onPause();
    }


    boolean limpio=true;
    private void iniciarListener() {
        pararListener();
        mServicios = new ArrayList<>();
        limpio = true;

        mListener = mInServiceProvider.getInService().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                childChanged(snapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                pararListener();
                iniciarListener();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                inService = false;
                mServicios = new ArrayList<>();
                try {polylineFinal.remove();} catch(Exception ignored){ }
                load_servicios();

                pararListener();
                iniciarListener();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                pararListener();
                iniciarListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });




    }



    private void childChanged(DataSnapshot snapshot)
    {
        if(limpio)
            mServicios = new ArrayList<>();


        Log.d("DEP", "childChanged");
        if(snapshot.exists())
        {
            inService = true;
            new Thread(() -> {
                limpio = false;

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}

                String idCliente = Objects.requireNonNull(snapshot.child("idCliente").getValue()).toString();

                mClientBookingProvider.getClientBooking(idCliente).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                        if(snapshot1.exists())
                        {
                            ClientBooking mClientBooking = new ClientBooking();

                            mClientBooking.setIdCliente(idCliente);
                            mClientBooking.setIdDriver(Objects.requireNonNull(snapshot1.child("idDriver").getValue()).toString());
                            mClientBooking.setId(Objects.requireNonNull(snapshot1.child("id").getValue()).toString());
                            mClientBooking.setType(Objects.requireNonNull(snapshot1.child("type").getValue()).toString());
                            mClientBooking.setStatus(Objects.requireNonNull(snapshot1.child("status").getValue()).toString());
                            mClientBooking.setCantidad30kg(Integer.parseInt(Objects.requireNonNull(snapshot1.child("cantidad30kg").getValue()).toString()));
                            mClientBooking.setCantidad20kg(Integer.parseInt(Objects.requireNonNull(snapshot1.child("cantidad20kg").getValue()).toString()));
                            mClientBooking.setCantidad10kg(Integer.parseInt(Objects.requireNonNull(snapshot1.child("cantidad10kg").getValue()).toString()));
                            mClientBooking.setEtiqueta(Objects.requireNonNull(snapshot1.child("etiqueta").getValue()).toString());
                            mClientBooking.setCalle(Objects.requireNonNull(snapshot1.child("calle").getValue()).toString());
                            mClientBooking.setColonia(Objects.requireNonNull(snapshot1.child("colonia").getValue()).toString());
                            mClientBooking.setEtiqueta(Objects.requireNonNull(snapshot1.child("codigo_postal").getValue()).toString());
                            mClientBooking.setDireccion(Objects.requireNonNull(snapshot1.child("direccion").getValue()).toString());

                            mClientBooking.setNumExterior(Objects.requireNonNull(snapshot1.child("numExterior").getValue()).toString());
                            mClientBooking.setNumInterior(Objects.requireNonNull(snapshot1.child("numInterior").getValue()).toString());
                            mClientBooking.setSParticular(Objects.requireNonNull(snapshot1.child("SParticular").getValue()).toString());


                            mClientBooking.setEmpresa(Integer.parseInt(Objects.requireNonNull(snapshot1.child("empresa").getValue()).toString()));
                            mClientBooking.setRuta(Objects.requireNonNull(snapshot1.child("ruta").getValue()).toString());

                            mClientBooking.setLatitud((double) snapshot1.child("latitud").getValue());
                            mClientBooking.setLongitud((double) snapshot1.child("longitud").getValue());

                            if(mClientBooking.getStatus().equals("accept"))
                            {
                                inService = true;
                                mServicios.add(mClientBooking);
                                destino = new LatLng(mClientBooking.getLatitud(), mClientBooking.getLongitud());
                            }


                            Log.d("DEP", "SI RECUPERÖ DATOS");
                            load_servicios();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }).start();
        }
        else
        {
            inService = false;
            try {polylineFinal.remove();} catch(Exception ignored){ }
        }

    }



    private void load_servicios() {
        ServiciosListAdapter sucursalesListAdapter = new ServiciosListAdapter(mServicios, this);
        rvServicios.setHasFixedSize(true);
        rvServicios.setLayoutManager(new LinearLayoutManager(this));
        rvServicios.setAdapter(null);
        rvServicios.setAdapter(sucursalesListAdapter);
    }

    private void pararListener() {
        try {
            mListener = null;
            mInServiceProvider.getInService().removeEventListener(mListener);
        }
        catch (Exception ignored) {}

    }


    //Diseño y lógica
    @SuppressLint({"NonConstantResourceId", "RtlHardcoded"})
    private void drawerMain()
    {
        //drawer.openDrawer(Gravity.LEFT);
        //drawer.closeDrawer(Gravity.LEFT);

        //drawer
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);

        drawerRight = findViewById(R.id.drawer_layoutRight);

        ImageView close_drawer_right = findViewById(R.id.close_drawer_right);
        close_drawer_right.setOnClickListener(v -> drawerRight.closeDrawer(Gravity.RIGHT));

        tvTelefono = hView.findViewById(R.id.slide_telefono);
        tvNombre = hView.findViewById(R.id.slide_nombre);
        tvApellido = hView.findViewById(R.id.slide_apellido);
        //drawer

        /// MASK 10 DIGITOS
        SimpleMaskFormatter smf1 = new SimpleMaskFormatter("+NN NN NNNN NNNN");
        MaskTextWatcher mtw1 = new MaskTextWatcher(tvTelefono, smf1);
        tvTelefono.addTextChangedListener(mtw1);

        try {
            tvTelefono.setText(mDriver.getTelefono());
            tvNombre.setText(mDriver.getNombre());
            tvApellido.setText(mDriver.getApellido());
        }
        catch(Exception e)
        {
            load_datos();
            try {
                tvTelefono.setText(mDriver.getTelefono());
                tvNombre.setText(mDriver.getNombre());
                tvApellido.setText(mDriver.getApellido());
            }
            catch (Exception u)
            { }
        }

        navigationView.bringToFront();

        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId())
            {
                case R.id.menu_perfil:
                    if(PushButton())
                        break;

                    if(!Driver.isLoad())
                    {
                        View v=findViewById(R.id.menu_perfil);
                        Snackbar.make(v, "No se han cargado los datos necesarios.", Snackbar.LENGTH_LONG)
                                .setActionTextColor(getResources().getColor(R.color.white)).show();
                        SleepButton();
                        break;
                    }
                    Intent b = new Intent(Home.this, HomePerfil.class );
                    startActivity(b);
                    SleepButton();
                    break;

                case R.id.cerrar_sesion:
                    if(PushButton())
                        break;

                    try
                    {
                        mPopup.setPopupCerrarSesion(mDriver.getNombre(), mDriver.getApellido());
                    }
                    catch (Exception ignored){}
                    SleepButton();
                    break;
            }
            drawer.closeDrawers();
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_home, menu); //MOSTRAR
        return true;
    }

    private boolean PushButton()
    {
        return pressButton;
    }

    private void SleepButton()
    {
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pressButton = false;
        }).start();
    }

    //BACK PRESS
    @SuppressLint({"RtlHardcoded", "NonConstantResourceId"})
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                drawer.openDrawer(Gravity.LEFT);
                break;

            case R.id.btnServiceList:
                drawerRight.openDrawer(Gravity.RIGHT);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true); //Minimizar
    }

    //Diseño y lógica


    /* Mapa y conexión de operador */

    void generateToken() {
        mTokenProvider = new TokenProvider();

        new Thread(() -> {
            while(true)
            {
                if(isConnected())
                {
                    mTokenProvider.create(mAuthProvider.getId());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void buttonConnect() {
        mButtonConnect = findViewById(R.id.mButtonConnect);
        mButtonConnect.setOnClickListener(view -> {
            if (mIsConnect) {
                mButtonConnect.setText("Conectarse");
                disconnect();
                //stopService();
                mIsConnect=false;
            }
            else {
                mButtonConnect.setText("Desconectarse");

                startLocation();
                mIsConnect=true;
            }
        });

        mGeofireProvider.isActive(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    mIsConnect = true;
                    mButtonConnect.setVisibility(View.VISIBLE);
                    startLocation();
                    mButtonConnect.setText("Desconectarse");
                } else {
                    mIsConnect = false;
                    mButtonConnect.setVisibility(View.VISIBLE);
                    disconnect();
                    mButtonConnect.setText("Conectarse");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void disconnect() {
        stopServiceSegundoPlano();
        if (mFusedLocation != null) {
            mIsStartLocation = false;
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            mMap.clear();
            removeLocation();
            if (mAuthProvider.existSession()) {
                mGeofireProvider.removeLocation(mAuthProvider.getId());
            }
        }
        else {
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeLocation() {
        mIsStartLocation = false;

        if (locationListenerGPS != null) {
            mLocationManager.removeUpdates(locationListenerGPS);
        }
        else
        {
            try {
                mLocationManager.removeUpdates(locationListenerGPS);
            }catch (Exception ignored){}
        }
    }

    private void stopLocation() {
        if (mLocationCallback != null && mFusedLocation != null) {
            mFusedLocation.removeLocationUpdates(mLocationCallback);
        }
        else
        {
            try {
                assert mFusedLocation != null;
                mFusedLocation.removeLocationUpdates(mLocationCallback);
            }catch (Exception ignored){}
        }
    }


    private void startServiceSegundoPlano() {
        stopServiceSegundoPlano();
            Intent serviceIntent = new Intent(Home.this, ForegroundService.class);
            ContextCompat.startForegroundService(Home.this, serviceIntent);
    }

    private void stopServiceSegundoPlano() {
        Intent serviceIntent = new Intent(Home.this, ForegroundService.class);
        stopService(serviceIntent);
    }

    ///////BUTTON connect
    private void mapCreate() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mGoogleApiClient = getAPIClientInstance();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMapRoute = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(20.6340165, -103.3536772))
                        .zoom(15f)
                        .build()
        ));

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
    }

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                }
                else {
                    //showAlertDialogNOGPS();
                    requestGPSSettings();
                }
            }
            else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
            else {
                //showAlertDialogNOGPS();
                requestGPSSettings();
            }
        }
    }

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            if (mStartLatLng != null) {
                mEndLatLng = mStartLatLng;
            }

            mStartLatLng = new LatLng(mCurrentLatLng.latitude, mCurrentLatLng.longitude);

            if (mEndLatLng != null) {
                CarMoveAnim.carAnim(mMarker, mEndLatLng, mStartLatLng);
            }

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
                            .zoom(15f)
                            .build()
            ));

            dibujandoRuta();
        }

        @Override
        public void onProviderEnabled(String s) { }

        @Override
        public void onProviderDisabled(String s) {}
    };

    LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location: locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    if(!mIsStartLocation)
                    {
                        mMap.clear();
                        mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mIsStartLocation = true;
                        // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                        .zoom(15f)
                                        .build()
                        ));

                        mMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.zeta_car))
                        );

                        updateLocation();

                        if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }

                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                2000,
                                10,
                                locationListenerGPS
                        );
                        stopLocation();

                    }
                    Log.d("DEP", "Actualizando posición 1 sola vez.");
                }
            }
        }
    };





    private void updateLocation() {
        if (mAuthProvider.existSession() && mCurrentLatLng != null) {
            mGeofireProvider.saveLocation(mAuthProvider.getId(), mCurrentLatLng);
        }
    }

    //Validaciones, permisos
    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    private void requestGPSSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        final PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(locationSettingsResult -> {
            Status status = locationSettingsResult.getStatus();

            if (status.getStatusCode() == LocationSettingsStatusCodes.SUCCESS) {
                Toast.makeText(Home.this, "El GPS ya está activado", Toast.LENGTH_SHORT).show();
            } else if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                try {
                    status.startResolutionForResult(Home.this, REQUEST_CHECK_SETTINGS);
                    if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    //ubicacion_actual();
                } catch (IntentSender.SendIntentException e) {
                    Toast.makeText(Home.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            } else if (status.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                Toast.makeText(Home.this, "La configuración del GPS tiene algún error o está disponible.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicación requiere de los permisos de ubicación para poder utilizarse.")
                        .setPositiveButton("OK", (dialogInterface, i) -> ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE))
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    private GoogleApiClient getAPIClientInstance() {
        return new GoogleApiClient.Builder(this).addApi(LocationServices.API).build();
    }

    private boolean isConnected(){
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    Thread thread = new Thread(new Runnable(){
            @Override
            public void run()
            {
                dibujandoRuta=true;
                Log.d("DEP", "DIBUJANDO RUTA");

                while(true)
                {
                    if(inService)
                    {
                        Log.d("DEP", "DIBUJANDO RUTA: BUCLE");
                        try {polylineFinal.remove();} catch(Exception ignored){ }
                        if(mServicios.isEmpty())
                            try {polylineFinal.remove();} catch(Exception ignored){ }
                        else
                            drawRoute(destino);
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        try {polylineFinal.remove();} catch(Exception ignored){ }
                        break;
                    }
                }
                try {polylineFinal.remove();} catch(Exception ignored){ }
            }
        });


    public static boolean dibujandoRuta = false;
    private void dibujandoRuta() {
        //Log.d("DEP", "Dibujando Ruta Estado: " + thread.getState());
        Log.d("DEP", "vacío" + mServicios.isEmpty());

        if(inService)
        {
            if(!dibujandoRuta)
            {
                if(thread.getState()!= Thread.State.TERMINATED)
                    thread.start();
                else
                    thread.run();
            }
        }
        //Log.d("DEP", "Dibujando Ruta Estado: " + thread.getState());
    }
}
