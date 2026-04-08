package com.example.controlstock;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.ClienteAdapter;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;
import com.example.controlstock.modelo.Cliente;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ClienteListActivity extends AppCompatActivity implements ClienteAdapter.OnClienteListener {

    private ListView lvClientes;
    private LinearLayout layoutEmpty;
    private EditText etBuscar;

    private List<Cliente> listaClientes = new ArrayList<>();
    private ClienteAdapter adapter;

    private static final String URL_GET = Config.local + "clientes/clientes_list.php";
    private static final String URL_DELETE = Config.local + "clientes/clientes_delete.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_list);

        lvClientes  = findViewById(R.id.lvClientes);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        etBuscar    = findViewById(R.id.etBuscar);

        ImageButton btnBack          = findViewById(R.id.btnBack);
        ImageButton btnNuevoCliente  = findViewById(R.id.btnNuevoCliente);

        adapter = new ClienteAdapter(this, listaClientes, this);
        lvClientes.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        btnNuevoCliente.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClienteFormActivity.class);
            startActivityForResult(intent, 100);
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargarClientes(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        cargarClientes("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            cargarClientes(etBuscar.getText().toString().trim());
        }
    }

    private void cargarClientes(String buscar) {
        String url = URL_GET + "?buscar=" + buscar;

        ApiService.get(url, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("success")) {
                        JSONArray array = json.getJSONArray("data");
                        listaClientes.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            Cliente c = new Cliente(
                                    obj.getInt("cliente_id"),
                                    obj.optString("cliente_documento", ""),
                                    obj.optString("cliente_nombre", ""),
                                    obj.optString("cliente_telefono", ""),
                                    obj.optString("cliente_email", "")
                            );
                            listaClientes.add(c);
                        }

                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            boolean vacio = listaClientes.isEmpty();
                            lvClientes.setVisibility(vacio ? View.GONE : View.VISIBLE);
                            layoutEmpty.setVisibility(vacio ? View.VISIBLE : View.GONE);
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> Dialog.toast(ClienteListActivity.this, "Error al cargar clientes"));
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Dialog.toast(ClienteListActivity.this, "Error de conexión"));
            }
        });
    }

    @Override
    public void onEditar(Cliente cliente) {
        Intent intent = new Intent(this, ClienteFormActivity.class);
        intent.putExtra("cliente_id", cliente.getClienteId());
        intent.putExtra("cliente_documento", cliente.getClienteDocumento());
        intent.putExtra("cliente_nombre", cliente.getClienteNombre());
        intent.putExtra("cliente_telefono", cliente.getClienteTelefono());
        intent.putExtra("cliente_email", cliente.getClienteEmail());
        startActivityForResult(intent, 100);
    }

    @Override
    public void onEliminar(Cliente cliente) {
        Dialog.confirm(
                this,
                "¿Eliminar cliente?",
                "Se eliminará a " + cliente.getClienteNombre() + ". Esta acción no se puede deshacer.",
                android.R.drawable.ic_dialog_alert,
                new Dialog.ConfirmationDialogCallback() {
                    @Override
                    public void onConfirm() {
                        eliminarCliente(cliente);
                    }

                    @Override
                    public void onCancel() {
                        // no hacer nada
                    }
                }
        );
    }

    private void eliminarCliente(Cliente cliente) {
        String url = URL_DELETE + "?id=" + cliente.getClienteId();

        ApiService.get(url, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    runOnUiThread(() -> {
                        Dialog.toast(ClienteListActivity.this, json.optString("message"));
                        if (json.optBoolean("success")) {
                            cargarClientes(etBuscar.getText().toString().trim());
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Dialog.toast(ClienteListActivity.this, "Error al eliminar"));
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Dialog.toast(ClienteListActivity.this, "Error de conexión"));
            }
        });
    }
}