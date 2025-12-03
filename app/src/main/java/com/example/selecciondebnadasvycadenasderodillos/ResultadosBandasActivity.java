package com.example.selecciondebnadasvycadenasderodillos;

// --- INICIO IMPORTACIONES PDF ---
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
// --- FIN IMPORTACIONES PDF ---

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
// --- INICIO IMPORTACIONES PDF ---
import androidx.core.content.FileProvider;
// BORRA la línea de import com.example.selecciondebnadasvycadenasderodillos.BuildConfig;

import java.io.File;
// --- FIN IMPORTACIONES PDF ---
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
// --- INICIO IMPORTACIONES PDF ---
import java.util.LinkedHashMap;
// --- FIN IMPORTACIONES PDF ---
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResultadosBandasActivity extends AppCompatActivity {

    // Vistas de Resumen
    private TextView tvPotenciaCorregidaResumen, tvTipoBandaResumen, tvNoResultados;
    // --- INICIO VISTAS PDF ---
    private Button btnImprimir;
    // --- FIN VISTAS PDF ---

    // Contenedores de tarjetas
    private LinearLayout layoutClasica, layoutEstrechaDin, layoutEstrechaArpm,
            layoutClasicaX, layoutEstrechaDinX, layoutEstrechaArpmX;

    // Almacenamiento de datos
    private Map<String, String[]> caracteristicasBanda = new HashMap<>();
    private List<String[]> perfilesDayco = new ArrayList<>();
    private List<String[]> tablaCl_Inches = new ArrayList<>(); // tabla_cl.csv (Pulgadas)

    // --- INICIO DE NUEVAS TABLAS CL (Factor de Longitud) ---
    private List<String[]> tablaCl_Cuna = new ArrayList<>();
    private List<String[]> tablaCl_CunaX = new ArrayList<>();
    private List<String[]> tablaCl_Estrecha = new ArrayList<>();
    private List<String[]> tablaCl_EstrechaX = new ArrayList<>();
    // --- FIN DE NUEVAS TABLAS CL ---

    private List<String[]> tablaFactorAngulo = new ArrayList<>(); // Para Cy
    private List<String[]> tablaFactorArco = new ArrayList<>();   // Para C_alfa

    // --- INICIO DE NUEVAS TABLAS PB ---
    private List<String[]> tablaPbClasicas = new ArrayList<>();
    private List<String[]> tablaPbClasicaX = new ArrayList<>();
    private List<String[]> tablaPbCuña = new ArrayList<>();
    private List<String[]> tablaPbCuñaX = new ArrayList<>();
    private List<String[]> tablaPbEstrecha = new ArrayList<>();
    private List<String[]> tablaPbEstrechaX = new ArrayList<>();
    // --- FIN DE NUEVAS TABLAS PB ---

    // --- INICIO DE NUEVAS TABLAS PD ---
    private List<String[]> tablaPdClasicas = new ArrayList<>();
    private List<String[]> tablaPdClasicaX = new ArrayList<>();
    private List<String[]> tablaPdCuña = new ArrayList<>();
    private List<String[]> tablaPdCuñaX = new ArrayList<>();
    private List<String[]> tablaPdEstrecha = new ArrayList<>();
    private List<String[]> tablaPdEstrechaX = new ArrayList<>();

    // Grupos de perfiles para seleccionar la tabla PD correcta
    private Set<String> perfilesClasicas;
    private Set<String> perfilesClasicaX;
    private Set<String> perfilesCuña;
    private Set<String> perfilesCuñaX;
    private Set<String> perfilesEstrecha;
    private Set<String> perfilesEstrechaX;
    // --- FIN DE NUEVAS TABLAS PD ---

    private Set<String> perfilesEnPulgadas;

    // Formateadores
    private final DecimalFormat df = new DecimalFormat("#.##");
    private final DecimalFormat df4 = new DecimalFormat("#.####");

    // Variables de entrada (Ahora como miembros de la clase)
    private double potenciaCorregida, neRpm, mg, dc;
    // --- INICIO VARIABLES PDF ---
    // Variables para almacenar los datos de entrada para el PDF
    private String potenciaNominalStr, factorServicioStr, neRpmStr, nsRpmStr, mgStr, distanciaStr, tipoBandaStr;
    // --- FIN VARIABLES PDF ---


    // Clase interna para agrupar las vistas de UNA tarjeta
    private static class ResultadoViews {
        TextView tvPerfilBanda, tvNumeroBandas, tvDiametroMinimo, tvDiametroMaximo,
                tvVelocidadPeriferica, tvLongitudCorrea, tvCodigoBanda, tvLongitudBanda,
                tvLongitudLp, tvLongitudLe, tvDistanciaCentrosNecesaria, tvDistanciaEfectiva,
                tvPbResultado, tvPdResultado, tvPotenciaAdmisible, tvArcoContacto,
                tvFactorCorreccion, tvClResultado, tvCAlfa, tvTensionEstatica,
                tvTensionCentrifuga, tvTensionTenso, tvTensionFlojo;
    }

    // Clase interna auxiliar para ordenar datos
    private static class DataPoint implements Comparable<DataPoint> {
        double value;
        int index;
        DataPoint(double v, int i) { value = v; index = i; }
        @Override public int compareTo(DataPoint o) { return Double.compare(this.value, o.value); }
    }

    // Variables para CADA grupo de vistas
    private ResultadoViews viewsClasica = new ResultadoViews();
    private ResultadoViews viewsEstrechaDin = new ResultadoViews();
    private ResultadoViews viewsEstrechaArpm = new ResultadoViews();
    private ResultadoViews viewsClasicaX = new ResultadoViews();
    private ResultadoViews viewsEstrechaDinX = new ResultadoViews();
    private ResultadoViews viewsEstrechaArpmX = new ResultadoViews();


    // --- INICIO: VISTAS DE SEGUNDA ITERACIÓN (PARA TODAS LAS TARJETAS) ---
    // Tarjeta 1: Clásica
    private Spinner spinnerPerfilIter2Clasica;
    private EditText etNuevoDIter2Clasica;
    private Button btnRecalcularIter2Clasica;
    private LinearLayout layoutResultadosIter2Clasica;
    private ResultadoViews viewsClasicaIter2 = new ResultadoViews();

    // Tarjeta 2: Estrecha DIN
    private Spinner spinnerPerfilIter2EstrechaDin;
    private EditText etNuevoDIter2EstrechaDin;
    private Button btnRecalcularIter2EstrechaDin;
    private LinearLayout layoutResultadosIter2EstrechaDin;
    private ResultadoViews viewsEstrechaDinIter2 = new ResultadoViews();

    // Tarjeta 3: Estrecha ARPM
    private Spinner spinnerPerfilIter2EstrechaArpm;
    private EditText etNuevoDIter2EstrechaArpm;
    private Button btnRecalcularIter2EstrechaArpm;
    private LinearLayout layoutResultadosIter2EstrechaArpm;
    private ResultadoViews viewsEstrechaArpmIter2 = new ResultadoViews();

    // Tarjeta 4: Clásica X
    private Spinner spinnerPerfilIter2ClasicaX;
    private EditText etNuevoDIter2ClasicaX;
    private Button btnRecalcularIter2ClasicaX;
    private LinearLayout layoutResultadosIter2ClasicaX;
    private ResultadoViews viewsClasicaXIter2 = new ResultadoViews();

    // Tarjeta 5: Estrecha DIN X
    private Spinner spinnerPerfilIter2EstrechaDinX;
    private EditText etNuevoDIter2EstrechaDinX;
    private Button btnRecalcularIter2EstrechaDinX;
    private LinearLayout layoutResultadosIter2EstrechaDinX;
    private ResultadoViews viewsEstrechaDinXIter2 = new ResultadoViews();

    // Tarjeta 6: Estrecha ARPM X
    private Spinner spinnerPerfilIter2EstrechaArpmX;
    private EditText etNuevoDIter2EstrechaArpmX;
    private Button btnRecalcularIter2EstrechaArpmX;
    private LinearLayout layoutResultadosIter2EstrechaArpmX;
    private ResultadoViews viewsEstrechaArpmXIter2 = new ResultadoViews();
    // --- FIN: VISTAS DE SEGUNDA ITERACIÓN ---


    // --- INICIO: LISTAS DE PERFILES FILTRADAS (PARA SPINNERS) ---
    private List<String> listaPerfilesClasica = new ArrayList<>();
    private List<String> listaPerfilesEstrechaDin = new ArrayList<>();
    private List<String> listaPerfilesEstrechaArpm = new ArrayList<>();
    private List<String> listaPerfilesClasicaX = new ArrayList<>();
    private List<String> listaPerfilesEstrechaDinX = new ArrayList<>();
    private List<String> listaPerfilesEstrechaArpmX = new ArrayList<>();
    // --- FIN: LISTAS DE PERFILES FILTRADAS ---


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultadosbandas);

        perfilesEnPulgadas = new HashSet<>(Arrays.asList(
                "Z", "A", "B", "C", "D", "E",
                "ZX", "AX", "BX", "CX"
        ));

        // --- INICIO DE INICIALIZACIÓN DE GRUPOS PD/PB ---
        perfilesClasicas = new HashSet<>(Arrays.asList("Z", "A", "B", "C", "D", "E"));
        perfilesClasicaX = new HashSet<>(Arrays.asList("ZX", "AX", "BX", "CX"));
        perfilesCuña = new HashSet<>(Arrays.asList("SPZ", "SPA", "SPB", "SPC"));       // Usado para Estrecha DIN
        perfilesCuñaX = new HashSet<>(Arrays.asList("XPZ", "XPA", "XPB", "XPC"));     // Usado para Estrecha DIN X
        perfilesEstrecha = new HashSet<>(Arrays.asList("3V", "5V", "8V"));             // Usado para Estrecha ARPM
        perfilesEstrechaX = new HashSet<>(Arrays.asList("3VX", "5VX"));                // Usado para Estrecha ARPM X
        // --- FIN DE INICIALIZACIÓN DE GRUPOS PD/PB ---

        loadAllCSVs();
        initViews();
        setupIteracion2Listeners(); // Configurar listeners para la 2da iteración
        displayResults();
    }

    private void initViews() {
        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());

        // --- INICIO PDF ---
        btnImprimir = findViewById(R.id.btnImprimir);
        btnImprimir.setOnClickListener(v -> {
            try {
                LinkedHashMap<String, String> data = collectDataForPdf();
                PdfGeneratorBandas pdfGenerator = new PdfGeneratorBandas(this, data);
                File pdfFile = pdfGenerator.createPdf();

                if (pdfFile != null) {
                    openPdf(pdfFile);
                } else {
                    Toast.makeText(this, "No se pudo crear el PDF.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                // Esto atrapará CUALQUIER error (SecurityException, etc.) y evitará el crash
                Log.e("PdfImprimir", "Error total al imprimir PDF", e);
                Toast.makeText(this, "Error al imprimir: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            // --- FIN: ASEGÚRATE DE TENER ESTE TRY-CATCH ---
        });
        // --- FIN PDF ---

        tvPotenciaCorregidaResumen = findViewById(R.id.tv_potencia_corregida_resultado);
        tvTipoBandaResumen = findViewById(R.id.tv_tipo_banda_resumen);
        tvNoResultados = findViewById(R.id.tv_no_resultados);

        layoutClasica = findViewById(R.id.layout_resultado_clasica);
        layoutEstrechaDin = findViewById(R.id.layout_resultado_estrecha_din);
        layoutEstrechaArpm = findViewById(R.id.layout_resultado_estrecha_arpm);
        layoutClasicaX = findViewById(R.id.layout_resultado_clasica_x);
        layoutEstrechaDinX = findViewById(R.id.layout_resultado_estrecha_din_x);
        layoutEstrechaArpmX = findViewById(R.id.layout_resultado_estrecha_arpm_x);

        // --- Encontrar todos los IDs para cada tarjeta ---

        // Tarjeta 1: Clásica (Iteración 1)
        viewsClasica.tvPerfilBanda = findViewById(R.id.tv_perfil_banda_clasica);
        viewsClasica.tvNumeroBandas = findViewById(R.id.tv_numero_bandas_clasica);
        viewsClasica.tvDiametroMinimo = findViewById(R.id.tv_diametro_minimo_clasica);
        viewsClasica.tvDiametroMaximo = findViewById(R.id.tv_diametro_maximo_clasica);
        viewsClasica.tvVelocidadPeriferica = findViewById(R.id.tv_velocidad_periferica_clasica);
        viewsClasica.tvLongitudCorrea = findViewById(R.id.tv_longitud_correa_clasica);
        viewsClasica.tvCodigoBanda = findViewById(R.id.tv_banda_seleccionada_codigo_clasica);
        viewsClasica.tvLongitudBanda = findViewById(R.id.tv_banda_seleccionada_longitud_clasica);
        viewsClasica.tvLongitudLp = findViewById(R.id.tv_longitud_lp_clasica);
        viewsClasica.tvLongitudLe = findViewById(R.id.tv_longitud_le_clasica);
        viewsClasica.tvDistanciaCentrosNecesaria = findViewById(R.id.tv_distancia_centros_necesaria_clasica);
        viewsClasica.tvDistanciaEfectiva = findViewById(R.id.tv_distancia_efectiva_clasica);
        viewsClasica.tvPbResultado = findViewById(R.id.tv_pb_resultado_clasica);
        viewsClasica.tvPdResultado = findViewById(R.id.tv_pd_resultado_clasica);
        viewsClasica.tvPotenciaAdmisible = findViewById(R.id.tv_potencia_admisible_clasica);
        viewsClasica.tvArcoContacto = findViewById(R.id.tv_arco_contacto_clasica);
        viewsClasica.tvFactorCorreccion = findViewById(R.id.tv_factor_correccion_clasica);
        viewsClasica.tvClResultado = findViewById(R.id.tv_cl_resultado_clasica);
        viewsClasica.tvCAlfa = findViewById(R.id.tv_calfa_resultado_clasica);
        viewsClasica.tvTensionEstatica = findViewById(R.id.tv_tension_estatica_clasica);
        viewsClasica.tvTensionCentrifuga = findViewById(R.id.tv_tension_centrifuga_clasica);
        viewsClasica.tvTensionTenso = findViewById(R.id.tv_tension_tenso_clasica);
        viewsClasica.tvTensionFlojo = findViewById(R.id.tv_tension_flojo_clasica);

        // --- INICIO: Vistas Iteración 2 (Tarjeta 1: Clásica) ---
        spinnerPerfilIter2Clasica = findViewById(R.id.spinner_perfil_iter2_clasica);
        etNuevoDIter2Clasica = findViewById(R.id.et_nuevo_d_iter2_clasica);
        btnRecalcularIter2Clasica = findViewById(R.id.btn_recalcular_iter2_clasica);
        layoutResultadosIter2Clasica = findViewById(R.id.layout_resultados_iter2_clasica);

        viewsClasicaIter2.tvPerfilBanda = findViewById(R.id.tv_perfil_banda_iter2_clasica);
        viewsClasicaIter2.tvNumeroBandas = findViewById(R.id.tv_numero_bandas_iter2_clasica);
        viewsClasicaIter2.tvDiametroMinimo = findViewById(R.id.tv_diametro_minimo_iter2_clasica);
        viewsClasicaIter2.tvDiametroMaximo = findViewById(R.id.tv_diametro_maximo_iter2_clasica);
        viewsClasicaIter2.tvVelocidadPeriferica = findViewById(R.id.tv_velocidad_periferica_iter2_clasica);
        viewsClasicaIter2.tvLongitudCorrea = findViewById(R.id.tv_longitud_correa_iter2_clasica);
        viewsClasicaIter2.tvCodigoBanda = findViewById(R.id.tv_banda_seleccionada_codigo_iter2_clasica);
        viewsClasicaIter2.tvLongitudBanda = findViewById(R.id.tv_banda_seleccionada_longitud_iter2_clasica);
        viewsClasicaIter2.tvLongitudLp = findViewById(R.id.tv_longitud_lp_iter2_clasica);
        viewsClasicaIter2.tvLongitudLe = findViewById(R.id.tv_longitud_le_iter2_clasica);
        viewsClasicaIter2.tvDistanciaCentrosNecesaria = findViewById(R.id.tv_distancia_centros_necesaria_iter2_clasica);
        viewsClasicaIter2.tvDistanciaEfectiva = findViewById(R.id.tv_distancia_efectiva_iter2_clasica);
        viewsClasicaIter2.tvPbResultado = findViewById(R.id.tv_pb_resultado_iter2_clasica);
        viewsClasicaIter2.tvPdResultado = findViewById(R.id.tv_pd_resultado_iter2_clasica);
        viewsClasicaIter2.tvPotenciaAdmisible = findViewById(R.id.tv_potencia_admisible_iter2_clasica);
        viewsClasicaIter2.tvArcoContacto = findViewById(R.id.tv_arco_contacto_iter2_clasica);
        viewsClasicaIter2.tvFactorCorreccion = findViewById(R.id.tv_factor_correccion_iter2_clasica);
        viewsClasicaIter2.tvClResultado = findViewById(R.id.tv_cl_resultado_iter2_clasica);
        viewsClasicaIter2.tvCAlfa = findViewById(R.id.tv_calfa_resultado_iter2_clasica);
        viewsClasicaIter2.tvTensionEstatica = findViewById(R.id.tv_tension_estatica_iter2_clasica);
        viewsClasicaIter2.tvTensionCentrifuga = findViewById(R.id.tv_tension_centrifuga_iter2_clasica);
        viewsClasicaIter2.tvTensionTenso = findViewById(R.id.tv_tension_tenso_iter2_clasica);
        viewsClasicaIter2.tvTensionFlojo = findViewById(R.id.tv_tension_flojo_iter2_clasica);
        // --- FIN: Vistas Iteración 2 (Tarjeta 1) ---


        // Tarjeta 2: Estrecha DIN (Iteración 1)
        viewsEstrechaDin.tvPerfilBanda = findViewById(R.id.tv_perfil_banda_estrecha_din);
        viewsEstrechaDin.tvNumeroBandas = findViewById(R.id.tv_numero_bandas_estrecha_din);
        viewsEstrechaDin.tvDiametroMinimo = findViewById(R.id.tv_diametro_minimo_estrecha_din);
        viewsEstrechaDin.tvDiametroMaximo = findViewById(R.id.tv_diametro_maximo_estrecha_din);
        viewsEstrechaDin.tvVelocidadPeriferica = findViewById(R.id.tv_velocidad_periferica_estrecha_din);
        viewsEstrechaDin.tvLongitudCorrea = findViewById(R.id.tv_longitud_correa_estrecha_din);
        viewsEstrechaDin.tvCodigoBanda = findViewById(R.id.tv_banda_seleccionada_codigo_estrecha_din);
        viewsEstrechaDin.tvLongitudBanda = findViewById(R.id.tv_banda_seleccionada_longitud_estrecha_din);
        viewsEstrechaDin.tvLongitudLp = findViewById(R.id.tv_longitud_lp_estrecha_din);
        viewsEstrechaDin.tvLongitudLe = findViewById(R.id.tv_longitud_le_estrecha_din);
        viewsEstrechaDin.tvDistanciaCentrosNecesaria = findViewById(R.id.tv_distancia_centros_necesaria_estrecha_din);
        viewsEstrechaDin.tvDistanciaEfectiva = findViewById(R.id.tv_distancia_efectiva_estrecha_din);
        viewsEstrechaDin.tvPbResultado = findViewById(R.id.tv_pb_resultado_estrecha_din);
        viewsEstrechaDin.tvPdResultado = findViewById(R.id.tv_pd_resultado_estrecha_din);
        viewsEstrechaDin.tvPotenciaAdmisible = findViewById(R.id.tv_potencia_admisible_estrecha_din);
        viewsEstrechaDin.tvArcoContacto = findViewById(R.id.tv_arco_contacto_estrecha_din);
        viewsEstrechaDin.tvFactorCorreccion = findViewById(R.id.tv_factor_correccion_estrecha_din);
        viewsEstrechaDin.tvClResultado = findViewById(R.id.tv_cl_resultado_estrecha_din);
        viewsEstrechaDin.tvCAlfa = findViewById(R.id.tv_calfa_resultado_estrecha_din);
        viewsEstrechaDin.tvTensionEstatica = findViewById(R.id.tv_tension_estatica_estrecha_din);
        viewsEstrechaDin.tvTensionCentrifuga = findViewById(R.id.tv_tension_centrifuga_estrecha_din);
        viewsEstrechaDin.tvTensionTenso = findViewById(R.id.tv_tension_tenso_estrecha_din);
        viewsEstrechaDin.tvTensionFlojo = findViewById(R.id.tv_tension_flojo_estrecha_din);

        // --- INICIO: Vistas Iteración 2 (Tarjeta 2: Estrecha DIN) ---
        spinnerPerfilIter2EstrechaDin = findViewById(R.id.spinner_perfil_iter2_estrecha_din);
        etNuevoDIter2EstrechaDin = findViewById(R.id.et_nuevo_d_iter2_estrecha_din);
        btnRecalcularIter2EstrechaDin = findViewById(R.id.btn_recalcular_iter2_estrecha_din);
        layoutResultadosIter2EstrechaDin = findViewById(R.id.layout_resultados_iter2_estrecha_din);

        viewsEstrechaDinIter2.tvPerfilBanda = findViewById(R.id.tv_perfil_banda_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvNumeroBandas = findViewById(R.id.tv_numero_bandas_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvDiametroMinimo = findViewById(R.id.tv_diametro_minimo_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvDiametroMaximo = findViewById(R.id.tv_diametro_maximo_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvVelocidadPeriferica = findViewById(R.id.tv_velocidad_periferica_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvLongitudCorrea = findViewById(R.id.tv_longitud_correa_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvCodigoBanda = findViewById(R.id.tv_banda_seleccionada_codigo_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvLongitudBanda = findViewById(R.id.tv_banda_seleccionada_longitud_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvLongitudLp = findViewById(R.id.tv_longitud_lp_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvLongitudLe = findViewById(R.id.tv_longitud_le_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvDistanciaCentrosNecesaria = findViewById(R.id.tv_distancia_centros_necesaria_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvDistanciaEfectiva = findViewById(R.id.tv_distancia_efectiva_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvPbResultado = findViewById(R.id.tv_pb_resultado_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvPdResultado = findViewById(R.id.tv_pd_resultado_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvPotenciaAdmisible = findViewById(R.id.tv_potencia_admisible_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvArcoContacto = findViewById(R.id.tv_arco_contacto_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvFactorCorreccion = findViewById(R.id.tv_factor_correccion_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvClResultado = findViewById(R.id.tv_cl_resultado_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvCAlfa = findViewById(R.id.tv_calfa_resultado_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvTensionEstatica = findViewById(R.id.tv_tension_estatica_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvTensionCentrifuga = findViewById(R.id.tv_tension_centrifuga_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvTensionTenso = findViewById(R.id.tv_tension_tenso_iter2_estrecha_din);
        viewsEstrechaDinIter2.tvTensionFlojo = findViewById(R.id.tv_tension_flojo_iter2_estrecha_din);
        // --- FIN: Vistas Iteración 2 (Tarjeta 2) ---


        // Tarjeta 3: Estrecha ARPM (Iteración 1)
        viewsEstrechaArpm.tvPerfilBanda = findViewById(R.id.tv_perfil_banda_estrecha_arpm);
        viewsEstrechaArpm.tvNumeroBandas = findViewById(R.id.tv_numero_bandas_estrecha_arpm);
        viewsEstrechaArpm.tvDiametroMinimo = findViewById(R.id.tv_diametro_minimo_estrecha_arpm);
        viewsEstrechaArpm.tvDiametroMaximo = findViewById(R.id.tv_diametro_maximo_estrecha_arpm);
        viewsEstrechaArpm.tvVelocidadPeriferica = findViewById(R.id.tv_velocidad_periferica_estrecha_arpm);
        viewsEstrechaArpm.tvLongitudCorrea = findViewById(R.id.tv_longitud_correa_estrecha_arpm);
        viewsEstrechaArpm.tvCodigoBanda = findViewById(R.id.tv_banda_seleccionada_codigo_estrecha_arpm);
        viewsEstrechaArpm.tvLongitudBanda = findViewById(R.id.tv_banda_seleccionada_longitud_estrecha_arpm);
        viewsEstrechaArpm.tvLongitudLp = findViewById(R.id.tv_longitud_lp_estrecha_arpm);
        viewsEstrechaArpm.tvLongitudLe = findViewById(R.id.tv_longitud_le_estrecha_arpm);
        viewsEstrechaArpm.tvDistanciaCentrosNecesaria = findViewById(R.id.tv_distancia_centros_necesaria_estrecha_arpm);
        viewsEstrechaArpm.tvDistanciaEfectiva = findViewById(R.id.tv_distancia_efectiva_estrecha_arpm);
        viewsEstrechaArpm.tvPbResultado = findViewById(R.id.tv_pb_resultado_estrecha_arpm);
        viewsEstrechaArpm.tvPdResultado = findViewById(R.id.tv_pd_resultado_estrecha_arpm);
        viewsEstrechaArpm.tvPotenciaAdmisible = findViewById(R.id.tv_potencia_admisible_estrecha_arpm);
        viewsEstrechaArpm.tvArcoContacto = findViewById(R.id.tv_arco_contacto_estrecha_arpm);
        viewsEstrechaArpm.tvFactorCorreccion = findViewById(R.id.tv_factor_correccion_estrecha_arpm);
        viewsEstrechaArpm.tvClResultado = findViewById(R.id.tv_cl_resultado_estrecha_arpm);
        viewsEstrechaArpm.tvCAlfa = findViewById(R.id.tv_calfa_resultado_estrecha_arpm);
        viewsEstrechaArpm.tvTensionEstatica = findViewById(R.id.tv_tension_estatica_estrecha_arpm);
        viewsEstrechaArpm.tvTensionCentrifuga = findViewById(R.id.tv_tension_centrifuga_estrecha_arpm);
        viewsEstrechaArpm.tvTensionTenso = findViewById(R.id.tv_tension_tenso_estrecha_arpm);
        viewsEstrechaArpm.tvTensionFlojo = findViewById(R.id.tv_tension_flojo_estrecha_arpm);

        // --- INICIO: Vistas Iteración 2 (Tarjeta 3: Estrecha ARPM) ---
        spinnerPerfilIter2EstrechaArpm = findViewById(R.id.spinner_perfil_iter2_estrecha_arpm);
        etNuevoDIter2EstrechaArpm = findViewById(R.id.et_nuevo_d_iter2_estrecha_arpm);
        btnRecalcularIter2EstrechaArpm = findViewById(R.id.btn_recalcular_iter2_estrecha_arpm);
        layoutResultadosIter2EstrechaArpm = findViewById(R.id.layout_resultados_iter2_estrecha_arpm);

        viewsEstrechaArpmIter2.tvPerfilBanda = findViewById(R.id.tv_perfil_banda_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvNumeroBandas = findViewById(R.id.tv_numero_bandas_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvDiametroMinimo = findViewById(R.id.tv_diametro_minimo_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvDiametroMaximo = findViewById(R.id.tv_diametro_maximo_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvVelocidadPeriferica = findViewById(R.id.tv_velocidad_periferica_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvLongitudCorrea = findViewById(R.id.tv_longitud_correa_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvCodigoBanda = findViewById(R.id.tv_banda_seleccionada_codigo_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvLongitudBanda = findViewById(R.id.tv_banda_seleccionada_longitud_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvLongitudLp = findViewById(R.id.tv_longitud_lp_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvLongitudLe = findViewById(R.id.tv_longitud_le_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvDistanciaCentrosNecesaria = findViewById(R.id.tv_distancia_centros_necesaria_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvDistanciaEfectiva = findViewById(R.id.tv_distancia_efectiva_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvPbResultado = findViewById(R.id.tv_pb_resultado_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvPdResultado = findViewById(R.id.tv_pd_resultado_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvPotenciaAdmisible = findViewById(R.id.tv_potencia_admisible_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvArcoContacto = findViewById(R.id.tv_arco_contacto_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvFactorCorreccion = findViewById(R.id.tv_factor_correccion_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvClResultado = findViewById(R.id.tv_cl_resultado_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvCAlfa = findViewById(R.id.tv_calfa_resultado_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvTensionEstatica = findViewById(R.id.tv_tension_estatica_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvTensionCentrifuga = findViewById(R.id.tv_tension_centrifuga_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvTensionTenso = findViewById(R.id.tv_tension_tenso_iter2_estrecha_arpm);
        viewsEstrechaArpmIter2.tvTensionFlojo = findViewById(R.id.tv_tension_flojo_iter2_estrecha_arpm);
        // --- FIN: Vistas Iteración 2 (Tarjeta 3) ---


        // Tarjeta 4: Clásica X (Iteración 1)
        viewsClasicaX.tvPerfilBanda = findViewById(R.id.tv_perfil_banda_clasica_x);
        viewsClasicaX.tvNumeroBandas = findViewById(R.id.tv_numero_bandas_clasica_x);
        viewsClasicaX.tvDiametroMinimo = findViewById(R.id.tv_diametro_minimo_clasica_x);
        viewsClasicaX.tvDiametroMaximo = findViewById(R.id.tv_diametro_maximo_clasica_x);
        viewsClasicaX.tvVelocidadPeriferica = findViewById(R.id.tv_velocidad_periferica_clasica_x);
        viewsClasicaX.tvLongitudCorrea = findViewById(R.id.tv_longitud_correa_clasica_x);
        viewsClasicaX.tvCodigoBanda = findViewById(R.id.tv_banda_seleccionada_codigo_clasica_x);
        viewsClasicaX.tvLongitudBanda = findViewById(R.id.tv_banda_seleccionada_longitud_clasica_x);
        viewsClasicaX.tvLongitudLp = findViewById(R.id.tv_longitud_lp_clasica_x);
        viewsClasicaX.tvLongitudLe = findViewById(R.id.tv_longitud_le_clasica_x);
        viewsClasicaX.tvDistanciaCentrosNecesaria = findViewById(R.id.tv_distancia_centros_necesaria_clasica_x);
        viewsClasicaX.tvDistanciaEfectiva = findViewById(R.id.tv_distancia_efectiva_clasica_x);
        viewsClasicaX.tvPbResultado = findViewById(R.id.tv_pb_resultado_clasica_x);
        viewsClasicaX.tvPdResultado = findViewById(R.id.tv_pd_resultado_clasica_x);
        viewsClasicaX.tvPotenciaAdmisible = findViewById(R.id.tv_potencia_admisible_clasica_x);
        viewsClasicaX.tvArcoContacto = findViewById(R.id.tv_arco_contacto_clasica_x);
        viewsClasicaX.tvFactorCorreccion = findViewById(R.id.tv_factor_correccion_clasica_x);
        viewsClasicaX.tvClResultado = findViewById(R.id.tv_cl_resultado_clasica_x);
        viewsClasicaX.tvCAlfa = findViewById(R.id.tv_calfa_resultado_clasica_x);
        viewsClasicaX.tvTensionEstatica = findViewById(R.id.tv_tension_estatica_clasica_x);
        viewsClasicaX.tvTensionCentrifuga = findViewById(R.id.tv_tension_centrifuga_clasica_x);
        viewsClasicaX.tvTensionTenso = findViewById(R.id.tv_tension_tenso_clasica_x);
        viewsClasicaX.tvTensionFlojo = findViewById(R.id.tv_tension_flojo_clasica_x);

        // --- INICIO: Vistas Iteración 2 (Tarjeta 4: Clásica X) ---
        spinnerPerfilIter2ClasicaX = findViewById(R.id.spinner_perfil_iter2_clasica_x);
        etNuevoDIter2ClasicaX = findViewById(R.id.et_nuevo_d_iter2_clasica_x);
        btnRecalcularIter2ClasicaX = findViewById(R.id.btn_recalcular_iter2_clasica_x);
        layoutResultadosIter2ClasicaX = findViewById(R.id.layout_resultados_iter2_clasica_x);

        viewsClasicaXIter2.tvPerfilBanda = findViewById(R.id.tv_perfil_banda_iter2_clasica_x);
        viewsClasicaXIter2.tvNumeroBandas = findViewById(R.id.tv_numero_bandas_iter2_clasica_x);
        viewsClasicaXIter2.tvDiametroMinimo = findViewById(R.id.tv_diametro_minimo_iter2_clasica_x);
        viewsClasicaXIter2.tvDiametroMaximo = findViewById(R.id.tv_diametro_maximo_iter2_clasica_x);
        viewsClasicaXIter2.tvVelocidadPeriferica = findViewById(R.id.tv_velocidad_periferica_iter2_clasica_x);
        viewsClasicaXIter2.tvLongitudCorrea = findViewById(R.id.tv_longitud_correa_iter2_clasica_x);
        viewsClasicaXIter2.tvCodigoBanda = findViewById(R.id.tv_banda_seleccionada_codigo_iter2_clasica_x);
        viewsClasicaXIter2.tvLongitudBanda = findViewById(R.id.tv_banda_seleccionada_longitud_iter2_clasica_x);
        viewsClasicaXIter2.tvLongitudLp = findViewById(R.id.tv_longitud_lp_iter2_clasica_x);
        viewsClasicaXIter2.tvLongitudLe = findViewById(R.id.tv_longitud_le_iter2_clasica_x);
        viewsClasicaXIter2.tvDistanciaCentrosNecesaria = findViewById(R.id.tv_distancia_centros_necesaria_iter2_clasica_x);
        viewsClasicaXIter2.tvDistanciaEfectiva = findViewById(R.id.tv_distancia_efectiva_iter2_clasica_x);
        viewsClasicaXIter2.tvPbResultado = findViewById(R.id.tv_pb_resultado_iter2_clasica_x);
        viewsClasicaXIter2.tvPdResultado = findViewById(R.id.tv_pd_resultado_iter2_clasica_x);
        viewsClasicaXIter2.tvPotenciaAdmisible = findViewById(R.id.tv_potencia_admisible_iter2_clasica_x);
        viewsClasicaXIter2.tvArcoContacto = findViewById(R.id.tv_arco_contacto_iter2_clasica_x);
        viewsClasicaXIter2.tvFactorCorreccion = findViewById(R.id.tv_factor_correccion_iter2_clasica_x);
        viewsClasicaXIter2.tvClResultado = findViewById(R.id.tv_cl_resultado_iter2_clasica_x);
        viewsClasicaXIter2.tvCAlfa = findViewById(R.id.tv_calfa_resultado_iter2_clasica_x);
        viewsClasicaXIter2.tvTensionEstatica = findViewById(R.id.tv_tension_estatica_iter2_clasica_x);
        viewsClasicaXIter2.tvTensionCentrifuga = findViewById(R.id.tv_tension_centrifuga_iter2_clasica_x);
        viewsClasicaXIter2.tvTensionTenso = findViewById(R.id.tv_tension_tenso_iter2_clasica_x);
        viewsClasicaXIter2.tvTensionFlojo = findViewById(R.id.tv_tension_flojo_iter2_clasica_x);
        // --- FIN: Vistas Iteración 2 (Tarjeta 4) ---


        // Tarjeta 5: Estrecha DIN X (Iteración 1)
        viewsEstrechaDinX.tvPerfilBanda = findViewById(R.id.tv_perfil_banda_estrecha_din_x);
        viewsEstrechaDinX.tvNumeroBandas = findViewById(R.id.tv_numero_bandas_estrecha_din_x);
        viewsEstrechaDinX.tvDiametroMinimo = findViewById(R.id.tv_diametro_minimo_estrecha_din_x);
        viewsEstrechaDinX.tvDiametroMaximo = findViewById(R.id.tv_diametro_maximo_estrecha_din_x);
        viewsEstrechaDinX.tvVelocidadPeriferica = findViewById(R.id.tv_velocidad_periferica_estrecha_din_x);
        viewsEstrechaDinX.tvLongitudCorrea = findViewById(R.id.tv_longitud_correa_estrecha_din_x);
        viewsEstrechaDinX.tvCodigoBanda = findViewById(R.id.tv_banda_seleccionada_codigo_estrecha_din_x);
        viewsEstrechaDinX.tvLongitudBanda = findViewById(R.id.tv_banda_seleccionada_longitud_estrecha_din_x);
        viewsEstrechaDinX.tvLongitudLp = findViewById(R.id.tv_longitud_lp_estrecha_din_x);
        viewsEstrechaDinX.tvLongitudLe = findViewById(R.id.tv_longitud_le_estrecha_din_x);
        viewsEstrechaDinX.tvDistanciaCentrosNecesaria = findViewById(R.id.tv_distancia_centros_necesaria_estrecha_din_x);
        viewsEstrechaDinX.tvDistanciaEfectiva = findViewById(R.id.tv_distancia_efectiva_estrecha_din_x);
        viewsEstrechaDinX.tvPbResultado = findViewById(R.id.tv_pb_resultado_estrecha_din_x);
        viewsEstrechaDinX.tvPdResultado = findViewById(R.id.tv_pd_resultado_estrecha_din_x);
        viewsEstrechaDinX.tvPotenciaAdmisible = findViewById(R.id.tv_potencia_admisible_estrecha_din_x);
        viewsEstrechaDinX.tvArcoContacto = findViewById(R.id.tv_arco_contacto_estrecha_din_x);
        viewsEstrechaDinX.tvFactorCorreccion = findViewById(R.id.tv_factor_correccion_estrecha_din_x);
        viewsEstrechaDinX.tvClResultado = findViewById(R.id.tv_cl_resultado_estrecha_din_x);
        viewsEstrechaDinX.tvCAlfa = findViewById(R.id.tv_calfa_resultado_estrecha_din_x);
        viewsEstrechaDinX.tvTensionEstatica = findViewById(R.id.tv_tension_estatica_estrecha_din_x);
        viewsEstrechaDinX.tvTensionCentrifuga = findViewById(R.id.tv_tension_centrifuga_estrecha_din_x);
        viewsEstrechaDinX.tvTensionTenso = findViewById(R.id.tv_tension_tenso_estrecha_din_x);
        viewsEstrechaDinX.tvTensionFlojo = findViewById(R.id.tv_tension_flojo_estrecha_din_x);

        // --- INICIO: Vistas Iteración 2 (Tarjeta 5: Estrecha DIN X) ---
        spinnerPerfilIter2EstrechaDinX = findViewById(R.id.spinner_perfil_iter2_estrecha_din_x);
        etNuevoDIter2EstrechaDinX = findViewById(R.id.et_nuevo_d_iter2_estrecha_din_x);
        btnRecalcularIter2EstrechaDinX = findViewById(R.id.btn_recalcular_iter2_estrecha_din_x);
        layoutResultadosIter2EstrechaDinX = findViewById(R.id.layout_resultados_iter2_estrecha_din_x);

        viewsEstrechaDinXIter2.tvPerfilBanda = findViewById(R.id.tv_perfil_banda_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvNumeroBandas = findViewById(R.id.tv_numero_bandas_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvDiametroMinimo = findViewById(R.id.tv_diametro_minimo_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvDiametroMaximo = findViewById(R.id.tv_diametro_maximo_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvVelocidadPeriferica = findViewById(R.id.tv_velocidad_periferica_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvLongitudCorrea = findViewById(R.id.tv_longitud_correa_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvCodigoBanda = findViewById(R.id.tv_banda_seleccionada_codigo_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvLongitudBanda = findViewById(R.id.tv_banda_seleccionada_longitud_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvLongitudLp = findViewById(R.id.tv_longitud_lp_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvLongitudLe = findViewById(R.id.tv_longitud_le_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvDistanciaCentrosNecesaria = findViewById(R.id.tv_distancia_centros_necesaria_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvDistanciaEfectiva = findViewById(R.id.tv_distancia_efectiva_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvPbResultado = findViewById(R.id.tv_pb_resultado_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvPdResultado = findViewById(R.id.tv_pd_resultado_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvPotenciaAdmisible = findViewById(R.id.tv_potencia_admisible_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvArcoContacto = findViewById(R.id.tv_arco_contacto_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvFactorCorreccion = findViewById(R.id.tv_factor_correccion_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvClResultado = findViewById(R.id.tv_cl_resultado_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvCAlfa = findViewById(R.id.tv_calfa_resultado_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvTensionEstatica = findViewById(R.id.tv_tension_estatica_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvTensionCentrifuga = findViewById(R.id.tv_tension_centrifuga_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvTensionTenso = findViewById(R.id.tv_tension_tenso_iter2_estrecha_din_x);
        viewsEstrechaDinXIter2.tvTensionFlojo = findViewById(R.id.tv_tension_flojo_iter2_estrecha_din_x);
        // --- FIN: Vistas Iteración 2 (Tarjeta 5) ---


        // Tarjeta 6: Estrecha ARPM X (Iteración 1)
        viewsEstrechaArpmX.tvPerfilBanda = findViewById(R.id.tv_perfil_banda_estrecha_arpm_x);
        viewsEstrechaArpmX.tvNumeroBandas = findViewById(R.id.tv_numero_bandas_estrecha_arpm_x);
        viewsEstrechaArpmX.tvDiametroMinimo = findViewById(R.id.tv_diametro_minimo_estrecha_arpm_x);
        viewsEstrechaArpmX.tvDiametroMaximo = findViewById(R.id.tv_diametro_maximo_estrecha_arpm_x);
        viewsEstrechaArpmX.tvVelocidadPeriferica = findViewById(R.id.tv_velocidad_periferica_estrecha_arpm_x);
        viewsEstrechaArpmX.tvLongitudCorrea = findViewById(R.id.tv_longitud_correa_estrecha_arpm_x);
        viewsEstrechaArpmX.tvCodigoBanda = findViewById(R.id.tv_banda_seleccionada_codigo_estrecha_arpm_x);
        viewsEstrechaArpmX.tvLongitudBanda = findViewById(R.id.tv_banda_seleccionada_longitud_estrecha_arpm_x);
        viewsEstrechaArpmX.tvLongitudLp = findViewById(R.id.tv_longitud_lp_estrecha_arpm_x);
        viewsEstrechaArpmX.tvLongitudLe = findViewById(R.id.tv_longitud_le_estrecha_arpm_x);
        viewsEstrechaArpmX.tvDistanciaCentrosNecesaria = findViewById(R.id.tv_distancia_centros_necesaria_estrecha_arpm_x);
        viewsEstrechaArpmX.tvDistanciaEfectiva = findViewById(R.id.tv_distancia_efectiva_estrecha_arpm_x);
        viewsEstrechaArpmX.tvPbResultado = findViewById(R.id.tv_pb_resultado_estrecha_arpm_x);
        viewsEstrechaArpmX.tvPdResultado = findViewById(R.id.tv_pd_resultado_estrecha_arpm_x);
        viewsEstrechaArpmX.tvPotenciaAdmisible = findViewById(R.id.tv_potencia_admisible_estrecha_arpm_x);
        viewsEstrechaArpmX.tvArcoContacto = findViewById(R.id.tv_arco_contacto_estrecha_arpm_x);
        viewsEstrechaArpmX.tvFactorCorreccion = findViewById(R.id.tv_factor_correccion_estrecha_arpm_x);
        viewsEstrechaArpmX.tvClResultado = findViewById(R.id.tv_cl_resultado_estrecha_arpm_x);
        viewsEstrechaArpmX.tvCAlfa = findViewById(R.id.tv_calfa_resultado_estrecha_arpm_x);
        viewsEstrechaArpmX.tvTensionEstatica = findViewById(R.id.tv_tension_estatica_estrecha_arpm_x);
        viewsEstrechaArpmX.tvTensionCentrifuga = findViewById(R.id.tv_tension_centrifuga_estrecha_arpm_x);
        viewsEstrechaArpmX.tvTensionTenso = findViewById(R.id.tv_tension_tenso_estrecha_arpm_x);
        viewsEstrechaArpmX.tvTensionFlojo = findViewById(R.id.tv_tension_flojo_estrecha_arpm_x);

        // --- INICIO: Vistas Iteración 2 (Tarjeta 6: Estrecha ARPM X) ---
        spinnerPerfilIter2EstrechaArpmX = findViewById(R.id.spinner_perfil_iter2_estrecha_arpm_x);
        etNuevoDIter2EstrechaArpmX = findViewById(R.id.et_nuevo_d_iter2_estrecha_arpm_x);
        btnRecalcularIter2EstrechaArpmX = findViewById(R.id.btn_recalcular_iter2_estrecha_arpm_x);
        layoutResultadosIter2EstrechaArpmX = findViewById(R.id.layout_resultados_iter2_estrecha_arpm_x);

        viewsEstrechaArpmXIter2.tvPerfilBanda = findViewById(R.id.tv_perfil_banda_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvNumeroBandas = findViewById(R.id.tv_numero_bandas_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvDiametroMinimo = findViewById(R.id.tv_diametro_minimo_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvDiametroMaximo = findViewById(R.id.tv_diametro_maximo_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvVelocidadPeriferica = findViewById(R.id.tv_velocidad_periferica_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvLongitudCorrea = findViewById(R.id.tv_longitud_correa_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvCodigoBanda = findViewById(R.id.tv_banda_seleccionada_codigo_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvLongitudBanda = findViewById(R.id.tv_banda_seleccionada_longitud_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvLongitudLp = findViewById(R.id.tv_longitud_lp_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvLongitudLe = findViewById(R.id.tv_longitud_le_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvDistanciaCentrosNecesaria = findViewById(R.id.tv_distancia_centros_necesaria_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvDistanciaEfectiva = findViewById(R.id.tv_distancia_efectiva_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvPbResultado = findViewById(R.id.tv_pb_resultado_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvPdResultado = findViewById(R.id.tv_pd_resultado_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvPotenciaAdmisible = findViewById(R.id.tv_potencia_admisible_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvArcoContacto = findViewById(R.id.tv_arco_contacto_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvFactorCorreccion = findViewById(R.id.tv_factor_correccion_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvClResultado = findViewById(R.id.tv_cl_resultado_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvCAlfa = findViewById(R.id.tv_calfa_resultado_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvTensionEstatica = findViewById(R.id.tv_tension_estatica_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvTensionCentrifuga = findViewById(R.id.tv_tension_centrifuga_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvTensionTenso = findViewById(R.id.tv_tension_tenso_iter2_estrecha_arpm_x);
        viewsEstrechaArpmXIter2.tvTensionFlojo = findViewById(R.id.tv_tension_flojo_iter2_estrecha_arpm_x);
        // --- FIN: Vistas Iteración 2 (Tarjeta 6) ---


        // --- [NUEVO] INICIO: CONFIGURAR SPINNERS DE ITERACIÓN 2 ---

        // 1. Poblar las listas filtradas
        listaPerfilesClasica = new ArrayList<>(perfilesClasicas);
        listaPerfilesEstrechaDin = new ArrayList<>(perfilesCuña);
        listaPerfilesEstrechaArpm = new ArrayList<>(perfilesEstrecha);
        listaPerfilesClasicaX = new ArrayList<>(perfilesClasicaX);
        listaPerfilesEstrechaDinX = new ArrayList<>(perfilesCuñaX);
        listaPerfilesEstrechaArpmX = new ArrayList<>(perfilesEstrechaX);

        Collections.sort(listaPerfilesClasica);
        Collections.sort(listaPerfilesEstrechaDin);
        Collections.sort(listaPerfilesEstrechaArpm);
        Collections.sort(listaPerfilesClasicaX);
        Collections.sort(listaPerfilesEstrechaDinX);
        Collections.sort(listaPerfilesEstrechaArpmX);

        // 2. Crear adaptadores
        ArrayAdapter<String> adapterClasica = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaPerfilesClasica);
        adapterClasica.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> adapterEstrechaDin = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaPerfilesEstrechaDin);
        adapterEstrechaDin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> adapterEstrechaArpm = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaPerfilesEstrechaArpm);
        adapterEstrechaArpm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> adapterClasicaX = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaPerfilesClasicaX);
        adapterClasicaX.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> adapterEstrechaDinX = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaPerfilesEstrechaDinX);
        adapterEstrechaDinX.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> adapterEstrechaArpmX = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaPerfilesEstrechaArpmX);
        adapterEstrechaArpmX.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 3. Asignar adaptadores a los Spinners
        spinnerPerfilIter2Clasica.setAdapter(adapterClasica);
        spinnerPerfilIter2EstrechaDin.setAdapter(adapterEstrechaDin);
        spinnerPerfilIter2EstrechaArpm.setAdapter(adapterEstrechaArpm);
        spinnerPerfilIter2ClasicaX.setAdapter(adapterClasicaX);
        spinnerPerfilIter2EstrechaDinX.setAdapter(adapterEstrechaDinX);
        spinnerPerfilIter2EstrechaArpmX.setAdapter(adapterEstrechaArpmX);

        // --- [NUEVO] FIN: CONFIGURAR SPINNERS DE ITERACIÓN 2 ---
    }


    private List<String[]> loadCSVFromAssets(String fileName) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(fileName)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    rows.add(line.split(";", -1)); // -1 para capturar celdas vacías
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("loadCSVFromAssets", "Error loading " + fileName, e);
        }
        return rows;
    }

    private List<String[]> loadCSVFromAssets(String fileName, boolean hasHeader) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(fileName)))) {
            if (hasHeader) {
                reader.readLine(); // Saltar cabecera
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    rows.add(line.split(";", -1));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("loadCSVFromAssets", "Error loading " + fileName, e);
        }
        return rows;
    }

    private void loadAllCSVs() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("caracteristicas_banda.csv")))) {
            reader.readLine(); // Saltar cabecera
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(";");
                if (columns.length > 6) {
                    caracteristicasBanda.put(columns[0].trim(), columns);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("LoadCSV", "Error loading caracteristicas_banda.csv", e);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("Dayco_perfiles.csv")))) {
            reader.readLine(); // Saltar cabecera
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String cleanLine = line.replaceAll(";,", ";").replaceAll(",;", ";").replaceAll(",", ";");
                    String[] columns = cleanLine.split(";");
                    ArrayList<String> cleanColumns = new ArrayList<>();
                    for (String col : columns) {
                        if (col != null && !col.trim().isEmpty()) {
                            cleanColumns.add(col.trim());
                        }
                    }
                    perfilesDayco.add(cleanColumns.toArray(new String[0]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("LoadCSV", "Error loading Dayco_perfiles.csv", e);
        }

        // Carga de tablas PB
        tablaPbClasicas = loadCSVFromAssets("tabla_pb_clasicas.csv");
        tablaPbClasicaX = loadCSVFromAssets("tabla_pb_clasicax.csv");
        tablaPbCuña = loadCSVFromAssets("tabla_pb_cuña.csv");
        tablaPbCuñaX = loadCSVFromAssets("tabla_pb_cuñax.csv");
        tablaPbEstrecha = loadCSVFromAssets("tabla_pb_estrecha.csv");
        tablaPbEstrechaX = loadCSVFromAssets("tabla_pb_estrechax.csv");

        // Carga de tablas PD
        tablaPdClasicas = loadCSVFromAssets("tabla_pd_clasicas.csv");
        tablaPdClasicaX = loadCSVFromAssets("tabla_pd_clasicax.csv");
        tablaPdCuña = loadCSVFromAssets("tabla_pd_cuña.csv");
        tablaPdCuñaX = loadCSVFromAssets("tabla_pd_cuñax.csv");
        tablaPdEstrecha = loadCSVFromAssets("tabla_pd_estrecha.csv");
        tablaPdEstrechaX = loadCSVFromAssets("tabla_pd_estrechax.csv");

        // Carga de tablas CL (Factor de Longitud)
        tablaCl_Inches = loadCSVFromAssets("tabla_cl.csv");
        tablaCl_Cuna = loadCSVFromAssets("tabal_cl_cuña.csv");
        tablaCl_CunaX = loadCSVFromAssets("tabal_cl_cuñax.csv");
        tablaCl_Estrecha = loadCSVFromAssets("tabal_cl_estrechas.csv");
        tablaCl_EstrechaX = loadCSVFromAssets("tabal_cl_estrechax.csv");

        // Carga de tablas de Factores (Estas SÍ tienen cabecera)
        tablaFactorAngulo = loadCSVFromAssets("factor_correccion_angulo.csv", true);
        tablaFactorArco = loadCSVFromAssets("factor_correccion_arco.csv", true);
    }


    private void displayResults() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) return;

        // Almacenar datos de entrada como miembros de la clase
        potenciaCorregida = extras.getDouble("POTENCIA_CORREGIDA", 0.0);
        neRpm = extras.getDouble("NE_RPM", 0.0);
        mg = extras.getDouble("MG_RELACION", 0.0);
        dc = extras.getDouble("DISTANCIA_CENTROS", 0.0);
        //String tipoBanda = extras.getString("TIPO_BANDA", "Bandas Lisas"); // Reemplazado por la variable de PDF

        // --- AÑADIDO: Capturar datos de entrada para PDF ---
        potenciaNominalStr = extras.getString("POTENCIA_NOMINAL_STR", "- Kw");
        factorServicioStr = extras.getString("FACTOR_SERVICIO_STR", "-");
        neRpmStr = extras.getString("NE_RPM_STR", "- RPM");
        nsRpmStr = extras.getString("NS_RPM_STR", "- RPM");
        mgStr = extras.getString("MG_STR", "-");
        distanciaStr = extras.getString("DISTANCIA_STR", "- mm");
        tipoBandaStr = extras.getString("TIPO_BANDA_STR", "Bandas Lisas"); // Usar esta variable
        // --- FIN AÑADIDO ---

        tvPotenciaCorregidaResumen.setText("Potencia Corregida: " + df.format(potenciaCorregida) + " Kw");
        tvTipoBandaResumen.setText("Tipo: " + tipoBandaStr); // Usar el string completo

        int resultadosEncontrados = 0;

        if (tipoBandaStr.equals("Bandas Lisas")) {
            layoutClasicaX.setVisibility(View.GONE);
            layoutEstrechaDinX.setVisibility(View.GONE);
            layoutEstrechaArpmX.setVisibility(View.GONE);

            String perfilClasica = seleccionarPerfilClasica(potenciaCorregida, neRpm);
            resultadosEncontrados += realizarCalculoCompleto(perfilClasica, layoutClasica, viewsClasica);

            String perfilEstrechaDin = seleccionarPerfilEstrechaDIN(potenciaCorregida, neRpm);
            resultadosEncontrados += realizarCalculoCompleto(perfilEstrechaDin, layoutEstrechaDin, viewsEstrechaDin);

            String perfilEstrechaArpm = seleccionarPerfilEstrechaARPM(potenciaCorregida, neRpm, true); // true = incluir 8V
            resultadosEncontrados += realizarCalculoCompleto(perfilEstrechaArpm, layoutEstrechaArpm, viewsEstrechaArpm);

        } else { // Bandas Dentadas
            layoutClasica.setVisibility(View.GONE);
            layoutEstrechaDin.setVisibility(View.GONE);
            layoutEstrechaArpm.setVisibility(View.GONE);

            String perfilClasicaX = seleccionarPerfilClasicaX(potenciaCorregida, neRpm);
            resultadosEncontrados += realizarCalculoCompleto(perfilClasicaX, layoutClasicaX, viewsClasicaX);

            String perfilEstrechaDinX = seleccionarPerfilEstrechaDIN_X(potenciaCorregida, neRpm);
            resultadosEncontrados += realizarCalculoCompleto(perfilEstrechaDinX, layoutEstrechaDinX, viewsEstrechaDinX);

            // CAMBIO IMPORTANTE: Llamamos con 'true' para detectar si cae en zona de 8V
            String perfilEstrechaArpmX_raw = seleccionarPerfilEstrechaARPM(potenciaCorregida, neRpm, true);

            String perfilEstrechaArpmX = "-";

            if(perfilEstrechaArpmX_raw.equals("3V")) {
                perfilEstrechaArpmX = "3VX";
            }
            else if(perfilEstrechaArpmX_raw.equals("5V")) {
                perfilEstrechaArpmX = "5VX";
            }
            else if(perfilEstrechaArpmX_raw.equals("8V")) {
                // AQUÍ LA CORRECCIÓN: Si cae en zona 8V, como no existe 8VX, forzamos 5VX.
                // Esto "amplía" el terreno de la 5VX.
                perfilEstrechaArpmX = "5VX";
            }

            resultadosEncontrados += realizarCalculoCompleto(perfilEstrechaArpmX, layoutEstrechaArpmX, viewsEstrechaArpmX);
        }

        if (resultadosEncontrados == 0) {
            tvNoResultados.setVisibility(View.VISIBLE);
        } else {
            tvNoResultados.setVisibility(View.GONE);
        }
    }

    /**
     * Realiza todos los cálculos para una tarjeta (Iteración 1).
     */
    private int realizarCalculoCompleto(String perfil, LinearLayout layoutContenedor, ResultadoViews views) {
        // Limpiar vistas de cálculos anteriores
        resetResultadosTotales(views, ""); // Limpia la tarjeta antes de empezar

        if (perfil.equals("-") || !caracteristicasBanda.containsKey(perfil)) {
            layoutContenedor.setVisibility(View.GONE);
            return 0;
        }

        layoutContenedor.setVisibility(View.VISIBLE);
        views.tvPerfilBanda.setText(perfil);

        String[] dataRow = caracteristicasBanda.get(perfil);
        if (dataRow == null || dataRow.length <= 6) {
            layoutContenedor.setVisibility(View.GONE);
            return 0;
        }

        try {
            double d = parsearDoble(dataRow[6]); // Diametro Mínimo
            if (d == 0.0) {
                Log.e("realizarCalculoCompleto", "El diámetro mínimo (dataRow[6]) es 0.0 para el perfil: " + perfil);
                layoutContenedor.setVisibility(View.GONE);
                return 0;
            }

            double D = mg * d;
            double deltaI = parsearDoble(dataRow[3]);
            double deltaE = parsearDoble(dataRow[4]);
            double m_kg_por_m = parsearDoble(dataRow[5]) / 1000.0;

            double pb = findPbValue(perfil, neRpm, d);
            double pd = findPdValue(perfil, neRpm, mg);
            double v = (d * neRpm) / 19100;
            double Lprima = (2 * dc) + (1.57 * (D + d)) + (Math.pow(D - d, 2) / (4 * dc));

            views.tvDiametroMinimo.setText("d: " + df.format(d) + " mm");
            views.tvDiametroMaximo.setText("D: " + df.format(D) + " mm");
            views.tvPbResultado.setText("Pb: " + df.format(pb) + " kW");
            views.tvPdResultado.setText("Pd: " + df.format(pd) + " kW");
            views.tvVelocidadPeriferica.setText("V: " + df.format(v) + " m/s");
            views.tvLongitudCorrea.setText("L calc: " + df.format(Lprima) + " mm");

            // Pasar todos los parámetros calculados
            seleccionarBandaComercial(perfil, Lprima, deltaI, deltaE, D, d, pb, pd, v, m_kg_por_m, views);

            return 1;

        } catch (NumberFormatException e) {
            e.printStackTrace();
            layoutContenedor.setVisibility(View.GONE);
            return 0;
        }
    }


    // --- INICIO: LÓGICA DE SEGUNDA ITERACIÓN ---

    /**
     * Configura los listeners para los botones de Recalcular (Iteración 2).
     */
    private void setupIteracion2Listeners() {

        // --- Botón Tarjeta 1: Clásica ---
        btnRecalcularIter2Clasica.setOnClickListener(v -> {
            try {
                if (spinnerPerfilIter2Clasica.getSelectedItem() == null) {
                    Toast.makeText(this, "No hay perfil seleccionado", Toast.LENGTH_SHORT).show();
                    return;
                }
                String perfil = spinnerPerfilIter2Clasica.getSelectedItem().toString();

                if (!validarDiametro(etNuevoDIter2Clasica, perfil)) {
                    return; // Detener si no es válido
                }

                double dNuevo = Double.parseDouble(etNuevoDIter2Clasica.getText().toString());

                realizarCalculoCompleto_Iter2(
                        perfil,
                        dNuevo,
                        layoutResultadosIter2Clasica,
                        viewsClasicaIter2
                );

            } catch (Exception e) {
                Toast.makeText(this, "Error al recalcular: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Iteracion2", "Error en recalculate Clásica", e);
            }
        });

        // --- Botón Tarjeta 2: Estrecha DIN ---
        btnRecalcularIter2EstrechaDin.setOnClickListener(v -> {
            try {
                if (spinnerPerfilIter2EstrechaDin.getSelectedItem() == null) {
                    Toast.makeText(this, "No hay perfil seleccionado", Toast.LENGTH_SHORT).show();
                    return;
                }
                String perfil = spinnerPerfilIter2EstrechaDin.getSelectedItem().toString();

                if (!validarDiametro(etNuevoDIter2EstrechaDin, perfil)) {
                    return;
                }

                double dNuevo = Double.parseDouble(etNuevoDIter2EstrechaDin.getText().toString());

                realizarCalculoCompleto_Iter2(
                        perfil,
                        dNuevo,
                        layoutResultadosIter2EstrechaDin,
                        viewsEstrechaDinIter2
                );

            } catch (Exception e) {
                Toast.makeText(this, "Error al recalcular: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Iteracion2", "Error en recalculate Estrecha DIN", e);
            }
        });

        // --- Botón Tarjeta 3: Estrecha ARPM ---
        btnRecalcularIter2EstrechaArpm.setOnClickListener(v -> {
            try {
                if (spinnerPerfilIter2EstrechaArpm.getSelectedItem() == null) {
                    Toast.makeText(this, "No hay perfil seleccionado", Toast.LENGTH_SHORT).show();
                    return;
                }
                String perfil = spinnerPerfilIter2EstrechaArpm.getSelectedItem().toString();

                if (!validarDiametro(etNuevoDIter2EstrechaArpm, perfil)) {
                    return;
                }

                double dNuevo = Double.parseDouble(etNuevoDIter2EstrechaArpm.getText().toString());

                realizarCalculoCompleto_Iter2(
                        perfil,
                        dNuevo,
                        layoutResultadosIter2EstrechaArpm,
                        viewsEstrechaArpmIter2
                );

            } catch (Exception e) {
                Toast.makeText(this, "Error al recalcular: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Iteracion2", "Error en recalculate Estrecha ARPM", e);
            }
        });

        // --- Botón Tarjeta 4: Clásica X ---
        btnRecalcularIter2ClasicaX.setOnClickListener(v -> {
            try {
                if (spinnerPerfilIter2ClasicaX.getSelectedItem() == null) {
                    Toast.makeText(this, "No hay perfil seleccionado", Toast.LENGTH_SHORT).show();
                    return;
                }
                String perfil = spinnerPerfilIter2ClasicaX.getSelectedItem().toString();

                if (!validarDiametro(etNuevoDIter2ClasicaX, perfil)) {
                    return;
                }

                double dNuevo = Double.parseDouble(etNuevoDIter2ClasicaX.getText().toString());

                realizarCalculoCompleto_Iter2(
                        perfil,
                        dNuevo,
                        layoutResultadosIter2ClasicaX,
                        viewsClasicaXIter2
                );

            } catch (Exception e) {
                Toast.makeText(this, "Error al recalcular: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Iteracion2", "Error en recalculate Clásica X", e);
            }
        });

        // --- Botón Tarjeta 5: Estrecha DIN X ---
        btnRecalcularIter2EstrechaDinX.setOnClickListener(v -> {
            try {
                if (spinnerPerfilIter2EstrechaDinX.getSelectedItem() == null) {
                    Toast.makeText(this, "No hay perfil seleccionado", Toast.LENGTH_SHORT).show();
                    return;
                }
                String perfil = spinnerPerfilIter2EstrechaDinX.getSelectedItem().toString();

                if (!validarDiametro(etNuevoDIter2EstrechaDinX, perfil)) {
                    return;
                }

                double dNuevo = Double.parseDouble(etNuevoDIter2EstrechaDinX.getText().toString());

                realizarCalculoCompleto_Iter2(
                        perfil,
                        dNuevo,
                        layoutResultadosIter2EstrechaDinX,
                        viewsEstrechaDinXIter2
                );

            } catch (Exception e) {
                Toast.makeText(this, "Error al recalcular: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Iteracion2", "Error en recalculate Estrecha DIN X", e);
            }
        });

        // --- Botón Tarjeta 6: Estrecha ARPM X ---
        btnRecalcularIter2EstrechaArpmX.setOnClickListener(v -> {
            try {
                if (spinnerPerfilIter2EstrechaArpmX.getSelectedItem() == null) {
                    Toast.makeText(this, "No hay perfil seleccionado", Toast.LENGTH_SHORT).show();
                    return;
                }
                String perfil = spinnerPerfilIter2EstrechaArpmX.getSelectedItem().toString();

                if (!validarDiametro(etNuevoDIter2EstrechaArpmX, perfil)) {
                    return;
                }

                double dNuevo = Double.parseDouble(etNuevoDIter2EstrechaArpmX.getText().toString());

                realizarCalculoCompleto_Iter2(
                        perfil,
                        dNuevo,
                        layoutResultadosIter2EstrechaArpmX,
                        viewsEstrechaArpmXIter2
                );

            } catch (Exception e) {
                Toast.makeText(this, "Error al recalcular: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Iteracion2", "Error en recalculate Estrecha ARPM X", e);
            }
        });
    }

    /**
     * Realiza todos los cálculos para una tarjeta (Iteración 2).
     */
    private int realizarCalculoCompleto_Iter2(String perfil, double d_nuevo, LinearLayout layoutContenedor, ResultadoViews views) {
        // Limpiar vistas de cálculos anteriores
        resetResultadosTotales(views, ""); // Limpia la tarjeta antes de empezar

        if (perfil.equals("-") || !caracteristicasBanda.containsKey(perfil)) {
            layoutContenedor.setVisibility(View.GONE);
            return 0;
        }

        // Mostrar el layout de resultados de la Iter 2
        layoutContenedor.setVisibility(View.VISIBLE);
        views.tvPerfilBanda.setText(perfil);

        String[] dataRow = caracteristicasBanda.get(perfil);
        if (dataRow == null || dataRow.length <= 6) {
            layoutContenedor.setVisibility(View.GONE);
            return 0;
        }

        try {
            // ¡La lógica cambia aquí! Usamos d_nuevo en lugar de dataRow[6]
            double d = d_nuevo;
            double D = mg * d; // 'mg' es miembro de la clase
            double deltaI = parsearDoble(dataRow[3]);
            double deltaE = parsearDoble(dataRow[4]);
            double m_kg_por_m = parsearDoble(dataRow[5]) / 1000.0;

            // 'neRpm' y 'mg' son miembros de la clase
            double pb = findPbValue(perfil, neRpm, d);
            double pd = findPdValue(perfil, neRpm, mg);
            double v = (d * neRpm) / 19100;
            double Lprima = (2 * dc) + (1.57 * (D + d)) + (Math.pow(D - d, 2) / (4 * dc)); // 'dc' es miembro de la clase

            views.tvDiametroMinimo.setText("d: " + df.format(d) + " mm");
            views.tvDiametroMaximo.setText("D: " + df.format(D) + " mm");
            views.tvPbResultado.setText("Pb: " + df.format(pb) + " kW");
            views.tvPdResultado.setText("Pd: " + df.format(pd) + " kW");
            views.tvVelocidadPeriferica.setText("V: " + df.format(v) + " m/s");
            views.tvLongitudCorrea.setText("L calc: " + df.format(Lprima) + " mm");

            // El resto de la lógica es la misma
            seleccionarBandaComercial(perfil, Lprima, deltaI, deltaE, D, d, pb, pd, v, m_kg_por_m, views);

            return 1;

        } catch (NumberFormatException e) {
            e.printStackTrace();
            layoutContenedor.setVisibility(View.GONE);
            return 0;
        }
    }

    // --- FIN: LÓGICA DE SEGUNDA ITERACIÓN ---


    /**
     * Valida el diámetro ingresado en un EditText para un perfil específico.
     */
    private boolean validarDiametro(EditText etDiametro, String perfil) {
        String dStr = etDiametro.getText().toString();
        if (dStr.isEmpty()) {
            etDiametro.setError("Ingrese un diámetro");
            return false;
        }

        double dNuevo;
        try {
            dNuevo = Double.parseDouble(dStr);
        } catch (NumberFormatException e) {
            etDiametro.setError("Número inválido");
            return false;
        }

        if (dNuevo == 0) {
            etDiametro.setError("No puede ser cero");
            return false;
        }

        double[] rango = getDiametroRangeForProfile(perfil);
        double minD = rango[0];
        double maxD = rango[1];

        if (minD == 0 && maxD == 0) {
            Log.e("validarDiametro", "No se encontró rango para el perfil: " + perfil);
            etDiametro.setError("No se encontró rango para " + perfil);
            return false;
        }

        if (dNuevo < minD || dNuevo > maxD) {
            etDiametro.setError("Rango válido: " + minD + " - " + maxD + " mm");
            return false; // ¡No calcular!
        }

        etDiametro.setError(null); // Limpiar error si es válido
        return true; // Válido
    }


    /**
     * Busca la tabla Pb correcta y llama a getDiametroRange.
     */
    private double[] getDiametroRangeForProfile(String perfil) {
        if (perfilesClasicas.contains(perfil)) {
            return getDiametroRange(perfil, tablaPbClasicas);
        } else if (perfilesClasicaX.contains(perfil)) {
            return getDiametroRange(perfil, tablaPbClasicaX);
        } else if (perfilesCuña.contains(perfil)) {
            return getDiametroRange(perfil, tablaPbCuña);
        } else if (perfilesCuñaX.contains(perfil)) {
            return getDiametroRange(perfil, tablaPbCuñaX);
        } else if (perfilesEstrecha.contains(perfil)) {
            return getDiametroRange(perfil, tablaPbEstrecha);
        } else if (perfilesEstrechaX.contains(perfil)) {
            return getDiametroRange(perfil, tablaPbEstrechaX);
        }
        Log.w("getDiametroRange", "Perfil no encontrado en ningún grupo: " + perfil);
        return new double[]{0, 99999};
    }

    /**
     * Extrae el rango de diámetro (min y max) para un perfil específico
     */
    private double[] getDiametroRange(String perfil, List<String[]> tablaPb) {
        if (tablaPb == null || tablaPb.isEmpty()) {
            return new double[]{0, 0};
        }

        String[] header = tablaPb.get(0);
        double min = Double.MAX_VALUE;
        double max = 0.0;

        Set<Integer> columnasValidas = new HashSet<>();

        for (int i = 1; i < tablaPb.size(); i++) { // Iterar filas de datos
            String[] row = tablaPb.get(i);
            if (row.length > 0 && row[0].trim().equalsIgnoreCase(perfil)) {
                for (int j = 2; j < row.length; j++) { // Iterar columnas de diámetro
                    double val = parsearDoble(row[j]);
                    if (val > 0) {
                        columnasValidas.add(j);
                    }
                }
            }
        }

        if (columnasValidas.isEmpty()) {
            Log.e("getDiametroRange", "No se encontraron datos de diámetro para el perfil: " + perfil);
            return new double[]{0, 0};
        }

        for (int colIndex : columnasValidas) {
            if (colIndex < header.length) {
                double d_header = parsearDoble(header[colIndex]);
                if (d_header > 0) {
                    if (d_header < min) min = d_header;
                    if (d_header > max) max = d_header;
                }
            }
        }

        if (min == Double.MAX_VALUE) min = 0.0;

        return new double[]{min, max};
    }


    /**
     * Lógica unificada para seleccionar banda comercial y calcular el resto de parámetros.
     */
    private void seleccionarBandaComercial(String perfil, double Lprima, double deltaI, double deltaE,
                                           double Dd, double dd, double pb, double pd,
                                           double v, double m, ResultadoViews views) {

        String tipoLongitudPerfil = findTipoLongitudParaPerfil(perfil);
        if (tipoLongitudPerfil == null) {
            resetResultadosTotales(views, "Perfil no en Dayco");
            return;
        }

        double longitudCalculada;
        if (tipoLongitudPerfil.equalsIgnoreCase("Interna")) {
            longitudCalculada = Lprima - deltaI;
        } else if (tipoLongitudPerfil.equalsIgnoreCase("Externa")) {
            longitudCalculada = Lprima + deltaE;
        } else { // "Referencia"
            longitudCalculada = Lprima;
        }

        String codigoSeleccionado = "--";
        double longitudSeleccionada_mm = 0.0;
        String tipoLongitudSeleccionada = "--";
        double minimaLongitudValida = Double.MAX_VALUE; // Rastrear la mejor banda

        for (String[] banda : perfilesDayco) {
            if (banda.length >= 4 && banda[0].equalsIgnoreCase(perfil) && banda[2].equalsIgnoreCase(tipoLongitudPerfil)) {
                try {
                    double longitudTabla = Double.parseDouble(banda[3].trim());
                    if (longitudTabla >= longitudCalculada) {
                        if (longitudTabla < minimaLongitudValida) {
                            minimaLongitudValida = longitudTabla;
                            codigoSeleccionado = banda[1];
                            longitudSeleccionada_mm = longitudTabla;
                            tipoLongitudSeleccionada = banda[2];
                        }
                    }
                } catch (NumberFormatException e) {
                    Log.e("SeleccionBanda", "Error parse Dayco: " + (banda.length > 3 ? banda[3] : "COLUMNA FALTANTE") + " en línea: " + Arrays.toString(banda));
                } catch (IndexOutOfBoundsException e) {
                    Log.e("SeleccionBanda", "Error de índice en línea: " + Arrays.toString(banda));
                }
            }
        }

        views.tvCodigoBanda.setText("Código Dayco: " + codigoSeleccionado);

        if (longitudSeleccionada_mm > 0 && longitudSeleccionada_mm != Double.MAX_VALUE) {
            double longitudLp, li_display, le_display;

            if (tipoLongitudSeleccionada.equalsIgnoreCase("Interna")) {
                li_display = longitudSeleccionada_mm;
                longitudLp = li_display + deltaI;       // Lp = Li + deltai
                le_display = longitudLp + deltaE;       // Le = Lp + deltae
            } else if (tipoLongitudSeleccionada.equalsIgnoreCase("Externa")) {
                le_display = longitudSeleccionada_mm;
                longitudLp = le_display - deltaE;       // Lp = Le - deltae
                li_display = longitudLp - deltaI;       // Li = Lp - deltai
            } else { // "Referencia" (La longitud de la tabla es Lp)
                longitudLp = longitudSeleccionada_mm;
                li_display = longitudLp - deltaI;       // Li = Lp - deltai
                le_display = longitudLp + deltaE;       // Le = Lp + deltae
            }

            views.tvLongitudBanda.setText("Li: " + df.format(li_display) + " mm");
            views.tvLongitudLp.setText("Lp: " + df.format(longitudLp) + " mm");
            views.tvLongitudLe.setText("Le: " + df.format(le_display) + " mm");

            double b = 2 * longitudLp - Math.PI * (Dd + dd);
            double interiorRaiz = Math.pow(b, 2) - 8 * Math.pow(Dd - dd, 2);

            if (interiorRaiz >= 0) {
                double C = (b + Math.sqrt(interiorRaiz)) / 8;
                double Ie = C - ((Lprima - longitudLp) / 2);
                views.tvDistanciaCentrosNecesaria.setText("C: " + df.format(C) + " mm");
                views.tvDistanciaEfectiva.setText("Ie: " + df.format(Ie) + " mm");

                if (Ie > 0) {
                    double arcoContacto = 180 - (57 * (Dd - dd) / Ie);
                    views.tvArcoContacto.setText("Arco: " + df.format(arcoContacto) + "°");

                    double Cy = findFactorCorreccion(arcoContacto, tablaFactorAngulo);
                    views.tvFactorCorreccion.setText("Cy: " + df4.format(Cy));

                    double cl = 1.0;
                    if (perfilesEnPulgadas.contains(perfil)) {
                        double li_pulgadas = li_display / 25.4;
                        cl = findClValue_Inches(perfil, li_pulgadas);

                    } else if (perfilesCuña.contains(perfil)) {
                        cl = findClValue_generic_mm(perfil, longitudSeleccionada_mm, tablaCl_Cuna);

                    } else if (perfilesCuñaX.contains(perfil)) {
                        cl = findClValue_generic_mm(perfil, longitudSeleccionada_mm, tablaCl_CunaX);

                    } else if (perfilesEstrecha.contains(perfil)) {
                        cl = findClValue_generic_mm(perfil, longitudSeleccionada_mm, tablaCl_Estrecha);

                    } else if (perfilesEstrechaX.contains(perfil)) {
                        cl = findClValue_generic_mm(perfil, longitudSeleccionada_mm, tablaCl_EstrechaX);
                    }

                    views.tvClResultado.setText("CL: " + String.valueOf(cl));

                    double pa = (pb + pd) * Cy * cl;
                    views.tvPotenciaAdmisible.setText("Pa: " + df.format(pa) + " kW");

                    if (pa > 0) {
                        // 'potenciaCorregida' es miembro de la clase
                        double Q_double = potenciaCorregida / pa;
                        int Q = (int) Math.ceil(Q_double);
                        views.tvNumeroBandas.setText(String.valueOf(Q));

                        double C_alfa = findFactorCorreccion(arcoContacto, tablaFactorArco);
                        views.tvCAlfa.setText("Cα: " + df4.format(C_alfa));

                        double Ts = 0.0;
                        if (C_alfa > 0 && v > 0 && Q > 0) {
                            Ts = (500 * ((2.5 - C_alfa) / C_alfa) * (potenciaCorregida / (Q * v))) + (m * Math.pow(v, 2));
                        }
                        double Fu = (v > 0 && Q > 0) ? (potenciaCorregida * 1000) / (v * Q) : 0.0;
                        double T1 = Ts + (Fu / 2);
                        double T2 = Ts - (Fu / 2);

                        views.tvTensionEstatica.setText("Ts: " + df.format(Ts) + " N");
                        views.tvTensionCentrifuga.setText("Fu: " + df.format(Fu) + " N");
                        views.tvTensionTenso.setText("T1: " + df.format(T1) + " N");
                        views.tvTensionFlojo.setText("T2: " + df.format(T2) + " N");
                    } else {
                        resetResultadosParciales(views, "Error Pa");
                    }
                } else {
                    resetResultadosParciales(views, "Error Ie");
                }
            } else {
                resetResultadosTotales(views, "Raíz neg.");
            }
        } else {
            resetResultadosTotales(views, "No hay banda disponible");
        }
    }

    private String findTipoLongitudParaPerfil(String perfil) {
        if (perfilesDayco.isEmpty()) return null;
        for (String[] banda : perfilesDayco) {
            if (banda.length > 2 && banda[0].equalsIgnoreCase(perfil)) {
                return banda[2];
            }
        }
        return null;
    }

    private void resetResultadosParciales(ResultadoViews views, String motivo) {
        views.tvNumeroBandas.setText("--");
        views.tvCAlfa.setText(motivo); // Pone el motivo de error aquí
        views.tvTensionEstatica.setText("Ts: -- N");
        views.tvTensionCentrifuga.setText("Fu: -- N");
        views.tvTensionTenso.setText("T1: -- N");
        views.tvTensionFlojo.setText("T2: -- N");
    }

    private void resetResultadosTotales(ResultadoViews views, String motivo) {
        views.tvDistanciaCentrosNecesaria.setText("Distancia entre centros necesaria: --");
        views.tvDistanciaEfectiva.setText("Ie: --");
        views.tvArcoContacto.setText("Arco: --");
        views.tvFactorCorreccion.setText("Cy: --");
        views.tvClResultado.setText(motivo); // Pone "Raíz neg." o "No banda com." aquí
        views.tvPotenciaAdmisible.setText("Pa: --");
        resetResultadosParciales(views, "--");
        views.tvLongitudBanda.setText("Li: -- mm");
        views.tvLongitudLp.setText("Lp: -- mm");
        views.tvLongitudLe.setText("Le: -- mm");
        if (motivo.equals("No banda com.")) {
            views.tvCodigoBanda.setText("Código: --");
        }
    }


    private double findFactorCorreccion(double arco, List<String[]> tabla) {
        if (tabla.isEmpty()) return 1.0;
        List<double[]> data = new ArrayList<>();
        for (String[] row : tabla) {
            try { data.add(new double[]{Double.parseDouble(row[0]), Double.parseDouble(row[1].replace(',', '.'))}); } catch (Exception e) { /* Ignorar */ }
        }
        if (data.isEmpty()) return 1.0;
        Collections.sort(data, new Comparator<double[]>() {
            @Override public int compare(double[] o1, double[] o2) { return Double.compare(o1[0], o2[0]); }
        });
        if (arco <= data.get(0)[0]) return data.get(0)[1];
        if (arco >= data.get(data.size() - 1)[0]) return data.get(data.size() - 1)[1];
        for (int i = 0; i < data.size() - 1; i++) {
            if (arco >= data.get(i)[0] && arco <= data.get(i + 1)[0]) {
                double x1 = data.get(i)[0], y1 = data.get(i)[1];
                double x2 = data.get(i + 1)[0], y2 = data.get(i + 1)[1];
                return (x2 == x1) ? y1 : y1 + (arco - x1) * (y2 - y1) / (x2 - x1);
            }
        }
        return 1.0;
    }


    // =================================================================================
    // LÓGICA DE CÁLCULO PB y PD (CORREGIDA Y ROBUSTA)
    // =================================================================================

    private double findPbValue(String perfil, double rpm, double diametro) {
        List<String[]> tablaSeleccionada = null;

        if (perfilesClasicas.contains(perfil)) {
            tablaSeleccionada = tablaPbClasicas;
        } else if (perfilesClasicaX.contains(perfil)) {
            tablaSeleccionada = tablaPbClasicaX;
        } else if (perfilesCuña.contains(perfil)) {
            tablaSeleccionada = tablaPbCuña;
        } else if (perfilesCuñaX.contains(perfil)) {
            tablaSeleccionada = tablaPbCuñaX;
        } else if (perfilesEstrecha.contains(perfil)) {
            tablaSeleccionada = tablaPbEstrecha;
        } else if (perfilesEstrechaX.contains(perfil)) {
            tablaSeleccionada = tablaPbEstrechaX;
        }

        if (tablaSeleccionada == null || tablaSeleccionada.isEmpty()) {
            Log.e("findPbValue", "No se encontró tabla de PB para el perfil: " + perfil);
            return 0.0;
        }
        return getPotenciaBase(tablaSeleccionada, perfil, rpm, diametro);
    }


    private double findPdValue(String perfil, double rpm, double mg_ratio) {
        List<String[]> tablaSeleccionada = null;

        if (perfilesClasicas.contains(perfil)) {
            tablaSeleccionada = tablaPdClasicas;
        } else if (perfilesClasicaX.contains(perfil)) {
            tablaSeleccionada = tablaPdClasicaX;
        } else if (perfilesCuña.contains(perfil)) {
            tablaSeleccionada = tablaPdCuña;
        } else if (perfilesCuñaX.contains(perfil)) {
            tablaSeleccionada = tablaPdCuñaX;
        } else if (perfilesEstrecha.contains(perfil)) {
            tablaSeleccionada = tablaPdEstrecha;
        } else if (perfilesEstrechaX.contains(perfil)) {
            tablaSeleccionada = tablaPdEstrechaX;
        }

        if (tablaSeleccionada == null || tablaSeleccionada.isEmpty()) {
            Log.e("findPdValue", "No se encontró tabla de PD para el perfil: " + perfil);
            return 0.0;
        }
        return getPotenciaAdicional(tablaSeleccionada, perfil, rpm, mg_ratio);
    }


    // --- INICIO: BLOQUE DE MÉTODOS ROBUSTOS (5 Métodos) ---

    private static double interpolar(double x1, double y1, double x2, double y2, double x) {
        if (x2 - x1 == 0) {
            return y1; // Evitar división por cero
        }
        return y1 + ((x - x1) * (y2 - y1) / (x2 - x1));
    }

    private static double parsearDoble(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0.0; // Celda vacía
        }
        try {
            return Double.parseDouble(valor.replace("*", "").replace(",", ".").trim());
        } catch (NumberFormatException e) {
            return 0.0; // No se pudo parsear
        }
    }

    /**
     * [MODIFICADO] Busca la Potencia Base (Pb) en una tabla usando interpolación bilineal.
     * Interpola tanto para RPM (ne) como para Diámetro (d), ignorando valores 0.
     */
    public static double getPotenciaBase(List<String[]> tablaDatos, String perfilBuscado, double ne, double d) {
        if (tablaDatos == null || tablaDatos.isEmpty()) return 0.0;

        String[] header = tablaDatos.get(0);

        // --- 1. Encontrar Filas de RPM (rpm_inf, rpm_sup) ---
        double rpm_inf = 0.0, rpm_sup = Double.MAX_VALUE;
        int idx_rpm_inf = -1, idx_rpm_sup = -1;

        for (int i = 1; i < tablaDatos.size(); i++) {
            String[] row = tablaDatos.get(i);
            // Solo procesar filas del perfil buscado
            if (row.length > 0 && row[0].trim().equalsIgnoreCase(perfilBuscado)) {
                double rpm_tabla = parsearDoble(row[1]);

                // Para rpm_inf (el más grande <= ne)
                if (rpm_tabla <= ne && rpm_tabla >= rpm_inf) {
                    rpm_inf = rpm_tabla;
                    idx_rpm_inf = i;
                }
                // Para rpm_sup (el más pequeño >= ne)
                if (rpm_tabla >= ne && rpm_tabla <= rpm_sup) {
                    rpm_sup = rpm_tabla;
                    idx_rpm_sup = i;
                }
            }
        }

        // --- 2. Manejar Casos de Borde (Clamping) para RPM ---
        if (idx_rpm_inf == -1) idx_rpm_inf = idx_rpm_sup; // Si ne es menor que el min, usar el min
        if (idx_rpm_sup == -1) idx_rpm_sup = idx_rpm_inf; // Si ne es mayor que el max, usar el max

        // Chequeo de seguridad
        if (idx_rpm_inf == -1) {
            Log.e("GetPb", "No se encontraron filas de RPM. Perfil: " + perfilBuscado + ", ne: " + ne);
            return 0.0; // No se encontró nada
        }

        // Actualizar valores inf/sup en caso de que solo se haya encontrado uno
        if (rpm_inf == 0.0) rpm_inf = rpm_sup;
        if (rpm_sup == Double.MAX_VALUE) rpm_sup = rpm_inf;

        // --- 3. Interpolar por Diámetro para cada Fila de RPM ---
        String[] row_inf = tablaDatos.get(idx_rpm_inf);
        String[] row_sup = tablaDatos.get(idx_rpm_sup);

        double val_interp_rpm_inf = interpolarEnFila(row_inf, header, d);
        double val_interp_rpm_sup = interpolarEnFila(row_sup, header, d);

        // --- 4. Interpolar verticalmente (por RPM) ---
        double val_final = interpolar(rpm_inf, val_interp_rpm_inf, rpm_sup, val_interp_rpm_sup, ne);

        return val_final;
    }

    /**
     * [NUEVO] Función de ayuda para interpolar el valor de 'd' en una sola fila de datos.
     * Esta función SÍ ignora las columnas con valor 0.
     * @param row La fila de datos (ej. la fila de 1400 RPM)
     * @param header La fila de cabecera (para obtener los diámetros)
     * @param d El diámetro que se busca (ej. 270)
     * @return El valor interpolado para esa fila.
     */
    private static double interpolarEnFila(String[] row, String[] header, double d) {
        double d_inf = 0.0, d_sup = Double.MAX_VALUE;
        double val_inf = 0.0, val_sup = 0.0;
        int col_d_inf = -1, col_d_sup = -1;

        for (int i = 2; i < header.length; i++) {
            if (i >= row.length) continue; // Seguridad por si la fila es más corta

            double val_celda = parsearDoble(row[i]);
            if (val_celda <= 0) continue; // ¡LA CLAVE! Ignorar celdas sin valor

            double d_header = parsearDoble(header[i]);
            if (d_header <= 0) continue;

            // Para d_inf (el más grande <= d con valor > 0)
            if (d_header <= d && d_header >= d_inf) {
                d_inf = d_header;
                col_d_inf = i;
                val_inf = val_celda;
            }
            // Para d_sup (el más pequeño >= d con valor > 0)
            if (d_header >= d && d_header <= d_sup) {
                d_sup = d_header;
                col_d_sup = i;
                val_sup = val_celda;
            }
        }

        // --- Manejar Casos de Borde (Clamping) para Diámetro ---
        if (col_d_inf == -1) { // d es menor que el primer diámetro con valor
            d_inf = d_sup;
            val_inf = val_sup;
        }
        if (col_d_sup == -1) { // d es mayor que el último diámetro con valor
            d_sup = d_inf;
            val_sup = val_inf;
        }

        if (col_d_inf == -1 && col_d_sup == -1) { // No se encontró ningún valor > 0 en la fila
            return 0.0;
        }

        // Interpolar horizontalmente (por Diámetro)
        return interpolar(d_inf, val_inf, d_sup, val_sup, d);
    }


    /**
     * Busca la Potencia Adicional (Pd) en una tabla interpolando por RPM.
     */
    public static double getPotenciaAdicional(List<String[]> tablaDatos, String perfilBuscado, double ne, double relacion) {
        if (tablaDatos == null || tablaDatos.isEmpty()) return 0.0;

        String[] header = tablaDatos.get(0);
        int col_rel = -1;
        for (int i = 2; i < header.length; i++) {
            if (esRelacionEnRango(relacion, header[i])) {
                col_rel = i;
                break;
            }
        }

        if (col_rel == -1) {
            Log.e("GetPd", "Relación no encontrada en rango: " + relacion + " para perfil " + perfilBuscado);
            return 0.0;
        }

        double rpm_inf = 0.0, val_inf = 0.0;
        double rpm_sup = Double.MAX_VALUE, val_sup = 0.0;
        boolean encontradoInf = false;
        boolean encontradoSup = false;

        for (int i = 1; i < tablaDatos.size(); i++) {
            String[] row = tablaDatos.get(i);
            if(row.length <= col_rel) continue;

            if (row[0].trim().equalsIgnoreCase(perfilBuscado)) {
                double rpm_tabla = parsearDoble(row[1]);

                if (Math.abs(rpm_tabla - ne) < 0.001) {
                    return parsearDoble(row[col_rel]);
                }

                if (rpm_tabla < ne && rpm_tabla >= rpm_inf) {
                    rpm_inf = rpm_tabla;
                    val_inf = parsearDoble(row[col_rel]);
                    encontradoInf = true;
                }

                if (rpm_tabla > ne && rpm_tabla < rpm_sup) {
                    rpm_sup = rpm_tabla;
                    val_sup = parsearDoble(row[col_rel]);
                    encontradoSup = true;
                }
            }
        }

        if (!encontradoInf || !encontradoSup) {
            Log.e("GetPd", "No se encontraron rangos de RPM para interpolar. Perfil: " + perfilBuscado + ", RPM: " + ne);
            if(encontradoInf && !encontradoSup) return val_inf;
            if(!encontradoInf && encontradoSup) return val_sup;
            return 0.0;
        }

        return interpolar(rpm_inf, val_inf, rpm_sup, val_sup, ne);
    }

    /**
     * Comprueba si una relación de transmisión está dentro de un rango de cabecera.
     */
    private static boolean esRelacionEnRango(double relacion, String rangoHeader) {
        rangoHeader = rangoHeader.trim().replace(",", ".");

        try {
            if (rangoHeader.toLowerCase().contains("mas de")) {
                double limite = parsearDoble(rangoHeader.replaceAll("(?i)mas de", ""));
                return relacion >= limite;

            } else if (rangoHeader.contains("-") || rangoHeader.contains("/")) {
                String[] partes = rangoHeader.split("[-/]");
                if (partes.length == 2) {
                    double min = parsearDoble(partes[0]);
                    double max = parsearDoble(partes[1]);
                    if (min > max) { double temp = min; min = max; max = temp; }
                    return (relacion >= (min - 0.001)) && (relacion <= (max + 0.001));
                }
            } else {
                double val = parsearDoble(rangoHeader);
                return Math.abs(relacion - val) < 0.001;
            }
        } catch (Exception e) {
            Log.e("Rango", "Error al parsear rango: " + rangoHeader, e);
            return false;
        }
        return false;
    }

    // --- FIN: BLOQUE DE MÉTODOS ROBUSTOS ---

    // =================================================================================


    private double findClValue_Inches(String perfil, double longitudPulgadas) {
        if (tablaCl_Inches.isEmpty()) return 1.0;
        String[] header = tablaCl_Inches.get(0);
        int colIndex = -1; double minDiff = Double.MAX_VALUE;

        for (int i = 1; i < header.length; i++) {
            try {
                if (header[i] == null || header[i].trim().isEmpty()) continue;
                double longitudHeader = Double.parseDouble(header[i].replace(',', '.'));
                double diff = Math.abs(longitudHeader - longitudPulgadas);
                if (diff < minDiff) {
                    minDiff = diff;
                    colIndex = i;
                }
            } catch (Exception e) { continue; }
        }

        if (colIndex == -1) return 1.0;

        for (int i = 1; i < tablaCl_Inches.size(); i++) {
            String[] row = tablaCl_Inches.get(i);
            if (row.length > colIndex && row[0].equalsIgnoreCase(perfil)) {
                try {
                    String value = row[colIndex];
                    return (value == null || value.trim().isEmpty()) ? 1.0 : Double.parseDouble(value.replace(',', '.'));
                } catch (Exception e) { return 1.0; }
            }
        }
        return 1.0;
    }

    /**
     * Busca el valor CL genérico para bandas en mm (Cuña, Estrecha, etc.)
     */
    private double findClValue_generic_mm(String perfil, double longitudMM, List<String[]> tabla) {
        if (tabla.isEmpty()) return 1.0;
        String[] header = tabla.get(0);
        int colIndex = -1; double minDiff = Double.MAX_VALUE;

        for (int i = 1; i < header.length; i++) {
            try {
                if (header[i] == null || header[i].trim().isEmpty()) continue;
                double longitudHeader = Double.parseDouble(header[i].replace(',', '.'));
                double diff = Math.abs(longitudHeader - longitudMM);
                if (diff < minDiff) {
                    minDiff = diff;
                    colIndex = i;
                }
            } catch (Exception e) { continue; }
        }

        if (colIndex == -1) return 1.0;

        for (int i = 1; i < tabla.size(); i++) {
            String[] row = tabla.get(i);
            if (row.length > colIndex && row[0].equalsIgnoreCase(perfil)) {
                try {
                    String value = row[colIndex];
                    return (value == null || value.trim().isEmpty()) ? 1.0 : Double.parseDouble(value.replace(',', '.'));
                } catch (Exception e) { return 1.0; }
            }
        }
        return 1.0;
    }


    // --- MÉTODOS DE SELECCIÓN DE PERFIL ---

    private String seleccionarPerfilClasica(double kw, double rpm) {
        if (rpm < 5000 && kw < (rpm / 1250.0)) return "Z";
        if (rpm < 4000 && kw < (rpm / 133.0)) return "A";
        if (rpm < 2500 && kw < (rpm / 35.7)) return "B";
        if (rpm < 3000 && kw < (rpm / 10.0)) return "C";
        if (rpm < 2000 && kw < (rpm / 3.33)) return "D";
        if (rpm < 1000) return "E";
        return "-";
    }

    private String seleccionarPerfilEstrechaDIN(double kw, double rpm) {
        try {
            // 1. Definición de las curvas divisorias (Límites inferiores de cada zona)
            double kw_L1_SPZ_SPA = 0.04 * Math.pow(rpm, 0.69); // Frontera entre SPZ y SPA
            double kw_L2_SPA_SPB = 0.44 * Math.pow(rpm, 0.63); // Frontera entre SPA y SPB
            double kw_L3_SPB_SPC = 1.9 * Math.pow(rpm, 0.60);  // Frontera entre SPB y SPC

            // 2. Definición del TECHO de la gráfica (Límite superior absoluto de potencia)
            double kw_Limite_Maximo = 8.0 * Math.pow(rpm, 0.60);

            // --- ZONA SPZ ---
            if (kw < kw_L1_SPZ_SPA) {
                // Límite físico rectificado: SPZ hasta ~5000 RPM
                if (rpm > 5000) return "-";
                return "SPZ";
            }

            // --- ZONA SPA ---
            if (kw < kw_L2_SPA_SPB) {
                // Límite físico rectificado: SPA hasta ~5000 RPM
                if (rpm > 5000) return "-";
                return "SPA";
            }

            // --- ZONA SPB ---
            if (kw < kw_L3_SPB_SPC) {
                // Límite físico rectificado: SPB hasta ~3500 RPM
                if (rpm > 3500) return "-";
                return "SPB";
            }

            // --- ZONA SPC ---

            // Chequeo 1: ¿La potencia es exageradamente alta?
            if (kw > kw_Limite_Maximo) return "-";

            // Chequeo 2: Límite físico rectificado: SPC hasta ~2000 RPM
            if (rpm > 2000) return "-";

            return "SPC";

        } catch (Exception e) {
            return "-";
        }
    }

    private String seleccionarPerfilEstrechaARPM(double kw, double rpm, boolean incluir8V) {
        try {
            // --- 1. LÍMITES GLOBALES DE LA FAMILIA ARPM ---
            // Si los valores exceden lo físicamente posible para estas bandas, descartar.
            if (rpm > 6000) return "-";  // Límite de velocidad para la familia
            if (kw > 500) return "-";    // Límite de potencia para la familia

            // --- 2. LÓGICA DE CURVAS DE SELECCIÓN ---
            // Curva divisoria entre 3V y 5V
            double rpm_L1_3V_5V = Math.min(29 * Math.pow(kw, 1.13), 3000);

            // Curva divisoria entre 5V y 8V
            double rpm_L2_5V_8V = Math.min(3.9 * Math.pow(kw, 1.176), 1500);

            // Zona Superior (Alta velocidad / Baja potencia) -> 3V
            if (rpm > rpm_L1_3V_5V) return "3V";

            // Zona Media -> 5V
            if (rpm > rpm_L2_5V_8V) return "5V";

            // Zona Inferior (Baja velocidad / Alta potencia) -> 8V
            // Si estamos en dentadas (incluir8V es false), 8V no existe, así que retornamos "-"
            return (incluir8V) ? "8V" : "-";

        } catch (Exception e) {
            return "-";
        }
    }

    private String seleccionarPerfilClasicaX(double kw, double rpm) {
        try {
            double rpm_L1_ZX_AX = Math.min(625 * Math.pow(kw, 1.51), 7000);
            double rpm_L2_AX_BX = Math.min(6.66 * Math.pow(kw, 1.7), 7000);
            double rpm_L3_BX_CX = Math.min(1.32 * Math.pow(kw, 1.88), 5000);

            if (rpm > rpm_L1_ZX_AX) return "ZX";
            if (rpm > rpm_L2_AX_BX) return "AX";
            if (rpm > rpm_L3_BX_CX) return "BX";
            return "CX";
        } catch (Exception e) { return "-"; }
    }

    private String seleccionarPerfilEstrechaDIN_X(double kw, double rpm) {
        try {
            double rpm_cap_L1 = 7000;
            double rpm_cap_L2 = 5000;
            double rpm_cap_L3 = 2500;

            double rpm_L1_XPZ_XPA = Math.min(172 * Math.pow(kw, 1.47), rpm_cap_L1);
            double rpm_L2_XPA_XPB = Math.min(58.8 * Math.pow(kw, 1.31), rpm_cap_L2);
            double rpm_L3_XPB_XPC = Math.min(19.3 * Math.pow(kw, 1.19), rpm_cap_L3);

            if (rpm > rpm_L1_XPZ_XPA) return "XPZ";
            if (rpm > rpm_L2_XPA_XPB) return "XPA";
            if (rpm > rpm_L3_XPB_XPC) return "XPB";
            return "XPC";
        } catch (Exception e) { return "-"; }
    }


    // --- INICIO: MÉTODOS PARA GENERAR PDF ---

    /**
     * Recopila todos los datos de entrada y resultados visibles para el PDF.
     */
    private LinkedHashMap<String, String> collectDataForPdf() {
        LinkedHashMap<String, String> data = new LinkedHashMap<>();

        // --- 1. DATOS DE CÁLCULO (ENTRADA) ---
        data.put("HEADER_DATOS", "Datos de Cálculo");
        data.put("DATO_Potencia_Nominal", potenciaNominalStr);
        data.put("DATO_Factor_Servicio", factorServicioStr);
        data.put("DATO_Potencia_Corregida", df.format(potenciaCorregida) + " Kw");
        data.put("DATO_RPM_Motor", neRpmStr);
        data.put("DATO_RPM_Maquina", nsRpmStr);
        data.put("DATO_Relacion_Transmision", mgStr);
        data.put("DATO_Distancia_Centros", distanciaStr);
        data.put("DATO_Tipo_Banda", tipoBandaStr);

        // --- 2. ALTERNATIVAS (RESULTADOS) ---
        data.put("HEADER_ALTERNATIVAS", "Alternativas de Selección");

        // Tarjeta 1: Clásica
        if (layoutClasica.getVisibility() == View.VISIBLE) {
            collectDataFromCard(data, "A1", "Alternativa 1: Clásica (LISA)",
                    viewsClasica, layoutResultadosIter2Clasica, viewsClasicaIter2);
            data.put("SPACER_A1", "10"); // Espaciador
        }
        // Tarjeta 2: Estrecha DIN
        if (layoutEstrechaDin.getVisibility() == View.VISIBLE) {
            collectDataFromCard(data, "A2", "Alternativa 2: Estrecha DIN (LISA)",
                    viewsEstrechaDin, layoutResultadosIter2EstrechaDin, viewsEstrechaDinIter2);
            data.put("SPACER_A2", "10");
        }
        // Tarjeta 3: Estrecha ARPM
        if (layoutEstrechaArpm.getVisibility() == View.VISIBLE) {
            collectDataFromCard(data, "A3", "Alternativa 3: Estrecha ARPM (LISA)",
                    viewsEstrechaArpm, layoutResultadosIter2EstrechaArpm, viewsEstrechaArpmIter2);
            data.put("SPACER_A3", "10");
        }
        // Tarjeta 4: Clásica X
        if (layoutClasicaX.getVisibility() == View.VISIBLE) {
            collectDataFromCard(data, "A4", "Alternativa 1: Clásica X (DENTADA)",
                    viewsClasicaX, layoutResultadosIter2ClasicaX, viewsClasicaXIter2);
            data.put("SPACER_A4", "10");
        }
        // Tarjeta 5: Estrecha DIN X
        if (layoutEstrechaDinX.getVisibility() == View.VISIBLE) {
            collectDataFromCard(data, "A5", "Alternativa 2: Estrecha DIN X (DENTADA)",
                    viewsEstrechaDinX, layoutResultadosIter2EstrechaDinX, viewsEstrechaDinXIter2);
            data.put("SPACER_A5", "10");
        }
        // Tarjeta 6: Estrecha ARPM X
        if (layoutEstrechaArpmX.getVisibility() == View.VISIBLE) {
            collectDataFromCard(data, "A6", "Alternativa 3: Estrecha ARPM X (DENTADA)",
                    viewsEstrechaArpmX, layoutResultadosIter2EstrechaArpmX, viewsEstrechaArpmXIter2);
            data.put("SPACER_A6", "10");
        }

        return data;
    }

    /**
     * Ayudante para recopilar datos de una sola tarjeta (Iter 1 e Iter 2 si es visible).
     */
    private void collectDataFromCard(LinkedHashMap<String, String> data, String prefix, String header,
                                     ResultadoViews iter1Views, LinearLayout iter2Layout, ResultadoViews iter2Views) {

        // --- Iteración 1 ---
        data.put(prefix + "_HEADER", header);
        // Perfil y # Bandas
        data.put(prefix + "_DATA_1_Perfil", "Perfil: " + iter1Views.tvPerfilBanda.getText().toString());
        data.put(prefix + "_DATA_2_NumBandas", "# Bandas (Q): " + iter1Views.tvNumeroBandas.getText().toString());
        // d, D, V, L calc
        data.put(prefix + "_DATA_3_dD", iter1Views.tvDiametroMinimo.getText().toString() + "  |  " + iter1Views.tvDiametroMaximo.getText().toString());
        data.put(prefix + "_DATA_4_VL", iter1Views.tvVelocidadPeriferica.getText().toString() + "  |  " + iter1Views.tvLongitudCorrea.getText().toString());
        // Banda Seleccionada
        data.put(prefix + "_DATA_TITLE_1_Banda", "Banda Seleccionada");
        data.put(prefix + "_DATA_5_Codigo", iter1Views.tvCodigoBanda.getText().toString());
        data.put(prefix + "_DATA_6_Li", iter1Views.tvLongitudBanda.getText().toString());
        data.put(prefix + "_DATA_7_Lp", iter1Views.tvLongitudLp.getText().toString());
        data.put(prefix + "_DATA_8_Le", iter1Views.tvLongitudLe.getText().toString());
// --- MODIFICACIÓN PARA CAMBIAR "C" POR TEXTO LARGO ---
        // 1. Obtenemos el texto original (Ej: "C: 450.5 mm")
        String textoC_Original = iter1Views.tvDistanciaCentrosNecesaria.getText().toString();

        // 2. Le quitamos la "C:" y los espacios sobrantes
        String valorSolo = textoC_Original.replace("C:", "").trim(); // Queda "450.5 mm"

        // 3. Creamos el texto final con el título largo
        String textoFinalC = "Distancia necesaria entre centros: " + valorSolo;

        // 4. Lo guardamos en el mapa
        data.put(prefix + "_DATA_9_C", textoFinalC);        data.put(prefix + "_DATA_10_Ie", iter1Views.tvDistanciaEfectiva.getText().toString());
        // Potencias y Factores
        data.put(prefix + "_DATA_11_PbPdPa", iter1Views.tvPbResultado.getText().toString() + " | " + iter1Views.tvPdResultado.getText().toString() + " | " + iter1Views.tvPotenciaAdmisible.getText().toString());
        data.put(prefix + "_DATA_12_ArcoCyCl", iter1Views.tvArcoContacto.getText().toString() + " | " + iter1Views.tvFactorCorreccion.getText().toString() + " | " + iter1Views.tvClResultado.getText().toString());
        // Cálculos de Ingeniería
        data.put(prefix + "_DATA_TITLE_2_Ing", "Cálculos de Ingeniería");
        data.put(prefix + "_DATA_13_CAlfaTs", iter1Views.tvCAlfa.getText().toString() + "  |  " + iter1Views.tvTensionEstatica.getText().toString());
        data.put(prefix + "_DATA_14_FuT1T2", iter1Views.tvTensionCentrifuga.getText().toString() + " | " + iter1Views.tvTensionTenso.getText().toString() + " | " + iter1Views.tvTensionFlojo.getText().toString());


        // --- Iteración 2 (SI ES VISIBLE) ---
        if (iter2Layout.getVisibility() == View.VISIBLE) {
            // "SI" = Segunda Iteración
            String si_prefix = prefix + "_SI";

            data.put(si_prefix + "_HEADER", "Resultados Iteración 2");
            // Perfil y # Bandas
            data.put(si_prefix + "_DATA_1_Perfil", "Perfil: " + iter2Views.tvPerfilBanda.getText().toString());
            data.put(si_prefix + "_DATA_2_NumBandas", "# Bandas (Q): " + iter2Views.tvNumeroBandas.getText().toString());
            // d, D, V, L calc
            data.put(si_prefix + "_DATA_3_dD", iter2Views.tvDiametroMinimo.getText().toString() + "  |  " + iter2Views.tvDiametroMaximo.getText().toString());
            data.put(si_prefix + "_DATA_4_VL", iter2Views.tvVelocidadPeriferica.getText().toString() + "  |  " + iter2Views.tvLongitudCorrea.getText().toString());
            // Banda Seleccionada
            data.put(si_prefix + "_DATA_TITLE_1_Banda", "Banda Seleccionada");
            data.put(si_prefix + "_DATA_5_Codigo", iter2Views.tvCodigoBanda.getText().toString());
            data.put(si_prefix + "_DATA_6_Li", iter2Views.tvLongitudBanda.getText().toString());
            data.put(si_prefix + "_DATA_7_Lp", iter2Views.tvLongitudLp.getText().toString());
            data.put(si_prefix + "_DATA_8_Le", iter2Views.tvLongitudLe.getText().toString());
// --- CAMBIO PARA ITERACIÓN 2: Texto largo para C ---
            String textoC_Iter2 = iter2Views.tvDistanciaCentrosNecesaria.getText().toString();
            String valorSolo_Iter2 = textoC_Iter2.replace("C:", "").trim();
            String textoFinalC_Iter2 = "Distancia necesaria entre centros: " + valorSolo_Iter2;

            data.put(si_prefix + "_DATA_9_C", textoFinalC_Iter2);
            data.put(si_prefix + "_DATA_10_Ie", iter2Views.tvDistanciaEfectiva.getText().toString());
            // Potencias y Factores
            data.put(si_prefix + "_DATA_11_PbPdPa", iter2Views.tvPbResultado.getText().toString() + " | " + iter2Views.tvPdResultado.getText().toString() + " | " + iter2Views.tvPotenciaAdmisible.getText().toString());
            data.put(si_prefix + "_DATA_12_ArcoCyCl", iter2Views.tvArcoContacto.getText().toString() + " | " + iter2Views.tvFactorCorreccion.getText().toString() + " | " + iter2Views.tvClResultado.getText().toString());
            // Cálculos de Ingeniería
            data.put(si_prefix + "_DATA_TITLE_2_Ing", "Cálculos de Ingeniería");
            data.put(si_prefix + "_DATA_13_CAlfaTs", iter2Views.tvCAlfa.getText().toString() + "  |  " + iter2Views.tvTensionEstatica.getText().toString());
            data.put(si_prefix + "_DATA_14_FuT1T2", iter2Views.tvTensionCentrifuga.getText().toString() + " | " + iter2Views.tvTensionTenso.getText().toString() + " | " + iter2Views.tvTensionFlojo.getText().toString());
        }
    }


    /**
     * Abre un archivo PDF usando un FileProvider.
     */
    private void openPdf(File file) {
        if (!file.exists()) {
            Toast.makeText(this, "El archivo PDF no existe.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener la URI usando FileProvider
        Uri pdfUri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".provider", // <-- CAMBIO AQUÍ
                file
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); // Opcional: no la mantiene en el historial

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No hay aplicación para ver PDF instalada.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("OpenPDF", "Error al abrir PDF", e);
            Toast.makeText(this, "Error al abrir PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // --- FIN: MÉTODOS PARA GENERAR PDF ---


} // Fin de la clase ResultadosBandasActivity