package com.example.controlstock.clases;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.controlstock.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


public class Utils {
    // ── Convertir dp a px ─────────────────────────────────
    public static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    // ── Estado visual del stock ───────────────────────────
    public static void aplicarEstadoStock(Context context, TextView tvStock,
                                          View punto, int stock) {
        if (stock <= 0) {
            tvStock.setText("Sin stock");
            tvStock.setTextColor(ContextCompat.getColor(context, R.color.rojo_negativo));
            punto.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_punto_rojo));
        } else if (stock <= 10) {
            tvStock.setText("Stock bajo (" + stock + ")");
            tvStock.setTextColor(ContextCompat.getColor(context, R.color.amber));
            punto.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_punto_amarillo));
        } else {
            tvStock.setText("En stock (" + stock + ")");
            tvStock.setTextColor(ContextCompat.getColor(context, R.color.verde_acento));
            punto.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_punto_verde));
        }
    }

    // ── Cargar spinner genérico desde JSONArray ────────────
    public static void cargarSpinner(Context context, Spinner spinner,
                                     JSONArray data, List<String> listaNombres,
                                     List<Integer> listaIds,
                                     String campoNombre, String campoId,
                                     String textoDefault) {
        try {
            listaNombres.clear();
            listaIds.clear();
            listaNombres.add(textoDefault);
            listaIds.add(0);

            for (int i = 0; i < data.length(); i++) {
                JSONObject item = data.getJSONObject(i);
                listaNombres.add(item.getString(campoNombre));
                listaIds.add(Integer.parseInt(item.getString(campoId)));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_spinner_item, listaNombres);
            adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Seleccionar item en spinner por ID ────────────────
    public static void seleccionarSpinner(Spinner spinner,
                                          List<Integer> listaIds, int idBuscado) {
        for (int i = 0; i < listaIds.size(); i++) {
            if (listaIds.get(i) == idBuscado) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    // ── Formatear fecha "2026-04-01 04:47:15" → "01/04/2026 04:47" ──
    public static String formatearFecha(String fecha) {
        try {
            java.text.SimpleDateFormat entrada =
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            java.util.Locale.getDefault());
            java.text.SimpleDateFormat salida =
                    new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm",
                            java.util.Locale.getDefault());
            return salida.format(entrada.parse(fecha));
        } catch (Exception e) {
            return fecha.length() >= 10 ? fecha.substring(0, 10) : fecha;
        }
    }
}
