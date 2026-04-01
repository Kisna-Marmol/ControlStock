package com.example.controlstock.clases;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Bitacora {
    public String bit_marca;
    public String bit_modelo;
    public String bit_hostname;
    public String bit_ip;
    public String bit_mac;
    public String bit_fecha;
    public int    user_id;
    public String bit_descrip;
    public String modulo_id;
    public String bit_actname;
    public String sql;

    public Bitacora(int user_id, String descrip, Context context, String modulo_id) {
        this.bit_marca    = getMarca();
        this.bit_modelo   = getModelo();
        this.bit_hostname = getHostName(context);
        this.bit_ip       = getIP();
        this.bit_mac      = getDirMac(context);
        this.user_id      = user_id;
        this.bit_descrip  = descrip;
        this.modulo_id    = modulo_id;
        this.bit_actname  = getNombreActivity(context);
        setSQL();
    }

    public Bitacora() {
        this.bit_marca  = getMarca();
        this.bit_modelo = getModelo();
        this.bit_ip     = getIP();
    }

    public void setSQL() {
        sql = "insert into tbl_bitacora values(0," + user_id + ",'" + modulo_id + "','" +
                bit_actname + "','" + bit_marca + "','" + bit_modelo + "','" + bit_hostname +
                "','" + bit_ip + "','" + bit_mac + "','" + bit_fecha + "','" + bit_descrip + "')";
        Log.d("SQL", sql);
    }

    public static String getNombreActivity(Context context) {
        if (context instanceof Activity) {
            return ((Activity) context).getClass().getSimpleName();
        } else {
            return "Not an Activity Context";
        }
    }

    // Método insert() ahora usa ApiService en lugar de BDAdmin
    // DESPUÉS — sin Context
    public void insert(ApiService.ApiCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("user_id",       user_id);
            json.put("modulo_codigo", modulo_id    != null ? modulo_id    : "");
            json.put("bit_actname",   bit_actname  != null ? bit_actname  : "");
            json.put("bit_marca",     bit_marca    != null ? bit_marca    : "");
            json.put("bit_modelo",    bit_modelo   != null ? bit_modelo   : "");
            json.put("bit_hostname",  bit_hostname != null ? bit_hostname : "");
            json.put("bit_hostip",    bit_ip       != null ? bit_ip       : "");
            json.put("bit_hostmac",   bit_mac      != null ? bit_mac      : "");
            json.put("bit_detalle",   bit_descrip  != null ? bit_descrip  : "");
            json.put("bit_fecha", new java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(new java.util.Date()));

            ApiService.post(Config.local + "bitacora_insert.php", json, callback);

        } catch (Exception e) {
            Log.e("BITACORA", "Error en insert(): " + e.getMessage());
        }
    }
    public String getIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String hostAddress = inetAddress.getHostAddress();
                        if (hostAddress.indexOf(':') < 0) return hostAddress;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getDirMac(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getHostName(Context context) {
        String hostname = Settings.Global.getString(context.getContentResolver(), "device_name");
        if (hostname == null) hostname = Build.MODEL;
        return hostname.replaceAll("[^a-zA-Z0-9]", "_");
    }

    public String getMarca()  { return Build.MANUFACTURER; }
    public String getModelo() { return Build.MODEL; }
}