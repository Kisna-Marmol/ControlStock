package com.example.controlstock;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;
import com.example.controlstock.clases.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class VentaDetalleActivity extends AppCompatActivity {

    private ImageView btnVolver;
    private TextView tvNumeroFactura, tvClienteNombre, tvClienteDoc;
    private TextView tvFecha, tvSubtotal, tvImpuesto, tvTotal;
    private LinearLayout layoutProductos;

    private int facturaId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venta_detalle);

        facturaId = getIntent().getIntExtra("factura_id", 0);

        inicializarVistas();
        btnVolver.setOnClickListener(v -> finish());

        if (facturaId > 0) {
            cargarDetalle();
        } else {
            Dialog.toast(this, "Error: factura no encontrada");
            finish();
        }
    }

    private void inicializarVistas() {
        btnVolver        = findViewById(R.id.btnVolver);
        tvNumeroFactura  = findViewById(R.id.tvNumeroFactura);
        tvClienteNombre  = findViewById(R.id.tvClienteNombre);
        tvClienteDoc     = findViewById(R.id.tvClienteDoc);
        tvFecha          = findViewById(R.id.tvFecha);
        tvSubtotal       = findViewById(R.id.tvSubtotal);
        tvImpuesto       = findViewById(R.id.tvImpuesto);
        tvTotal          = findViewById(R.id.tvTotal);
        layoutProductos  = findViewById(R.id.layoutProductos);
    }

    private void cargarDetalle() {
        ApiService.get(Config.local + "ventas/ventas_get_detalle.php?id=" + facturaId,
                new ApiService.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject res = new JSONObject(response);
                            if (res.optBoolean("success", false)) {
                                JSONObject factura  = res.getJSONObject("factura");
                                JSONArray  detalles = res.getJSONArray("detalles");
                                runOnUiThread(() -> mostrarDatos(factura, detalles));
                            }
                        } catch (Exception e) {
                            Log.e("DETALLE_VENTA", e.getMessage());
                            runOnUiThread(() -> Dialog.toast(VentaDetalleActivity.this, "Error cargando detalle"));
                        }
                    }
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> Dialog.toast(VentaDetalleActivity.this, "Error de conexión"));
                    }
                });
    }

    private void mostrarDatos(JSONObject factura, JSONArray detalles) {
        try {
            // Cabecera
            tvNumeroFactura.setText("#" + factura.optString("factura_numero", ""));
            tvClienteNombre.setText(factura.optString("cliente_nombre", ""));
            tvClienteDoc.setText("Doc: " + factura.optString("cliente_documento", ""));
            tvFecha.setText(Utils.formatearFecha(factura.optString("factura_fecha", "")));
            tvSubtotal.setText(String.format("L. %.2f", factura.optDouble("factura_subtotal", 0)));
            tvImpuesto.setText(String.format("L. %.2f", factura.optDouble("factura_total_impuesto", 0)));
            tvTotal.setText(String.format("L. %.2f", factura.optDouble("factura_total", 0)));

            // Productos
            layoutProductos.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(this);

            for (int i = 0; i < detalles.length(); i++) {
                JSONObject d = detalles.getJSONObject(i);

                View item = inflater.inflate(R.layout.item_detalle_venta, layoutProductos, false);

                TextView tvNombre    = item.findViewById(R.id.tvNombreProducto);
                TextView tvCantidad  = item.findViewById(R.id.tvCantidad);
                TextView tvPrecio    = item.findViewById(R.id.tvPrecioUnitario);
                TextView tvSublineal = item.findViewById(R.id.tvSubtotalLineal);

                tvNombre.setText(d.optString("producto_nombre", ""));
                tvCantidad.setText("Cant: " + d.optInt("detalle_cantidad", 0));
                tvPrecio.setText(String.format("L. %.2f c/u", d.optDouble("detalle_precio_venta_unitario", 0)));
                tvSublineal.setText(String.format("L. %.2f", d.optDouble("detalle_subtotal_lineal", 0)));

                // Separador entre items
                if (i < detalles.length() - 1) {
                    View divider = new View(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    params.setMargins(0, 8, 0, 8);
                    divider.setLayoutParams(params);
                    divider.setBackgroundColor(0xFFE5E7EB);
                    layoutProductos.addView(item);
                    layoutProductos.addView(divider);
                } else {
                    layoutProductos.addView(item);
                }
            }

        } catch (Exception e) {
            Log.e("DETALLE_VENTA", "Error mostrando datos: " + e.getMessage());
        }
    }
}