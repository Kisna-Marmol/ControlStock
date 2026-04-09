package com.example.controlstock;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;
import com.example.controlstock.clases.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VentaListActivity extends AppCompatActivity {

    // ── Vistas ───────────────────────────────────────────
    private ImageView btnVolver;
    private EditText etBuscar;
    private LinearLayout layoutVentas, layoutVacio;
    private ProgressBar progressBar;
    private TextView tvContador;
    private FloatingActionButton fabNuevaVenta;

    // ── Datos ─────────────────────────────────────────────
    private List<JSONObject> listaCompleta = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venta_list);

        inicializarVistas();
        configurarListeners();
        cargarVentas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarVentas();
    }

    private void inicializarVistas() {
        btnVolver     = findViewById(R.id.btnVolver);
        etBuscar      = findViewById(R.id.etBuscar);
        layoutVentas  = findViewById(R.id.layoutVentas);
        layoutVacio   = findViewById(R.id.layoutVacio);
        progressBar   = findViewById(R.id.progressBar);
        tvContador    = findViewById(R.id.tvContador);
        fabNuevaVenta = findViewById(R.id.fabNuevaVenta);
    }

    private void configurarListeners() {
        btnVolver.setOnClickListener(v -> finish());

        fabNuevaVenta.setOnClickListener(v ->
                startActivity(new Intent(this, VentaFormActivity.class)));

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                filtrarVentas(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ── Cargar ventas desde API ───────────────────────────
    private void cargarVentas() {
        progressBar.setVisibility(View.VISIBLE);
        layoutVentas.setVisibility(View.GONE);

        ApiService.get(Config.local + "ventas/ventas_get.php", new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject res = new JSONObject(response);
                    progressBar.setVisibility(View.GONE);
                    layoutVentas.setVisibility(View.VISIBLE);

                    if (res.optBoolean("success", false)) {
                        JSONArray data = res.getJSONArray("data");
                        listaCompleta.clear();
                        for (int i = 0; i < data.length(); i++)
                            listaCompleta.add(data.getJSONObject(i));
                        renderizarVentas(listaCompleta);
                    }
                } catch (Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("VENTAS", e.getMessage());
                }
            }
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Dialog.toast(VentaListActivity.this, "Error de conexión");
            }
        });
    }

    // ── Filtrar en memoria ────────────────────────────────
    private void filtrarVentas(String query) {
        List<JSONObject> filtrados = new ArrayList<>();
        for (JSONObject v : listaCompleta) {
            String cliente = v.optString("cliente_nombre", "").toLowerCase();
            String numero  = v.optString("factura_numero", "").toLowerCase();
            if (query.isEmpty()
                    || cliente.contains(query.toLowerCase())
                    || numero.contains(query.toLowerCase())) {
                filtrados.add(v);
            }
        }
        renderizarVentas(filtrados);
    }

    // ── Renderizar lista ──────────────────────────────────
    private void renderizarVentas(List<JSONObject> ventas) {
        layoutVentas.removeAllViews();

        if (ventas.isEmpty()) {
            layoutVacio.setVisibility(View.VISIBLE);
            tvContador.setText("Sin ventas");
            return;
        }

        layoutVacio.setVisibility(View.GONE);
        int total = ventas.size();
        tvContador.setText(total + " venta" + (total != 1 ? "s" : ""));

        LayoutInflater inflater = LayoutInflater.from(this);

        for (JSONObject venta : ventas) {
            try {
                View itemView = inflater.inflate(
                        R.layout.item_venta, layoutVentas, false);

                TextView tvNumero    = itemView.findViewById(R.id.tvNumeroFactura);
                TextView tvFecha     = itemView.findViewById(R.id.tvFecha);
                TextView tvCliente   = itemView.findViewById(R.id.tvCliente);
                TextView tvDocumento = itemView.findViewById(R.id.tvDocumento);
                TextView tvTotal     = itemView.findViewById(R.id.tvTotal);
                TextView tvSubtotal  = itemView.findViewById(R.id.tvSubtotal);

                String numero    = venta.optString("factura_numero", "");
                String fecha     = venta.optString("factura_fecha", "");
                String cliente   = venta.optString("cliente_nombre", "");
                String documento = venta.optString("cliente_documento", "");
                double tot       = venta.optDouble("factura_total", 0.0);
                double sub       = venta.optDouble("factura_subtotal", 0.0);
                int facturaId    = venta.optInt("factura_id");

                tvNumero.setText("Factura #" + numero);
                tvCliente.setText(cliente);
                tvDocumento.setText(documento.isEmpty() ? "Sin documento" : "Doc: " + documento);
                tvTotal.setText(String.format("L %.2f", tot));
                tvSubtotal.setText(String.format("Sub: L %.2f", sub));
                tvFecha.setText(Utils.formatearFecha(fecha));

                // Click → ver detalle
                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(this, VentaDetalleActivity.class);
                    intent.putExtra("factura_id", facturaId);
                    startActivity(intent);
                });

                layoutVentas.addView(itemView);

            } catch (Exception e) {
                Log.e("VENTAS", "Error item: " + e.getMessage());
            }
        }
    }
}