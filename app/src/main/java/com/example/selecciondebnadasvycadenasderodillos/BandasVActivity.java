package com.example.selecciondebnadasvycadenasderodillos;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BandasVActivity extends AppCompatActivity {

    // Vistas
    private Spinner spinnerImpulsor, spinnerActividad, spinnerHorasDiarias, spinnerTipoBanda;
    private TextView tvFactorServicio, tvVelocidadWarning, tvPotenciaWarning, tvDistanciaWarning;
    private Button btnRegresar, btnCalcular;
    private ImageButton btnHelpImpulsor, btnHelpActividad;
    private EditText etNe, etNs, etMg, etE, etPotencia, etDistancia;
    private List<EditText> velocidadFields;
    private List<EditText> fieldsToClearOnResume = new ArrayList<>();

    // Almacenamiento de datos
    private String[] impulsorDescriptions;
    private List<String> actividadTipos = new ArrayList<>();
    private List<String> actividadClasificaciones = new ArrayList<>();
    private double[][][] factorServicioData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bandas_v);

        initViews();
        loadCSVData();
        setupSpinners();
        setupInputValidation();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!fieldsToClearOnResume.isEmpty()) {
            for (EditText field : fieldsToClearOnResume) {
                field.setText("");
            }
            fieldsToClearOnResume.clear();
            updateVelocidadFieldsState();
        }
    }

    private void initViews() {
        spinnerTipoBanda = findViewById(R.id.spinner_tipo_banda);
        spinnerImpulsor = findViewById(R.id.spinner_impulsor);
        spinnerActividad = findViewById(R.id.spinner_actividad);
        spinnerHorasDiarias = findViewById(R.id.spinner_horas_diarias);
        tvFactorServicio = findViewById(R.id.tv_factor_servicio);
        tvVelocidadWarning = findViewById(R.id.tv_velocidad_warning);
        tvPotenciaWarning = findViewById(R.id.tv_potencia_warning);
        tvDistanciaWarning = findViewById(R.id.tv_distancia_warning);
        btnRegresar = findViewById(R.id.btnRegresar);
        btnCalcular = findViewById(R.id.btnCalcular);
        btnHelpImpulsor = findViewById(R.id.btn_help_impulsor);
        btnHelpActividad = findViewById(R.id.btn_help_actividad);
        etNe = findViewById(R.id.et_ne);
        etNs = findViewById(R.id.et_ns);
        etMg = findViewById(R.id.et_mg);
        etE = findViewById(R.id.et_e);
        etPotencia = findViewById(R.id.et_potencia);
        etDistancia = findViewById(R.id.et_distancia);
        velocidadFields = Arrays.asList(etNe, etNs, etMg, etE);
    }

    private void setupInputValidation() {
        TextWatcher velocidadWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateVelocidadFieldsState();
            }
        };
        for (EditText field : velocidadFields) {
            field.addTextChangedListener(velocidadWatcher);
        }
    }

    private void updateVelocidadFieldsState() {
        int filledCount = 0;
        for (EditText field : velocidadFields) {
            if (!field.getText().toString().isEmpty()) filledCount++;
        }

        boolean limitReached = filledCount >= 2;

        for (EditText field : velocidadFields) {
            boolean isCurrentlyEmpty = field.getText().toString().isEmpty();
            boolean shouldBeDisabled = false;

            if (limitReached && isCurrentlyEmpty) {
                shouldBeDisabled = true;
            } else if (filledCount == 1) {
                if (!etMg.getText().toString().isEmpty() && field.equals(etE))
                    shouldBeDisabled = true;
                else if (!etE.getText().toString().isEmpty() && field.equals(etMg))
                    shouldBeDisabled = true;
            }

            field.setEnabled(!shouldBeDisabled);
            if (shouldBeDisabled) {
                field.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.warning_red), PorterDuff.Mode.SRC_ATOP);
            } else {
                field.getBackground().clearColorFilter();
            }
        }
        if (!limitReached) {
            for (EditText field : velocidadFields) {
                field.getBackground().clearColorFilter();
            }
        }
    }


    private void setupListeners() {
        btnRegresar.setOnClickListener(v -> finish());
        btnCalcular.setOnClickListener(v -> {
            fieldsToClearOnResume.clear();
            for (EditText field : velocidadFields) {
                if (field.getText().toString().isEmpty()) fieldsToClearOnResume.add(field);
            }
            calculateVelocidades();

            if (isInputValid()) {
                double potenciaCorregida = getPotenciaCorregida();
                double ne = Double.parseDouble(etNe.getText().toString());
                double mg = Double.parseDouble(etMg.getText().toString());
                double dc = Double.parseDouble(etDistancia.getText().toString());
                String tipoBanda = spinnerTipoBanda.getSelectedItem().toString();

                Intent intent = new Intent(BandasVActivity.this, ResultadosBandasActivity.class);
                intent.putExtra("POTENCIA_CORREGIDA", potenciaCorregida);
                intent.putExtra("NE_RPM", ne);
                intent.putExtra("MG_RELACION", mg);
                intent.putExtra("DISTANCIA_CENTROS", dc);
                intent.putExtra("TIPO_BANDA", tipoBanda);

                intent.putExtra("POTENCIA_NOMINAL_STR", etPotencia.getText().toString() + " Kw");
                intent.putExtra("FACTOR_SERVICIO_STR", tvFactorServicio.getText().toString());
                intent.putExtra("NE_RPM_STR", etNe.getText().toString() + " RPM");
                intent.putExtra("NS_RPM_STR", etNs.getText().toString() + " RPM");
                intent.putExtra("MG_STR", etMg.getText().toString());
                intent.putExtra("DISTANCIA_STR", etDistancia.getText().toString() + " mm");
                intent.putExtra("TIPO_BANDA_STR", spinnerTipoBanda.getSelectedItem().toString());

                startActivity(intent);
            }
        });

        btnHelpImpulsor.setOnClickListener(v -> {
            if (spinnerImpulsor.getSelectedItemPosition() < impulsorDescriptions.length) {
                showHelpDialog("Descripción del Impulsor", impulsorDescriptions[spinnerImpulsor.getSelectedItemPosition()]);
            }
        });
        btnHelpActividad.setOnClickListener(v -> {
            if (spinnerActividad.getSelectedItemPosition() < actividadClasificaciones.size()) {
                showHelpDialog("Clasificación de Carga", actividadClasificaciones.get(spinnerActividad.getSelectedItemPosition()));
            }
        });
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateAndShowFactor();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinnerImpulsor.setOnItemSelectedListener(listener);
        spinnerActividad.setOnItemSelectedListener(listener);
        spinnerHorasDiarias.setOnItemSelectedListener(listener);
    }

    // --- MÉTODO CON NUEVOS LÍMITES ---
    private boolean isInputValid() {
        boolean isValid = true;

        // 1. Validar Potencia (0 < P <= 1000)
        String potenciaStr = etPotencia.getText().toString();
        if (potenciaStr.isEmpty()) {
            tvPotenciaWarning.setText("La potencia es obligatoria");
            tvPotenciaWarning.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            try {
                double potencia = Double.parseDouble(potenciaStr);
                if (potencia <= 0 || potencia > 1000) { // Límite agregado
                    tvPotenciaWarning.setText("La potencia debe ser > 0 y <= 1000 Kw");
                    tvPotenciaWarning.setVisibility(View.VISIBLE);
                    isValid = false;
                } else {
                    tvPotenciaWarning.setVisibility(View.GONE);
                }
            } catch (NumberFormatException e) {
                tvPotenciaWarning.setText("Valor inválido");
                tvPotenciaWarning.setVisibility(View.VISIBLE);
                isValid = false;
            }
        }

        // 2. Validar Distancia (C > 60)
        String distanciaStr = etDistancia.getText().toString();
        if (distanciaStr.isEmpty()) {
            tvDistanciaWarning.setText("La distancia es obligatoria");
            tvDistanciaWarning.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            try {
                double distancia = Double.parseDouble(distanciaStr);
                if (distancia <= 60) { // Límite agregado
                    tvDistanciaWarning.setText("La distancia debe ser mayor a 60 mm");
                    tvDistanciaWarning.setVisibility(View.VISIBLE);
                    isValid = false;
                } else {
                    tvDistanciaWarning.setVisibility(View.GONE);
                }
            } catch (NumberFormatException e) {
                tvDistanciaWarning.setText("Valor inválido");
                tvDistanciaWarning.setVisibility(View.VISIBLE);
                isValid = false;
            }
        }

        // 3. Validar Existencia de Datos de Velocidad
        // Primero verificamos que haya datos suficientes para calcular
        int filledCount = 0;
        for (EditText field : velocidadFields) {
            if (!field.getText().toString().isEmpty()) {
                filledCount++;
            }
        }

        if (filledCount < 2) { // Se necesitan al menos 2 datos para calcular el resto
            tvVelocidadWarning.setText("Debe ingresar al menos 2 datos de velocidad");
            tvVelocidadWarning.setVisibility(View.VISIBLE);
            return false; // Si no hay datos, no podemos validar rangos
        }

        // 4. Validar Rangos Lógicos de Velocidad (Ne, Ns, Mg)
        // Nota: Como 'calculateVelocidades()' se llama ANTES de 'isInputValid()' en el botón,
        // podemos asumir que los campos ya tienen valores si filledCount >= 2.
        try {
            // Parseamos los valores (usando 0 si por alguna razón siguen vacíos para evitar crash)
            double ne = etNe.getText().toString().isEmpty() ? 0 : Double.parseDouble(etNe.getText().toString());
            double ns = etNs.getText().toString().isEmpty() ? 0 : Double.parseDouble(etNs.getText().toString());
            double mg = etMg.getText().toString().isEmpty() ? 0 : Double.parseDouble(etMg.getText().toString());

            // Validación A: Ne entre 100 y 6000
            if (ne < 100 || ne > 6000) {
                tvVelocidadWarning.setText("Ne debe estar entre 100 y 6000 RPM");
                tvVelocidadWarning.setVisibility(View.VISIBLE);
                return false;
            }

            // Validación B: Ns debe ser menor a Ne
            if (ns > ne) {
                tvVelocidadWarning.setText("Ns debe ser menor a Ne (Sistema Reductor)");
                tvVelocidadWarning.setVisibility(View.VISIBLE);
                return false;
            }

            // Validación C: Mg mayor o igual a 1
            if (mg < 1) {
                tvVelocidadWarning.setText("La relación Mg debe ser mayor o igual a 1");
                tvVelocidadWarning.setVisibility(View.VISIBLE);
                return false;
            }

            // Si pasa todo:
            tvVelocidadWarning.setVisibility(View.GONE);

        } catch (NumberFormatException e) {
            tvVelocidadWarning.setText("Error en el formato de números");
            tvVelocidadWarning.setVisibility(View.VISIBLE);
            return false;
        }

        return isValid;
    }
    // -----------------------

    private double getPotenciaCorregida() {
        try {
            double potencia = Double.parseDouble(etPotencia.getText().toString());
            double factorServicio = Double.parseDouble(tvFactorServicio.getText().toString());
            return potencia * factorServicio;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void calculateVelocidades() {
        String neStr = etNe.getText().toString();
        String nsStr = etNs.getText().toString();
        String mgStr = etMg.getText().toString();
        String eStr = etE.getText().toString();
        Double ne = neStr.isEmpty() ? null : Double.valueOf(neStr);
        Double ns = nsStr.isEmpty() ? null : Double.valueOf(nsStr);
        Double mg = mgStr.isEmpty() ? null : Double.valueOf(mgStr);
        Double e = eStr.isEmpty() ? null : Double.valueOf(eStr);
        DecimalFormat df = new DecimalFormat("#.###");
        if (ne != null && ns != null) {
            mg = ne / ns;
            e = 1 / mg;
            etMg.setText(df.format(mg));
            etE.setText(df.format(e));
        } else if (ne != null && mg != null) {
            ns = ne / mg;
            e = 1 / mg;
            etNs.setText(df.format(ns));
            etE.setText(df.format(e));
        } else if (ne != null && e != null) {
            mg = 1 / e;
            ns = ne / mg;
            etMg.setText(df.format(mg));
            etNs.setText(df.format(ns));
        } else if (ns != null && mg != null) {
            ne = ns * mg;
            e = 1 / mg;
            etNe.setText(df.format(ne));
            etE.setText(df.format(e));
        } else if (ns != null && e != null) {
            mg = 1 / e;
            ne = ns * mg;
            etMg.setText(df.format(mg));
            etNe.setText(df.format(ne));
        }
    }

    private void loadCSVData() {
        impulsorDescriptions = getResources().getStringArray(R.array.impulsor_descriptions_array);
        List<List<double[]>> allData = new ArrayList<>();
        allData.add(new ArrayList<>());
        allData.add(new ArrayList<>());
        try (InputStream inputStream = getAssets().open("factores_bandas.csv"); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split("\\|");
                if (columns.length < 8) continue;
                actividadTipos.add(columns[0]);
                actividadClasificaciones.add(columns[1]);
                allData.get(0).add(new double[]{Double.parseDouble(columns[2].replace(',', '.')), Double.parseDouble(columns[3].replace(',', '.')), Double.parseDouble(columns[4].replace(',', '.'))});
                allData.get(1).add(new double[]{Double.parseDouble(columns[5].replace(',', '.')), Double.parseDouble(columns[6].replace(',', '.')), Double.parseDouble(columns[7].replace(',', '.'))});
            }
            factorServicioData = new double[2][actividadTipos.size()][3];
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < actividadTipos.size(); j++) {
                    factorServicioData[i][j] = allData.get(i).get(j);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSpinners() {
        String[] tiposBanda = {"Bandas Lisas", "Bandas Dentadas"};
        ArrayAdapter<String> tipoBandaAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, tiposBanda);
        tipoBandaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoBanda.setAdapter(tipoBandaAdapter);

        ArrayAdapter<CharSequence> impulsorAdapter = ArrayAdapter.createFromResource(this, R.array.impulsores_array, R.layout.custom_spinner_item);
        ArrayAdapter<String> actividadAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, actividadTipos);
        ArrayAdapter<CharSequence> horasDiariasAdapter = ArrayAdapter.createFromResource(this, R.array.horas_diarias_array, R.layout.custom_spinner_item);

        impulsorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actividadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        horasDiariasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerImpulsor.setAdapter(impulsorAdapter);
        spinnerActividad.setAdapter(actividadAdapter);
        spinnerHorasDiarias.setAdapter(horasDiariasAdapter);
    }

    private void showHelpDialog(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("Aceptar", null).show();
    }
//Calcula el Factor de Servicio cruzando los parámetros seleccionados.
//Evita la subjetividad al usar índices predefinidos en lugar de entrada manual.

    private void calculateAndShowFactor() {
        // 1. Validación de integridad: Asegurar que la matriz de datos existe
        if (factorServicioData == null) return;

        // 2. Captura de la selección del usuario (Índices de los Menús Desplegables)
        // Se obtienen las coordenadas [x, y, z] basadas en la elección del usuario.
        int impulsorIndex = spinnerImpulsor.getSelectedItemPosition();   // Tipo de Motor
        int actividadIndex = spinnerActividad.getSelectedItemPosition(); // Tipo de Carga
        int horasIndex = spinnerHorasDiarias.getSelectedItemPosition();  // Duración

        // 3. Cruce de datos internos (Matriz Tridimensional)
        // Se verifica que los índices estén dentro de los límites de la matriz para evitar errores.
        if (impulsorIndex < factorServicioData.length &&
                actividadIndex < factorServicioData[impulsorIndex].length &&
                horasIndex < factorServicioData[impulsorIndex][actividadIndex].length) {

            // Extracción del factor exacto según la norma (RMA/MPTA)
            // Coordenadas: [Impulsor] -> [Actividad] -> [Horas]
            double factor = factorServicioData[impulsorIndex][actividadIndex][horasIndex];

            // 4. Presentación del resultado en la interfaz
            tvFactorServicio.setText(String.valueOf(factor));
        }
    }
}