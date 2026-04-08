package com.example.controlstock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONObject;

public class DashboardActivity extends AppCompatActivity {

    // ── Vistas ───────────────────────────────────────────
    private LinearLayout btnProducto;
    private TextView tvNombreUsuario;
    private TextView tvTotalProductos, tvStockBajo, tvSinStock, tvCategorias;
    private LinearLayout layoutActividad;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Recibir datos del login
        String nombreUsuario = getIntent().getStringExtra("Nombre_Usuario");
        int userId = getIntent().getIntExtra("User_ID", 0);

        // Guardar para usar en otras pantallas
        Config.usuario = nombreUsuario;
        Config.iduser  = userId;

        inicializarVistas();

        // Mostrar en el header
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
        if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
            tvNombreUsuario.setText(nombreUsuario);
        }

        configurarAccesos();
        cargarEstadisticas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarEstadisticas(); // actualizar al volver de otra pantalla
    }

    private void inicializarVistas() {
        tvNombreUsuario  = findViewById(R.id.tvNombreUsuario);
        btnProducto      = findViewById(R.id.btnAccesoProductos);
        // Tarjetas de resumen — usar los IDs reales de tu XML
        tvTotalProductos = findViewById(R.id.tvProductosValor);  // Productos
        tvStockBajo      = findViewById(R.id.tvIngresosValor);   // Stock bajo
        tvSinStock       = findViewById(R.id.tvVentasValor);     // Sin stock
        tvCategorias     = findViewById(R.id.tvClientesValor);   // Categorías
        layoutActividad  = findViewById(R.id.layoutActividad);   // ← nuevo
    }

    private void configurarAccesos() {
        btnProducto.setOnClickListener(v -> {
            if (Config.tieneAcceso("EMP_PROD_VER") || Config.esAdmin()) {
                startActivity(new Intent(this, ProductoListActivity.class));
            } else {
                Dialog.toast(this, "No tienes acceso a este módulo");
            }
        });

        // Acceso Reportes — puedes conectar después
        LinearLayout btnReportes = findViewById(R.id.btnAccesoReportes);
        if (btnReportes != null) {
            btnReportes.setOnClickListener(v ->
                    Dialog.toast(this, "Módulo de reportes próximamente"));
        }

        // Acceso Config
        // Config — solo admin
        LinearLayout btnConfig = findViewById(R.id.btnAccesoConfig);
        if (btnConfig != null) {
            if (Config.esAdmin()) {
                btnConfig.setOnClickListener(v ->
                        startActivity(new Intent(this, ConfiguracionActivity.class)));
            } else {
                btnConfig.setAlpha(0.4f); // visualmente desactivado
                btnConfig.setOnClickListener(v ->
                        Dialog.toast(this, "Solo el administrador puede acceder"));
            }
        }

        // Acceso Ventas
        LinearLayout btnVentas = findViewById(R.id.btnAccesoVentas);
        if (btnVentas != null) {
            btnVentas.setOnClickListener(v ->
                    Dialog.toast(this, "Módulo de ventas próximamente"));
        }

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                return true;
            } else if (id == R.id.nav_reportes) {
                Dialog.toast(this, "Reportes próximamente");
                return true;
            } else if (id == R.id.nav_notificaciones) {
                Dialog.toast(this, "Notificaciones próximamente");
                return true;
            } else if (id == R.id.nav_config) {
                startActivity(new Intent(this, ConfiguracionActivity.class));
                return true;
            }
            return false;
        });
    }

    // ── Cargar estadísticas desde API ─────────────────────
    private void cargarEstadisticas() {
        ApiService.get(Config.local + "dashboard_stats.php", new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject res = new JSONObject(response);
                    if (res.optBoolean("success", false)) {

                        // Métricas en las tarjetas
                        String totalProductos  = res.optString("total_productos", "0");
                        String stockBajo       = res.optString("stock_bajo", "0");
                        String sinStock        = res.optString("sin_stock", "0");
                        String totalCategorias = res.optString("total_categorias", "0");

                        if (tvTotalProductos != null) tvTotalProductos.setText(totalProductos);
                        if (tvStockBajo != null)      tvStockBajo.setText(stockBajo);
                        if (tvSinStock != null)        tvSinStock.setText(sinStock);
                        if (tvCategorias != null)      tvCategorias.setText(totalCategorias);

                        // Actividad reciente
                        JSONArray recientes = res.getJSONArray("recientes");
                        mostrarActividadReciente(recientes);
                    }
                } catch (Exception e) {
                    Log.e("DASHBOARD", e.getMessage());
                }
            }
            @Override
            public void onError(String error) {
                Log.e("DASHBOARD", "Error: " + error);
            }
        });
    }

    // ── Renderizar actividad reciente ─────────────────────
    private void mostrarActividadReciente(JSONArray recientes) {
        if (layoutActividad == null) return;
        layoutActividad.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < recientes.length(); i++) {
            try {
                JSONObject item = recientes.getJSONObject(i);

                String nombre    = item.optString("producto_nombre", "");
                int stock        = item.optInt("producto_stock", 0);
                String categoria = item.optString("categoria_nombre", "");
                String fecha     = item.optString("producto_fecha_registro", "");

                // Inflar item de actividad
                View itemView = inflater.inflate(
                        R.layout.item_actividad, layoutActividad, false);

                TextView tvTitulo      = itemView.findViewById(R.id.tvActividadTitulo);
                TextView tvSubtitulo   = itemView.findViewById(R.id.tvActividadSubtitulo);
                TextView tvTiempo      = itemView.findViewById(R.id.tvActividadTiempo);
                ImageView ivIcono      = itemView.findViewById(R.id.ivActividadIcono);

                tvTitulo.setText(nombre);
                tvSubtitulo.setText(categoria + " • Stock: " + stock);

                // Mostrar fecha corta
                if (!fecha.isEmpty() && !fecha.equals("null") && fecha.length() >= 10) {
                    tvTiempo.setText(fecha.substring(5, 10)); // MM-DD
                }

                // Color del ícono según stock
                if (stock <= 0) {
                    ivIcono.setImageResource(R.drawable.ct_inventory);
                    ivIcono.setColorFilter(ContextCompat.getColor(this, R.color.rojo_negativo));
                } else if (stock <= 10) {
                    ivIcono.setImageResource(R.drawable.ct_inventory);
                    ivIcono.setColorFilter(ContextCompat.getColor(this, R.color.amber));
                } else {
                    ivIcono.setImageResource(R.drawable.ct_inventory);
                    ivIcono.setColorFilter(ContextCompat.getColor(this, R.color.verde_acento));
                }

                itemView.setOnClickListener(v -> {
                    // Click en actividad reciente — ir a la lista
                    startActivity(new Intent(this, ProductoListActivity.class));
                });

                layoutActividad.addView(itemView);

            } catch (Exception e) {
                Log.e("DASHBOARD", "Error item: " + e.getMessage());
            }
        }
    }
}