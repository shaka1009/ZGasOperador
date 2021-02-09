package zgas.operador.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import zgas.operador.Home;
import zgas.operador.HomeCalificacion;
import zgas.operador.R;
import zgas.operador.models.ClientBooking;
import zgas.operador.providers.AuthProvider;
import zgas.operador.providers.ClientBookingProvider;
import zgas.operador.providers.InServiceProvider;

import static zgas.operador.Home.destino;
import static zgas.operador.Home.inService;


public class ServiciosListAdapter extends RecyclerView.Adapter<ServiciosListAdapter.ViewHolder> {

    private List<ClientBooking> mData;
    private LayoutInflater mInflater;
    private Context context;

    public ServiciosListAdapter(List<ClientBooking> itemList, Context context)
    {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mData = itemList;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mInflater.inflate(R.layout.rv_booking, null);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(final ViewHolder holder, final int position)
    {
        holder.bindData(mData.get(position));
    }

    public void setItems(List<ClientBooking> items)
    {
        mData = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvEtiqueta, tvDireccion, tvCantidad, tvNumExt, tvNumInt, tvSParticular;
        Button btnFinalizar, btnCancelar, btnElegirRuta;
        ClientBookingProvider mClientBookingProvider;
        InServiceProvider mInverviceProvider;
        AuthProvider mAuthProvider;


        ViewHolder(View itemView)
        {
            super(itemView);


            tvEtiqueta = itemView.findViewById(R.id.tvEtiquetaRV);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvNumExt = itemView.findViewById(R.id.tvNumExt);
            tvNumInt = itemView.findViewById(R.id.tvNumInt);
            tvSParticular = itemView.findViewById(R.id.tvSParticular);

            btnFinalizar  = itemView.findViewById(R.id.btnFinalizar);
            btnCancelar  = itemView.findViewById(R.id.btnCancelar);
            btnElegirRuta = itemView.findViewById(R.id.btnElegirRuta);


            mClientBookingProvider = new ClientBookingProvider();
            mAuthProvider = new AuthProvider();
            mInverviceProvider = new InServiceProvider(mAuthProvider.getId());

        }

        @SuppressLint("SetTextI18n")
        void bindData(final ClientBooking item)
        {
            tvEtiqueta.setText(item.getCalle());
            tvDireccion.setText("Colonia: " + item.getColonia());
            String cantidad ="";
            if (item.getCantidad30kg() != 0)
            {
                cantidad = "Cil 30kg: " + item.getCantidad30kg();
            }
            if (item.getCantidad20kg() != 0)
            {
                cantidad = cantidad + "\n" + "Cil 20kg: " + item.getCantidad20kg();
            }
            if (item.getCantidad10kg() != 0)
            {
                cantidad = cantidad + "\n" + "Cil 10kg: " + item.getCantidad10kg();
            }

            tvCantidad.setText(cantidad);
            tvNumExt.setText("Num Ext: " + item.getNumExterior());
            tvNumInt.setText("Num Int: " + item.getNumInterior());
            tvSParticular.setText("Seña particular: " + item.getSParticular());


            btnFinalizar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {



                    Intent intent = new Intent(context, HomeCalificacion.class);
                    intent.putExtra("idCliente", item.getIdCliente());
                    context.startActivity(intent);


                    //mClientBookingProvider.updateStatus(item.getIdCliente(), "finish");
                    //mInverviceProvider.delete(item.getIdCliente());
                }
            });

            btnCancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClientBookingProvider.updateStatus(item.getIdCliente(), "cancel");
                    mInverviceProvider.delete(item.getIdCliente());
                    //inService=false;
                }
            });

            btnElegirRuta.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    destino = new LatLng(item.getLatitud(), item.getLongitud());
                    //inService = true;
                    //dibujandoRuta = true;
                }
            });



        }
    }

}
