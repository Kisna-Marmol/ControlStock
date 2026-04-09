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

public class UnidadesFragment extends Fragment {

    private LinearLayout layoutLista, layoutVacio;
    private ProgressBar progressBar;
    private FloatingActionButton fabAgregar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unidades, container, false);

        layoutLista = view.findViewById(R.id.layoutLista);
        layoutVacio = view.findViewById(R.id.layoutVacio);
        progressBar = view.findViewById(R.id.progressBar);
        fabAgregar  = view.findViewById(R.id.fabAgregar);

        fabAgregar.setOnClickListener(v -> mostrarDialogo(null, -1));
        cargarUnidades();

        return view;
    }

    private void cargarUnidades() {
        progressBar.setVisibility(View.VISIBLE);
        layoutLista.removeAllViews();

        ApiService.get(Config.local + "unidad_list.php", new ApiService.ApiCallback() {
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
                        Dialog.toast(requireContext(), "Error cargando unidades");
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

    private void agregarItem(JSONObject um) {
        try {
            int id      = um.optInt("um_id");
            String nombre = um.optString("um_nombre", "");
            String abrev  = um.optString("um_abreviatura", "");

            View item = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_unidad, layoutLista, false);

            TextView tvNombre      = item.findViewById(R.id.tvNombre);
            TextView tvAbreviatura = item.findViewById(R.id.tvAbreviatura);
            View btnEditar         = item.findViewById(R.id.btnEditar);
            View btnEliminar       = item.findViewById(R.id.btnEliminar);

            tvNombre.setText(nombre);
            tvAbreviatura.setText(abrev);

            btnEditar.setOnClickListener(v -> mostrarDialogo(um, id));
            btnEliminar.setOnClickListener(v -> confirmarEliminar(id, nombre));

            layoutLista.addView(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDialogo(@Nullable JSONObject um, int id) {
        boolean esEditar = um != null;

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_unidad, null);

        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etAbrev  = dialogView.findViewById(R.id.etAbreviatura);

        if (esEditar) {
            etNombre.setText(um.optString("um_nombre", ""));
            etAbrev.setText(um.optString("um_abreviatura", ""));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(esEditar ? "Editar unidad" : "Nueva unidad")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String abrev  = etAbrev.getText().toString().trim();
                    if (nombre.isEmpty() || abrev.isEmpty()) {
                        Dialog.toast(requireContext(), "Completa todos los campos");
                        return;
                    }
                    guardar(id, nombre, abrev, esEditar);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardar(int id, String nombre, String abrev, boolean esEditar) {
        try {
            JSONObject body = new JSONObject();
            body.put("um_nombre",       nombre);
            body.put("um_abreviatura",  abrev);

            String url;
            if (esEditar) {
                body.put("um_id", id);
                url = Config.local + "unidad_update.php";
            } else {
                url = Config.local + "unidad_insert.php";
            }

            ApiService.post(url, body, new ApiService.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    requireActivity().runOnUiThread(() -> {
                        Dialog.toast(requireContext(), esEditar ? "Unidad actualizada" : "Unidad creada");
                        cargarUnidades();
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
                .setTitle("Eliminar unidad")
                .setMessage("¿Eliminar \"" + nombre + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    ApiService.get(Config.local + "unidad_delete.php?id=" + id,
                            new ApiService.ApiCallback() {
                                @Override
                                public void onSuccess(String response) {
                                    requireActivity().runOnUiThread(() -> {
                                        Dialog.toast(requireContext(), "Unidad eliminada");
                                        cargarUnidades();
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