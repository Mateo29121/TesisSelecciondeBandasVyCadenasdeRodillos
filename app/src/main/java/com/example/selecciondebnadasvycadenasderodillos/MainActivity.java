package com.example.selecciondebnadasvycadenasderodillos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    CardView cardBandas, cardCadenas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. activamos la advertencia
        mostrarAdvertencia();

        // 2. Apuntamos a los nuevos CardView
        cardBandas = findViewById(R.id.cardBandas);
        cardCadenas = findViewById(R.id.cardCadenas);

        cardBandas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, BandasVActivity.class);
                startActivity(i);
            }
        });

        cardCadenas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, CadenasActivity.class);
                startActivity(i);
            }
        });
    }
    private void mostrarAdvertencia() {
        // 3. Usamos AlertDialog de AppCompat para compatibilidad con el tema
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Aviso");
        builder.setMessage("Esta aplicación utiliza únicamente unidades del Sistema Internacional (SI).");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
}