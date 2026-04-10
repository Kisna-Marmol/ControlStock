package com.example.controlstock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotificacionesActivity extends AppCompatActivity {

    private ImageView btnVolver;
    private LinearLayout layoutNotificaciones, layoutVacio;
    private ProgressBar progressBar;
    private TextView tvContador, tvChipSinStock, tvChipStockBajo, tvChipVentas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        inicializarVistas();
        btnVolver.setOnClickListener(v -> finish());
        cargarNotificaciones();
    }

    private void inicializarVistas() {
        btnVolver             = findViewById(R.id.btnVolver);
        layoutNotificaciones  = findViewById(R.id.layoutNotificaciones);
        layoutVacio           = findViewById(R.id.layoutVacio);
        progressBar           = findViewById(R.id.progressBar);
        tvContador            = findViewById(R.id.tvContador);
        tvChipSinStock        = findViewById(R.id.tvChipSinStock);
        tvChipStockBajo       = findViewById(R.id.tvChipStockBajo);
        tvChipVentas          = findViewById(R.id.tvChipVentas);
    }

    private void cargarNotificaciones() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService.get(Config.local + "notificaciones.php", new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject res = new JSONObject(response);
                        if (res.optBoolean("success", false)) {
                            int total      = res.optInt("total", 0);
                            int sinStock   = res.optInt("sin_stock", 0);
                            int stockBajo  = res.optInt("stock_bajo", 0);
                            int ventasHoy  = res.optInt("ventas_hoy", 0);

                            tvContador.setText(total + " alertas");
                            tvChipSinStock.setText("Sin stock: " + sinStock);
                            tvChipStockBajo.setText("Stock bajo: " + stockBajo);
                            tvChipVentas.setText("Ventas hoy: " + ventasHoy);

                            JSONArray notif = res.getJSONArray("notificaciones");

                            if (notif.length() == 0) {
                                layoutVacio.setVisibility(View.VISIBLE);
                                return;
                            }

                            renderizar(notif);
                        }
                    } catch (Exception e) {
                        Dialog.toast(NotificacionesActivity.this, "Error cargando notificaciones");
                    }
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Dialog.toast(NotificacionesActivity.this, "Error de conexión");
                });
            }
        });
    }

    private void renderizar(JSONArray notif) {
        layoutNotificaciones.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < notif.length(); i++) {
            try {
                JSONObject n = notif.getJSONObject(i);
                String tipo        = n.optString("tipo", "");
                String titulo      = n.optString("titulo", "");
                String descripcion = n.optString("descripcion", "");
                int productoId     = n.optInt("producto_id", 0);
                int facturaId      = n.optInt("factura_id", 0);

                View item = inflater.inflate(R.layout.item_notificacion, layoutNotificaciones, false);

                ImageView ivIcono      = item.findViewById(R.id.ivIcono);
                TextView tvTitulo      = item.findViewById(R.id.tvTitulo);
                TextView tvDesc        = item.findViewById(R.id.tvDescripcion);
                View viewIndicador     = item.findViewById(R.id.viewIndicador);

                tvTitulo.setText(titulo);
                tvDesc.setText(descripcion);

                switch (tipo) {
                    case "SIN_STOCK":
                        ivIcono.setImageResource(R.drawable.ct_inventory);
                        ivIcono.setColorFilter(ContextCompat.getColor(this, R.color.rojo_negativo));
                        viewIndicador.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_punto_rojo));
                        break;
                    case "STOCK_BAJO":
                        ivIcono.setImageResource(R.drawable.ct_inventory);
                        ivIcono.setColorFilter(ContextCompat.getColor(this, R.color.amber));
                        viewIndicador.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_punto_amarillo));
                        break;
                    case "VENTA_HOY":
                        ivIcono.setImageResource(R.drawable.ct_shopping_cart);
                        ivIcono.setColorFilter(ContextCompat.getColor(this, R.color.verde_acento));
                        viewIndicador.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_punto_verde));
                        break;
                }

                // Click para ir al detalle
                item.setOnClickListener(v -> {
                    if (tipo.equals("VENTA_HOY") && facturaId > 0) {
                        Intent intent = new Intent(this, VentaDetalleActivity.class);
                        intent.putExtra("factura_id", facturaId);
                        startActivity(intent);
                    } else if (productoId > 0) {
                        Intent intent = new Intent(this, ProductoDetalleActivity.class);
                        intent.putExtra("producto_id", productoId);
                        startActivity(intent);
                    }
                });

                layoutNotificaciones.addView(item);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}