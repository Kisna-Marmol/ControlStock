package com.example.controlstock;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.controlstock.clases.Config;

public class DashboardActivity extends AppCompatActivity {

    private LinearLayout btnProducto;
    private TextView tvNombreUsuario;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Recibir datos del login
        String nombreUsuario = getIntent().getStringExtra("Nombre_Usuario");
        int userId = getIntent().getIntExtra("User_ID", 0);

        // Guardar para usar en otras pantallas
        Config.usuario = nombreUsuario;

        // Mostrar en el header
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
        if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
            tvNombreUsuario.setText(nombreUsuario);
        }

        btnProducto = findViewById(R.id.btnAccesoProductos);
        accesos();
    }

    private void accesos(){
        btnProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intento = new Intent(DashboardActivity.this, ProductoListActivity.class);
                startActivity(intento);
            }
        });
    }
}