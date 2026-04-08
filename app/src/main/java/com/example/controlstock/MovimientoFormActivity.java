package com.example.controlstock;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MovimientoFormActivity extends AppCompatActivity {

    // ── Vistas ───────────────────────────────────────────
    private ImageView btnVolver;
    private LinearLayout btnEntrada, btnSalida;
    private Spinner spinnerProducto;
    private TextView tvStockActual;
    private EditText etCantidad;
    private Button btnRegistrar;

    // ── Datos ─────────────────────────────────────────────
    private String tipoSeleccionado = "ENTRADA";
    private List<String> listaNombresProductos = new ArrayList<>();
    private List<Integer> listaIdsProductos    = new ArrayList<>();
    private List<Integer> listaStockProductos  = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimiento_form);

        inicializarVistas();
        configurarListeners();
        cargarProductos();
    }

    private void inicializarVistas() {
        btnVolver       = findViewById(R.id.btnVolver);
        btnEntrada      = findViewById(R.id.btnEntrada);
        btnSalida       = findViewById(R.id.btnSalida);
        spinnerProducto = findViewById(R.id.spinnerProducto);
        tvStockActual   = findViewById(R.id.tvStockActual);
        etCantidad      = findViewById(R.id.etCantidad);
        btnRegistrar    = findViewById(R.id.btnRegistrar);
    }

    private void configurarListeners() {
        btnVolver.setOnClickListener(v -> finish());

        // Selección tipo ENTRADA
        btnEntrada.setOnClickListener(v -> seleccionarTipo("ENTRADA"));

        // Selección tipo SALIDA
        btnSalida.setOnClickListener(v -> seleccionarTipo("SALIDA"));

        // Cambio de producto — mostrar stock actual
        spinnerProducto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    int stock = listaStockProductos.get(position);
                    tvStockActual.setText(String.valueOf(stock));
                } else {
                    tvStockActual.setText("--");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnRegistrar.setOnClickListener(v -> validarYRegistrar());
    }

    // ── Seleccionar tipo ──────────────────────────────────
    private void seleccionarTipo(String tipo) {
        tipoSeleccionado = tipo;

        if (tipo.equals("ENTRADA")) {
            btnEntrada.setBackgroundResource(R.drawable.bg_btn_tipo_selected);
            btnSalida.setBackgroundResource(R.drawable.bg_btn_tipo_unselected);

            // Cambiar colores del texto e ícono entrada
            ((TextView) btnEntrada.getChildAt(1)).setTextColor(
                    getResources().getColor(R.color.blanco, null));
            ((ImageView) btnEntrada.getChildAt(0)).setColorFilter(
                    getResources().getColor(R.color.blanco, null));

            // Cambiar colores del texto e ícono salida
            ((TextView) btnSalida.getChildAt(1)).setTextColor(
                    getResources().getColor(R.color.texto_secundario, null));
            ((ImageView) btnSalida.getChildAt(0)).setColorFilter(
                    getResources().getColor(R.color.texto_secundario, null));
        } else {
            btnSalida.setBackgroundResource(R.drawable.bg_btn_tipo_selected);
            btnEntrada.setBackgroundResource(R.drawable.bg_btn_tipo_unselected);

            ((TextView) btnSalida.getChildAt(1)).setTextColor(
                    getResources().getColor(R.color.blanco, null));
            ((ImageView) btnSalida.getChildAt(0)).setColorFilter(
                    getResources().getColor(R.color.blanco, null));

            ((TextView) btnEntrada.getChildAt(1)).setTextColor(
                    getResources().getColor(R.color.texto_secundario, null));
            ((ImageView) btnEntrada.getChildAt(0)).setColorFilter(
                    getResources().getColor(R.color.texto_secundario, null));
        }
    }

    // ── Cargar productos ──────────────────────────────────
    private void cargarProductos() {
        ApiService.get(Config.local + "producto_list.php", new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject res = new JSONObject(response);
                    if (res.optBoolean("success", false)) {
                        JSONArray data = res.getJSONArray("data");
                        listaNombresProductos.clear();
                        listaIdsProductos.clear();
                        listaStockProductos.clear();

                        listaNombresProductos.add("Seleccione producto");
                        listaIdsProductos.add(0);
                        listaStockProductos.add(0);

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject p = data.getJSONObject(i);
                            listaNombresProductos.add(p.getString("producto_nombre"));
                            listaIdsProductos.add(p.optInt("producto_id"));
                            listaStockProductos.add(p.optInt("producto_stock", 0));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                MovimientoFormActivity.this,
                                android.R.layout.simple_spinner_item,
                                listaNombresProductos);
                        adapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item);
                        spinnerProducto.setAdapter(adapter);
                    }
                } catch (Exception e) {
                    Log.e("MOVIMIENTO", e.getMessage());
                }
            }
            @Override
            public void onError(String error) {
                Dialog.toast(MovimientoFormActivity.this, "Error cargando productos");
            }
        });
    }

    // ── Validar y registrar ───────────────────────────────
    private void validarYRegistrar() {
        if (spinnerProducto.getSelectedItemPosition() == 0) {
            Dialog.toast(this, "Selecciona un producto");
            return;
        }

        String cantidadStr = etCantidad.getText().toString().trim();
        if (cantidadStr.isEmpty()) {
            Dialog.toast(this, "Ingresa la cantidad");
            etCantidad.requestFocus();
            return;
        }

        int cantidad   = Integer.parseInt(cantidadStr);
        int productoId = listaIdsProductos.get(spinnerProducto.getSelectedItemPosition());
        int stockActual = listaStockProductos.get(spinnerProducto.getSelectedItemPosition());

        if (cantidad <= 0) {
            Dialog.toast(this, "La cantidad debe ser mayor a 0");
            return;
        }

        if (tipoSeleccionado.equals("SALIDA") && cantidad > stockActual) {
            Dialog.toast(this, "Stock insuficiente. Stock actual: " + stockActual);
            return;
        }

        registrarMovimiento(productoId, cantidad);
    }

    // ── Llamar API ────────────────────────────────────────
    private void registrarMovimiento(int productoId, int cantidad) {
        try {
            JSONObject json = new JSONObject();
            json.put("movimiento_tipo",     tipoSeleccionado);
            json.put("movimiento_cantidad", cantidad);
            json.put("producto_id",         productoId);
            json.put("usuario_id",          Config.iduser);

            btnRegistrar.setEnabled(false);
            btnRegistrar.setText("Registrando...");

            ApiService.post(Config.local + "movimiento_insert.php", json,
                    new ApiService.ApiCallback() {
                        @Override
                        public void onSuccess(String response) {
                            try {
                                JSONObject res = new JSONObject(response);
                                if (res.optBoolean("success", false)) {
                                    Dialog.toast(MovimientoFormActivity.this,
                                            res.optString("message", "Movimiento registrado"));
                                    finish();
                                } else {
                                    Dialog.toast(MovimientoFormActivity.this,
                                            "Error: " + res.optString("error"));
                                    resetBoton();
                                }
                            } catch (Exception e) {
                                Dialog.toast(MovimientoFormActivity.this, "Error en respuesta");
                                resetBoton();
                            }
                        }
                        @Override
                        public void onError(String error) {
                            Dialog.toast(MovimientoFormActivity.this, "Error de conexión");
                            resetBoton();
                        }
                    });

        } catch (Exception e) {
            Dialog.toast(this, "Error preparando datos");
        }
    }

    private void resetBoton() {
        btnRegistrar.setEnabled(true);
        btnRegistrar.setText("Registrar movimiento");
    }
}