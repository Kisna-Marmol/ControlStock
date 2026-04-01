package com.example.controlstock;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ProductoFormActivity extends AppCompatActivity {

    // ── Constantes ────────────────────────────────────────
    private static final int REQUEST_FOTO       = 300;
    private static final int PERMISO_UBICACION  = 200;
    private static final int PERMISO_CAMARA     = 301;

    // ── Vistas ───────────────────────────────────────────
    private ImageView btnVolver, ivFotoPreview;
    private EditText etNombre, etDescripcion, etStock, etCodigoQr;
    private EditText etLatitud, etLongitud;
    private Button btnGuardar;
    private Spinner spinnerCategoria, spinnerUnidad, spinnerProveedor;
    private TextView tvEstadoGps;

    // ── Datos ─────────────────────────────────────────────
    private Bitmap bitmapFoto;
    private FusedLocationProviderClient fusedLocationClient;

    private List<String> listaNombresCat    = new ArrayList<>();
    private List<Integer> listaIdsCat       = new ArrayList<>();
    private List<String> listaNombresUnidad = new ArrayList<>();
    private List<Integer> listaIdsUnidad    = new ArrayList<>();
    private List<String> listaNombresProv   = new ArrayList<>();
    private List<Integer> listaIdsProv      = new ArrayList<>();

    // ─────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_form);
        inicializarVistas();
        configurarListeners();
        cargarCategorias();
        cargarUnidades();
        cargarProveedores();
    }

    private void inicializarVistas() {
        btnVolver        = findViewById(R.id.btnVolver);
        ivFotoPreview    = findViewById(R.id.ivFotoPreview);
        etNombre         = findViewById(R.id.etNombre);
        etDescripcion    = findViewById(R.id.etDescripcion);
        etStock          = findViewById(R.id.etStock);
        etCodigoQr       = findViewById(R.id.etCodigoQr);
        etLatitud        = findViewById(R.id.etLatitud);
        etLongitud       = findViewById(R.id.etLongitud);
        btnGuardar       = findViewById(R.id.btnGuardar);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        spinnerUnidad    = findViewById(R.id.spinnerUnidad);
        spinnerProveedor = findViewById(R.id.spinnerProveedor);
        tvEstadoGps      = findViewById(R.id.tvEstadoGps);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void configurarListeners() {
        btnVolver.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> validarYGuardar());
        findViewById(R.id.btnTomarFoto).setOnClickListener(v -> tomarFoto());
        findViewById(R.id.btnEscanearQr).setOnClickListener(v -> escanearQr());
        findViewById(R.id.btnObtenerUbicacion).setOnClickListener(v -> obtenerUbicacion());
    }

    // ── CÁMARA ────────────────────────────────────────────
    private void tomarFoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISO_CAMARA);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_FOTO);
        } else {
            Dialog.toast(this, "No hay cámara disponible");
        }
    }

    // ── QR ────────────────────────────────────────────────
    private void escanearQr() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Escanea el código QR del producto");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    // ── GPS ───────────────────────────────────────────────
    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISO_UBICACION);
            return;
        }
        tvEstadoGps.setText("Obteniendo ubicación...");
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                etLatitud.setText(String.valueOf(location.getLatitude()));
                etLongitud.setText(String.valueOf(location.getLongitude()));
                tvEstadoGps.setText("Ubicación obtenida correctamente");
            } else {
                tvEstadoGps.setText("No se pudo obtener la ubicación");
            }
        });
    }

    // ── onActivityResult ──────────────────────────────────
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Foto de cámara
        if (requestCode == REQUEST_FOTO && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            bitmapFoto = (Bitmap) extras.get("data");

            if (bitmapFoto != null) {
                Log.d("FOTO", "Bitmap obtenido: " + bitmapFoto.getWidth() + "x" + bitmapFoto.getHeight());

                runOnUiThread(() -> {
                    ivFotoPreview.setImageDrawable(null); // limpiar primero
                    ivFotoPreview.setImageBitmap(bitmapFoto);
                    ivFotoPreview.setPadding(0, 0, 0, 0);
                    ivFotoPreview.clearColorFilter();
                    ivFotoPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ivFotoPreview.invalidate(); // forzar redibujado
                    Dialog.toast(ProductoFormActivity.this, "Foto capturada");
                });
            }
        }

        // Resultado QR
        IntentResult resultadoQr = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (resultadoQr != null && resultadoQr.getContents() != null) {
            etCodigoQr.setText(resultadoQr.getContents());
            Dialog.toast(this, "QR: " + resultadoQr.getContents());
        }
    }

    // ── Permisos ──────────────────────────────────────────
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISO_UBICACION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                obtenerUbicacion();
            else
                Dialog.toast(this, "Permiso de ubicación denegado");
        }
        if (requestCode == PERMISO_CAMARA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                tomarFoto();
            else
                Dialog.toast(this, "Permiso de cámara denegado");
        }
    }

    // ── Cargar spinners ───────────────────────────────────
    private void cargarCategorias() {
        ApiService.get(Config.local + "categoria_list.php", new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject res = new JSONObject(response);
                    if (res.optBoolean("success", false)) {
                        JSONArray data = res.getJSONArray("data");
                        listaNombresCat.clear(); listaIdsCat.clear();
                        listaNombresCat.add("Seleccione categoría"); listaIdsCat.add(0);
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            listaNombresCat.add(item.getString("categoria_nombre"));
                            listaIdsCat.add(Integer.parseInt(item.getString("categoria_id")));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ProductoFormActivity.this,
                                android.R.layout.simple_spinner_item, listaNombresCat);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCategoria.setAdapter(adapter);
                    }
                } catch (Exception e) { Log.e("SPINNER", e.getMessage()); }
            }
            @Override public void onError(String error) {
                Dialog.toast(ProductoFormActivity.this, "Error cargando categorías");
            }
        });
    }

    private void cargarUnidades() {
        ApiService.get(Config.local + "unidad_list.php", new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject res = new JSONObject(response);
                    if (res.optBoolean("success", false)) {
                        JSONArray data = res.getJSONArray("data");
                        listaNombresUnidad.clear(); listaIdsUnidad.clear();
                        listaNombresUnidad.add("Seleccione unidad"); listaIdsUnidad.add(0);
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            listaNombresUnidad.add(item.getString("um_nombre")
                                    + " (" + item.getString("um_abreviatura") + ")");
                            listaIdsUnidad.add(Integer.parseInt(item.getString("um_id")));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ProductoFormActivity.this,
                                android.R.layout.simple_spinner_item, listaNombresUnidad);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerUnidad.setAdapter(adapter);
                    }
                } catch (Exception e) { Log.e("SPINNER", e.getMessage()); }
            }
            @Override public void onError(String error) {
                Dialog.toast(ProductoFormActivity.this, "Error cargando unidades");
            }
        });
    }

    private void cargarProveedores() {
        ApiService.get(Config.local + "proveedor_list.php", new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject res = new JSONObject(response);
                    if (res.optBoolean("success", false)) {
                        JSONArray data = res.getJSONArray("data");
                        listaNombresProv.clear(); listaIdsProv.clear();
                        listaNombresProv.add("Seleccione proveedor"); listaIdsProv.add(0);
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            listaNombresProv.add(item.getString("proveedor_nombre"));
                            listaIdsProv.add(Integer.parseInt(item.getString("proveedor_id")));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ProductoFormActivity.this,
                                android.R.layout.simple_spinner_item, listaNombresProv);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerProveedor.setAdapter(adapter);
                    }
                } catch (Exception e) { Log.e("SPINNER", e.getMessage()); }
            }
            @Override public void onError(String error) {
                Dialog.toast(ProductoFormActivity.this, "Error cargando proveedores");
            }
        });
    }

    // ── Validar ───────────────────────────────────────────
    private void validarYGuardar() {
        String nombre      = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String stockStr    = etStock.getText().toString().trim();
        String codigoQr    = etCodigoQr.getText().toString().trim();
        String latitud     = etLatitud.getText().toString().trim();
        String longitud    = etLongitud.getText().toString().trim();

        if (nombre.isEmpty()) {
            Dialog.toast(this, "El nombre es obligatorio");
            etNombre.requestFocus(); return;
        }
        if (stockStr.isEmpty()) {
            Dialog.toast(this, "Ingresa el stock inicial");
            etStock.requestFocus(); return;
        }
        if (spinnerCategoria.getSelectedItemPosition() == 0) {
            Dialog.toast(this, "Selecciona una categoría"); return;
        }
        if (spinnerUnidad.getSelectedItemPosition() == 0) {
            Dialog.toast(this, "Selecciona una unidad de medida"); return;
        }
        if (spinnerProveedor.getSelectedItemPosition() == 0) {
            Dialog.toast(this, "Selecciona un proveedor"); return;
        }

        guardarProducto(nombre, descripcion, codigoQr,
                Integer.parseInt(stockStr), latitud, longitud,
                listaIdsCat.get(spinnerCategoria.getSelectedItemPosition()),
                listaIdsUnidad.get(spinnerUnidad.getSelectedItemPosition()),
                listaIdsProv.get(spinnerProveedor.getSelectedItemPosition()));
    }

    // ── Guardar producto ──────────────────────────────────
    private void guardarProducto(String nombre, String descripcion, String codigoQr,
                                 int stock, String latitud, String longitud,
                                 int catId, int unidadId, int proveedorId) {
        try {
            JSONObject json = new JSONObject();
            json.put("producto_nombre",      nombre);
            json.put("producto_descripcion", descripcion);
            json.put("producto_codigo_qr",   codigoQr);
            json.put("producto_stock",       stock);
            json.put("producto_latitud",  latitud.isEmpty()  ? JSONObject.NULL : Double.parseDouble(latitud));
            json.put("producto_longitud", longitud.isEmpty() ? JSONObject.NULL : Double.parseDouble(longitud));
            json.put("usuario_id",   Config.iduser);
            json.put("categoria_id", catId);
            json.put("um_id",        unidadId);
            json.put("proveedor_id", proveedorId);

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");

            ApiService.post(Config.local + "producto_insert.php", json, new ApiService.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject res = new JSONObject(response);
                        if (res.optBoolean("success", false)) {
                            int nuevoId = res.optInt("producto_id", 0);
                            if (bitmapFoto != null) {
                                subirFotoYCerrar(nuevoId);
                            } else {
                                Dialog.toast(ProductoFormActivity.this, "Producto guardado");
                                finish();
                            }
                        } else {
                            Dialog.toast(ProductoFormActivity.this,
                                    "Error: " + res.optString("error"));
                            resetBotonGuardar();
                        }
                    } catch (Exception e) {
                        Dialog.toast(ProductoFormActivity.this, "Error en respuesta");
                        resetBotonGuardar();
                    }
                }
                @Override
                public void onError(String error) {
                    Dialog.toast(ProductoFormActivity.this, "Error: " + error);
                    resetBotonGuardar();
                }
            });
        } catch (Exception e) {
            Dialog.toast(this, "Error preparando datos");
        }
    }

    // ── Subir foto desde Bitmap y cerrar ──────────────────
    private void subirFotoYCerrar(int productoId) {
        Dialog.toast(this, "Subiendo foto...");

        // Convertir Bitmap a archivo temporal
        File tempFile;
        try {
            tempFile = File.createTempFile("producto_", ".jpg", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            bitmapFoto.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Dialog.toast(this, "Error preparando foto");
            finish();
            return;
        }

        File fotoFinal = tempFile;

        new Thread(() -> {
            try {
                String boundary   = "----Boundary" + System.currentTimeMillis();
                String lineEnd    = "\r\n";
                String twoHyphens = "--";

                URL url = new URL(Config.local + "foto_upload.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data; boundary=" + boundary);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                // producto_id
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"producto_id\""
                        + lineEnd + lineEnd);
                dos.writeBytes(String.valueOf(productoId) + lineEnd);

                // foto
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"foto\"; filename=\""
                        + fotoFinal.getName() + "\"" + lineEnd);
                dos.writeBytes("Content-Type: image/jpeg" + lineEnd + lineEnd);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmapFoto.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                dos.write(baos.toByteArray());

                dos.writeBytes(lineEnd + twoHyphens + boundary + twoHyphens + lineEnd);
                dos.flush();
                dos.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                String respuesta = sb.toString();
                Log.d("FOTO_UPLOAD", respuesta);

                JSONObject res = new JSONObject(respuesta);
                fotoFinal.delete();

                runOnUiThread(() -> {
                    if (res.optBoolean("success", false)) {
                        Dialog.toast(ProductoFormActivity.this, "Producto y foto guardados");
                    } else {
                        Dialog.toast(ProductoFormActivity.this,
                                "Producto guardado, error en foto: "
                                        + res.optString("error"));
                    }
                    finish();
                });

            } catch (Exception e) {
                Log.e("FOTO_UPLOAD", e.getMessage());
                fotoFinal.delete();
                runOnUiThread(() -> {
                    Dialog.toast(ProductoFormActivity.this, "Producto guardado, error en foto");
                    finish();
                });
            }
        }).start();
    }

    private void resetBotonGuardar() {
        btnGuardar.setEnabled(true);
        btnGuardar.setText("Guardar producto");
    }
}