package com.example.controlstock;

import static com.example.controlstock.clases.Utils.dpToPx;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;
import com.example.controlstock.clases.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProductoListActivity extends AppCompatActivity {

    // ── Vistas ───────────────────────────────────────────
    private FloatingActionButton fabAgregar;
    private EditText etBuscar;
    private LinearLayout layoutProductos, layoutVacio, layoutFiltros;
    private ProgressBar progressBar;
    private TextView tvContador;
    private ImageView btnVolver, btnQrScan;


    // ── Datos ─────────────────────────────────────────────
    private List<JSONObject> listaCompleta       = new ArrayList<>();
    private String categoriaSeleccionada         = "Todos";
    private static final int QR_REQUEST_CODE     = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_list);

        inicializarVistas();
        configurarListeners();
        cargarCategorias();
        cargarProductos();
    }

    private void inicializarVistas() {
        fabAgregar      = findViewById(R.id.fabAgregar);
        etBuscar        = findViewById(R.id.etBuscar);
        layoutProductos = findViewById(R.id.layoutProductos);
        layoutVacio     = findViewById(R.id.layoutVacio);
        layoutFiltros   = findViewById(R.id.layoutFiltros);
        progressBar     = findViewById(R.id.progressBar);
        tvContador      = findViewById(R.id.tvContador);
        btnVolver       = findViewById(R.id.btnVolver);
        btnQrScan       = findViewById(R.id.btnQrScan);
    }

    private void configurarListeners() {
        fabAgregar.setOnClickListener(v -> {
            startActivity(new Intent(this, ProductoFormActivity.class));
        });

        btnVolver.setOnClickListener(v -> finish());

        btnQrScan.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("Escanea el código QR del producto");
            integrator.setBeepEnabled(true);
            integrator.initiateScan();
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                filtrarProductos(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Chip Todos
        TextView chipTodos = findViewById(R.id.chipTodos);
        chipTodos.setOnClickListener(v -> seleccionarCategoria("Todos", chipTodos));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarProductos(); // recargar al volver del form
    }

    // ── QR Result ─────────────────────────────────────────
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult resultado = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (resultado != null && resultado.getContents() != null) {
            etBuscar.setText(resultado.getContents());
        }
    }

    // ── Cargar categorías ─────────────────────────────────
    private void cargarCategorias() {
        ApiService.get(Config.local + "categoria_list.php", new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject res = new JSONObject(response);
                    if (res.optBoolean("success", false)) {
                        JSONArray data = res.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject cat = data.getJSONObject(i);
                            String nombre = cat.getString("categoria_nombre");
                            agregarChip(nombre);
                        }
                    }
                } catch (Exception e) {
                    Log.e("CATEGORIAS", e.getMessage());
                }
            }
            @Override
            public void onError(String error) {}
        });
    }

    private void agregarChip(String nombre) {
        TextView chip = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, Utils.dpToPx(this,34));
        params.setMarginEnd(Utils.dpToPx(this,8));
        chip.setLayoutParams(params);
        chip.setText(nombre);
        chip.setTextColor(ContextCompat.getColor(this, R.color.blanco));
        chip.setTextSize(13f);
        chip.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_unselected));
        chip.setPadding(Utils.dpToPx(this,16), 0, Utils.dpToPx(this,16), 0);
        chip.setGravity(android.view.Gravity.CENTER);
        chip.setOnClickListener(v -> seleccionarCategoria(nombre, chip));
        layoutFiltros.addView(chip);
    }

    private void seleccionarCategoria(String nombre, TextView chipSeleccionado) {
        categoriaSeleccionada = nombre;
        for (int i = 0; i < layoutFiltros.getChildCount(); i++) {
            View child = layoutFiltros.getChildAt(i);
            if (child instanceof TextView) {
                TextView chip = (TextView) child;
                chip.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_unselected));
                chip.setTextColor(ContextCompat.getColor(this, R.color.blanco));
                chip.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
        chipSeleccionado.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_selected));
        chipSeleccionado.setTextColor(ContextCompat.getColor(this, R.color.azul_principal));
        chipSeleccionado.setTypeface(null, android.graphics.Typeface.BOLD);
        filtrarProductos(etBuscar.getText().toString().trim());
    }

    // ── Cargar productos desde API ────────────────────────
    private void cargarProductos() {
        mostrarLoading(true);
        ApiService.get(Config.local + "producto_list.php", new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject res = new JSONObject(response);
                    if (res.optBoolean("success", false)) {
                        JSONArray data = res.getJSONArray("data");
                        listaCompleta.clear();
                        for (int i = 0; i < data.length(); i++)
                            listaCompleta.add(data.getJSONObject(i));
                        mostrarLoading(false);
                        renderizarProductos(listaCompleta);
                    }
                } catch (Exception e) {
                    Log.e("PRODUCTO_LIST", e.getMessage());
                    mostrarLoading(false);
                }
            }
            @Override
            public void onError(String error) {
                mostrarLoading(false);
                Dialog.toast(ProductoListActivity.this, "Error de conexión");
            }
        });
    }

    // ── Filtrar en memoria ────────────────────────────────
    private void filtrarProductos(String query) {
        List<JSONObject> filtrados = new ArrayList<>();
        for (JSONObject p : listaCompleta) {
            try {
                String nombre    = p.optString("producto_nombre", "").toLowerCase();
                String codigo    = p.optString("producto_codigo_qr", "").toLowerCase();
                String categoria = p.optString("categoria_nombre", "");

                boolean coincideBusqueda = query.isEmpty()
                        || nombre.contains(query.toLowerCase())
                        || codigo.contains(query.toLowerCase());

                boolean coincideCategoria = categoriaSeleccionada.equals("Todos")
                        || categoria.equalsIgnoreCase(categoriaSeleccionada);

                if (coincideBusqueda && coincideCategoria) filtrados.add(p);
            } catch (Exception e) { e.printStackTrace(); }
        }
        renderizarProductos(filtrados);
    }

    // ── Renderizar tarjetas ───────────────────────────────
    private void renderizarProductos(List<JSONObject> productos) {
        layoutProductos.removeAllViews();

        if (productos.isEmpty()) {
            layoutVacio.setVisibility(View.VISIBLE);
            tvContador.setText("0 productos encontrados");
            return;
        }

        layoutVacio.setVisibility(View.GONE);
        int total = productos.size();
        tvContador.setText(total + " producto" + (total != 1 ? "s" : "") + " encontrado" + (total != 1 ? "s" : ""));

        LayoutInflater inflater = LayoutInflater.from(this);

        for (JSONObject producto : productos) {
            try {
                View itemView = inflater.inflate(R.layout.item_producto, layoutProductos, false);

                ImageView ivFoto          = itemView.findViewById(R.id.ivFotoProducto);
                TextView tvNombre         = itemView.findViewById(R.id.tvNombreProducto);
                TextView tvBadgeCategoria = itemView.findViewById(R.id.tvBadgeCategoria);
                TextView tvStock          = itemView.findViewById(R.id.tvStock);
                TextView tvPrecio         = itemView.findViewById(R.id.tvPrecio);
                View viewPuntoStock       = itemView.findViewById(R.id.viewPuntoStock);
                ImageView btnEditar       = itemView.findViewById(R.id.btnEditar);

                int productoId   = producto.optInt("producto_id");
                String nombre    = producto.optString("producto_nombre", "Sin nombre");
                String categoria = producto.optString("categoria_nombre", "Sin categoría");
                int stock        = producto.optInt("producto_stock", 0);
                String fotoNombre = producto.optString("producto_foto", "");

                tvNombre.setText(nombre);
                tvBadgeCategoria.setText(categoria);

                double precio = producto.optDouble("producto_precio", 0.0);
                if (tvPrecio != null) {
                    tvPrecio.setText(String.format("L%.2f", precio));
                    tvPrecio.setVisibility(View.VISIBLE);
                }

                //aplicarEstadoStock(tvStock, viewPuntoStock, stock);
                Utils.aplicarEstadoStock(this, tvStock, viewPuntoStock, stock);


                // Cargar foto con Glide
                if (!fotoNombre.isEmpty() && !fotoNombre.equals("null")) {
                    String urlFoto = Config.local + "uploads/productos/" + fotoNombre;
                    Glide.with(this)
                            .load(urlFoto)
                            .placeholder(R.drawable.ct_inventory)
                            .error(R.drawable.ct_inventory)
                            .centerCrop()
                            .into(ivFoto);
                    ivFoto.setPadding(0, 0, 0, 0);
                    ivFoto.clearColorFilter();
                }

                // Click tarjeta → detalle
                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ProductoDetalleActivity.class);
                    intent.putExtra("producto_id", productoId);
                    startActivity(intent);
                });

                // Botón editar — verificar acceso
                btnEditar.setOnClickListener(v -> {
                    if (Config.tieneAcceso("EMP_PROD_EDITAR") || Config.esAdmin()) {
                        Intent intent = new Intent(this, ProductoFormActivity.class);
                        intent.putExtra("modo", "editar");
                        intent.putExtra("producto_id", productoId);
                        startActivity(intent);
                    } else {
                        Dialog.toast(this, "No tienes permiso para editar productos");
                    }
                });

                // FAB agregar — verificar acceso
                fabAgregar.setOnClickListener(v -> {
                    if (Config.tieneAcceso("EMP_PROD_CREAR") || Config.esAdmin()) {
                        startActivity(new Intent(this, ProductoFormActivity.class));
                    } else {
                        Dialog.toast(this, "No tienes permiso para crear productos");
                    }
                });

                layoutProductos.addView(itemView);

            } catch (Exception e) {
                Log.e("RENDER", e.getMessage());
            }
        }
    }

    // ── Estado stock ──────────────────────────────────────
    /*private void aplicarEstadoStock(TextView tvStock, View punto, int stock) {
        if (stock <= 0) {
            tvStock.setText("Sin stock");
            tvStock.setTextColor(ContextCompat.getColor(this, R.color.rojo_negativo));
            punto.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_punto_rojo));
        } else if (stock <= 10) {
            tvStock.setText("Stock bajo (" + stock + ")");
            tvStock.setTextColor(ContextCompat.getColor(this, R.color.amber));
            punto.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_punto_amarillo));
        } else {
            tvStock.setText("En stock (" + stock + ")");
            tvStock.setTextColor(ContextCompat.getColor(this, R.color.verde_acento));
            punto.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_punto_verde));
        }
    }*/


    // ── Helpers ───────────────────────────────────────────
    private void mostrarLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        layoutProductos.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}