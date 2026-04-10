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

public class CategoriasFragment extends Fragment {

    private LinearLayout layoutLista, layoutVacio;
    private ProgressBar progressBar;
    private FloatingActionButton fabAgregar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categorias, container, false);

        layoutLista  = view.findViewById(R.id.layoutLista);
        layoutVacio  = view.findViewById(R.id.layoutVacio);
        progressBar  = view.findViewById(R.id.progressBar);
        fabAgregar   = view.findViewById(R.id.fabAgregar);

        fabAgregar.setOnClickListener(v -> mostrarDialogo(null, -1));
        cargarCategorias();

        return view;
    }

    private void cargarCategorias() {
        progressBar.setVisibility(View.VISIBLE);
        layoutLista.removeAllViews();

        ApiService.get(Config.local + "categoria_list.php", new ApiService.ApiCallback() {
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
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject cat = data.getJSONObject(i);
                                agregarItem(cat);
                            }
                        }
                    } catch (Exception e) {
                        Dialog.toast(requireContext(), "Error cargando categorías");
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

    private void agregarItem(JSONObject cat) {
        try {
            int id        = cat.optInt("categoria_id");
            String nombre = cat.optString("categoria_nombre", "");
            int estado    = cat.optInt("categoria_estado", 1);

            View item = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_categoria, layoutLista, false);

            TextView tvNombre = item.findViewById(R.id.tvNombre);
            TextView tvEstado = item.findViewById(R.id.tvEstado);
            View btnEditar    = item.findViewById(R.id.btnEditar);
            View btnEliminar  = item.findViewById(R.id.btnEliminar);

            tvNombre.setText(nombre);
            tvEstado.setText(estado == 1 ? "Activa" : "Inactiva");
            tvEstado.setTextColor(estado == 1 ? 0xFF10B981 : 0xFFEF4444);

            btnEditar.setOnClickListener(v -> mostrarDialogo(cat, id));
            btnEliminar.setOnClickListener(v -> confirmarEliminar(id, nombre));

            layoutLista.addView(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDialogo(@Nullable JSONObject cat, int id) {
        boolean esEditar = cat != null;

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_categoria, null);

        EditText etNombre = dialogView.findViewById(R.id.etNombre);

        if (esEditar) {
            etNombre.setText(cat.optString("categoria_nombre", ""));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(esEditar ? "Editar categoría" : "Nueva categoría")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        Dialog.toast(requireContext(), "Ingresa un nombre");
                        return;
                    }
                    guardar(id, nombre, esEditar);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardar(int id, String nombre, boolean esEditar) {
        try {
            JSONObject body = new JSONObject();
            body.put("usuario_id", Config.iduser);
            body.put("categoria_nombre", nombre);

            String url;
            if (esEditar) {
                body.put("categoria_id", id);
                url = Config.local + "categoria_update.php";
            } else {
                url = Config.local + "categoria_insert.php";
            }

            ApiService.post(url, body, new ApiService.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    requireActivity().runOnUiThread(() -> {
                        Dialog.toast(requireContext(), esEditar ? "Categoría actualizada" : "Categoría creada");
                        cargarCategorias();
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
                .setTitle("Eliminar categoría")
                .setMessage("¿Eliminar \"" + nombre + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    ApiService.get(Config.local + "categoria_delete.php?id=" + id + "&usuario_id=" + Config.iduser,
                            new ApiService.ApiCallback() {
                                @Override
                                public void onSuccess(String response) {
                                    requireActivity().runOnUiThread(() -> {
                                        Dialog.toast(requireContext(), "Categoría eliminada");
                                        cargarCategorias();
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