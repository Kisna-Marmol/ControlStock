package com.example.controlstock;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;
import com.example.controlstock.clases.CarritoAdapter;
import com.example.controlstock.modelo.Cliente;
import com.example.controlstock.modelo.DetalleVenta;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VentaFormActivity extends AppCompatActivity implements CarritoAdapter.OnCarritoListener {

    // Views
    private EditText    etBuscarCliente, etBuscarProducto, etNombreCliente, etDocumentoCliente;
    private ListView    lvBusquedaProductos, lvCarrito;
    private TextView    tvNumeroFactura;
    private TextView    tvCantidadItems, tvSubtotal, tvImpuesto, tvTotal, tvCarritoVacio;

    // Datos
    private List<DetalleVenta> carrito            = new ArrayList<>();
    private CarritoAdapter    carritoAdapter;
    private String            numeroFactura       = "";

    private static final double ISV        = 0.15;
    private static final String URL_PRODUCTOS = Config.local + "producto_list.php";
    private static final String URL_NUMERO    = Config.local + "ventas/ventas_get_numero.php";
    private static final String URL_POST      = Config.local + "ventas/ventas_post.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venta_form);

        initViews();
        setupCarrito();
        cargarNumeroFactura();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnBuscarProducto).setOnClickListener(v ->
                buscarProducto(etBuscarProducto.getText().toString().trim()));
        findViewById(R.id.btnGuardarVenta).setOnClickListener(v -> guardarVenta());

    }

    private void initViews() {
        etNombreCliente    = findViewById(R.id.etNombreCliente);
        etDocumentoCliente = findViewById(R.id.etDocumentoCliente);
        etBuscarProducto         = findViewById(R.id.etBuscarProducto);
        lvBusquedaProductos      = findViewById(R.id.lvBusquedaProductos);
        lvCarrito                = findViewById(R.id.lvCarrito);
        tvNumeroFactura          = findViewById(R.id.tvNumeroFactura);
        tvCantidadItems          = findViewById(R.id.tvCantidadItems);
        tvSubtotal               = findViewById(R.id.tvSubtotal);
        tvImpuesto               = findViewById(R.id.tvImpuesto);
        tvTotal                  = findViewById(R.id.tvTotal);
        tvCarritoVacio           = findViewById(R.id.tvCarritoVacio);
    }

    private void setupCarrito() {
        carritoAdapter = new CarritoAdapter(this, carrito, this);
        lvCarrito.setAdapter(carritoAdapter);
    }

    // ─── NÚMERO FACTURA ───────────────────────────────────────────────────────

    private void cargarNumeroFactura() {
        ApiService.get(URL_NUMERO, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("success")) {
                        numeroFactura = json.getString("numero");
                        runOnUiThread(() -> tvNumeroFactura.setText("#" + numeroFactura));
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> Dialog.toast(VentaFormActivity.this, "Error al cargar número de factura"));
                }
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> Dialog.toast(VentaFormActivity.this, "Error de conexión"));
            }
        });
    }

    // ─── CLIENTE ──────────────────────────────────────────────────────────────



    // ─── PRODUCTO ─────────────────────────────────────────────────────────────

    private void buscarProducto(String texto) {
        if (texto.isEmpty()) {
            Dialog.toast(this, "Escribe un nombre de producto");
            return;
        }

        String url = URL_PRODUCTOS + "?busqueda=" + texto;
        ApiService.get(url, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json  = new JSONObject(response);
                    JSONArray  array = json.getJSONArray("data");

                    List<JSONObject> resultados = new ArrayList<>();
                    List<String>     etiquetas  = new ArrayList<>();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        resultados.add(obj);
                        etiquetas.add(obj.getString("producto_nombre")
                                + "  |  L. " + obj.optString("producto_precio", "0.00")
                                + "  |  Stock: " + obj.getString("producto_stock"));
                    }

                    runOnUiThread(() -> {
                        if (resultados.isEmpty()) {
                            Dialog.toast(VentaFormActivity.this, "No se encontraron productos");
                            lvBusquedaProductos.setVisibility(View.GONE);
                            return;
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                VentaFormActivity.this,
                                android.R.layout.simple_list_item_1,
                                etiquetas
                        );
                        lvBusquedaProductos.setAdapter(adapter);
                        lvBusquedaProductos.setVisibility(View.VISIBLE);

                        lvBusquedaProductos.setOnItemClickListener((parent, view, position, id) -> {
                            try {
                                agregarAlCarrito(resultados.get(position));
                            } catch (Exception e) {
                                Dialog.toast(VentaFormActivity.this, "Error al agregar producto");
                            }
                        });
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Dialog.toast(VentaFormActivity.this, "Error al buscar productos"));
                }
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> Dialog.toast(VentaFormActivity.this, "Error de conexión"));
            }
        });
    }

    private void agregarAlCarrito(JSONObject producto) throws Exception {
        int    productoId = producto.getInt("producto_id");
        String nombre     = producto.getString("producto_nombre");
        double precio     = producto.optDouble("producto_precio", 0.0);

        // Si ya existe en el carrito, solo suma cantidad
        for (DetalleVenta item : carrito) {
            if (item.getProductoId() == productoId) {
                item.setCantidad(item.getCantidad() + 1);
                carritoAdapter.notifyDataSetChanged();
                actualizarTotales();
                lvBusquedaProductos.setVisibility(View.GONE);
                etBuscarProducto.setText("");
                return;
            }
        }

        // Producto nuevo
        carrito.add(new DetalleVenta(productoId, nombre, precio, 1));
        carritoAdapter.notifyDataSetChanged();
        actualizarTotales();
        lvBusquedaProductos.setVisibility(View.GONE);
        etBuscarProducto.setText("");
    }

    // ─── CARRITO ──────────────────────────────────────────────────────────────

    @Override
    public void onCantidadCambiada() {
        actualizarTotales();
    }

    @Override
    public void onEliminarItem(int position) {
        carrito.remove(position);
        carritoAdapter.notifyDataSetChanged();
        actualizarTotales();
    }

    private void actualizarTotales() {
        double subtotal = 0;
        for (DetalleVenta item : carrito) {
            subtotal += item.getSubtotalLineal();
        }

        double impuesto = subtotal * ISV;
        double total    = subtotal + impuesto;

        tvSubtotal.setText(String.format("L. %.2f", subtotal));
        tvImpuesto.setText(String.format("L. %.2f", impuesto));
        tvTotal.setText(String.format("L. %.2f", total));
        tvCantidadItems.setText(carrito.size() + " item(s)");

        boolean vacio = carrito.isEmpty();
        lvCarrito.setVisibility(vacio ? View.GONE : View.VISIBLE);
        tvCarritoVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
    }

    // ─── GUARDAR VENTA ────────────────────────────────────────────────────────

    private void guardarVenta() {
        String nombreCliente = etNombreCliente.getText().toString().trim();
        String docCliente    = etDocumentoCliente.getText().toString().trim();

        if (nombreCliente.isEmpty()) {
            Dialog.toast(this, "Ingresa el nombre del cliente");
            return;
        }
        if (docCliente.isEmpty()) {
            Dialog.toast(this, "Ingresa el documento del cliente");
            return;
        }
        if (carrito.isEmpty()) {
            Dialog.toast(this, "El carrito está vacío");
            return;
        }
        procesarVenta(nombreCliente, docCliente);
    }

    private void procesarVenta(String nombreCliente, String docCliente) {
        try {
            double subtotal = 0;
            for (DetalleVenta item : carrito) subtotal += item.getSubtotalLineal();
            double impuesto = subtotal * ISV;
            double total    = subtotal + impuesto;

            // Armar detalles
            JSONArray detallesJson = new JSONArray();
            for (DetalleVenta item : carrito) {
                JSONObject d = new JSONObject();
                d.put("producto_id",    item.getProductoId());
                d.put("cantidad",       item.getCantidad());
                d.put("precio_unitario", item.getPrecioUnitario());
                detallesJson.put(d);
            }

            // Armar body
            JSONObject body = new JSONObject();
            body.put("factura_numero",         numeroFactura);
            body.put("factura_subtotal",        subtotal);
            body.put("factura_total_impuesto",  impuesto);
            body.put("factura_total",           total);
            body.put("cliente_nombre",    nombreCliente);
            body.put("cliente_documento", docCliente);
            body.put("usuario_id",             Config.iduser);
            body.put("detalles",               detallesJson);

            ApiService.post(URL_POST, body, new ApiService.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject json = new JSONObject(response);
                        runOnUiThread(() -> {
                            Dialog.toast(VentaFormActivity.this, json.optString("message"));
                            if (json.optBoolean("success")) {
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Dialog.toast(VentaFormActivity.this, "Error al procesar respuesta"));
                    }
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Dialog.toast(VentaFormActivity.this, "Error de conexión"));
                }
            });

        } catch (Exception e) {
            Dialog.toast(this, "Error al preparar la venta");
        }
    }
}