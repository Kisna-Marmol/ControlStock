package com.example.controlstock;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UsuariosActivity extends AppCompatActivity {

    private ImageView btnVolver;
    private LinearLayout layoutUsuarios, layoutVacio;
    private ProgressBar progressBar;
    private TextView tvContador;
    private FloatingActionButton fabAgregar;

    private List<JSONObject> listaUsuarios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        inicializarVistas();
        btnVolver.setOnClickListener(v -> finish());
        fabAgregar.setOnClickListener(v -> mostrarDialogoCrear());
        //cargarUsuarios();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarUsuarios();
    }

    private void inicializarVistas() {
        btnVolver      = findViewById(R.id.btnVolver);
        layoutUsuarios = findViewById(R.id.layoutUsuarios);
        layoutVacio    = findViewById(R.id.layoutVacio);
        progressBar    = findViewById(R.id.progressBar);
        tvContador     = findViewById(R.id.tvContador);
        fabAgregar     = findViewById(R.id.fabAgregar);
    }

    private void cargarUsuarios() {
        progressBar.setVisibility(View.VISIBLE);
        layoutUsuarios.removeAllViews();

        ApiService.get(Config.local + "usuario_list.php", new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject res = new JSONObject(response);
                        if (res.optBoolean("success", false)) {
                            JSONArray data = res.getJSONArray("data");
                            listaUsuarios.clear();

                            if (data.length() == 0) {
                                layoutVacio.setVisibility(View.VISIBLE);
                                return;
                            }

                            layoutVacio.setVisibility(View.GONE);
                            tvContador.setText(data.length() + " usuarios");

                            for (int i = 0; i < data.length(); i++)
                                agregarItem(data.getJSONObject(i));
                        }
                    } catch (Exception e) {
                        Dialog.toast(UsuariosActivity.this, "Error cargando usuarios");
                    }
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Dialog.toast(UsuariosActivity.this, "Error de conexión");
                });
            }
        });
    }

    private void agregarItem(JSONObject u) {
        try {
            int id       = u.optInt("usuario_id");
            String nombre = u.optString("usuario_nombre", "");
            String correo = u.optString("usuario_correo", "");
            String rol    = u.optString("rol_nombre", "");
            String estado = u.optString("usuario_estado", "ACTIVO");

            View item = LayoutInflater.from(this)
                    .inflate(R.layout.item_usuario, layoutUsuarios, false);

            TextView tvInicial  = item.findViewById(R.id.tvInicial);
            TextView tvNombre   = item.findViewById(R.id.tvNombre);
            TextView tvCorreo   = item.findViewById(R.id.tvCorreo);
            TextView tvRol      = item.findViewById(R.id.tvRol);
            TextView tvEstado   = item.findViewById(R.id.tvEstado);
            ImageView btnOpc    = item.findViewById(R.id.btnOpciones);

            tvInicial.setText(nombre.isEmpty() ? "U" : String.valueOf(nombre.charAt(0)).toUpperCase());
            tvNombre.setText(nombre);
            tvCorreo.setText(correo);
            tvRol.setText(rol);

            // Estado
            tvEstado.setText(estado);
            if (estado.equals("ACTIVO")) {
                tvEstado.setTextColor(ContextCompat.getColor(this, R.color.verde_acento));
                tvEstado.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_azul));
            } else {
                tvEstado.setTextColor(ContextCompat.getColor(this, R.color.rojo_negativo));
                tvEstado.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_circle_red));
            }

            // Menú opciones
            btnOpc.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, v);
                popup.getMenu().add(0, 1, 0, "Editar");
                popup.getMenu().add(0, 2, 0, estado.equals("ACTIVO") ? "Desactivar" : "Activar");
                popup.getMenu().add(0, 3, 0, "Cambiar contraseña");

                popup.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case 1: mostrarDialogoEditar(u); break;
                        case 2: cambiarEstado(id, nombre, estado.equals("ACTIVO") ? "INACTIVO" : "ACTIVO"); break;
                        case 3: mostrarDialogoPassword(id, nombre); break;
                    }
                    return true;
                });
                popup.show();
            });

            layoutUsuarios.addView(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDialogoCrear() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_usuario, null);

        EditText etNombre   = dialogView.findViewById(R.id.etNombre);
        EditText etCorreo   = dialogView.findViewById(R.id.etCorreo);
        EditText etUser     = dialogView.findViewById(R.id.etUser);
        EditText etPassword = dialogView.findViewById(R.id.etPassword);
        Spinner  spRol      = dialogView.findViewById(R.id.spRol);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Empleado", "Admin"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRol.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Nuevo usuario")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre   = etNombre.getText().toString().trim();
                    String correo   = etCorreo.getText().toString().trim();
                    String user     = etUser.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();
                    int rolId       = spRol.getSelectedItemPosition() == 1 ? 1 : 2;

                    if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty()) {
                        Dialog.toast(this, "Completa los campos obligatorios");
                        return;
                    }
                    crearUsuario(nombre, correo, user, password, rolId);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditar(JSONObject u) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_usuario_editar, null);

        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etCorreo = dialogView.findViewById(R.id.etCorreo);
        Spinner  spRol    = dialogView.findViewById(R.id.spRol);

        etNombre.setText(u.optString("usuario_nombre", ""));
        etCorreo.setText(u.optString("usuario_correo", ""));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Empleado", "Admin"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRol.setAdapter(adapter);

        int rolId = u.optInt("rol_id", 2);
        spRol.setSelection(rolId == 1 ? 1 : 0);

        new AlertDialog.Builder(this)
                .setTitle("Editar usuario")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String correo = etCorreo.getText().toString().trim();
                    int nuevoRol  = spRol.getSelectedItemPosition() == 1 ? 1 : 2;

                    if (nombre.isEmpty() || correo.isEmpty()) {
                        Dialog.toast(this, "Completa los campos");
                        return;
                    }
                    editarUsuario(u.optInt("usuario_id"), nombre, correo, nuevoRol);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoPassword(int id, String nombre) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_password, null);
        EditText etPassword = dialogView.findViewById(R.id.etPassword);

        new AlertDialog.Builder(this)
                .setTitle("Cambiar contraseña de " + nombre)
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String pass = etPassword.getText().toString().trim();
                    if (pass.length() < 4) {
                        Dialog.toast(this, "Mínimo 4 caracteres");
                        return;
                    }
                    cambiarPassword(id, pass);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void crearUsuario(String nombre, String correo, String user, String password, int rolId) {
        try {
            JSONObject body = new JSONObject();
            body.put("usuario_nombre",   nombre);
            body.put("usuario_correo",   correo);
            body.put("usuario_user",     user);
            body.put("usuario_password", password);
            body.put("rol_id",           rolId);
            body.put("usuario_id",       Config.iduser);

            ApiService.post(Config.local + "usuario_insert.php", body, new ApiService.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject res = new JSONObject(response);
                            Dialog.toast(UsuariosActivity.this, res.optString("message"));
                            if (res.optBoolean("success")) cargarUsuarios();
                        } catch (Exception e) { e.printStackTrace(); }
                    });
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Dialog.toast(UsuariosActivity.this, "Error de conexión"));
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void editarUsuario(int id, String nombre, String correo, int rolId) {
        try {
            JSONObject body = new JSONObject();
            body.put("usuario_id", id);
            body.put("usuario_nombre", nombre);
            body.put("usuario_correo", correo);
            body.put("rol_id",         rolId);
            body.put("admin_id",       Config.iduser);

            ApiService.post(Config.local + "usuario_update.php", body, new ApiService.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject res = new JSONObject(response);
                            Dialog.toast(UsuariosActivity.this, res.optString("message"));
                            if (res.optBoolean("success")) cargarUsuarios();
                        } catch (Exception e) { e.printStackTrace(); }
                    });
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Dialog.toast(UsuariosActivity.this, "Error de conexión"));
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cambiarEstado(int id, String nombre, String nuevoEstado) {
        try {
            JSONObject body = new JSONObject();
            body.put("usuario_id", id);
            body.put("estado",     nuevoEstado);
            body.put("admin_id",   Config.iduser);

            ApiService.post(Config.local + "usuario_estado.php", body, new ApiService.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject res = new JSONObject(response);
                            Dialog.toast(UsuariosActivity.this, res.optString("message"));
                            if (res.optBoolean("success")) cargarUsuarios();
                        } catch (Exception e) { e.printStackTrace(); }
                    });
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Dialog.toast(UsuariosActivity.this, "Error de conexión"));
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cambiarPassword(int id, String password) {
        try {
            JSONObject body = new JSONObject();
            body.put("usuario_id",      id);
            body.put("nueva_password",  password);
            body.put("admin_id",        Config.iduser);

            ApiService.post(Config.local + "usuario_password.php", body, new ApiService.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject res = new JSONObject(response);
                            Dialog.toast(UsuariosActivity.this, res.optString("message"));
                        } catch (Exception e) { e.printStackTrace(); }
                    });
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Dialog.toast(UsuariosActivity.this, "Error de conexión"));
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }
}