package com.example.controlstock.clases;

import android.content.Context;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Dialog
{
    public static void confirm(Context context, String title, String msg, int icon, ConfirmationDialogCallback callback)
    {
        int res=0;
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Sí", (dialog, which) -> {
                    if (callback != null) {
                        callback.onConfirm();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    if (callback != null) {
                        callback.onCancel();
                    }})
                .setIcon(icon) // Puedes usar tus propios íconos
                .show();
    }
    public static void msgbox(Context context, String title, String msg, int icon)
    {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .setIcon(icon)
                .show();
    }
    public static void toast(Context context,String mens)
    {
        Toast.makeText(context,mens,Toast.LENGTH_LONG).show();
    }

    public interface ConfirmationDialogCallback {
        void onConfirm();
        void onCancel();
    }
}