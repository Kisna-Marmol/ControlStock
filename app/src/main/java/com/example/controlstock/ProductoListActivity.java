package com.example.controlstock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.Instant;

public class ProductoListActivity extends AppCompatActivity {

    private FloatingActionButton btnAgregar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_list);

        btnAgregar = findViewById(R.id.fabAgregar);
        funcionesBoton();
    }

    private void funcionesBoton(){
        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intento = new Intent(ProductoListActivity.this, ProductoFormActivity.class);
                startActivity(intento);
            }
        });
    }
}