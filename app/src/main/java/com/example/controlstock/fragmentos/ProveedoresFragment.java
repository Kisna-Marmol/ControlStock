package com.example.controlstock.fragmentos;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.controlstock.R;
import com.example.controlstock.clases.ApiService;
import com.example.controlstock.clases.Config;
import com.example.controlstock.clases.Dialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProveedoresFragment extends Fragment {

    private LinearLayout layoutLista, layoutVacio;
    private ProgressBar progressBar;
    private FloatingActionButton fabAgregar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proveedores, container, false);

        layoutLista = view.findViewById(R.id.layoutLista);
        layoutVacio = view.findViewById(R.id.layoutVacio);
        progressBar = view.findViewById(R.id.progressBar);
        fabAgregar  = view.findViewById(R.id.fabAgregar);

        fabAgregar.setOnClickListener(v -> mostrarDialogo(null, -1));
        cargarProveedores();

        return view;
    }

    private void cargarProveedores() {
        progressBar.setVisibility(View.VISIBLE);
        layoutLista.removeAllViews();

        ApiService.get(Config.local + "proveedor_list.php", new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject res = new JSONObject(response);
                        if (res.optBoolean("success", false)) {
                            JSONArray data = res.getJSONArray("data");
                            if (data.length() == 0) {
                                layoutVacio.setVisibility(View.VISIBLE);
                                return;
                            }
                            layoutVacio.setVisibility(View.GONE);
                            for (int i = 0; i < data.length(); i++)
                                agregarItem(data.getJSONObject(i));
                        }
                    } catch (Exception e) {
                        Dialog.toast(requireContext(), "Error cargando proveedores");
                    }
                });
            }
            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Dialog.toast(requireContext(), "Error de conexión");
                });
            }
        });
    }

    private void agregarItem(JSONObject prov) {
        try {
            int id         = prov.optInt("proveedor_id");
            String nombre  = prov.optString("proveedor_nombre", "");
            String contacto = prov.optString("proveedor_contacto", "");
            String telefono = prov.optString("proveedor_telefono", "");

            View item = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_proveedor, layoutLista, false);

            TextView tvNombre   = item.findViewById(R.id.tvNombre);
            TextView tvContacto = item.findViewById(R.id.tvContacto);
            TextView tvTelefono = item.findViewById(R.id.tvTelefono);
            View btnEditar      = item.findViewById(R.id.btnEditar);
            View btnEliminar    = item.findViewById(R.id.btnEliminar);

            tvNombre.setText(nombre);
            tvContacto.setText(contacto.isEmpty() ? "Sin contacto" : contacto);
            tvTelefono.setText(telefono.isEmpty() ? "Sin teléfono" : telefono);

            btnEditar.setOnClickListener(v -> mostrarDialogo(prov, id));
            btnEliminar.setOnClickListener(v -> confirmarEliminar(id, nombre));

            layoutLista.addView(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDialogo(@Nullable JSONObject prov, int id) {
        boolean esEditar = prov != null;

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_proveedor, null);

        EditText etNombre   = dialogView.findViewById(R.id.etNombre);
        EditText etContacto = dialogView.findViewById(R.id.etContacto);
        EditText etTelefono = dialogView.findViewById(R.id.etTelefono);
        EditText etNit      = dialogView.findViewById(R.id.etNit);

        if (esEditar) {
            etNombre.setText(prov.optString("proveedor_nombre", ""));
            etContacto.setText(prov.optString("proveedor_contacto", ""));
            etTelefono.setText(prov.optString("proveedor_telefono", ""));
            etNit.setText(prov.optString("proveedor_nit_rut", ""));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(esEditar ? "Editar proveedor" : "Nuevo proveedor")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        Dialog.toast(requireContext(), "Ingresa un nombre");
                        return;
                    }
                    guardar(id, nombre,
                            etContacto.getText().toString().trim(),
                            etTelefono.getText().toString().trim(),
                            etNit.getText().toString().trim(),
                            esEditar);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardar(int id, String nombre, String contacto,
                         String telefono, String nit, boolean esEditar) {
        try {
            JSONObject body = new JSONObject();
            body.put("proveedor_nombre",   nombre);
            body.put("proveedor_contacto", contacto);
            body.put("proveedor_telefono", telefono);
            body.put("proveedor_nit_rut",  nit);

            String url;
            if (esEditar) {
                body.put("proveedor_id", id);
                url = Config.local + "proveedor_update.php";
            } else {
                url = Config.local + "proveedor_insert.php";
            }

            ApiService.post(url, body, new ApiService.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    requireActivity().runOnUiThread(() -> {
                        Dialog.toast(requireContext(), esEditar ? "Proveedor actualizado" : "Proveedor creado");
                        cargarProveedores();
                    });
                }
                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() ->
                            Dialog.toast(requireContext(), "Error de conexión"));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void confirmarEliminar(int id, String nombre) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar proveedor")
                .setMessage("¿Eliminar \"" + nombre + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    ApiService.get(Config.local + "proveedor_delete.php?id=" + id,
                            new ApiService.ApiCallback() {
                                @Override
                                public void onSuccess(String response) {
                                    requireActivity().runOnUiThread(() -> {
                                        Dialog.toast(requireContext(), "Proveedor eliminado");
                                        cargarProveedores();
                                    });
                                }
                                @Override
                                public void onError(String error) {
                                    requireActivity().runOnUiThread(() ->
                                            Dialog.toast(requireContext(), "Error de conexión"));
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}