package com.example.controlstock;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;

import org.json.JSONObject;

public class PerfilActivity extends AppCompatActivity {

    private ImageView btnVolver;
    private TextView tvInicial, tvNombre, tvRol, tvCorreo, tvUsuario;
    private EditText etPasswordActual, etPasswordNueva, etPasswordConfirmar;
    private Button btnCambiarPassword, btnCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        inicializarVistas();
        mostrarDatos();

        btnVolver.setOnClickListener(v -> finish());

        btnCambiarPassword.setOnClickListener(v -> cambiarPassword());

        btnCerrarSesion.setOnClickListener(v ->
                Dialog.confirm(this,
                        "Cerrar sesión",
                        "¿Estás seguro que deseas cerrar sesión?",
                        R.drawable.ct_lock,
                        new Dialog.ConfirmationDialogCallback() {
                            @Override
                            public void onConfirm() { cerrarSesion(); }
                            @Override
                            public void onCancel() {}
                        }));
    }

    private void inicializarVistas() {
        btnVolver           = findViewById(R.id.btnVolver);
        tvInicial           = findViewById(R.id.tvInicial);
        tvNombre            = findViewById(R.id.tvNombre);
        tvRol               = findViewById(R.id.tvRol);
        tvCorreo            = findViewById(R.id.tvCorreo);
        tvUsuario           = findViewById(R.id.tvUsuario);
        etPasswordActual    = findViewById(R.id.etPasswordActual);
        etPasswordNueva     = findViewById(R.id.etPasswordNueva);
        etPasswordConfirmar = findViewById(R.id.etPasswordConfirmar);
        btnCambiarPassword  = findViewById(R.id.btnCambiarPassword);
        btnCerrarSesion     = findViewById(R.id.btnCerrarSesion);
    }

    private void mostrarDatos() {
        android.util.Log.d("PERFIL", "correo=" + Config.correo + " | userNombre=" + Config.userNombre);
        String nombre = Config.usuario != null ? Config.usuario : "Usuario";
        String rol    = Config.esAdmin() ? "Administrador" : "Empleado";

        tvNombre.setText(nombre);
        tvRol.setText(rol);
        tvCorreo.setText(Config.correo != null ? Config.correo : "");
        tvUsuario.setText(Config.userNombre != null ? Config.userNombre : "");
        tvInicial.setText(nombre.isEmpty() ? "U" : String.valueOf(nombre.charAt(0)).toUpperCase());
    }

    private void cambiarPassword() {
        String actual     = etPasswordActual.getText().toString().trim();
        String nueva      = etPasswordNueva.getText().toString().trim();
        String confirmar  = etPasswordConfirmar.getText().toString().trim();

        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            Dialog.toast(this, "Completa todos los campos");
            return;
        }
        if (!nueva.equals(confirmar)) {
            Dialog.toast(this, "Las contraseñas no coinciden");
            return;
        }
        if (nueva.length() < 4) {
            Dialog.toast(this, "Mínimo 4 caracteres");
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("usuario_id",      Config.iduser);
            body.put("password_actual", actual);
            body.put("password_nueva",  nueva);

            btnCambiarPassword.setEnabled(false);

            ApiService.post(Config.local + "perfil_password.php", body, new ApiService.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        btnCambiarPassword.setEnabled(true);
                        try {
                            JSONObject res = new JSONObject(response);
                            Dialog.toast(PerfilActivity.this, res.optString("message"));
                            if (res.optBoolean("success")) {
                                etPasswordActual.setText("");
                                etPasswordNueva.setText("");
                                etPasswordConfirmar.setText("");
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    });
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        btnCambiarPassword.setEnabled(true);
                        Dialog.toast(PerfilActivity.this, "Error de conexión");
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cerrarSesion() {
        Config.iduser   = 0;
        Config.usuario  = null;
        Config.correo   = null;
        Config.rolId    = 0;

        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}