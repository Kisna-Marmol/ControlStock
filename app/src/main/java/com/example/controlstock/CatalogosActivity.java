package com.example.controlstock;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.controlstock.clases.CatalogosAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CatalogosActivity extends AppCompatActivity {

    private ImageView btnVolver;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogos);

        btnVolver = findViewById(R.id.btnVolver);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        btnVolver.setOnClickListener(v -> finish());

        viewPager.setAdapter(new CatalogosAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Categorías");  break;
                case 1: tab.setText("Proveedores"); break;
                case 2: tab.setText("Unidades");    break;
            }
        }).attach();
    }
}