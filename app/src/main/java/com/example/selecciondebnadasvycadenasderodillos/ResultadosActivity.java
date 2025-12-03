package com.example.selecciondebnadasvycadenasderodillos;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class ResultadosActivity extends AppCompatActivity {

    private Button btnVolver;
    private TextView tvPotenciaCorregida;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultadosbandas);

        btnVolver = findViewById(R.id.btnVolver);
        tvPotenciaCorregida = findViewById(R.id.tv_potencia_corregida_resultado);

        btnVolver.setOnClickListener(v -> finish());

        // Recibir los datos de la actividad anterior
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            double potenciaCorregida = extras.getDouble("POTENCIA_CORREGIDA", 0.0);

            // Formatear a 2 decimales y mostrar
            DecimalFormat df = new DecimalFormat("#.##");
            tvPotenciaCorregida.setText(df.format(potenciaCorregida) + " Kw");
        }
    }
}