package com.example.controlstock;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class Login extends AppCompatActivity {

    EditText etEmail, etPassword;
    ImageView eye;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        eye = findViewById(R.id.iv_toggle_password);
        mostrarPassword();

    }

    public void mostrarPassword(){
        eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    // Mostrar contraseña
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    eye.setImageResource(R.drawable.ct_eye_off); // icono ojo cerrado
                } else {
                    // Ocultar contraseña
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    eye.setImageResource(R.drawable.ct_eye); // icono ojo abierto
                }

                // Mantener cursor al final
                etPassword.setSelection(etPassword.getText().length());
            }
        });
    }
}