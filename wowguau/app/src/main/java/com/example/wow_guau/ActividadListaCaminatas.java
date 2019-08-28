package com.example.wow_guau;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class ActividadListaCaminatas extends AppCompatActivity
{
    TextView txtCaminata;
    ImageButton imgBtnLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_lista_de_caminatas);

        txtCaminata = findViewById(R.id.txtViewMap2);
        imgBtnLocation = findViewById(R.id.imgBtnLocation);

        txtCaminata.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i= new Intent(getApplicationContext(),ActividadSolicitudCaminata.class);
                startActivity(i);
            }
        });

        imgBtnLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(getApplicationContext(),ActividadSolicitudPaseador.class);
                startActivity(i);
            }
        });
    }
}
