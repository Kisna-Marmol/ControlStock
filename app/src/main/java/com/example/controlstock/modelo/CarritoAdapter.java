package com.example.controlstock.modelo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.controlstock.R;
import com.example.controlstock.modelo.DetalleVenta;

import java.util.List;

public class CarritoAdapter extends ArrayAdapter<DetalleVenta> {

    public interface OnCarritoListener {
        void onCantidadCambiada();
        void onEliminarItem(int position);
    }

    private final Context context;
    private final List<DetalleVenta> carrito;
    private final OnCarritoListener listener;

    public CarritoAdapter(Context context, List<DetalleVenta> carrito, OnCarritoListener listener) {
        super(context, 0, carrito);
        this.context  = context;
        this.carrito  = carrito;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_carrito, parent, false);
        }

        DetalleVenta item = carrito.get(position);

        TextView    tvNombre        = convertView.findViewById(R.id.tvProductoNombre);
        TextView    tvPrecio        = convertView.findViewById(R.id.tvPrecioUnitario);
        TextView    tvCantidad      = convertView.findViewById(R.id.tvCantidad);
        TextView    tvSubtotal      = convertView.findViewById(R.id.tvSubtotalLinea);
        ImageButton btnMenos        = convertView.findViewById(R.id.btnMenos);
        ImageButton btnMas          = convertView.findViewById(R.id.btnMas);
        ImageButton btnEliminar     = convertView.findViewById(R.id.btnEliminarItem);

        tvNombre.setText(item.getProductoNombre());
        tvPrecio.setText(String.format("L. %.2f c/u", item.getPrecioUnitario()));
        tvCantidad.setText(String.valueOf(item.getCantidad()));
        tvSubtotal.setText(String.format("L. %.2f", item.getSubtotalLineal()));

        btnMenos.setOnClickListener(v -> {
            if (item.getCantidad() > 1) {
                item.setCantidad(item.getCantidad() - 1);
                tvCantidad.setText(String.valueOf(item.getCantidad()));
                tvSubtotal.setText(String.format("L. %.2f", item.getSubtotalLineal()));
                listener.onCantidadCambiada();
            }
        });

        btnMas.setOnClickListener(v -> {
            item.setCantidad(item.getCantidad() + 1);
            tvCantidad.setText(String.valueOf(item.getCantidad()));
            tvSubtotal.setText(String.format("L. %.2f", item.getSubtotalLineal()));
            listener.onCantidadCambiada();
        });

        btnEliminar.setOnClickListener(v -> listener.onEliminarItem(position));

        return convertView;
    }
}