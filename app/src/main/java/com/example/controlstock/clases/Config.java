package com.example.controlstock.clases;

import java.util.ArrayList;
import java.util.List;

public class Config {
    //public static String local="http://192.168.1.104/movil1/";
    //public static String local="http://192.168.1.10/movil1/";
    //public static String local="http://192.168.10.103/movil1/";//UTH CLASE
    //public static String local="http://test.bonaquian.com/movil1/";//UTH CLASE
    //public static String usuario="";

    public static String local="http://kisna.bonaquian.com/inventario/";//UTH CLASE
    public static String usuario="";
    public static int iduser = 0;
    public static int rolId     = 0;
    public static String correo     = "";
    public static String userNombre = "";

    // Lista de accesos activos del usuario
    public static List<String> accesosActivos = new ArrayList<>();
    public static boolean esAdmin() { return rolId == 1; } // ← Helper

    // Verificar si tiene un acceso específico
    public static boolean tieneAcceso(String accesoCodigo) {
        if (esAdmin()) return true; // Admin tiene todo
        return accesosActivos.contains(accesoCodigo);
    }

}