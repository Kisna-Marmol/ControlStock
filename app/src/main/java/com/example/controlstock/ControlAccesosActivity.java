package com.example.controlstock;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ControlAccesosActivity extends AppCompatActivity {

    private ImageView btnVolver;
    private Spinner spinnerUsuarios;
    private LinearLayout layoutAccesos;
    private ProgressBar progressBar;

    private List<String> listaNombresUsuarios = new ArrayList<>();
    private List<Integer> listaIdsUsuarios    = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_accesos);

        inicializarVistas();
        configurarListeners();
        cargarUsuarios();
    }

    private void inicializarVistas() {
        btnVolver       = findViewById(R.id.btnVolver);
        spinnerUsuarios = findViewById(R.id.spinnerUsuarios);
        layoutAccesos   = findViewById(R.id.layoutAccesos);
        progressBar     = findViewById(R.id.progressBar);
    }

    private void configurarListeners() {
        btnVolver.setOnClickListener(v -> finish());

        spinnerUsuarios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    int usuarioId = listaIdsUsuarios.get(position);
                    cargarAccesos(usuarioId);
                } else {
                    layoutAccesos.removeAllViews();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ── Cargar usuarios ───────────────────────────────────
    private void cargarUsuarios() {
        ApiService.get(Config.local + "usuario_list.php", new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject res = new JSONObject(response);
                    if (res.optBoolean("success", false)) {
                        JSONArray data = res.getJSONArray("data");
                        listaNombresUsuarios.clear();
                        listaIdsUsuarios.clear();
                        listaNombresUsuarios.add("Seleccione usuario");
                        listaIdsUsuarios.add(0);
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject u = data.getJSONObject(i);
                            listaNombresUsuarios.add(u.getString("usuario_nombre")
                                    + " (" + u.getString("rol_nombre") + ")");
                            listaIdsUsuarios.add(u.optInt("usuario_id", 0));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                ControlAccesosActivity.this,
                                android.R.layout.simple_spinner_item,
                                listaNombresUsuarios);
                        adapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item);
                        spinnerUsuarios.setAdapter(adapter);
                    }
                } catch (Exception e) {
                    Log.e("ACCESOS", e.getMessage());
                }
            }
            @Override
            public void onError(String error) {
                Dialog.toast(ControlAccesosActivity.this, "Error cargando usuarios");
            }
        });
    }

    // ── Cargar accesos del usuario ────────────────────────
    private void cargarAccesos(int usuarioId) {
        progressBar.setVisibility(View.VISIBLE);
        layoutAccesos.removeAllViews();

        ApiService.get(Config.local + "acceso_list.php?usuario_id=" + usuarioId,
                new ApiService.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject res = new JSONObject(response);
                            progressBar.setVisibility(View.GONE);
                            if (res.optBoolean("success", false)) {
                                JSONArray data = res.getJSONArray("data");
                                renderizarAccesos(data);
                            }
                        } catch (Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Log.e("ACCESOS", e.getMessage());
                        }
                    }
                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        Dialog.toast(ControlAccesosActivity.this, "Error de conexión");
                    }
                });
    }

    // ── Renderizar módulos y accesos ──────────────────────
    private void renderizarAccesos(JSONArray data) {
        layoutAccesos.removeAllViews();

        // Agrupar por módulo
        Map<String, List<JSONObject>> modulosMap = new LinkedHashMap<>();
        try {
            for (int i = 0; i < data.length(); i++) {
                JSONObject item = data.getJSONObject(i);
                String modulo = item.optString("um_modelo_codigo", "");
                if (!modulosMap.containsKey(modulo)) {
                    modulosMap.put(modulo, new ArrayList<>());
                }
                if (!item.optString("acceso_codigo", "").isEmpty()) {
                    modulosMap.get(modulo).add(item);
                }
            }
        } catch (Exception e) {
            Log.e("ACCESOS", e.getMessage());
        }

        // Crear card por cada módulo
        for (Map.Entry<String, List<JSONObject>> entry : modulosMap.entrySet()) {
            String modulo = entry.getKey();
            List<JSONObject> accesos = entry.getValue();

            // Card del módulo
            CardView card = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, dpToPx(12));
            card.setLayoutParams(cardParams);
            card.setRadius(dpToPx(12));
            card.setCardElevation(dpToPx(2));
            card.setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.blanco));

            LinearLayout cardContent = new LinearLayout(this);
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(dpToPx(16), dpToPx(12),
                    dpToPx(16), dpToPx(12));

            // Título del módulo
            TextView tvModulo = new TextView(this);
            tvModulo.setText(modulo);
            tvModulo.setTextColor(ContextCompat.getColor(this, R.color.azul_principal));
            tvModulo.setTextSize(15);
            tvModulo.setTypeface(null, android.graphics.Typeface.BOLD);
            tvModulo.setPadding(0, 0, 0, dpToPx(8));
            cardContent.addView(tvModulo);

            // Switch por cada acceso
            for (JSONObject acceso : accesos) {
                try {
                    String codigo   = acceso.optString("acceso_codigo", "");
                    String nombre   = acceso.optString("acceso_nombre", "");
                    String estado   = acceso.optString("acceso_estado", "INACTIVO");

                    LinearLayout row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dpToPx(44));
                    row.setLayoutParams(rowParams);

                    TextView tvNombre = new TextView(this);
                    LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                    tvNombre.setLayoutParams(tvParams);
                    tvNombre.setText(nombre);
                    tvNombre.setTextColor(ContextCompat.getColor(this, R.color.texto_oscuro));
                    tvNombre.setTextSize(14);

                    Switch switchAcceso = new Switch(this);
                    switchAcceso.setChecked(estado.equals("ACTIVO"));

                    switchAcceso.setOnCheckedChangeListener(
                            (buttonView, isChecked) -> {
                                actualizarAcceso(codigo, isChecked ? "ACTIVO" : "INACTIVO");
                            });

                    row.addView(tvNombre);
                    row.addView(switchAcceso);
                    cardContent.addView(row);

                } catch (Exception e) {
                    Log.e("ACCESOS", "Error item: " + e.getMessage());
                }
            }

            card.addView(cardContent);
            layoutAccesos.addView(card);
        }
    }

    // ── Actualizar estado del acceso ──────────────────────
    private void actualizarAcceso(String codigo, String estado) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            json.put("acceso_codigo", codigo);
            json.put("acceso_estado", estado);

            ApiService.post(Config.local + "acceso_update.php", json,
                    new ApiService.ApiCallback() {
                        @Override
                        public void onSuccess(String response) {
                            Dialog.toast(ControlAccesosActivity.this,
                                    estado.equals("ACTIVO") ? "Acceso habilitado" : "Acceso deshabilitado");
                        }
                        @Override
                        public void onError(String error) {
                            Dialog.toast(ControlAccesosActivity.this, "Error actualizando acceso");
                        }
                    });
        } catch (Exception e) {
            Log.e("ACCESOS", e.getMessage());
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}