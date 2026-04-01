package com.example.controlstock.clases;

import android.util.Log;
import android.view.View;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiService {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    // Método genérico para POST con JSON
    public static void post(String url, JSONObject data, ApiCallback callback)
    {
        RequestBody body = RequestBody.create(data.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        execute(request, callback);
    }

    // Método genérico para GET
    public static void get(String url, ApiCallback callback)
    {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        execute(request, callback);
    }

    private static void execute(Request request, ApiCallback callback)
    {
        Log.d("ApiService", "Request: " + request.url());
        client.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                Log.e("ApiService", "Error red: " + e.getMessage());
                // Llamar al callback en el hilo principal
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d("ApiService", "Response (" + response.code() + "): " + responseBody);
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> {
                    if (response.isSuccessful())
                    {
                        callback.onSuccess(responseBody);
                    } else
                    {
                        callback.onError("HTTP " + response.code() + ": " + responseBody);
                    }
                });
            }
        });
    }
    public static void login(String usuario, String clave, LoginCallback callback)
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("usuario", usuario);
            json.put("clave", clave);
        }
        catch (Exception e)
        {
            callback.onError("Error al preparar datos");
            return;
        }

        post(Config.local + "login.php", json, new ApiCallback()
        {
            @Override
            public void onSuccess(String response)
            {
                try
                {
                    if (response == null || response.trim().isEmpty())
                    {
                        callback.onError("Respuesta vacía del servidor");
                        return;
                    }

                    if (response.contains("<") || response.toLowerCase().contains("warning")
                            || response.toLowerCase().contains("fatal error"))
                    {
                        callback.onError("Error en el servidor PHP. Revisa los logs.");
                        return;
                    }

                    JSONObject res = new JSONObject(response);
                    if (res.optBoolean("success", false))
                    {
                        callback.onSuccess(res);
                    }
                    else
                    {
                        String msg = res.optString("error", "Error desconocido");
                        callback.onError(msg);
                    }
                } catch (Exception e)
                {
                    Log.e("ApiService", "Error parseando login response: " + e.getMessage());
                    callback.onError("Respuesta inválida del servidor");
                }
            }
            @Override
            public void onError(String error)
            {
                if (error.contains("HTTP 5"))callback.onError("Error del servidor: " + error);
                else if (error.contains("HTTP 4"))callback.onError("Error de solicitud: " + error);
                else callback.onError(" " + error);
            }
        });
    }
    // Interfaz para el callback de login
    public interface LoginCallback
    {
        void onSuccess(JSONObject response);
        void onError(String errorMessage);
    }
    // Interfaz para callbacks
    public interface ApiCallback
    {
        void onSuccess(String usuario);
        void onError(String error);
    }
    public static void guardarUsuario(String dni, String nombre, String apellido, String email, String usuario, String clave, String telefono, boolean activo, ApiCallback callback)
    {
        JSONObject json = new JSONObject();
        try {
            json.put("dni", dni);
            json.put("nombre", nombre);
            json.put("apellido", apellido);
            json.put("correoe", email);
            json.put("usuario", usuario);
            json.put("clave", clave);
            json.put("telefono", telefono);
            json.put("estado", activo ? "ACTIVO" : "INACTIVO"); // En PHP: 1=activo, 0=inactivo
        } catch (Exception e) {
            callback.onError("Error al preparar datos del usuario");
            return;
        }
        post(Config.local + "usuario_insert.php", json, callback);
    }
}