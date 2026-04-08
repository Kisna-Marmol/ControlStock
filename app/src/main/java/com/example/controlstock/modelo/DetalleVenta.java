package com.example.controlstock.modelo;

public class DetalleVenta {
    private int productoId;
    private String productoNombre;
    private double precioUnitario;
    private int cantidad;

    public DetalleVenta(int productoId, String productoNombre, double precioUnitario, int cantidad) {
        this.productoId      = productoId;
        this.productoNombre  = productoNombre;
        this.precioUnitario  = precioUnitario;
        this.cantidad        = cantidad;
    }

    public int    getProductoId()      { return productoId; }
    public String getProductoNombre()  { return productoNombre; }
    public double getPrecioUnitario()  { return precioUnitario; }
    public int    getCantidad()        { return cantidad; }
    public void   setCantidad(int c)   { this.cantidad = c; }

    public double getSubtotalLineal()  { return precioUnitario * cantidad; }
}
