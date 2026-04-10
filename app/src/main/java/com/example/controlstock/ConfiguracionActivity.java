package com.example.controlstock;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;

import org.json.JSONObject;

public class ConfiguracionActivity extends AppCompatActivity {

    // ── Vistas ───────────────────────────────────────────
    private ImageView btnVolver;
    private TextView tvNombreUsuario, tvRolUsuario, tvCorreoUsuario, tvInicialUsuario;
    private LinearLayout layoutSeccionAdmin;
    private LinearLayout btnBitacora, btnControlAccesos, btnUsuarios;
    private LinearLayout btnCatalogos, btnMovimientos, btnPerfil;
    private android.widget.Button btnCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        inicializarVistas();
        configurarListeners();
        cargarDatosUsuario();
        aplicarPermisosPorRol();
    }

    private void inicializarVistas() {
        btnVolver           = findViewById(R.id.btnVolver);
        tvNombreUsuario     = findViewById(R.id.tvNombreUsuario);
        tvRolUsuario        = findViewById(R.id.tvRolUsuario);
        tvCorreoUsuario     = findViewById(R.id.tvCorreoUsuario);
        tvInicialUsuario    = findViewById(R.id.tvInicialUsuario);
        layoutSeccionAdmin  = findViewById(R.id.layoutSeccionAdmin);
        btnBitacora         = findViewById(R.id.btnBitacora);
        btnControlAccesos   = findViewById(R.id.btnControlAccesos);
        btnUsuarios         = findViewById(R.id.btnUsuarios);
        btnCatalogos        = findViewById(R.id.btnCatalogos);
        btnMovimientos      = findViewById(R.id.btnMovimientos);
        btnPerfil           = findViewById(R.id.btnPerfil);
        btnCerrarSesion     = findViewById(R.id.btnCerrarSesion);
    }

    private void configurarListeners() {
        btnVolver.setOnClickListener(v -> finish());

        btnBitacora.setOnClickListener(v ->
                startActivity(new Intent(this, BitacoraActivity.class)));

        btnControlAccesos.setOnClickListener(v ->
                startActivity(new Intent(this, ControlAccesosActivity.class)));

        btnUsuarios.setOnClickListener(v ->
                startActivity(new Intent(this, UsuariosActivity.class)));

        btnCatalogos.setOnClickListener(v ->
                startActivity(new Intent(this, CatalogosActivity.class)));

        /*btnMovimientos.setOnClickListener(v ->
                startActivity(new Intent(this, MovimientoFormActivity.class)));*/

        btnMovimientos.setOnClickListener(v ->
                startActivity(new Intent(this, MovimientoListActivity.class)));

        btnPerfil.setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));;

        btnCerrarSesion.setOnClickListener(v -> {
            Dialog.confirm(this,
                    "Cerrar sesión",
                    "¿Estás seguro que deseas cerrar sesión?",
                    R.drawable.error,
                    new Dialog.ConfirmationDialogCallback() {
                        @Override
                        public void onConfirm() {
                            cerrarSesion();
                        }
                        @Override
                        public void onCancel() {}
                    });
        });
    }

    // ── Aplicar permisos según rol ────────────────────────
    private void aplicarPermisosPorRol() {
        if (Config.esAdmin()) {
            // Admin ve la sección de administración
            layoutSeccionAdmin.setVisibility(View.VISIBLE);
            tvRolUsuario.setText("Administrador");
        } else {
            // Empleado no ve la sección admin
            layoutSeccionAdmin.setVisibility(View.GONE);
            tvRolUsuario.setText("Empleado");
        }
    }

    // ── Cargar datos del usuario desde API ────────────────
    private void cargarDatosUsuario() {
        // Mostrar datos básicos desde Config
        tvNombreUsuario.setText(Config.usuario);

        // Inicial del nombre para el avatar
        if (Config.usuario != null && !Config.usuario.isEmpty()) {
            tvInicialUsuario.setText(
                    String.valueOf(Config.usuario.charAt(0)).toUpperCase());
        }

        // Cargar datos completos desde la API
        ApiService.get(Config.local + "usuario_get.php?id=" + Config.iduser,
                new ApiService.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject res = new JSONObject(response);
                            if (res.optBoolean("success", false)) {
                                JSONObject u = res.getJSONObject("data");
                                String correo = u.optString("usuario_correo", "");
                                runOnUiThread(() -> tvCorreoUsuario.setText(correo));
                            }
                        } catch (Exception e) {
                            Log.e("CONFIG", e.getMessage());
                        }
                    }
                    @Override
                    public void onError(String error) {
                        Log.e("CONFIG", "Error: " + error);
                    }
                });
    }

    // ── Cerrar sesión ─────────────────────────────────────
    private void cerrarSesion() {
        // Limpiar Config
        Config.usuario = "";
        Config.iduser  = 0;
        Config.rolId   = 0;

        // Ir al Login y limpiar el stack
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}