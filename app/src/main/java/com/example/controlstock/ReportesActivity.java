package com.example.controlstock;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;

import org.json.JSONArray;
import org.json.JSONObject;

public class ReportesActivity extends AppCompatActivity {

    private ImageView btnVolver, btnRefrescar;
    private TextView tvTotalProductos, tvTotalVentas, tvTotalClientes;
    private TextView tvValorInventario, tvTotalIngresos;
    private LinearLayout layoutStockBajo, layoutMasVendidos, layoutPorCategoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);

        inicializarVistas();
        configurarListeners();
        cargarReportes();
    }

    private void inicializarVistas() {
        btnVolver           = findViewById(R.id.btnVolver);
        btnRefrescar        = findViewById(R.id.btnRefrescar);
        tvTotalProductos    = findViewById(R.id.tvTotalProductos);
        tvTotalVentas       = findViewById(R.id.tvTotalVentas);
        tvTotalClientes     = findViewById(R.id.tvTotalClientes);
        tvValorInventario   = findViewById(R.id.tvValorInventario);
        tvTotalIngresos     = findViewById(R.id.tvTotalIngresos);
        layoutStockBajo     = findViewById(R.id.layoutStockBajo);
        layoutMasVendidos   = findViewById(R.id.layoutMasVendidos);
        layoutPorCategoria  = findViewById(R.id.layoutPorCategoria);
    }

    private void configurarListeners() {
        btnVolver.setOnClickListener(v -> finish());
        btnRefrescar.setOnClickListener(v -> cargarReportes());
    }

    private void cargarReportes() {
        ApiService.get(Config.local + "reportes.php", new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject res = new JSONObject(response);
                    if (res.optBoolean("success", false)) {

                        // Resumen
                        JSONObject resumen = res.getJSONObject("resumen");
                        tvTotalProductos.setText(resumen.optString("total_productos", "0"));
                        tvTotalVentas.setText(resumen.optString("total_ventas", "0"));
                        tvTotalClientes.setText(resumen.optString("total_clientes", "0"));
                        tvTotalIngresos.setText(String.format("L %.2f",
                                resumen.optDouble("total_ingresos", 0.0)));
                        tvValorInventario.setText(String.format("L %.0f",
                                resumen.optDouble("valor_inventario", 0.0)));

                        // Stock bajo
                        mostrarStockBajo(res.getJSONArray("stock_bajo"));

                        // Más vendidos
                        mostrarMasVendidos(res.getJSONArray("mas_vendidos"));

                        // Por categoría
                        mostrarPorCategoria(res.getJSONArray("por_categoria"));
                    }
                } catch (Exception e) {
                    Log.e("REPORTES", e.getMessage());
                    Dialog.toast(ReportesActivity.this, "Error cargando reportes");
                }
            }
            @Override
            public void onError(String error) {
                Dialog.toast(ReportesActivity.this, "Error de conexión");
            }
        });
    }

    // ── Stock bajo ────────────────────────────────────────
    private void mostrarStockBajo(JSONArray data) {
        layoutStockBajo.removeAllViews();

        if (data.length() == 0) {
            agregarFilaTexto(layoutStockBajo, "✅ Todos los productos tienen stock suficiente",
                    R.color.verde_acento);
            return;
        }

        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject item = data.getJSONObject(i);
                String nombre   = item.optString("producto_nombre", "");
                int stock       = item.optInt("producto_stock", 0);
                String categoria = item.optString("categoria_nombre", "");

                View fila = crearFilaProducto(nombre, categoria,
                        stock + " unidades", stock == 0 ? R.color.rojo_negativo : R.color.amber);
                layoutStockBajo.addView(fila);

                if (i < data.length() - 1) agregarDivisor(layoutStockBajo);

            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // ── Más vendidos ──────────────────────────────────────
    private void mostrarMasVendidos(JSONArray data) {
        layoutMasVendidos.removeAllViews();

        if (data.length() == 0) {
            agregarFilaTexto(layoutMasVendidos, "Sin ventas registradas aún",
                    R.color.texto_secundario);
            return;
        }

        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject item  = data.getJSONObject(i);
                String nombre    = item.optString("producto_nombre", "");
                int vendido      = item.optInt("total_vendido", 0);
                double ingresos  = item.optDouble("total_ingresos", 0.0);

                View fila = crearFilaProducto(
                        (i + 1) + ". " + nombre,
                        String.format("L %.2f en ventas", ingresos),
                        vendido + " vendidos",
                        R.color.verde_acento);
                layoutMasVendidos.addView(fila);

                if (i < data.length() - 1) agregarDivisor(layoutMasVendidos);

            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // ── Por categoría ─────────────────────────────────────
    private void mostrarPorCategoria(JSONArray data) {
        layoutPorCategoria.removeAllViews();

        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject item   = data.getJSONObject(i);
                String categoria  = item.optString("categoria_nombre", "");
                int cantidad      = item.optInt("cantidad_productos", 0);
                int stockTotal    = item.optInt("stock_total", 0);
                double valor      = item.optDouble("valor_total", 0.0);

                View fila = crearFilaProducto(
                        categoria,
                        cantidad + " productos · Stock: " + stockTotal,
                        String.format("L %.0f", valor),
                        R.color.azul_claro);
                layoutPorCategoria.addView(fila);

                if (i < data.length() - 1) agregarDivisor(layoutPorCategoria);

            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // ── Helpers UI ────────────────────────────────────────
    private View crearFilaProducto(String titulo, String subtitulo,
                                   String valor, int colorValor) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 4, 0, 4);
        fila.setLayoutParams(params);
        fila.setPadding(16, 12, 16, 12);

        LinearLayout textos = new LinearLayout(this);
        textos.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textos.setLayoutParams(textParams);

        TextView tvTitulo = new TextView(this);
        tvTitulo.setText(titulo);
        tvTitulo.setTextColor(ContextCompat.getColor(this, R.color.texto_oscuro));
        tvTitulo.setTextSize(14);
        tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvSub = new TextView(this);
        tvSub.setText(subtitulo);
        tvSub.setTextColor(ContextCompat.getColor(this, R.color.texto_secundario));
        tvSub.setTextSize(12);

        textos.addView(tvTitulo);
        textos.addView(tvSub);

        TextView tvValor = new TextView(this);
        tvValor.setText(valor);
        tvValor.setTextColor(ContextCompat.getColor(this, colorValor));
        tvValor.setTextSize(13);
        tvValor.setTypeface(null, android.graphics.Typeface.BOLD);
        tvValor.setGravity(android.view.Gravity.END);

        fila.addView(textos);
        fila.addView(tvValor);

        return fila;
    }

    private void agregarDivisor(LinearLayout layout) {
        View divisor = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.setMargins(16, 0, 16, 0);
        divisor.setLayoutParams(params);
        divisor.setBackgroundColor(ContextCompat.getColor(this, R.color.fondo_gris));
        layout.addView(divisor);
    }

    private void agregarFilaTexto(LinearLayout layout, String texto, int color) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextColor(ContextCompat.getColor(this, color));
        tv.setTextSize(13);
        tv.setPadding(16, 16, 16, 16);
        layout.addView(tv);
    }
}