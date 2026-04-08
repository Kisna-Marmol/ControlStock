package com.example.controlstock;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;

import org.json.JSONObject;

public class Login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ImageView eye;
    private Button btnIngresar;

    CheckBox chRecordarme;

    // Claves para SharedPreferences
    private static final String PREFS_NOMBRE   = "LoginPrefs";
    private static final String CLAVE_RECORDAR = "recordarme";
    private static final String CLAVE_USUARIO  = "usuario";

    private static final String CLAVE_CLAVE    = "clave";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        eye = findViewById(R.id.iv_toggle_password);
        chRecordarme = findViewById(R.id.cb_recordarme);
        mostrarPassword();

        btnIngresar = findViewById(R.id.btn_iniciar_sesion);

        btnIngresar.setOnClickListener(View -> validarCampos());

        //iniciarSesion();
        cargarUsuarioGuardado();

    }
    public void mostrarPassword(){
        eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    // Mostrar contraseña
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    eye.setImageResource(R.drawable.ct_eye_off); // icono ojo cerrado
                } else {
                    // Ocultar contraseña
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    eye.setImageResource(R.drawable.ct_eye); // icono ojo abierto
                }

                // Mantener cursor al final
                etPassword.setSelection(etPassword.getText().length());
            }
        });
    }
    /*public void iniciarSesion(){
        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, DashboardActivity.class);
                startActivity(intent);
            }
        });
    }*/
    //Empiezar codigo recordarme
    private void cargarUsuarioGuardado(){
        SharedPreferences prefs = getSharedPreferences(PREFS_NOMBRE, Context.MODE_PRIVATE);
        boolean recordar = prefs.getBoolean(CLAVE_RECORDAR, false);

        Log.d("RECORDARME", "Recordar: " + recordar +
                " | Usuario: " + prefs.getString(CLAVE_USUARIO, "vacío"));

        if (recordar) {
            String usuarioGuardado = prefs.getString(CLAVE_USUARIO, "");
            String claveGuardada   = prefs.getString(CLAVE_CLAVE, "");
            etEmail.setText(usuarioGuardado);
            etPassword.setText(claveGuardada);
            chRecordarme.setChecked(true);
        }
    }

    private void guardarUsuario(String usuario, String clave) {
        SharedPreferences.Editor editor =
                getSharedPreferences(PREFS_NOMBRE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(CLAVE_RECORDAR, true);
        editor.putString(CLAVE_USUARIO, usuario);
        editor.putString(CLAVE_CLAVE, clave);
        editor.apply();

        // ← Agrega esto para verificar en Logcat
        Log.d("RECORDARME", "Usuario guardado: " + usuario);
    }

    private void borrarUsuarioGuardado() {
        SharedPreferences.Editor editor =
                getSharedPreferences(PREFS_NOMBRE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(CLAVE_RECORDAR, false);
        editor.remove(CLAVE_USUARIO);
        editor.remove(CLAVE_CLAVE);
        editor.apply();
    }

    public void validarCampos()
    {
        if(etEmail.getText().toString().trim().equals(""))
        {
            //Dialog.msgbox(MainActivity.this,"Inválido","Ingrese el Usuario",R.drawable.error);
            Dialog.toast(Login.this,"Favor Ingrese el Usuario");
            etEmail.requestFocus();
        }
        else if(etPassword.getText().toString().trim().equals(""))
        {
            //Dialog.msgbox(MainActivity.this,"Inválido","Ingrese la Clave",R.drawable.error);
            Dialog.toast(Login.this,"Favor Ingrese la Clave");
            etEmail.requestFocus();
        }
        else
        {
            String usuario=etEmail.getText().toString();
            String clave=etPassword.getText().toString();

            // ← Guardar el estado ANTES de llamar la API
            boolean recordar = chRecordarme.isChecked(); // ← Capturar aquí
            confirmarCredenciales(usuario, clave, recordar); // ← Pasar el valor
            //Dialog.msgbox(MainActivity.this,"Informacion","Entramos Bien",R.drawable.ok);
        }
    }

    private void confirmarCredenciales(String usuario, String clave, boolean recordar) {
        ApiService.login(usuario, clave, new ApiService.LoginCallback() {
            @Override
            public void onSuccess(JSONObject userdata) {
                Log.d("LOGIN_RESPONSE", "Respuesta completa: " + userdata.toString());

                try {
                    // userdata ya es el JSON completo del servidor
                    // Leer directamente desde "data"
                    JSONObject data = userdata.getJSONObject("data");

                    String username = data.getString("user_nombre");
                    int userId      = data.getInt("user_id");
                    int rolId       = data.getInt("rol_id");

                    Config.usuario = username;
                    Config.iduser = userId;
                    Config.rolId   = rolId;

                    // Cargar accesos antes de ir al Dashboard
                    cargarAccesosYNavegar(username, userId);

                    if (recordar) {
                        guardarUsuario(usuario, clave);
                    } else {
                        borrarUsuarioGuardado();
                    }

                    llamarPral(username, userId);

                } catch (Exception e) {
                    Log.e("LOGIN_PARSE", "Error: " + e.getMessage());
                    Dialog.toast(Login.this, "Error procesando respuesta: " + e.getMessage());
                }
            }

            private void cargarAccesosYNavegar(String username, int userId) {
                ApiService.get(Config.local + "acceso_usuario.php?usuario_id=" + userId,
                        new ApiService.ApiCallback() {
                            @Override
                            public void onSuccess(String response) {
                                try {
                                    JSONObject res = new JSONObject(response);
                                    if (res.optBoolean("success", false)) {
                                        org.json.JSONArray accesos = res.getJSONArray("accesos");
                                        Config.accesosActivos.clear();
                                        for (int i = 0; i < accesos.length(); i++) {
                                            Config.accesosActivos.add(accesos.getString(i));
                                        }
                                        Log.d("ACCESOS", "Accesos cargados: " + Config.accesosActivos.toString());
                                    }
                                } catch (Exception e) {
                                    Log.e("ACCESOS", e.getMessage());
                                }
                                // Navegar siempre, aunque falle la carga de accesos
                                llamarPral(username, userId);
                            }
                            @Override
                            public void onError(String error) {
                                Log.e("ACCESOS", "Error: " + error);
                                llamarPral(username, userId);
                            }
                        });
            }

            @Override
            public void onError(String errorMessage) {
                Dialog.toast(Login.this, "Error: " + errorMessage);
            }
        });
    }

    private void llamarPral(String nombreUsuario, int userId)
    {
        Intent intent = new Intent(Login.this, DashboardActivity.class);
        intent.putExtra("Nombre_Usuario", nombreUsuario);
        intent.putExtra("User_ID", userId);
        startActivity(intent);
        finish(); // Elimina el splash del stack para que no se regrese a él
    }

}