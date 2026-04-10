package com.example.controlstock;

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

import org.json.JSONArray;
import org.json.JSONObject;
public class BitacoraActivity extends AppCompatActivity {

    // ── Vistas ───────────────────────────────────────────
    private ImageView btnVolver;
    private LinearLayout layoutBitacora, layoutVacio;
    private ProgressBar progressBar;
    private TextView tvContador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitacora);

        inicializarVistas();
        configurarListeners();
        cargarBitacora();
    }

    private void inicializarVistas() {
        btnVolver       = findViewById(R.id.btnVolver);
        layoutBitacora  = findViewById(R.id.layoutBitacora);
        layoutVacio     = findViewById(R.id.layoutVacio);
        progressBar     = findViewById(R.id.progressBar);
        tvContador      = findViewById(R.id.tvContador);
    }

    private void configurarListeners() {
        btnVolver.setOnClickListener(v -> finish());
    }

    // ── Cargar bitácora desde API ─────────────────────────
    private void cargarBitacora() {
        progressBar.setVisibility(View.VISIBLE);
        layoutBitacora.setVisibility(View.GONE);

        ApiService.get(Config.local + "bitacora_list.php", new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("BITACORA_RESPONSE", response);
                runOnUiThread(() -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        if (res.optBoolean("success", false)) {
                            JSONArray data = res.getJSONArray("data");
                            progressBar.setVisibility(View.GONE);
                            layoutBitacora.setVisibility(View.VISIBLE);

                            if (data.length() == 0) {
                                layoutVacio.setVisibility(View.VISIBLE);
                                tvContador.setText("Sin registros");
                                return;
                            }

                            tvContador.setText(data.length() + " registros");
                            renderizarBitacora(data);
                        }
                    } catch (Exception e) {
                        Log.e("BITACORA", e.getMessage());
                        progressBar.setVisibility(View.GONE);
                        Dialog.toast(BitacoraActivity.this, "Error cargando bitácora");
                    }
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Dialog.toast(BitacoraActivity.this, "Error de conexión");
                });
            }
        });
    }

    // ── Renderizar registros ──────────────────────────────
    private void renderizarBitacora(JSONArray data) {
        layoutBitacora.removeAllViews();
        Log.d("BITACORA_RENDER", "Total items: " + data.length());
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject item = data.getJSONObject(i);
                Log.d("BITACORA_RENDER", "Item " + i + ": " + item.optString("bitacora_accion"));

                String accion      = item.optString("bitacora_accion", "");
                String descripcion = item.optString("bitacora_descripcion", "");
                String fecha       = item.optString("bitacora_fecha", "");
                String usuario     = item.optString("usuario_nombre", "Sistema");

                View itemView = inflater.inflate(
                        R.layout.item_bitacora, layoutBitacora, false);

                ImageView ivIcono   = itemView.findViewById(R.id.ivIconoBitacora);
                TextView tvAccion   = itemView.findViewById(R.id.tvAccion);
                TextView tvDesc     = itemView.findViewById(R.id.tvDescripcion);
                TextView tvUsuario  = itemView.findViewById(R.id.tvUsuario);
                TextView tvFecha    = itemView.findViewById(R.id.tvFecha);

                tvAccion.setText(formatearAccion(accion));
                tvDesc.setText(descripcion);
                tvUsuario.setText(usuario);

                // Formatear fecha
                if (!fecha.isEmpty() && !fecha.equals("null") && fecha.length() >= 16) {
                    tvFecha.setText(fecha.substring(5, 10) + "\n" + fecha.substring(11, 16));
                }

                // Ícono y color según acción
                aplicarEstiloAccion(ivIcono, accion);

                layoutBitacora.addView(itemView);

            } catch (Exception e) {
                Log.e("BITACORA", "Error item: " + e.getMessage());
            }
        }
    }

    // ── Formatear nombre de la acción ─────────────────────
    private String formatearAccion(String accion) {
        switch (accion) {
            case "CREAR_PRODUCTO":     return "Producto creado";
            case "EDITAR_PRODUCTO":    return "Producto editado";
            case "ELIMINAR_PRODUCTO":  return "Producto eliminado";
            case "CREAR_CATEGORIA":     return "Categoría creada";
            case "EDITAR_CATEGORIA":    return "Categoría editada";
            case "ELIMINAR_CATEGORIA":  return "Categoría eliminada";
            case "CREAR_PROVEEDOR":     return "Proveedor creado";
            case "EDITAR_PROVEEDOR":    return "Proveedor editado";
            case "ELIMINAR_PROVEEDOR":  return "Proveedor eliminado";
            case "CREAR_UNIDAD":        return "Unidad creada";
            case "EDITAR_UNIDAD":       return "Unidad editada";
            case "ELIMINAR_UNIDAD":     return "Unidad eliminada";
            case "ENTRADA_STOCK":  return "Entrada de stock";
            case "SALIDA_STOCK":   return "Salida de stock";
            case "VENTA":               return "Venta registrada";
            case "LOGIN":              return "Inicio de sesión";
            case "LOGOUT":             return "Cierre de sesión";
            default:                   return accion;
        }
    }

    // ── Ícono y color según tipo de acción ────────────────
    private void aplicarEstiloAccion(ImageView ivIcono, String accion) {
        if (accion.contains("ELIMINAR")) {
            ivIcono.setImageResource(R.drawable.ct_check_circle);
            ivIcono.setColorFilter(ContextCompat.getColor(this, R.color.rojo_negativo));
        } else if (accion.contains("CREAR")) {
            ivIcono.setImageResource(R.drawable.ct_check_circle);
            ivIcono.setColorFilter(ContextCompat.getColor(this, R.color.verde_acento));
        } else if (accion.contains("EDITAR")) {
            ivIcono.setImageResource(R.drawable.ct_check_circle);
            ivIcono.setColorFilter(ContextCompat.getColor(this, R.color.azul_claro));
        } else if (accion.contains("LOGIN")) {
            ivIcono.setImageResource(R.drawable.ct_person);
            ivIcono.setColorFilter(ContextCompat.getColor(this, R.color.amber));
        } else {
            ivIcono.setImageResource(R.drawable.ct_schedule);
            ivIcono.setColorFilter(ContextCompat.getColor(this, R.color.gris_icono));
        }
    }
}