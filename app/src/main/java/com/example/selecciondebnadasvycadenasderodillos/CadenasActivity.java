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
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class CadenasActivity extends AppCompatActivity {

    // Vistas
    private Spinner spinnerEntradaPoder, spinnerActividad;
    private Spinner spinnerLubricacion;
    private Spinner spinnerTipoCadena;

    private TextView tvFactorServicio, tvVelocidadWarning, tvPotenciaWarning, tvDistanciaWarning, tv_z1_warning;
    private Button btnRegresar, btnCalcular;
    private ImageButton btnHelpActividad;
    private EditText etNe, etNs, etMg, etE, etPotencia, etDistancia, etZ1, etZ2;
    private List<EditText> velocidadFields;
    private List<EditText> fieldsToClearOnResume = new ArrayList<>();

    private List<String> tiposDeCarga = new ArrayList<>();
    private List<String> clasificaciones = new ArrayList<>();
    private List<String> tiposDeEntrada = new ArrayList<>();
    private List<String[]> factorFlData = new ArrayList<>();
    private double[][] factorServicioData;

    private final LinkedHashMap<String, String> tipoCadenaMap = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadenas);

        initViews();
        loadCSVData();
        setupSpinners();
        setupTipoCadenaSpinner();
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
        spinnerEntradaPoder = findViewById(R.id.spinner_entrada_poder);
        spinnerActividad = findViewById(R.id.spinner_actividad);
        tvFactorServicio = findViewById(R.id.tv_factor_servicio);
        tvVelocidadWarning = findViewById(R.id.tv_velocidad_warning);
        tvPotenciaWarning = findViewById(R.id.tv_potencia_warning);
        tvDistanciaWarning = findViewById(R.id.tv_distancia_warning);
        btnRegresar = findViewById(R.id.btnRegresar);
        btnCalcular = findViewById(R.id.btnCalcular);
        btnHelpActividad = findViewById(R.id.btn_help_actividad);
        etNe = findViewById(R.id.et_ne);
        etNs = findViewById(R.id.et_ns);
        etMg = findViewById(R.id.et_mg);
        etE = findViewById(R.id.et_e);
        etPotencia = findViewById(R.id.et_potencia);
        etDistancia = findViewById(R.id.et_distancia);
        tv_z1_warning = findViewById(R.id.tv_z1_warning);
        etZ1 = findViewById(R.id.et_z1);
        etZ2 = findViewById(R.id.et_z2);
        etZ1.setText("19");
        velocidadFields = Arrays.asList(etNe, etNs, etMg, etE);
        spinnerLubricacion = findViewById(R.id.spinner_lubricacion);
        spinnerTipoCadena = findViewById(R.id.spinner_tipo_cadena);

        updateDientesFieldsState();
    }

    private void setupInputValidation() {
        TextWatcher velocidadWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                updateVelocidadFieldsState();
            }
        };
        for (EditText field : velocidadFields) {
            field.addTextChangedListener(velocidadWatcher);
        }

        TextWatcher dientesWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                updateDientesFieldsState();
                // isInputValid(); // No llamar aquí para evitar warnings prematuros mientras escribe
            }
        };
        etZ1.addTextChangedListener(dientesWatcher);
        etZ2.addTextChangedListener(dientesWatcher);
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
                if (!etMg.getText().toString().isEmpty() && field.equals(etE)) shouldBeDisabled = true;
                else if (!etE.getText().toString().isEmpty() && field.equals(etMg)) shouldBeDisabled = true;
            }
            field.setEnabled(!shouldBeDisabled);
            if (shouldBeDisabled) {
                field.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.warning_red), PorterDuff.Mode.SRC_ATOP);
            } else {
                field.getBackground().clearColorFilter();
            }
        }
    }

    private void updateDientesFieldsState() {
        String z1Text = etZ1.getText().toString();
        String z2Text = etZ2.getText().toString();
        boolean disableZ2 = !z1Text.isEmpty();
        boolean disableZ1 = !z2Text.isEmpty();

        etZ1.setEnabled(!disableZ1);
        if (disableZ1) {
            etZ1.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.warning_red), PorterDuff.Mode.SRC_ATOP);
        } else {
            etZ1.getBackground().clearColorFilter();
        }

        etZ2.setEnabled(!disableZ2);
        if (disableZ2) {
            etZ2.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.warning_red), PorterDuff.Mode.SRC_ATOP);
        } else {
            etZ2.getBackground().clearColorFilter();
        }
    }

    private void setupListeners() {
        btnRegresar.setOnClickListener(v -> finish());
        btnCalcular.setOnClickListener(v -> {
            fieldsToClearOnResume.clear();
            for(EditText field : velocidadFields){
                if(field.getText().toString().isEmpty()) fieldsToClearOnResume.add(field);
            }
            calculateVelocidades();

            // 1. Validaciones generales (Potencia, Distancia, Velocidades)
            if (isInputValid()) {

                // 2. VALIDACIÓN Y CÁLCULO DE DIENTES
                int[] dientes = calculateAndGetDientes();

                // Si dientes es null, significa que falló la validación de rango (11-25)
                // o faltan datos, así que NO entramos al if.
                if (dientes != null) {
                    int z1 = dientes[0];
                    int z2 = dientes[1];

                    // --- Procedemos con el Intent (Código original) ---
                    double potenciaDiseno = getPotenciaDeDiseno();
                    double neRpm = Double.parseDouble(etNe.getText().toString());
                    double distancia = Double.parseDouble(etDistancia.getText().toString());
                    double factorServicio = Double.parseDouble(tvFactorServicio.getText().toString());

                    // ... resto de tus puts extra ...
                    String potenciaNominalStr = etPotencia.getText().toString() + " Kw";
                    // ... etc ...

                    Intent intent = new Intent(CadenasActivity.this, ResultadosCadenasActivity.class);
                    intent.putExtra("POTENCIA_DISENO", potenciaDiseno);
                    intent.putExtra("NE_RPM", neRpm);

                    // USAMOS LOS DIENTES VALIDADOS
                    intent.putExtra("Z1", z1);
                    intent.putExtra("Z2", z2);

                    intent.putExtra("DISTANCIA_CENTROS", distancia);

                    // ... resto del código del intent ...
                    String tipoLubricacion = spinnerLubricacion.getSelectedItem().toString();
                    intent.putExtra("TIPO_LUBRICACION", tipoLubricacion);

                    // ... (resto de tus extras) ...
                    String nombreAmigableSeleccionado = spinnerTipoCadena.getSelectedItem().toString();
                    String archivoCsvSeleccionado = tipoCadenaMap.get(nombreAmigableSeleccionado);
                    intent.putExtra("TIPO_CADENA_CSV", archivoCsvSeleccionado);
                    intent.putExtra("TIPO_CADENA_NOMBRE", nombreAmigableSeleccionado);
                    intent.putExtra("FACTOR_SERVICIO", factorServicio);

                    // Strings para PDF
                    intent.putExtra("POTENCIA_NOMINAL_STR", etPotencia.getText().toString() + " Kw");
                    intent.putExtra("FACTOR_SERVICIO_STR", tvFactorServicio.getText().toString());
                    intent.putExtra("NE_RPM_STR", etNe.getText().toString() + " RPM");
                    intent.putExtra("NS_RPM_STR", etNs.getText().toString() + " RPM");
                    intent.putExtra("MG_STR", etMg.getText().toString());
                    intent.putExtra("Z2_STR", String.valueOf(z2));

                    double potenciaNominal = 0.0;
                    try { potenciaNominal = Double.parseDouble(etPotencia.getText().toString()); } catch (Exception e) {}
                    intent.putExtra("POTENCIA_NOMINAL", potenciaNominal);

                    startActivity(intent);
                    // -------------------------------------------------
                }
                else {
                    // Si retornó null y no hay mensaje visible, es porque faltaban datos de dientes
                    if (etZ1.getText().toString().isEmpty() && etZ2.getText().toString().isEmpty()) {
                        tv_z1_warning.setText("Ingrese Z1 o Z2");
                        tv_z1_warning.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        btnHelpActividad.setOnClickListener(v -> showHelpDialog());

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { calculateAndShowFactor(); }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        };
        spinnerEntradaPoder.setOnItemSelectedListener(listener);
        spinnerActividad.setOnItemSelectedListener(listener);
    }

    // --- MÉTODO DE VALIDACIÓN FINAL (Con límite Mg 1-7) ---
    private boolean isInputValid() {
        boolean isValid = true;

// 1. Validación de Dientes (Z1 y Z2)
        boolean z1EsValido = false;
        int z1Valor = 0;

        // --- Validar Z1 ---
        if (!etZ1.getText().toString().isEmpty()) {
            try {
                z1Valor = Integer.parseInt(etZ1.getText().toString());
                // Tu restricción original para Z1
                if (z1Valor < 11 || z1Valor > 25) {
                    tv_z1_warning.setText("Z1 debe estar entre 11 y 25 dientes");
                    tv_z1_warning.setVisibility(View.VISIBLE);
                    isValid = false;
                } else {
                    tv_z1_warning.setVisibility(View.GONE);
                    z1EsValido = true;
                }
            } catch (NumberFormatException e) {
                tv_z1_warning.setText("Z1 debe ser un número entero");
                tv_z1_warning.setVisibility(View.VISIBLE);
                isValid = false;
            }
        }

        // --- Validar Z2 (Solo si el usuario escribió algo) ---
        if (!etZ2.getText().toString().isEmpty()) {
            try {
                int z2Valor = Integer.parseInt(etZ2.getText().toString());


                // Regla B: No puede ser 0 o negativo
                if (z2Valor <= 0) {
                    tv_z1_warning.setText("Z2 debe ser mayor a 0");
                    tv_z1_warning.setVisibility(View.VISIBLE);
                    isValid = false;
                }
                // Regla C: Coherencia (Si Z1 es válido, Z2 debe ser mayor para reducir)
                else if (z1EsValido && z2Valor <= z1Valor) {
                    tv_z1_warning.setText("Z2 debe ser mayor que Z1 para reducir velocidad");
                    tv_z1_warning.setVisibility(View.VISIBLE);
                    isValid = false;
                }
                // Si todo está bien y Z1 también estaba bien, ocultamos el error
                else if (isValid && z1EsValido) {
                    tv_z1_warning.setVisibility(View.GONE);
                }

            } catch (NumberFormatException e) {
                tv_z1_warning.setText("Z2 debe ser un número entero");
                tv_z1_warning.setVisibility(View.VISIBLE);
                isValid = false;
            }
        }

        // 2. Validación Potencia (0.1 a 500 Kw)
        String potenciaStr = etPotencia.getText().toString();
        if (potenciaStr.isEmpty()) {
            tvPotenciaWarning.setText("Ingrese la potencia");
            tvPotenciaWarning.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            try {
                double potencia = Double.parseDouble(potenciaStr);
                if (potencia < 0.1 || potencia > 500) {
                    tvPotenciaWarning.setText("La potencia debe estar entre 0.1 y 500 Kw");
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

        // 3. Validación Distancia (> 0)
        String distanciaStr = etDistancia.getText().toString();
        if (distanciaStr.isEmpty()) {
            tvDistanciaWarning.setText("Ingrese la distancia entre centros");
            tvDistanciaWarning.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            try {
                double distancia = Double.parseDouble(distanciaStr);
                if (distancia <= 0) {
                    tvDistanciaWarning.setText("La distancia debe ser mayor a 0 mm");
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

        // 4. Validación Completa de Velocidades (Ne, Ns, Mg)
        int filledCount = 0;
        for (EditText field : velocidadFields) {
            if (!field.getText().toString().isEmpty()) filledCount++;
        }

        if (filledCount < 2) {
            tvVelocidadWarning.setText("Faltan datos de velocidad para el cálculo");
            tvVelocidadWarning.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            try {
                // Parseamos valores (usando 0.0 si están vacíos para evitar crash)
                double ne = etNe.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(etNe.getText().toString());
                double ns = etNs.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(etNs.getText().toString());
                double mg = etMg.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(etMg.getText().toString());

                // Regla A: Ne entre 1 y 5000
                if (ne < 1 || ne > 5000) {
                    tvVelocidadWarning.setText("Ne debe estar entre 1 y 5000 RPM");
                    tvVelocidadWarning.setVisibility(View.VISIBLE);
                    return false;
                }

                // Regla B: Ne > Ns (Sistema Reductor)
                if (ns > ne) {
                    tvVelocidadWarning.setText("Ne debe ser mayor que Ns (Sistema Reductor)");
                    tvVelocidadWarning.setVisibility(View.VISIBLE);
                    return false;
                }

                // Regla C: Mg entre 1 y 7 (NUEVA REGLA)
                if (mg < 1 || mg > 7) {
                    tvVelocidadWarning.setText("La relación mg debe estar entre 1 y 7");
                    tvVelocidadWarning.setVisibility(View.VISIBLE);
                    return false;
                }

                // Regla D: Validaciones básicas positivas
                if (ns <= 0) {
                    tvVelocidadWarning.setText("Las velocidades deben ser mayores a 0");
                    tvVelocidadWarning.setVisibility(View.VISIBLE);
                    return false;
                }

                // Si pasa todo
                tvVelocidadWarning.setVisibility(View.GONE);

            } catch (NumberFormatException e) {
                tvVelocidadWarning.setText("Error en formato de velocidades");
                tvVelocidadWarning.setVisibility(View.VISIBLE);
                isValid = false;
            }
        }

        return isValid;
    }

    private double getPotenciaDeDiseno() {
        try {
            double potencia = Double.parseDouble(etPotencia.getText().toString());
            double factorServicio = Double.parseDouble(tvFactorServicio.getText().toString());
            return potencia * factorServicio;
        } catch (NumberFormatException e) {
            return 0;
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
        if (ne != null && ns != null) { mg = ne / ns; e = 1 / mg; etMg.setText(df.format(mg)); etE.setText(df.format(e)); } else if (ne != null && mg != null) { ns = ne / mg; e = 1 / mg; etNs.setText(df.format(ns)); etE.setText(df.format(e)); } else if (ne != null && e != null) { mg = 1 / e; ns = ne / mg; etMg.setText(df.format(mg)); etNs.setText(df.format(ns)); } else if (ns != null && mg != null) { ne = ns * mg; e = 1 / mg; etNe.setText(df.format(ne)); etE.setText(df.format(e)); } else if (ns != null && e != null) { mg = 1 / e; ne = ns * mg; etMg.setText(df.format(mg)); etNe.setText(df.format(ne)); }
    }

    private void loadCSVData() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("factores_cadenas.csv")))) {
            String line = reader.readLine();
            String[] headers = line.split("\\|");
            for (int i = 2; i < headers.length; i++) {
                tiposDeEntrada.add(headers[i]);
            }
            List<double[]> dataRows = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split("\\|");
                if (columns.length > 2) {
                    tiposDeCarga.add(columns[0]);
                    clasificaciones.add(columns[1].replace("\\\\n", "\n"));
                    double[] rowValues = new double[tiposDeEntrada.size()];
                    for (int i = 0; i < tiposDeEntrada.size(); i++) {
                        rowValues[i] = Double.parseDouble(columns[i + 2].replace(',', '.'));
                    }
                    dataRows.add(rowValues);
                }
            }
            factorServicioData = new double[dataRows.size()][];
            for (int i = 0; i < dataRows.size(); i++) {
                factorServicioData[i] = dataRows.get(i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("factor_fl.csv")))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                factorFlData.add(line.split(";"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSpinners() {
        if (this == null || tiposDeEntrada.isEmpty() || tiposDeCarga.isEmpty()) return;
        ArrayAdapter<String> entradaPoderAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, tiposDeEntrada);
        entradaPoderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEntradaPoder.setAdapter(entradaPoderAdapter);
        ArrayAdapter<String> actividadAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, tiposDeCarga);
        actividadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActividad.setAdapter(actividadAdapter);

        List<String> descripcionesLubricacion = new ArrayList<>();
        for (String[] row : factorFlData) {
            descripcionesLubricacion.add(row[0]);
        }

        ArrayAdapter<String> lubricacionAdapter = new ArrayAdapter<>(this,
                R.layout.custom_spinner_item,
                descripcionesLubricacion);
        lubricacionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLubricacion.setAdapter(lubricacionAdapter);
    }

    private void setupTipoCadenaSpinner() {
        tipoCadenaMap.put("Estándar", "Cadenas_Estandar.csv");
        tipoCadenaMap.put("Acero Inoxidable", "cadenas_acero_inoxidable.csv");
        tipoCadenaMap.put("Resistentes", "cadenas_resistentes.csv");
        tipoCadenaMap.put("Horquilla", "cadenas_horquilla.csv");
        tipoCadenaMap.put("Placa Lateral Recta", "cadenas_placa_lateral_recta.csv");
        tipoCadenaMap.put("Perno Hueco", "cadenas_perno_hueco.csv");
        tipoCadenaMap.put("Niquel Plateada", "cadenas_plateada_niquel.csv");
        tipoCadenaMap.put("Zinc Plateada", "cadenas_plateada_zinc.csv");
        tipoCadenaMap.put("Flexión Lateral (Baja)", "cadenas_flexion_lateral_baja.csv");
        tipoCadenaMap.put("Flexión Lateral (Pin Cónico)", "cadenas_flexion_lateral_pin_conico.csv");
        tipoCadenaMap.put("Perno Extendido", "cadenas_perno_extendido.csv");

        List<String> nombresAmigables = new ArrayList<>(tipoCadenaMap.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, nombresAmigables);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoCadena.setAdapter(adapter);
    }

    private void showHelpDialog() {
        int selectedActividad = spinnerActividad.getSelectedItemPosition();
        if (selectedActividad >= 0 && selectedActividad < clasificaciones.size()) {
            String helpText = clasificaciones.get(selectedActividad);
            new AlertDialog.Builder(this)
                    .setTitle("Clasificación de Carga")
                    .setMessage(helpText)
                    .setPositiveButton("Aceptar", null)
                    .show();
        }
    }

    private void calculateAndShowFactor() {
        int actividadIndex = spinnerActividad.getSelectedItemPosition();
        int entradaPoderIndex = spinnerEntradaPoder.getSelectedItemPosition();
        if (factorServicioData != null && actividadIndex < factorServicioData.length && entradaPoderIndex < factorServicioData[actividadIndex].length) {
            double factor = factorServicioData[actividadIndex][entradaPoderIndex];
            tvFactorServicio.setText(String.valueOf(factor));
        }
    }

    private int[] calculateAndGetDientes() {
        int z1Final = 0;
        int z2Final = 0;

        // Necesitamos la relación mg para calcular
        if (etMg.getText().toString().isEmpty()) {
            return null;
        }

        try {
            double i = Double.parseDouble(etMg.getText().toString());

            if (!etZ1.getText().toString().isEmpty()) {
                // Caso A: Usuario ingresó Z1 -> Calculamos Z2
                z1Final = Integer.parseInt(etZ1.getText().toString());
                z2Final = (int) Math.round(z1Final * i);
            } else if (!etZ2.getText().toString().isEmpty()) {
                // Caso B: Usuario ingresó Z2 -> Calculamos Z1
                z2Final = Integer.parseInt(etZ2.getText().toString());
                z1Final = (int) Math.round(z2Final / i);

                // (Opcional) Mostramos el Z1 calculado en la pantalla para que el usuario sepa
                // etZ1.setText(String.valueOf(z1Final));
            } else {
                // No hay datos de dientes
                return null;
            }

            // --- AQUÍ ESTÁ LA VALIDACIÓN AUMENTADA ---
            // Verificamos que Z1 (sea ingresado o calculado) esté en el rango permitido
            if (z1Final < 11 || z1Final > 25) {
                tv_z1_warning.setText("Z1 (calculado o ingresado) debe estar entre 11 y 25 dientes. Valor actual: " + z1Final);
                tv_z1_warning.setVisibility(View.VISIBLE);
                return null; // Retornamos null para bloquear el avance
            } else {
                tv_z1_warning.setVisibility(View.GONE);
            }
            // -----------------------------------------

        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }

        return new int[]{z1Final, z2Final};
    }
}