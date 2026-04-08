package com.example.controlstock.clases;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import com.example.controlstock.R;
import com.example.controlstock.modelo.Cliente;

public class ClienteAdapter extends ArrayAdapter<Cliente> {
    public interface OnClienteListener {
        void onEditar(Cliente cliente);
        void onEliminar(Cliente cliente);
    }

    private final Context context;
    private final List<Cliente> lista;
    private final OnClienteListener listener;

    public ClienteAdapter(Context context, List<Cliente> lista, OnClienteListener listener) {
        super(context, 0, lista);
        this.context = context;
        this.lista = lista;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_cliente, parent, false);
        }

        Cliente cliente = lista.get(position);

        TextView tvIniciales     = convertView.findViewById(R.id.tvIniciales);
        TextView tvNombreCliente = convertView.findViewById(R.id.tvNombreCliente);
        TextView tvDocumento     = convertView.findViewById(R.id.tvDocumento);
        TextView tvTelefono      = convertView.findViewById(R.id.tvTelefono);
        ImageButton btnEditar    = convertView.findViewById(R.id.btnEditar);
        ImageButton btnEliminar  = convertView.findViewById(R.id.btnEliminar);

        tvIniciales.setText(cliente.getIniciales());
        tvNombreCliente.setText(cliente.getClienteNombre());

        tvDocumento.setText(cliente.getClienteDocumento() != null
                ? "Doc: " + cliente.getClienteDocumento() : "Sin documento");

        tvTelefono.setText(cliente.getClienteTelefono() != null
                ? "Tel: " + cliente.getClienteTelefono() : "");

        btnEditar.setOnClickListener(v -> listener.onEditar(cliente));
        btnEliminar.setOnClickListener(v -> listener.onEliminar(cliente));

        return convertView;
    }
}
