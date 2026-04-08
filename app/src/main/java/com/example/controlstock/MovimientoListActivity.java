package com.example.controlstock;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.controlstock.clases.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MovimientoListActivity extends AppCompatActivity {

    // ── Vistas ───────────────────────────────────────────
    private ImageView btnVolver;
    private TextView chipTodos, chipEntrada, chipSalida;
    private LinearLayout layoutMovimientos, layoutVacio;
    private ProgressBar progressBar;
    private TextView tvContador;
    private FloatingActionButton fabAjuste;

    // ── Datos ─────────────────────────────────────────────
    private List<JSONObject> listaCompleta = new ArrayList<>();
    private String filtroActual = "TODOS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimiento_list);

        inicializarVistas();
        configurarListeners();
        cargarMovimientos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMovimientos();
    }

    private void inicializarVistas() {
        btnVolver        = findViewById(R.id.btnVolver);
        chipTodos        = findViewById(R.id.chipTodos);
        chipEntrada      = findViewById(R.id.chipEntrada);
        chipSalida       = findViewById(R.id.chipSalida);
        layoutMovimientos = findViewById(R.id.layoutMovimientos);
        layoutVacio      = findViewById(R.id.layoutVacio);
        progressBar      = findViewById(R.id.progressBar);
        tvContador       = findViewById(R.id.tvContador);
        fabAjuste        = findViewById(R.id.fabAjuste);
    }

    private void configurarListeners() {
        btnVolver.setOnClickListener(v -> finish());

        chipTodos.setOnClickListener(v -> seleccionarFiltro("TODOS"));
        chipEntrada.setOnClickListener(v -> seleccionarFiltro("ENTRADA"));
        chipSalida.setOnClickListener(v -> seleccionarFiltro("SALIDA"));

        // FAB — ajuste manual (solo admin)
        fabAjuste.setOnClickListener(v -> {
            if (Config.esAdmin()) {
                startActivity(new Intent(this, MovimientoFormActivity.class));
            } else {
                Dialog.toast(this, "Solo el administrador puede hacer ajustes manuales");
            }
        });
    }

    // ── Seleccionar filtro ────────────────────────────────
    private void seleccionarFiltro(String filtro) {
        filtroActual = filtro;

        // Resetear todos
        chipTodos.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_unselected));
        chipTodos.setTextColor(ContextCompat.getColor(this, R.color.blanco));
        chipEntrada.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_unselected));
        chipEntrada.setTextColor(ContextCompat.getColor(this, R.color.blanco));
        chipSalida.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_unselected));
        chipSalida.setTextColor(ContextCompat.getColor(this, R.color.blanco));

        // Marcar seleccionado
        TextView chipSeleccionado = filtro.equals("TODOS") ? chipTodos :
                filtro.equals("ENTRADA") ? chipEntrada : chipSalida;
        chipSeleccionado.setBackground(
                ContextCompat.getDrawable(this, R.drawable.bg_chip_selected));
        chipSeleccionado.setTextColor(
                ContextCompat.getColor(this, R.color.azul_principal));

        filtrarMovimientos();
    }

    // ── Cargar movimientos desde API ──────────────────────
    private void cargarMovimientos() {
        progressBar.setVisibility(View.VISIBLE);
        layoutMovimientos.setVisibility(View.GONE);

        ApiService.get(Config.local + "movimiento_list.php", new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject res = new JSONObject(response);
                    progressBar.setVisibility(View.GONE);
                    layoutMovimientos.setVisibility(View.VISIBLE);

                    if (res.optBoolean("success", false)) {
                        JSONArray data = res.getJSONArray("data");
                        listaCompleta.clear();
                        for (int i = 0; i < data.length(); i++)
                            listaCompleta.add(data.getJSONObject(i));
                        filtrarMovimientos();
                    }
                } catch (Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("MOVIMIENTO", e.getMessage());
                }
            }
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Dialog.toast(MovimientoListActivity.this, "Error de conexión");
            }
        });
    }

    // ── Filtrar en memoria ────────────────────────────────
    private void filtrarMovimientos() {
        List<JSONObject> filtrados = new ArrayList<>();
        for (JSONObject m : listaCompleta) {
            String tipo = m.optString("movimiento_tipo", "");
            if (filtroActual.equals("TODOS") || tipo.equals(filtroActual)) {
                filtrados.add(m);
            }
        }
        renderizarMovimientos(filtrados);
    }

    // ── Renderizar lista ──────────────────────────────────
    private void renderizarMovimientos(List<JSONObject> movimientos) {
        layoutMovimientos.removeAllViews();

        if (movimientos.isEmpty()) {
            layoutVacio.setVisibility(View.VISIBLE);
            tvContador.setText("Sin movimientos");
            return;
        }

        layoutVacio.setVisibility(View.GONE);
        tvContador.setText(movimientos.size() + " movimiento" +
                (movimientos.size() != 1 ? "s" : ""));

        LayoutInflater inflater = LayoutInflater.from(this);

        for (JSONObject m : movimientos) {
            try {
                View itemView = inflater.inflate(
                        R.layout.item_movimiento, layoutMovimientos, false);

                ImageView ivIcono       = itemView.findViewById(R.id.ivTipoIcono);
                TextView tvNombre       = itemView.findViewById(R.id.tvProductoNombre);
                TextView tvTipo         = itemView.findViewById(R.id.tvTipo);
                TextView tvFecha        = itemView.findViewById(R.id.tvFecha);
                TextView tvCantidad     = itemView.findViewById(R.id.tvCantidad);
                TextView tvStock        = itemView.findViewById(R.id.tvStockResultante);

                String tipo     = m.optString("movimiento_tipo", "");
                String nombre   = m.optString("producto_nombre", "Producto");
                int cantidad    = m.optInt("movimiento_cantidad", 0);
                int stock       = m.optInt("producto_stock", 0);
                String fecha    = m.optString("movimiento_fecha", "");

                tvNombre.setText(nombre);
                tvTipo.setText(tipo);
                tvStock.setText("Stock: " + stock);

                // Cantidad con signo
                if (tipo.equals("ENTRADA")) {
                    tvCantidad.setText("+" + cantidad);
                    tvCantidad.setTextColor(
                            ContextCompat.getColor(this, R.color.verde_acento));
                    tvTipo.setTextColor(
                            ContextCompat.getColor(this, R.color.verde_acento));
                    ivIcono.setImageResource(R.drawable.ct_trending_up);
                    ivIcono.setColorFilter(
                            ContextCompat.getColor(this, R.color.verde_acento));
                } else {
                    tvCantidad.setText("-" + cantidad);
                    tvCantidad.setTextColor(
                            ContextCompat.getColor(this, R.color.rojo_negativo));
                    tvTipo.setTextColor(
                            ContextCompat.getColor(this, R.color.rojo_negativo));
                    ivIcono.setImageResource(R.drawable.ct_trending_up);
                    ivIcono.setColorFilter(
                            ContextCompat.getColor(this, R.color.rojo_negativo));
                    ivIcono.setRotation(180f);
                }

                // Formatear fecha
                tvFecha.setText(Utils.formatearFecha(fecha));

                layoutMovimientos.addView(itemView);

            } catch (Exception e) {
                Log.e("MOVIMIENTO", "Error item: " + e.getMessage());
            }
        }
    }
}