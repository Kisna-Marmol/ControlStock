package com.example.controlstock;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;

import org.json.JSONObject;


public class ProductoDetalleActivity extends AppCompatActivity {

    // ── Vistas ───────────────────────────────────────────
    private ImageView ivFotoProducto, btnVolver, btnEditar;
    private TextView tvNombreProducto, tvDescripcion, tvBadgeCategoria;
    private TextView tvStock, tvUnidad, tvFecha;
    private TextView tvProveedor, tvCodigoQr, tvCoordenadas;
    private Button btnVerMapa, btnEliminar, btnEditarDetalle;

    // ── Datos ─────────────────────────────────────────────
    private int productoId = 0;
    private double latitud = 0, longitud = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_detalle);

        productoId = getIntent().getIntExtra("producto_id", 0);

        inicializarVistas();
        configurarListeners();

        if (productoId > 0) {
            cargarDetalle();
        } else {
            Dialog.toast(this, "Error: producto no encontrado");
            finish();
        }
    }

    private void inicializarVistas() {
        ivFotoProducto   = findViewById(R.id.ivFotoProducto);
        btnVolver        = findViewById(R.id.btnVolver);
        btnEditar        = findViewById(R.id.btnEditar);
        tvNombreProducto = findViewById(R.id.tvNombreProducto);
        tvDescripcion    = findViewById(R.id.tvDescripcion);
        tvBadgeCategoria = findViewById(R.id.tvBadgeCategoria);
        tvStock          = findViewById(R.id.tvStock);
        tvUnidad         = findViewById(R.id.tvUnidad);
        tvFecha          = findViewById(R.id.tvFecha);
        tvProveedor      = findViewById(R.id.tvProveedor);
        tvCodigoQr       = findViewById(R.id.tvCodigoQr);
        tvCoordenadas    = findViewById(R.id.tvCoordenadas);
        btnVerMapa       = findViewById(R.id.btnVerMapa);
        btnEliminar      = findViewById(R.id.btnEliminar);
        btnEditarDetalle = findViewById(R.id.btnEditarDetalle);
    }

    private void configurarListeners() {
        btnVolver.setOnClickListener(v -> finish());

        // Editar desde el header
        btnEditar.setOnClickListener(v -> irAEditar());

        // Editar desde el botón inferior
        btnEditarDetalle.setOnClickListener(v -> irAEditar());

        // Ver en mapa
        btnVerMapa.setOnClickListener(v -> {
            if (latitud != 0 && longitud != 0) {
                Uri gmmIntentUri = Uri.parse("geo:" + latitud + "," + longitud
                        + "?q=" + latitud + "," + longitud + "(Producto)");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // Si no tiene Google Maps abrir en navegador
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://maps.google.com/?q=" + latitud + "," + longitud));
                    startActivity(browserIntent);
                }
            } else {
                Dialog.toast(this, "Este producto no tiene ubicación registrada");
            }
        });

        // Eliminar
        btnEliminar.setOnClickListener(v -> {
            Dialog.confirm(this,
                    "Eliminar producto",
                    "¿Estás seguro que deseas eliminar este producto? Esta acción no se puede deshacer.",
                    R.drawable.error,
                    new com.example.controlstock.clases.Dialog.ConfirmationDialogCallback() {
                        @Override
                        public void onConfirm() {
                            eliminarProducto();
                        }
                        @Override
                        public void onCancel() {}
                    });
        });
    }

    // ── Cargar detalle desde API ──────────────────────────
    private void cargarDetalle() {
        ApiService.get(Config.local + "producto_get.php?id=" + productoId,
                new ApiService.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject res = new JSONObject(response);
                            if (res.optBoolean("success", false)) {
                                JSONObject p = res.getJSONObject("data");
                                mostrarDatos(p);
                            } else {
                                Dialog.toast(ProductoDetalleActivity.this, "Producto no encontrado");
                                finish();
                            }
                        } catch (Exception e) {
                            Log.e("DETALLE", e.getMessage());
                            Dialog.toast(ProductoDetalleActivity.this, "Error cargando detalle");
                        }
                    }
                    @Override
                    public void onError(String error) {
                        Dialog.toast(ProductoDetalleActivity.this, "Error de conexión");
                    }
                });
    }

    // ── Mostrar datos en pantalla ─────────────────────────
    private void mostrarDatos(JSONObject p) {
        try {
            String nombre      = p.optString("producto_nombre", "Sin nombre");
            String descripcion = p.optString("producto_descripcion", "Sin descripción");
            String categoria   = p.optString("categoria_nombre", "Sin categoría");
            int stock          = p.optInt("producto_stock", 0);
            String unidad      = p.optString("um_abreviatura", "und");
            String proveedor   = p.optString("proveedor_nombre", "Sin proveedor");
            String codigoQr    = p.optString("producto_codigo_qr", "");
            String fecha       = p.optString("producto_fecha_registro", "");
            String fotoNombre  = p.optString("producto_foto", "");
            String lat         = p.optString("producto_latitud", "");
            String lng         = p.optString("producto_longitud", "");

            // Textos
            tvNombreProducto.setText(nombre);
            tvDescripcion.setText(descripcion.isEmpty() ? "Sin descripción" : descripcion);
            tvBadgeCategoria.setText(categoria);
            tvStock.setText(String.valueOf(stock));
            tvUnidad.setText(unidad);
            tvProveedor.setText(proveedor);
            tvCodigoQr.setText(codigoQr.isEmpty() ? "Sin código QR" : codigoQr);

            // Fecha — mostrar solo la parte de la fecha
            if (!fecha.isEmpty() && !fecha.equals("null")) {
                try {
                    // Convertir "2026-04-01 04:47:15" a "01/04/2026 04:47"
                    java.text.SimpleDateFormat formatoEntrada =
                            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                    java.text.SimpleDateFormat formatoSalida =
                            new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                    java.util.Date date = formatoEntrada.parse(fecha);
                    tvFecha.setText(formatoSalida.format(date));
                } catch (Exception e) {
                    tvFecha.setText(fecha.substring(0, 10));
                }
            } else {
                tvFecha.setText("--");
            }

            // GPS
            if (!lat.isEmpty() && !lat.equals("null")
                    && !lng.isEmpty() && !lng.equals("null")) {
                latitud  = Double.parseDouble(lat);
                longitud = Double.parseDouble(lng);
                tvCoordenadas.setText(String.format("%.4f, %.4f", latitud, longitud));
                btnVerMapa.setVisibility(View.VISIBLE);
            } else {
                tvCoordenadas.setText("Sin ubicación registrada");
                btnVerMapa.setVisibility(View.GONE);
            }

            // Foto
            if (!fotoNombre.isEmpty() && !fotoNombre.equals("null")) {
                String urlFoto = Config.local + "uploads/productos/" + fotoNombre;
                Glide.with(this)
                        .load(urlFoto)
                        .centerCrop()
                        .placeholder(R.drawable.ct_inventory)
                        .error(R.drawable.ct_inventory)
                        .into(ivFotoProducto);
            }

        } catch (Exception e) {
            Log.e("DETALLE", "Error mostrando datos: " + e.getMessage());
        }
    }

    // ── Ir a editar ───────────────────────────────────────
    private void irAEditar() {
        Intent intent = new Intent(this, ProductoFormActivity.class);
        intent.putExtra("modo", "editar");
        intent.putExtra("producto_id", productoId);
        startActivity(intent);
    }

    // ── Eliminar producto ─────────────────────────────────
    private void eliminarProducto() {
        ApiService.get(Config.local + "producto_delete.php?id=" + productoId
                        + "&usuario_id=" + Config.iduser,
                new ApiService.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject res = new JSONObject(response);
                            if (res.optBoolean("success", false)) {
                                Dialog.toast(ProductoDetalleActivity.this, "Producto eliminado");
                                finish();
                            } else {
                                Dialog.toast(ProductoDetalleActivity.this,
                                        "Error: " + res.optString("error"));
                            }
                        } catch (Exception e) {
                            Dialog.toast(ProductoDetalleActivity.this, "Error en respuesta");
                        }
                    }
                    @Override
                    public void onError(String error) {
                        Dialog.toast(ProductoDetalleActivity.this, "Error de conexión");
                    }
                });
    }
}