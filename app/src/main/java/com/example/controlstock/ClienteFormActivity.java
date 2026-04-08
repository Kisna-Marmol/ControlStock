package com.example.controlstock;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;

import org.json.JSONObject;

public class ClienteFormActivity extends AppCompatActivity {

    private EditText etDocumento, etNombre, etTelefono, etEmail;
    private TextView tvTituloForm;

    private int clienteId = -1;
    private boolean esEdicion = false;

    private static final String URL_POST   = Config.local + "clientes/clientes_insert.php";
    private static final String URL_UPDATE = Config.local + "clientes/clientes_update.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_form);

        tvTituloForm = findViewById(R.id.tvTituloForm);
        etDocumento  = findViewById(R.id.etDocumento);
        etNombre     = findViewById(R.id.etNombre);
        etTelefono   = findViewById(R.id.etTelefono);
        etEmail      = findViewById(R.id.etEmail);

        ImageButton btnBack   = findViewById(R.id.btnBack);
        Button      btnGuardar = findViewById(R.id.btnGuardar);

        btnBack.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardar());

        // Si viene con datos → modo edición
        Intent intent = getIntent();
        clienteId = intent.getIntExtra("cliente_id", -1);

        if (clienteId != -1) {
            esEdicion = true;
            tvTituloForm.setText("Editar Cliente");
            etDocumento.setText(intent.getStringExtra("cliente_documento"));
            etNombre.setText(intent.getStringExtra("cliente_nombre"));
            etTelefono.setText(intent.getStringExtra("cliente_telefono"));
            etEmail.setText(intent.getStringExtra("cliente_email"));
        }
    }

    private void guardar() {
        String documento = etDocumento.getText().toString().trim();
        String nombre    = etNombre.getText().toString().trim();
        String telefono  = etTelefono.getText().toString().trim();
        String email     = etEmail.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            etNombre.requestFocus();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("cliente_documento", documento);
            body.put("cliente_nombre", nombre);
            body.put("cliente_telefono", telefono);
            body.put("cliente_email", email);

            if (esEdicion) {
                body.put("cliente_id", clienteId);
            }

            String url = esEdicion ? URL_UPDATE : URL_POST;

            ApiService.post(url, body, new ApiService.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject json = new JSONObject(response);
                        runOnUiThread(() -> {
                            Dialog.toast(ClienteFormActivity.this, json.optString("message"));
                            if (json.optBoolean("success")) {
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Dialog.toast(ClienteFormActivity.this, "Error al procesar respuesta"));
                    }
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Dialog.toast(ClienteFormActivity.this, "Error de conexión"));
                }
            });

        } catch (Exception e) {
            Dialog.toast(this, "Error al preparar datos");
        }
    }
}