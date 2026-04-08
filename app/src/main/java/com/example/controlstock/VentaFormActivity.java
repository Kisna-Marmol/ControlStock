package com.example.controlstock;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;
import com.example.controlstock.modelo.CarritoAdapter;
import com.example.controlstock.modelo.Cliente;
import com.example.controlstock.modelo.DetalleVenta;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VentaFormActivity extends AppCompatActivity implements CarritoAdapter.OnCarritoListener {

    // Views
    private EditText    etBuscarCliente, etBuscarProducto;
    private ListView    lvBusquedaClientes, lvBusquedaProductos, lvCarrito;
    private TextView    tvNumeroFactura, tvClienteNombre, tvClienteDoc;
    private TextView    tvCantidadItems, tvSubtotal, tvImpuesto, tvTotal, tvCarritoVacio;
    private View        layoutClienteSeleccionado;

    // Datos
    private Cliente clienteSeleccionado = null;
    private List<DetalleVenta> carrito            = new ArrayList<>();
    private CarritoAdapter    carritoAdapter;
    private String            numeroFactura       = "";

    private static final double ISV        = 0.15;
    private static final String URL_CLIENTES  = Config.local + "clientes/clientes_get.php";
    private static final String URL_PRODUCTOS = Config.local + "productos/productos_get.php";
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
        findViewById(R.id.btnBuscarCliente).setOnClickListener(v ->
                buscarCliente(etBuscarCliente.getText().toString().trim()));
        findViewById(R.id.btnBuscarProducto).setOnClickListener(v ->
                buscarProducto(etBuscarProducto.getText().toString().trim()));
        findViewById(R.id.btnQuitarCliente).setOnClickListener(v -> quitarCliente());
        findViewById(R.id.btnGuardarVenta).setOnClickListener(v -> guardarVenta());

    }

    private void initViews() {
        etBuscarCliente          = findViewById(R.id.etBuscarCliente);
        etBuscarProducto         = findViewById(R.id.etBuscarProducto);
        lvBusquedaClientes       = findViewById(R.id.lvBusquedaClientes);
        lvBusquedaProductos      = findViewById(R.id.lvBusquedaProductos);
        lvCarrito                = findViewById(R.id.lvCarrito);
        tvNumeroFactura          = findViewById(R.id.tvNumeroFactura);
        tvClienteNombre          = findViewById(R.id.tvClienteNombre);
        tvClienteDoc             = findViewById(R.id.tvClienteDoc);
        tvCantidadItems          = findViewById(R.id.tvCantidadItems);
        tvSubtotal               = findViewById(R.id.tvSubtotal);
        tvImpuesto               = findViewById(R.id.tvImpuesto);
        tvTotal                  = findViewById(R.id.tvTotal);
        tvCarritoVacio           = findViewById(R.id.tvCarritoVacio);
        layoutClienteSeleccionado = findViewById(R.id.layoutClienteSeleccionado);
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

    private void buscarCliente(String texto) {
        if (texto.isEmpty()) {
            Dialog.toast(this, "Escribe un nombre o documento");
            return;
        }

        String url = URL_CLIENTES + "?buscar=" + texto;
        ApiService.get(url, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json  = new JSONObject(response);
                    JSONArray  array = json.getJSONArray("data");

                    List<Cliente> resultados = new ArrayList<>();
                    List<String>  etiquetas  = new ArrayList<>();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        Cliente c = new Cliente(
                                obj.getInt("cliente_id"),
                                obj.optString("cliente_documento", ""),
                                obj.optString("cliente_nombre", ""),
                                obj.optString("cliente_telefono", ""),
                                obj.optString("cliente_email", "")
                        );
                        resultados.add(c);
                        etiquetas.add(c.getClienteNombre() + " - " + c.getClienteDocumento());
                    }

                    runOnUiThread(() -> {
                        if (resultados.isEmpty()) {
                            Dialog.toast(VentaFormActivity.this, "No se encontraron clientes");
                            lvBusquedaClientes.setVisibility(View.GONE);
                            return;
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                VentaFormActivity.this,
                                android.R.layout.simple_list_item_1,
                                etiquetas
                        );
                        lvBusquedaClientes.setAdapter(adapter);
                        lvBusquedaClientes.setVisibility(View.VISIBLE);

                        lvBusquedaClientes.setOnItemClickListener((parent, view, position, id) -> {
                            seleccionarCliente(resultados.get(position));
                        });
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Dialog.toast(VentaFormActivity.this, "Error al buscar clientes"));
                }
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> Dialog.toast(VentaFormActivity.this, "Error de conexión"));
            }
        });
    }

    private void seleccionarCliente(Cliente cliente) {
        clienteSeleccionado = cliente;
        tvClienteNombre.setText(cliente.getClienteNombre());
        tvClienteDoc.setText("Doc: " + cliente.getClienteDocumento());
        layoutClienteSeleccionado.setVisibility(View.VISIBLE);
        lvBusquedaClientes.setVisibility(View.GONE);
        etBuscarCliente.setText("");
    }

    private void quitarCliente() {
        clienteSeleccionado = null;
        layoutClienteSeleccionado.setVisibility(View.GONE);
    }

    // ─── PRODUCTO ─────────────────────────────────────────────────────────────

    private void buscarProducto(String texto) {
        if (texto.isEmpty()) {
            Dialog.toast(this, "Escribe un nombre de producto");
            return;
        }

        String url = URL_PRODUCTOS + "?buscar=" + texto;
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
                                + "  |  L. " + obj.getString("producto_precio_venta")
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
        double precio     = producto.getDouble("producto_precio_venta");

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
        if (clienteSeleccionado == null) {
            Dialog.toast(this, "Debe seleccionar un cliente");
            return;
        }
        if (carrito.isEmpty()) {
            Dialog.toast(this, "El carrito está vacío");
            return;
        }

        Dialog.confirm(
                this,
                "Confirmar venta",
                "¿Desea guardar la venta #" + numeroFactura + " por " + tvTotal.getText() + "?",
                android.R.drawable.ic_dialog_info,
                new Dialog.ConfirmationDialogCallback() {
                    @Override
                    public void onConfirm() { procesarVenta(); }
                    @Override
                    public void onCancel()  { }
                }
        );
    }

    private void procesarVenta() {
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
            body.put("cliente_id",             clienteSeleccionado.getClienteId());
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