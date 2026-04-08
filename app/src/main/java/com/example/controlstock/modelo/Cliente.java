package com.example.controlstock.modelo;

public class Cliente {
    private int clienteId;
    private String clienteDocumento;
    private String clienteNombre;
    private String clienteTelefono;
    private String clienteEmail;

    public Cliente() {}

    public Cliente(int clienteId, String clienteDocumento, String clienteNombre,
                   String clienteTelefono, String clienteEmail) {
        this.clienteId = clienteId;
        this.clienteDocumento = clienteDocumento;
        this.clienteNombre = clienteNombre;
        this.clienteTelefono = clienteTelefono;
        this.clienteEmail = clienteEmail;
    }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; }

    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }

    // Retorna las iniciales para el avatar
    public String getIniciales() {
        if (clienteNombre == null || clienteNombre.isEmpty()) return "?";
        String[] partes = clienteNombre.trim().split(" ");
        if (partes.length >= 2) {
            return String.valueOf(partes[0].charAt(0)).toUpperCase()
                    + String.valueOf(partes[1].charAt(0)).toUpperCase();
        }
        return String.valueOf(partes[0].charAt(0)).toUpperCase();
    }
}
