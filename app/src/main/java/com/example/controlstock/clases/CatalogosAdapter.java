package com.example.controlstock.clases;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.controlstock.fragmentos.CategoriasFragment;
import com.example.controlstock.fragmentos.ProveedoresFragment;
import com.example.controlstock.fragmentos.UnidadesFragment;

public class CatalogosAdapter extends FragmentStateAdapter {

    public CatalogosAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1: return new ProveedoresFragment();
            case 2: return new UnidadesFragment();
            default: return new CategoriasFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}