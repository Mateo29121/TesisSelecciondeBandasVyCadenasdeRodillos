package com.example.selecciondebnadasvycadenasderodillos;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.util.Log;

public class ResultadosCadenasActivity extends AppCompatActivity {

    // --- Vistas ---
    private TextView tvPotenciaDiseno, tvResultadoZ1, tvFactorFzResultado, tvFactorFiResultado;
    private Button btnImprimir;

    private final LinearLayout[] layoutAlternativas = new LinearLayout[4];
    private final TextView[] textViewsCadenas = new TextView[4];
    private final TextView[] textViewsCodigoAnsi = new TextView[4];
    private final TextView[] textViewsAp = new TextView[4];
    private final TextView[] textViewsApWarning = new TextView[4]; // NUEVO
    private final TextView[] textViewsFa = new TextView[4];
    private final TextView[] textViewsVelocidad = new TextView[4];
    private final TextView[] textViewsFl = new TextView[4];
    private final TextView[] textViewsFg = new TextView[4];
    private final TextView[] textViewsPc = new TextView[4];
    private final TextView[] textViewsValidacion = new TextView[4];
    private final TextView[] textViewsAr = new TextView[4];
    private final TextView[] textViewsF = new TextView[4];
    private final TextView[] textViewsFdyn = new TextView[4];
    private final TextView[] textViewsFf = new TextView[4];
    private final TextView[] textViewsFgTotal = new TextView[4];
    private final TextView[] textViewsXUniones = new TextView[4];
    private final TextView[] textViewsDimTitulo = new TextView[4];
    private final TextView[] textViewsDimDo = new TextView[4];
    private final TextView[] textViewsDimDa = new TextView[4];
    private final TextView[] textViewsDimDf = new TextView[4];
    private final TextView[] textViewsDimB1 = new TextView[4];

    private TextView tvTituloSegundaIteracion;
    private final LinearLayout[] layoutsSegundaIteracion = new LinearLayout[4];
    private final TextView[] textViewsSiCadena = new TextView[4];
    private final TextView[] textViewsSiCodigoAnsi = new TextView[4];
    private final TextView[] textViewsSiAp = new TextView[4];
    private final TextView[] textViewsSiApWarning = new TextView[4]; // NUEVO
    private final TextView[] textViewsSiFa = new TextView[4];
    private final TextView[] textViewsSiV = new TextView[4];
    private final TextView[] textViewsSiFl = new TextView[4];
    private final TextView[] textViewsSiFg = new TextView[4];
    private final TextView[] textViewsSiPc = new TextView[4];
    private final TextView[] textViewsSiValidacion = new TextView[4];
    private final TextView[] textViewsSiAr = new TextView[4];
    private final TextView[] textViewsSiF = new TextView[4];
    private final TextView[] textViewsSiFdyn = new TextView[4];
    private final TextView[] textViewsSiFf = new TextView[4];
    private final TextView[] textViewsSiFgTotal = new TextView[4];
    private final TextView[] textViewsSiXUniones = new TextView[4];
    private final TextView[] textViewsSiDimTitulo = new TextView[4];
    private final TextView[] textViewsSiDimDo = new TextView[4];
    private final TextView[] textViewsSiDimDa = new TextView[4];
    private final TextView[] textViewsSiDimDf = new TextView[4];
    private final TextView[] textViewsSiDimB1 = new TextView[4];

    // --- Datos ---
    private double fz, fi;
    private List<String[]> factorFzData = new ArrayList<>();
    private List<String[]> factorFiData = new ArrayList<>();
    private List<String[]> factorFaData = new ArrayList<>();
    private List<String[]> factorFlData = new ArrayList<>();
    private List<String[]> catalogoCadenasData = new ArrayList<>();
    private List<String[]> distanciaRecomendadaData = new ArrayList<>();
    private List<String[]> tablaCData = new ArrayList<>();

    private String selectedChainCsv;
    private Map<String, Integer> headerMap = new HashMap<>();
    private String csvErrorMessage = null;

    private int colTipo = -1;
    private int colCodigo = -1;
    private int colPaso = -1;
    private int colRodillo = -1;
    private int colPlacas = -1;
    private int colPeso = -1;

    private String potenciaNominalStr, factorServicioStr, neRpmStr, nsRpmStr, mgStr;
    private String z1Str, z2Str, tipoCadenaNombre, tipoLubricacion;

    // --- Clases de datos ---
    private static class RpmKwPoint {
        final double rpm, kw;
        RpmKwPoint(double rpm, double kw) { this.rpm = rpm; this.kw = kw; }
    }

    private static class ChainRating {
        final String name;
        final RpmKwPoint[] data;
        final int index;
        ChainRating(String name, RpmKwPoint[] data, int index) {
            this.name = name; this.data = data; this.index = index;
        }
    }

    private static class ChainResult {
        final String displayText;
        final double pitch;
        final ChainRating originalChain;
        ChainResult(String displayText, double pitch, ChainRating originalChain) {
            this.displayText = displayText;
            this.pitch = pitch;
            this.originalChain = originalChain;
        }
    }

    private static class AnalysisResult {
        String displayText = "", ansiCodeText = "", apText = "", apWarningText = "", faText = "", vText = "", flText = "", fgText = "", pcText = "", validationText = "", arText = "";
        boolean resiste = false;
        boolean apValid = true; // Nuevo flag
        double pc = 0;
        String fText = "";
        String fdynText= "";
        String ffText = "";
        String fgTotalText = "";
        String xUnionesText = "";
        String dimTituloText = "";
        String dimDoText = "";
        String dimDaText = "";
        String dimDfText = "";
        String dimB1Text = "";
    }

    // --- Constantes ---
    private static final double[] STRAND_FACTORS = {1.0, 1.7, 2.5};
    private static final String[] TIPO_CADENA_MAP = {"SIMPLEX", "DUPLEX", "TRIPLEX"};

    private static final ChainRating[] CHAIN_RATINGS = {
            new ChainRating("25 P 6,35 mm", new RpmKwPoint[]{ new RpmKwPoint(140, 0.1), new RpmKwPoint(300, 0.21), new RpmKwPoint(500, 0.3), new RpmKwPoint(1000, 0.6), new RpmKwPoint(2000, 1.25), new RpmKwPoint(3000, 1.7), new RpmKwPoint(5000, 2.4) }, 0),
            new ChainRating("35 P 9,525 mm", new RpmKwPoint[]{ new RpmKwPoint(38, 0.1), new RpmKwPoint(100, 0.25), new RpmKwPoint(500, 1.2), new RpmKwPoint(1000, 2.1), new RpmKwPoint(2000, 3.9), new RpmKwPoint(3000, 4.9), new RpmKwPoint(5000, 3.8) }, 1),
            new ChainRating("40 P 12,7 mm", new RpmKwPoint[]{ new RpmKwPoint(14, 0.1), new RpmKwPoint(100, 0.52), new RpmKwPoint(500, 2.8), new RpmKwPoint(1000, 4.8), new RpmKwPoint(2000, 3.9), new RpmKwPoint(3000, 4.8), new RpmKwPoint(3500, 4.7) }, 2),
            new ChainRating("50 P 15,875 mm", new RpmKwPoint[]{ new RpmKwPoint(7, 0.1), new RpmKwPoint(100, 1.2), new RpmKwPoint(200, 2.25), new RpmKwPoint(500, 5.0), new RpmKwPoint(800, 8.0), new RpmKwPoint(1000, 9.5), new RpmKwPoint(2000, 10), new RpmKwPoint(2600, 7.0) }, 3),
            new ChainRating("60 P 19,05 mm", new RpmKwPoint[]{ new RpmKwPoint(3.7, 0.1), new RpmKwPoint(100, 2.1), new RpmKwPoint(300, 6), new RpmKwPoint(500, 9.8), new RpmKwPoint(800, 14), new RpmKwPoint(1000, 17.5), new RpmKwPoint(1500, 16), new RpmKwPoint(1750, 11) }, 4),
            new ChainRating("80 P 25.4 mm", new RpmKwPoint[]{ new RpmKwPoint(1.5, 0.1), new RpmKwPoint(3, 0.19), new RpmKwPoint(10, 0.58), new RpmKwPoint(50, 2.5), new RpmKwPoint(100, 4.9), new RpmKwPoint(500, 21.0), new RpmKwPoint(800, 30), new RpmKwPoint(1250, 18.0) }, 5),
            new ChainRating("100 P 31.75 mm", new RpmKwPoint[]{ new RpmKwPoint(1, 0.14), new RpmKwPoint(5, 0.56), new RpmKwPoint(10, 1.1), new RpmKwPoint(50, 4.9), new RpmKwPoint(100, 9.2), new RpmKwPoint(250, 22.0), new RpmKwPoint(700, 50), new RpmKwPoint(1000, 27.5) }, 6),
            new ChainRating("120 P 38.1 mm", new RpmKwPoint[]{ new RpmKwPoint(1, 0.225), new RpmKwPoint(3, 0.58), new RpmKwPoint(10, 1.8), new RpmKwPoint(20, 3.5), new RpmKwPoint(50, 7.9), new RpmKwPoint(100, 16.0), new RpmKwPoint(500, 65), new RpmKwPoint(700, 50) }, 7),
            new ChainRating("140 P 44.45 mm", new RpmKwPoint[]{ new RpmKwPoint(1, 0.35), new RpmKwPoint(3, 0.89), new RpmKwPoint(10, 2.55), new RpmKwPoint(50, 12.5), new RpmKwPoint(100, 22.5), new RpmKwPoint(200, 44.0), new RpmKwPoint(480, 90.0), new RpmKwPoint(610, 60.0) }, 8),
            new ChainRating("160 P 50.8 mm", new RpmKwPoint[]{ new RpmKwPoint(1, 0.5), new RpmKwPoint(3, 1.3), new RpmKwPoint(10, 4), new RpmKwPoint(50, 18.0), new RpmKwPoint(100, 35.0), new RpmKwPoint(200, 65.0), new RpmKwPoint(450, 125.0), new RpmKwPoint(600, 80.0) }, 9),
            new ChainRating("180 P 57.15 mm", new RpmKwPoint[]{ new RpmKwPoint(1, 0.68), new RpmKwPoint(3, 1.8), new RpmKwPoint(10, 5.5), new RpmKwPoint(50, 24.0), new RpmKwPoint(100, 46.0), new RpmKwPoint(200, 84), new RpmKwPoint(370, 140), new RpmKwPoint(410, 120) }, 10),
            new ChainRating("200 P 63,5 mm", new RpmKwPoint[]{ new RpmKwPoint(1, 0.99), new RpmKwPoint(3, 2.7), new RpmKwPoint(10, 8.2), new RpmKwPoint(50, 39.0), new RpmKwPoint(100, 72.0), new RpmKwPoint(200, 130.0), new RpmKwPoint(275, 150.0), new RpmKwPoint(320, 125.0) }, 11),
            new ChainRating("240 P 76,2 mm", new RpmKwPoint[]{ new RpmKwPoint(1, 1.8), new RpmKwPoint(3, 4.8), new RpmKwPoint(10, 14.9), new RpmKwPoint(50, 64.0), new RpmKwPoint(100, 125.0), new RpmKwPoint(150, 170.0), new RpmKwPoint(180, 175.0), new RpmKwPoint(240, 150.0) }, 12)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultadoscadenas);

        selectedChainCsv = getIntent().getStringExtra("TIPO_CADENA_CSV");
        if (selectedChainCsv == null || selectedChainCsv.isEmpty()) {
            selectedChainCsv = "Cadenas_Estandar.csv";
        }

        initViews();
        loadCsvData();
        displayResults();
    }

    private void initViews() {
        Button btnVolver = findViewById(R.id.btnVolver);
        btnImprimir = findViewById(R.id.btnImprimir);

        tvPotenciaDiseno = findViewById(R.id.tv_potencia_diseno);
        tvResultadoZ1 = findViewById(R.id.tv_resultado_z1);
        tvFactorFzResultado = findViewById(R.id.tv_factor_fz_resultado);
        tvFactorFiResultado = findViewById(R.id.tv_factor_fi_resultado);
        tvTituloSegundaIteracion = findViewById(R.id.tv_titulo_segunda_iteracion);

        int[] layoutAlternativaIds = {R.id.layout_alternativa_1, R.id.layout_alternativa_2, R.id.layout_alternativa_3, R.id.layout_alternativa_4};
        int[] cadenaIds = {R.id.tv_alternativa_1, R.id.tv_alternativa_2, R.id.tv_alternativa_3, R.id.tv_alternativa_4};
        int[] codigoAnsiIds = {R.id.tv_codigo_ansi_1, R.id.tv_codigo_ansi_2, R.id.tv_codigo_ansi_3, R.id.tv_codigo_ansi_4};
        int[] apIds = {R.id.tv_ap_resultado_1, R.id.tv_ap_resultado_2, R.id.tv_ap_resultado_3, R.id.tv_ap_resultado_4};
        int[] apWarningIds = {R.id.tv_ap_warning_1, R.id.tv_ap_warning_2, R.id.tv_ap_warning_3, R.id.tv_ap_warning_4}; // NUEVO
        int[] faIds = {R.id.tv_factor_fa_resultado_1, R.id.tv_factor_fa_resultado_2, R.id.tv_factor_fa_resultado_3, R.id.tv_factor_fa_resultado_4};
        int[] vIds = {R.id.tv_velocidad_resultado_1, R.id.tv_velocidad_resultado_2, R.id.tv_velocidad_resultado_3, R.id.tv_velocidad_resultado_4};
        int[] flIds = {R.id.tv_factor_fl_resultado_1, R.id.tv_factor_fl_resultado_2, R.id.tv_factor_fl_resultado_3, R.id.tv_factor_fl_resultado_4};
        int[] fgIds = {R.id.tv_factor_fg_resultado_1, R.id.tv_factor_fg_resultado_2, R.id.tv_factor_fg_resultado_3, R.id.tv_factor_fg_resultado_4};
        int[] pcIds = {R.id.tv_potencia_corregida_1, R.id.tv_potencia_corregida_2, R.id.tv_potencia_corregida_3, R.id.tv_potencia_corregida_4};
        int[] validacionIds = {R.id.tv_validacion_cadena_1, R.id.tv_validacion_cadena_2, R.id.tv_validacion_cadena_3, R.id.tv_validacion_cadena_4};
        int[] arIds = {R.id.tv_ar_resultado_1, R.id.tv_ar_resultado_2, R.id.tv_ar_resultado_3, R.id.tv_ar_resultado_4};
        int[] fdynIds = {R.id.tv_fdyn_resultado_1, R.id.tv_fdyn_resultado_2, R.id.tv_fdyn_resultado_3,R.id.tv_fdyn_resultado_4};
        int[] ffIds = {R.id.tv_ff_resultado_1, R.id.tv_ff_resultado_2, R.id.tv_ff_resultado_3, R.id.tv_ff_resultado_4};
        int[] fgTotalIds = {R.id.tv_fg_total_resultado_1, R.id.tv_fg_total_resultado_2, R.id.tv_fg_total_resultado_3, R.id.tv_fg_total_resultado_4};
        int[] xUnionesIds = {R.id.tv_x_uniones_1, R.id.tv_x_uniones_2, R.id.tv_x_uniones_3, R.id.tv_x_uniones_4};
        int[] dimTituloIds = {R.id.tv_titulo_dims_1, R.id.tv_titulo_dims_2, R.id.tv_titulo_dims_3, R.id.tv_titulo_dims_4};
        int[] dimDoIds = {R.id.tv_dim_do_1, R.id.tv_dim_do_2, R.id.tv_dim_do_3, R.id.tv_dim_do_4};
        int[] dimDaIds = {R.id.tv_dim_da_1, R.id.tv_dim_da_2, R.id.tv_dim_da_3, R.id.tv_dim_da_4};
        int[] dimDfIds = {R.id.tv_dim_df_1, R.id.tv_dim_df_2, R.id.tv_dim_df_3, R.id.tv_dim_df_4};
        int[] dimB1Ids = {R.id.tv_dim_b1_1, R.id.tv_dim_b1_2, R.id.tv_dim_b1_3, R.id.tv_dim_b1_4};


        int[] siLayoutIds = {R.id.layout_segunda_iteracion_1, R.id.layout_segunda_iteracion_2, R.id.layout_segunda_iteracion_3, R.id.layout_segunda_iteracion_4};
        int[] siCadenaIds = {R.id.tv_si_cadena_1, R.id.tv_si_cadena_2, R.id.tv_si_cadena_3, R.id.tv_si_cadena_4};
        int[] siCodigoAnsiIds = {R.id.tv_si_codigo_ansi_1, R.id.tv_si_codigo_ansi_2, R.id.tv_si_codigo_ansi_3, R.id.tv_si_codigo_ansi_4};
        int[] siApIds = {R.id.tv_si_ap_1, R.id.tv_si_ap_2, R.id.tv_si_ap_3, R.id.tv_si_ap_4};
        int[] siApWarningIds = {R.id.tv_si_ap_warning_1, R.id.tv_si_ap_warning_2, R.id.tv_si_ap_warning_3, R.id.tv_si_ap_warning_4}; // NUEVO
        int[] siFaIds = {R.id.tv_si_fa_1, R.id.tv_si_fa_2, R.id.tv_si_fa_3, R.id.tv_si_fa_4};
        int[] siVIds = {R.id.tv_si_v_1, R.id.tv_si_v_2, R.id.tv_si_v_3, R.id.tv_si_v_4};
        int[] siFlIds = {R.id.tv_si_fl_1, R.id.tv_si_fl_2, R.id.tv_si_fl_3, R.id.tv_si_fl_4};
        int[] siFgIds = {R.id.tv_si_fg_1, R.id.tv_si_fg_2, R.id.tv_si_fg_3, R.id.tv_si_fg_4};
        int[] siPcIds = {R.id.tv_si_pc_1, R.id.tv_si_pc_2, R.id.tv_si_pc_3, R.id.tv_si_pc_4};
        int[] siValidacionIds = {R.id.tv_si_validacion_1, R.id.tv_si_validacion_2, R.id.tv_si_validacion_3, R.id.tv_si_validacion_4};
        int[] siArIds = {R.id.tv_si_ar_resultado_1, R.id.tv_si_ar_resultado_2, R.id.tv_si_ar_resultado_3, R.id.tv_si_ar_resultado_4};
        int[] siFdynIds = {R.id.tv_si_fdyn_resultado_1, R.id.tv_si_fdyn_resultado_2, R.id.tv_si_fdyn_resultado_3, R.id.tv_si_fdyn_resultado_4};
        int[] siFfIds = {R.id.tv_si_ff_resultado_1, R.id.tv_si_ff_resultado_2, R.id.tv_si_ff_resultado_3, R.id.tv_si_ff_resultado_4};
        int[] siFgTotalIds = {R.id.tv_si_fg_total_resultado_1, R.id.tv_si_fg_total_resultado_2, R.id.tv_si_fg_total_resultado_3, R.id.tv_si_fg_total_resultado_4};
        int[] siXUnionesIds = {R.id.tv_si_x_uniones_1, R.id.tv_si_x_uniones_2, R.id.tv_si_x_uniones_3, R.id.tv_si_x_uniones_4};
        int[] siDimTituloIds = {R.id.tv_si_titulo_dims_1, R.id.tv_si_titulo_dims_2, R.id.tv_si_titulo_dims_3, R.id.tv_si_titulo_dims_4};
        int[] siDimDoIds = {R.id.tv_si_dim_do_1, R.id.tv_si_dim_do_2, R.id.tv_si_dim_do_3, R.id.tv_si_dim_do_4};
        int[] siDimDaIds = {R.id.tv_si_dim_da_1, R.id.tv_si_dim_da_2, R.id.tv_si_dim_da_3, R.id.tv_si_dim_da_4};
        int[] siDimDfIds = {R.id.tv_si_dim_df_1, R.id.tv_si_dim_df_2, R.id.tv_si_dim_df_3, R.id.tv_si_dim_df_4};
        int[] siDimB1Ids = {R.id.tv_si_dim_b1_1, R.id.tv_si_dim_b1_2, R.id.tv_si_dim_b1_3, R.id.tv_si_dim_b1_4};


        for (int i = 0; i < 4; i++) {
            layoutAlternativas[i] = findViewById(layoutAlternativaIds[i]);
            textViewsCadenas[i] = findViewById(cadenaIds[i]);
            textViewsCodigoAnsi[i] = findViewById(codigoAnsiIds[i]);
            textViewsAp[i] = findViewById(apIds[i]);
            textViewsApWarning[i] = findViewById(apWarningIds[i]); // Nuevo
            textViewsFa[i] = findViewById(faIds[i]);
            textViewsVelocidad[i] = findViewById(vIds[i]);
            textViewsFl[i] = findViewById(flIds[i]);
            textViewsFg[i] = findViewById(fgIds[i]);
            textViewsPc[i] = findViewById(pcIds[i]);
            textViewsValidacion[i] = findViewById(validacionIds[i]);
            textViewsAr[i] = findViewById(arIds[i]);
            textViewsFdyn[i] = findViewById(fdynIds[i]);
            textViewsFf[i] = findViewById(ffIds[i]);
            textViewsFgTotal[i] = findViewById(fgTotalIds[i]);
            textViewsXUniones[i] = findViewById(xUnionesIds[i]);
            textViewsDimTitulo[i] = findViewById(dimTituloIds[i]);
            textViewsDimDo[i] = findViewById(dimDoIds[i]);
            textViewsDimDa[i] = findViewById(dimDaIds[i]);
            textViewsDimDf[i] = findViewById(dimDfIds[i]);
            textViewsDimB1[i] = findViewById(dimB1Ids[i]);

            layoutsSegundaIteracion[i] = findViewById(siLayoutIds[i]);
            textViewsSiCadena[i] = findViewById(siCadenaIds[i]);
            textViewsSiCodigoAnsi[i] = findViewById(siCodigoAnsiIds[i]);
            textViewsSiAp[i] = findViewById(siApIds[i]);
            textViewsSiApWarning[i] = findViewById(siApWarningIds[i]); // Nuevo
            textViewsSiFa[i] = findViewById(siFaIds[i]);
            textViewsSiV[i] = findViewById(siVIds[i]);
            textViewsSiFl[i] = findViewById(siFlIds[i]);
            textViewsSiFg[i] = findViewById(siFgIds[i]);
            textViewsSiPc[i] = findViewById(siPcIds[i]);
            textViewsSiValidacion[i] = findViewById(siValidacionIds[i]);
            textViewsSiAr[i] = findViewById(siArIds[i]);
            textViewsSiFdyn[i] = findViewById(siFdynIds[i]);
            textViewsSiFf[i] = findViewById(siFfIds[i]);
            textViewsSiFgTotal[i] = findViewById(siFgTotalIds[i]);
            textViewsSiXUniones[i] = findViewById(siXUnionesIds[i]);
            textViewsSiDimTitulo[i] = findViewById(siDimTituloIds[i]);
            textViewsSiDimDo[i] = findViewById(siDimDoIds[i]);
            textViewsSiDimDa[i] = findViewById(siDimDaIds[i]);
            textViewsSiDimDf[i] = findViewById(siDimDfIds[i]);
            textViewsSiDimB1[i] = findViewById(siDimB1Ids[i]);

            textViewsF[i] = new TextView(this);
            textViewsSiF[i] = new TextView(this);
        }
        btnVolver.setOnClickListener(v -> finish());

        btnImprimir.setOnClickListener(v -> {
            try {
                LinkedHashMap<String, String> data = collectDataForPdf_Cadenas();
                PdfGenerator pdfGenerator = new PdfGenerator(this, data);
                File pdfFile = pdfGenerator.createPdf();

                if (pdfFile != null) {
                    openPdf(pdfFile);
                } else {
                    Toast.makeText(this, "No se pudo crear el PDF.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("PdfImprimirCadenas", "Error total al imprimir PDF de Cadenas", e);
                Toast.makeText(this, "Error al imprimir: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadCsvData() {
        try (BufferedReader readerFz = new BufferedReader(new InputStreamReader(getAssets().open("factor_fz.csv")));
             BufferedReader readerFi = new BufferedReader(new InputStreamReader(getAssets().open("factor_fi.csv")));
             BufferedReader readerFa = new BufferedReader(new InputStreamReader(getAssets().open("factor_fa.csv")));
             BufferedReader readerFl = new BufferedReader(new InputStreamReader(getAssets().open("factor_fl.csv")));
             BufferedReader readerDistancia = new BufferedReader(new InputStreamReader(getAssets().open("Distancia_recomendada_cadenas.csv")));
             BufferedReader readerTablaC = new BufferedReader(new InputStreamReader(getAssets().open("valores_c.csv")))
        ) {
            String line;
            readerFz.readLine(); while ((line = readerFz.readLine()) != null) { factorFzData.add(line.split(";")); }
            readerFi.readLine(); while ((line = readerFi.readLine()) != null) { factorFiData.add(line.split(";")); }
            readerFa.readLine(); while ((line = readerFa.readLine()) != null) { factorFaData.add(line.split(";")); }
            readerFl.readLine(); while ((line = readerFl.readLine()) != null) { factorFlData.add(line.split(";")); }
            readerDistancia.readLine(); while ((line = readerDistancia.readLine()) != null) { distanciaRecomendadaData.add(line.split(";")); }
            readerTablaC.readLine(); while ((line = readerTablaC.readLine()) != null) { tablaCData.add(line.split(";")); }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar archivos CSV de factores", Toast.LENGTH_LONG).show();
        }

        try (BufferedReader readerCatalogo = new BufferedReader(new InputStreamReader(getAssets().open(selectedChainCsv)))) {
            String headerLine = readerCatalogo.readLine();
            if (headerLine == null) throw new IOException("Archivo CSV de catálogo está vacío");
            headerLine = headerLine.replaceAll("[^\\x20-\\x7E]", "");
            String[] headers = headerLine.split(";");
            headerMap.clear();
            for (int i = 0; i < headers.length; i++) { headerMap.put(headers[i].trim().toUpperCase(), i); }

            colTipo = headerMap.getOrDefault("TIPO_CADENA", -1);
            colCodigo = headerMap.getOrDefault("CODIGO_ANSI", headerMap.getOrDefault("CODIGO_CADENA", -1));
            colPaso = headerMap.getOrDefault("PASO_P_MM", -1);
            colRodillo = headerMap.getOrDefault("DIAM_RODILLO_A_MM", -1);
            colPlacas = headerMap.getOrDefault("DIST_PLACAS_INT_B_MM", -1);
            colPeso = headerMap.getOrDefault("PESO_KG_M", -1);

            if (colTipo == -1 || colPaso == -1 || colRodillo == -1 || colPlacas == -1 || colPeso == -1) {
                csvErrorMessage = "El archivo " + selectedChainCsv + " no es compatible.";
            }

            String lineCatalogo;
            while ((lineCatalogo = readerCatalogo.readLine()) != null) {
                catalogoCadenasData.add(lineCatalogo.split(";"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            csvErrorMessage = "Error al leer el archivo: " + selectedChainCsv;
            Toast.makeText(this, "Error al leer " + selectedChainCsv, Toast.LENGTH_LONG).show();
        }
    }

    private void displayResults() {
        Intent intent = getIntent();
        if (intent == null) return;

        double potenciaDiseno = intent.getDoubleExtra("POTENCIA_DISENO", 0.0);
        double neRpm = intent.getDoubleExtra("NE_RPM", 0.0);
        int z1 = intent.getIntExtra("Z1", 0);
        int z2 = intent.getIntExtra("Z2", 0);
        double distanciaCentros = intent.getDoubleExtra("DISTANCIA_CENTROS", 0.0);
        double factorServicio = intent.getDoubleExtra("FACTOR_SERVICIO", 1.0);
        double potenciaNominal = intent.getDoubleExtra("POTENCIA_NOMINAL", 0.0);

        this.potenciaNominalStr = intent.getStringExtra("POTENCIA_NOMINAL_STR");
        this.factorServicioStr = intent.getStringExtra("FACTOR_SERVICIO_STR");
        this.neRpmStr = intent.getStringExtra("NE_RPM_STR");
        this.nsRpmStr = intent.getStringExtra("NS_RPM_STR");
        this.mgStr = intent.getStringExtra("MG_STR");
        this.z1Str = String.valueOf(z1) + " dientes";
        this.z2Str = intent.getStringExtra("Z2_STR") + " dientes";
        this.tipoCadenaNombre = intent.getStringExtra("TIPO_CADENA_NOMBRE");
        this.tipoLubricacion = intent.getStringExtra("TIPO_LUBRICACION");

        this.fz = findFzValue(z1);
        double mg = (z1 > 0) ? (double) z2 / z1 : 0;
        this.fi = findFiValue(mg);

        DecimalFormat df = new DecimalFormat("#.##");
        tvPotenciaDiseno.setText(df.format(potenciaDiseno) + " Kw");
        tvResultadoZ1.setText(z1 + " dientes");
        // tvFactorFzResultado.setText(df.format(this.fz)); // Oculto
        // tvFactorFiResultado.setText(df.format(this.fi)); // Oculto

        if (csvErrorMessage != null) {
            layoutAlternativas[0].setVisibility(View.VISIBLE);
            textViewsCadenas[0].setText(csvErrorMessage);
            textViewsCadenas[0].setTextColor(ContextCompat.getColor(this, R.color.warning_red));
            hideAllAnalysisTextViews(0);
            cleanUpViews(1); cleanUpViews(2); cleanUpViews(3);
            return;
        }

        if (neRpm > 0 && potenciaDiseno > 0) {
            List<ChainResult> cadenasSeleccionadas = selectAllChains(potenciaDiseno, neRpm);
            if (areAllChainsNull(cadenasSeleccionadas)) {
                layoutAlternativas[0].setVisibility(View.VISIBLE);
                textViewsCadenas[0].setText("Valores fuera de rango para el tipo de cadena seleccionado");
                textViewsCadenas[0].setTextColor(ContextCompat.getColor(this, R.color.warning_red));
                hideAllAnalysisTextViews(0);
                cleanUpViews(1); cleanUpViews(2); cleanUpViews(3);
                return;
            }
            displayChainAlternatives(cadenasSeleccionadas, potenciaDiseno, potenciaNominal, distanciaCentros, z1, z2, neRpm, this.tipoLubricacion, factorServicio, df);
        } else {
            textViewsCadenas[0].setText("Datos de entrada inválidos");
        }
    }

    private void displayChainAlternatives(List<ChainResult> cadenas, double pd, double pNominal, double distancia, int z1, int z2, double ne, String tipoLubricacion, double fy, DecimalFormat df) {
        boolean segundaIteracionGlobalNecesaria = false;

        for (int i = 0; i < 4; i++) {
            if (i >= STRAND_FACTORS.length || i >= cadenas.size()) {
                cleanUpViews(i);
                continue;
            }

            ChainResult result = cadenas.get(i);
            if (result.originalChain == null) {
                cleanUpViews(i);
                continue;
            }

            textViewsCadenas[i].setTextColor(ContextCompat.getColor(this, R.color.text_color_primary));
            layoutAlternativas[i].setVisibility(View.VISIBLE);

            AnalysisResult analisisInicial = performChainAnalysis(result, pd, pNominal, distancia, z1, z2, (int) ne, tipoLubricacion, fy, df, i);
            displayAnalysisResults(analisisInicial, textViewsCadenas[i], textViewsCodigoAnsi[i], textViewsAp[i], textViewsApWarning[i], textViewsFa[i], textViewsVelocidad[i], textViewsFl[i], textViewsFg[i], textViewsPc[i], textViewsValidacion[i], textViewsAr[i],
                    textViewsF[i],
                    textViewsFdyn[i], textViewsFf[i], textViewsFgTotal[i], textViewsXUniones[i],
                    textViewsDimTitulo[i], textViewsDimDo[i], textViewsDimDa[i], textViewsDimDf[i], textViewsDimB1[i]);

            if (!analisisInicial.resiste) {
                layoutsSegundaIteracion[i].setVisibility(View.VISIBLE);
                segundaIteracionGlobalNecesaria = true;

                ChainRating nuevaCadena = findSecondIterationChain(analisisInicial.pc, result.originalChain.index, i, ne);

                if (nuevaCadena != null) {
                    ChainResult nuevoResult = new ChainResult(
                            getPrefix(i) + ": " + nuevaCadena.name,
                            extractPitchFromName(nuevaCadena.name),
                            nuevaCadena
                    );

                    AnalysisResult analisisSecundario = performChainAnalysis(nuevoResult, pd, pNominal, distancia, z1, z2, ne, tipoLubricacion, fy, df, i);
                    displayAnalysisResults(analisisSecundario, textViewsSiCadena[i], textViewsSiCodigoAnsi[i], textViewsSiAp[i], textViewsSiApWarning[i], textViewsSiFa[i], textViewsSiV[i], textViewsSiFl[i], textViewsSiFg[i], textViewsSiPc[i], textViewsSiValidacion[i], textViewsSiAr[i],
                            textViewsSiF[i],
                            textViewsSiFdyn[i], textViewsSiFf[i], textViewsSiFgTotal[i], textViewsSiXUniones[i],
                            textViewsSiDimTitulo[i], textViewsSiDimDo[i], textViewsSiDimDa[i], textViewsSiDimDf[i], textViewsSiDimB1[i]);
                } else {
                    textViewsSiCadena[i].setText(getPrefix(i) + ": No se encontró cadena superior");
                    cleanUpSecondIterationViews(i);
                }
            } else {
                layoutsSegundaIteracion[i].setVisibility(View.GONE);
            }
        }
        tvTituloSegundaIteracion.setVisibility(segundaIteracionGlobalNecesaria ? View.VISIBLE : View.GONE);
    }

    private AnalysisResult performChainAnalysis(ChainResult chainResult, double pd, double pNominal, double distancia, int z1, int z2, double ne, String tipoLubricacion, double fy, DecimalFormat df, int toronIndex) {
        AnalysisResult analisis = new AnalysisResult();
        analisis.displayText = chainResult.displayText;

        String tipoCadena = TIPO_CADENA_MAP[toronIndex];
        String[] catalogRow = findCatalogRow(chainResult.pitch, tipoCadena);

        if (catalogRow == null) {
            analisis.validationText = "Datos no encontrados en " + selectedChainCsv + " para Paso " + df.format(chainResult.pitch) + " y Tipo " + tipoCadena;
            return analisis;
        }

        double weight, R, W;
        String ansiCode;
        try {
            ansiCode = (colCodigo != -1 && colCodigo < catalogRow.length) ? catalogRow[colCodigo] : "N/A";
            R = Double.parseDouble(catalogRow[colRodillo].replace(',', '.'));
            W = Double.parseDouble(catalogRow[colPlacas].replace(',', '.'));
            weight = Double.parseDouble(catalogRow[colPeso].replace(',', '.'));
        } catch (Exception e) {
            analisis.validationText = "Error al leer datos CSV.";
            return analisis;
        }

        analisis.ansiCodeText = "Código Challenge: " + ansiCode;

        // --- LÓGICA DE VALIDACIÓN DE DISTANCIA (a/p) ---
        double p = chainResult.pitch;
        double ratioAP = distancia / p;
        int uniones = (int) Math.ceil(ratioAP);
        analisis.apText = "a/p = " + uniones + " uniones"; // Para visualización (aunque esté oculto)

        boolean isApValid = (ratioAP >= 20 && ratioAP <= 160);
        analisis.apValid = isApValid;

        double a_calculation; // La distancia que se usará para calcular X

        if (isApValid) {
            a_calculation = distancia;
            analisis.apWarningText = "Distancia entre centros aceptable (" + df.format(distancia) + " mm)";
        } else {
            double arValue = findDistanciaRecomendada(p);
            a_calculation = (arValue > 0) ? arValue : distancia;
            // Mensaje de advertencia
            analisis.apWarningText = "AVISO: a/p fuera de rango (20-160). Usando recomendada: " + (int)arValue + " mm";
        }
        // -----------------------------------------------

        // Usamos 'uniones' calculado con la distancia real para buscar el factor Fa (comportamiento estándar)
        // Ojo: Fa se busca con el a/p real. Si a/p está mal, el Fa podría ser raro, pero la validación ya avisa.
        double fa = findFaValue(uniones);
        analisis.faText = "fa = " + df.format(fa);

        double velocidad = (z1 * chainResult.pitch * ne) / 60000;
        velocidad = Math.round(velocidad * 100.0) / 100.0;
        analisis.vText = "V = " + df.format(velocidad) + " m/s";
        double fl = findFlValue(tipoLubricacion, velocidad);
        analisis.flText = fl < 0 ? "f₁ = N" : "f₁ = " + df.format(fl);

        if (fl >= 0) {
            double fg = fy * this.fz * this.fi * fa * fl;
            analisis.fgText = "fG = " + df.format(fg);
            double pc = pd * fg;
            analisis.pcText = "Pc = " + df.format(pc) + " Kw";
            analisis.pc = pc;

            double potenciaAdmisible = getRatingAtRpm(chainResult.originalChain.data, ne) * STRAND_FACTORS[toronIndex];

            if (Double.isNaN(potenciaAdmisible)) {
                analisis.validationText = "Error al calcular Pa";
            } else if (pc <= potenciaAdmisible) {
                analisis.validationText = "Resiste (Pc <= " + df.format(potenciaAdmisible) + " Kw)";
                analisis.resiste = true;

                double arValue = findDistanciaRecomendada(chainResult.pitch);
                if (arValue > 0) {
                    analisis.arText = "Distancia Recomendada: " + (int)arValue + " mm";
                }

                if (velocidad > 0) {
                    double F = (1000 * pd) / velocidad;
                    analisis.fText = "F = " + df.format(F) + " N";
                    double Fdyn = F * fy;
                    double Ff = weight * velocidad * velocidad;
                    double FG_total = Fdyn + Ff;

                    analisis.fdynText = "Fdyn = " + df.format(Fdyn) + " N";
                    analisis.ffText = "Ff = " + df.format(Ff) + " N";
                    analisis.fgTotalText = "FG = " + df.format(FG_total) + " N";
                }

                // CÁLCULO DE X USANDO LA DISTANCIA VALIDADA (a_calculation)
                double X = 0;
                if (z1 == z2) {
                    X = (2 * a_calculation / p) + z1;
                } else {
                    int z_diff = z2 - z1;
                    double C = findCValue(z_diff);
                    X = (2 * a_calculation / p) + ((double)(z1 + z2) / 2) + C;
                }

                // --- CORRECCIÓN DE REDONDEO: Entero Par Inmediato Superior ---

                // 1. Primero redondeamos hacia arriba al entero más cercano
                long xRedondeado = (long) Math.ceil(X);

                // 2. Si el resultado es IMPAR, le sumamos 1 para hacerlo PAR
                // (Esto evita el uso de eslabones de compensación/offset links)
                if (xRedondeado % 2 != 0) {
                    xRedondeado++;
                }
                // -------------------------------------------------------------
                analisis.xUnionesText = "Número de uniones (X) = " + xRedondeado + " uniones";

                analisis.dimTituloText = "Cálculo de Dimensiones de la Rueda";
                double d0 = (z1 * p) / Math.PI;
                analisis.dimDoText = "d₀ = " + df.format(d0) + " mm";
                double da = 0;
                if (z1 >= 6 && z1 <= 12) { da = d0 + 0.6 * R; }
                else if (z1 >= 13 && z1 <= 25) { da = d0 + 0.7 * R; }
                else if (z1 > 25) { da = d0 + 0.8 * R; }
                else { da = d0 + 0.6 * R; }
                analisis.dimDaText = "dₐ = " + df.format(da) + " mm";
                double df_calc = d0 - R;
                analisis.dimDfText = "dᶠ = " + df.format(df_calc) + " mm";
                double B1 = 0.9 * W;
                analisis.dimB1Text = "B₁ = " + df.format(B1) + " mm";

            } else {
                analisis.validationText = "No Resiste (Pc > " + df.format(potenciaAdmisible) + " Kw)";
            }
        } else {
            analisis.fgText = "fG = Incalculable";
            analisis.pcText = "Pc = Incalculable";
            analisis.validationText = "Combinación Inapropiada";
        }
        return analisis;
    }

    private void displayAnalysisResults(AnalysisResult analisis, TextView tvCadena, TextView tvCodigoAnsi, TextView tvAp, TextView tvApWarning, TextView tvFa, TextView tvV, TextView tvFl, TextView tvFg, TextView tvPc, TextView tvValidacion, TextView tvAr,
                                        TextView tvF,
                                        TextView tvFdyn, TextView tvFf, TextView tvFgTotal, TextView tvXUniones,
                                        TextView tvDimTitulo, TextView tvDimDo, TextView tvDimDa, TextView tvDimDf, TextView tvDimB1) {
        tvCadena.setText(analisis.displayText);
        tvCadena.setTextColor(ContextCompat.getColor(this, R.color.text_color_primary));

        tvCodigoAnsi.setText(analisis.ansiCodeText);
        tvAp.setText(analisis.apText);
        // SET WARNING TEXT
        tvApWarning.setText(analisis.apWarningText);

        tvFa.setText(analisis.faText);
        tvV.setText(analisis.vText);
        tvFl.setText(analisis.flText);
        tvFg.setText(analisis.fgText);
        tvPc.setText(analisis.pcText);
        tvValidacion.setText(analisis.validationText);
        tvFdyn.setText(analisis.fdynText);
        tvFf.setText(analisis.ffText);
        tvFgTotal.setText(analisis.fgTotalText);
        tvXUniones.setText(analisis.xUnionesText);

        tvDimTitulo.setText(analisis.dimTituloText);
        tvDimDo.setText(analisis.dimDoText);
        tvDimDa.setText(analisis.dimDaText);
        tvDimDf.setText(analisis.dimDfText);
        tvDimB1.setText(analisis.dimB1Text);

        tvAr.setText(analisis.arText);
        tvF.setText(analisis.fText);

        boolean mostrarCalculos = analisis.resiste || !(analisis.validationText.startsWith("Datos no encontrados") || analisis.validationText.startsWith("Error:"));
        int visibility = mostrarCalculos ? View.VISIBLE : View.GONE;

        // SOLO VISIBLES:
        tvCodigoAnsi.setVisibility(visibility);

        // WARNING VISIBILITY: Siempre visible si hay cálculos, color depende de si es válido
        tvApWarning.setVisibility(visibility);
        if (analisis.apValid) {
            tvApWarning.setTextColor(ContextCompat.getColor(this, R.color.success_green)); // O color normal
        } else {
            tvApWarning.setTextColor(ContextCompat.getColor(this, R.color.warning_red));
        }

        tvV.setVisibility(visibility);
        tvPc.setVisibility(visibility);

        // OCULTOS (Comentados para que se mantengan 'gone'):
        // tvAp.setVisibility(visibility);
        // tvFa.setVisibility(visibility);
        // tvFl.setVisibility(visibility);
        // tvFg.setVisibility(visibility);

        int resisteVisibility = analisis.resiste ? View.VISIBLE : View.GONE;

        tvAr.setVisibility(analisis.resiste && !analisis.arText.isEmpty() ? View.VISIBLE : View.GONE);

        // SOLO VISIBLES:
        tvFgTotal.setVisibility(resisteVisibility);
        tvXUniones.setVisibility(resisteVisibility);

        // OCULTOS (Comentados):
        // tvFdyn.setVisibility(resisteVisibility);
        // tvFf.setVisibility(resisteVisibility);
        // tvDimTitulo.setVisibility(resisteVisibility);
        // tvDimDo.setVisibility(resisteVisibility);
        // tvDimDa.setVisibility(resisteVisibility);
        // tvDimDf.setVisibility(resisteVisibility);
        // tvDimB1.setVisibility(resisteVisibility);

        int color = (analisis.resiste) ? R.color.success_green : R.color.warning_red;
        tvValidacion.setTextColor(ContextCompat.getColor(ResultadosCadenasActivity.this, color));
    }

    private void cleanUpViews(int index) {
        if (index < 0 || index >= 4) return;
        if (layoutAlternativas[index] != null) {
            layoutAlternativas[index].setVisibility(View.GONE);
        }
        if (layoutsSegundaIteracion[index] != null) {
            layoutsSegundaIteracion[index].setVisibility(View.GONE);
        }
    }

    private void cleanUpSecondIterationViews(int index) {
        if (index < 0 || index >= 4) return;
        TextView[] allSiViews = {
                textViewsSiCadena[index], textViewsSiCodigoAnsi[index], textViewsSiAp[index], textViewsSiApWarning[index], textViewsSiFa[index], textViewsSiV[index], textViewsSiFl[index],
                textViewsSiFg[index], textViewsSiPc[index], textViewsSiValidacion[index], textViewsSiAr[index],
                textViewsSiF[index],
                textViewsSiFdyn[index], textViewsSiFf[index], textViewsSiFgTotal[index], textViewsSiXUniones[index],
                textViewsSiDimTitulo[index], textViewsSiDimDo[index], textViewsSiDimDa[index], textViewsSiDimDf[index], textViewsSiDimB1[index]
        };
        for (TextView view : allSiViews) {
            if (view != null) view.setText("");
        }
    }

    private void hideAllAnalysisTextViews(int index) {
        if (index < 0 || index >= 4) return;
        if(textViewsCodigoAnsi[index] != null) textViewsCodigoAnsi[index].setVisibility(View.GONE);
        if(textViewsAp[index] != null) textViewsAp[index].setVisibility(View.GONE);
        if(textViewsApWarning[index] != null) textViewsApWarning[index].setVisibility(View.GONE); // Nuevo
        if(textViewsFa[index] != null) textViewsFa[index].setVisibility(View.GONE);
        if(textViewsVelocidad[index] != null) textViewsVelocidad[index].setVisibility(View.GONE);
        if(textViewsFl[index] != null) textViewsFl[index].setVisibility(View.GONE);
        if(textViewsFg[index] != null) textViewsFg[index].setVisibility(View.GONE);
        if(textViewsPc[index] != null) textViewsPc[index].setVisibility(View.GONE);
        if(textViewsValidacion[index] != null) textViewsValidacion[index].setVisibility(View.GONE);
        if(textViewsAr[index] != null) textViewsAr[index].setVisibility(View.GONE);
        if(textViewsFdyn[index] != null) textViewsFdyn[index].setVisibility(View.GONE);
        if(textViewsFf[index] != null) textViewsFf[index].setVisibility(View.GONE);
        if(textViewsFgTotal[index] != null) textViewsFgTotal[index].setVisibility(View.GONE);
        if(textViewsXUniones[index] != null) textViewsXUniones[index].setVisibility(View.GONE);
        if(textViewsDimTitulo[index] != null) textViewsDimTitulo[index].setVisibility(View.GONE);
        if(textViewsDimDo[index] != null) textViewsDimDo[index].setVisibility(View.GONE);
        if(textViewsDimDa[index] != null) textViewsDimDa[index].setVisibility(View.GONE);
        if(textViewsDimDf[index] != null) textViewsDimDf[index].setVisibility(View.GONE);
        if(textViewsDimB1[index] != null) textViewsDimB1[index].setVisibility(View.GONE);
        if(layoutsSegundaIteracion[index] != null) layoutsSegundaIteracion[index].setVisibility(View.GONE);
    }

    private ChainRating findSecondIterationChain(double potenciaRequerida, int indiceFallido, int toronIndex, double rpm) {
        if (toronIndex >= STRAND_FACTORS.length) return null;
        double factorToron = STRAND_FACTORS[toronIndex];
        for (int i = indiceFallido + 1; i < CHAIN_RATINGS.length; i++) {
            ChainRating candidata = CHAIN_RATINGS[i];
            double potenciaCandidata = getRatingAtRpm(candidata.data, rpm);
            if (Double.isNaN(potenciaCandidata)) continue;
            if (potenciaRequerida <= (potenciaCandidata * factorToron)) {
                String tipoCadena = TIPO_CADENA_MAP[toronIndex];
                double pitch = extractPitchFromName(candidata.name);
                if (findCatalogRow(pitch, tipoCadena) != null) {
                    return candidata;
                }
            }
        }
        return null;
    }

    private List<ChainResult> selectAllChains(double power, double rpm) {
        List<ChainResult> results = new ArrayList<>();
        String[] prefixes = {"1 torón: ", "2 torones: ", "3 torones: "};
        for (int i = 0; i < STRAND_FACTORS.length; i++) {
            ChainResult foundChain = null;
            for (ChainRating chain : CHAIN_RATINGS) {
                double chainMaxKw = getRatingAtRpm(chain.data, rpm);
                if (Double.isNaN(chainMaxKw) || chainMaxKw < 0) continue;
                if (power <= (chainMaxKw * STRAND_FACTORS[i])) {
                    double pitch = extractPitchFromName(chain.name);
                    String tipoCadena = TIPO_CADENA_MAP[i];
                    if (findCatalogRow(pitch, tipoCadena) != null) {
                        foundChain = new ChainResult(prefixes[i] + chain.name, pitch, chain);
                        break;
                    }
                }
            }
            if (foundChain == null) {
                results.add(new ChainResult(prefixes[i] + "Valores fuera de rango o cadena no disponible en este catálogo", 0.0, null));
            } else {
                results.add(foundChain);
            }
        }
        return results;
    }

    private boolean areAllChainsNull(List<ChainResult> results) {
        if (results == null || results.isEmpty()) { return true; }
        for (ChainResult result : results) {
            if (result.originalChain != null) { return false; }
        }
        return true;
    }

    private String[] findCatalogRow(double pitch, String tipoCadena) {
        if (catalogoCadenasData.isEmpty() || colTipo == -1 || colPaso == -1) return null;
        final double EPSILON = 0.001;
        for (String[] row : catalogoCadenasData) {
            if (row.length > colTipo && row.length > colPaso) {
                try {
                    String tipoCsv = row[colTipo].trim();
                    double pasoCsv = Double.parseDouble(row[colPaso].replace(',', '.'));
                    if (tipoCsv.equalsIgnoreCase(tipoCadena) && Math.abs(pasoCsv - pitch) < EPSILON) {
                        return row;
                    }
                } catch (NumberFormatException | NullPointerException e) {}
            }
        }
        return null;
    }

    private double extractPitchFromName(String chainName) {
        Pattern pattern = Pattern.compile("P\\s*([\\d,.]+)");
        Matcher matcher = pattern.matcher(chainName);
        if (matcher.find()) {
            String pitchString = matcher.group(1);
            if (pitchString != null) {
                return Double.parseDouble(pitchString.replace(',', '.'));
            }
        }
        return 0.0;
    }

    private double findCValue(int z_diff) {
        int z_diff_abs = Math.abs(z_diff);
        if (z_diff_abs == 0) return 0.0;
        if (tablaCData.isEmpty()) return 0.0;
        try {
            int lastIndex = tablaCData.size() - 1;
            int maxZDiff = Integer.parseInt(tablaCData.get(lastIndex)[0]);
            if (z_diff_abs >= maxZDiff) {
                return Double.parseDouble(tablaCData.get(lastIndex)[1].replace(',', '.'));
            }
            int minZDiff = Integer.parseInt(tablaCData.get(0)[0]);
            if(z_diff_abs < minZDiff) {
                return (Double.parseDouble(tablaCData.get(0)[1].replace(',', '.')) / minZDiff) * z_diff_abs;
            }
            for (String[] row : tablaCData) {
                if (Integer.parseInt(row[0]) == z_diff_abs) {
                    return Double.parseDouble(row[1].replace(',', '.'));
                }
            }
            for (int i = 0; i < lastIndex; i++) {
                int z1_c = Integer.parseInt(tablaCData.get(i)[0]);
                double c1 = Double.parseDouble(tablaCData.get(i)[1].replace(',', '.'));
                int z2_c = Integer.parseInt(tablaCData.get(i+1)[0]);
                double c2 = Double.parseDouble(tablaCData.get(i+1)[1].replace(',', '.'));
                if (z_diff_abs > z1_c && z_diff_abs < z2_c) {
                    return c1 + ((double)(z_diff_abs - z1_c) / (z2_c - z1_c)) * (c2 - c1);
                }
            }
        } catch (NumberFormatException e) { e.printStackTrace(); }
        return 0.0;
    }

    private double findFlValue(String tipoLubricacion, double velocidad) {
        if (factorFlData.isEmpty() || tipoLubricacion == null) return -1.0;
        for (String[] row : factorFlData) {
            if (row[0].equalsIgnoreCase(tipoLubricacion.trim())) {
                try {
                    if (velocidad < 4) { return Double.parseDouble(row[1].replace(',', '.')); }
                    else if (velocidad <= 7) { return Double.parseDouble(row[2].replace(',', '.')); }
                    else {
                        if(row[3].equalsIgnoreCase("N")) return -1.0;
                        return Double.parseDouble(row[3].replace(',', '.'));
                    }
                } catch (NumberFormatException e) { return -1.0; }
            }
        }
        return -1.0;
    }

    private double findDistanciaRecomendada(double pitch) {
        if (distanciaRecomendadaData.isEmpty() || pitch <= 0) return 0;
        try {
            double bestMatchDiff = Double.MAX_VALUE;
            double recomendacion = 0;
            for (String[] row : distanciaRecomendadaData) {
                double pasoTabla = Double.parseDouble(row[0].replace(',', '.'));
                double diff = Math.abs(pitch - pasoTabla);
                if (diff < bestMatchDiff) {
                    bestMatchDiff = diff;
                    recomendacion = Double.parseDouble(row[1].replace(',', '.'));
                }
            }
            return recomendacion;
        } catch (NumberFormatException e) { e.printStackTrace(); }
        return 0;
    }

    private double findFaValue(int uniones) {
        if (factorFaData.isEmpty()) return 1.0;
        try {
            int firstAp = Integer.parseInt(factorFaData.get(0)[0]);
            if (uniones <= firstAp) { return Double.parseDouble(factorFaData.get(0)[1].replace(',', '.')); }
            int lastIndex = factorFaData.size() - 1;
            int lastAp = Integer.parseInt(factorFaData.get(lastIndex)[0]);
            if (uniones >= lastAp) { return Double.parseDouble(factorFaData.get(lastIndex)[1].replace(',', '.')); }
            for (int i = 0; i < lastIndex; i++) {
                int ap1 = Integer.parseInt(factorFaData.get(i)[0]);
                double fa1 = Double.parseDouble(factorFaData.get(i)[1].replace(',', '.'));
                int ap2 = Integer.parseInt(factorFaData.get(i + 1)[0]);
                double fa2 = Double.parseDouble(factorFaData.get(i + 1)[1].replace(',', '.'));
                if (uniones >= ap1 && uniones <= ap2) {
                    return fa1 + ((double)(uniones - ap1) / (ap2 - ap1)) * (fa2 - fa1);
                }
            }
        } catch (NumberFormatException e) { e.printStackTrace(); }
        return 1.0;
    }

    private double findFzValue(int z1) {
        if (factorFzData.isEmpty()) return 1.0;
        try {
            int firstZ1 = Integer.parseInt(factorFzData.get(0)[0]);
            if (z1 <= firstZ1) { return Double.parseDouble(factorFzData.get(0)[1].replace(',', '.')); }
            int lastIndex = factorFzData.size() - 1;
            int lastZ1 = Integer.parseInt(factorFzData.get(lastIndex)[0]);
            if (z1 >= lastZ1) { return Double.parseDouble(factorFzData.get(lastIndex)[1].replace(',', '.')); }
            for (int i = 0; i < lastIndex; i++) {
                int z1_1 = Integer.parseInt(factorFzData.get(i)[0]);
                double fz_1 = Double.parseDouble(factorFzData.get(i)[1].replace(',', '.'));
                int z1_2 = Integer.parseInt(factorFzData.get(i + 1)[0]);
                double fz_2 = Double.parseDouble(factorFzData.get(i + 1)[1].replace(',', '.'));
                if (z1 >= z1_1 && z1 <= z1_2) {
                    return fz_1 + ((double)(z1 - z1_1) / (z1_2 - z1_1)) * (fz_2 - fz_1);
                }
            }
        } catch (NumberFormatException e) { e.printStackTrace(); }
        return 1.0;
    }

    private double findFiValue(double mg) {
        if (factorFiData.isEmpty()) return 1.0;
        try {
            double firstMg = Double.parseDouble(factorFiData.get(0)[0].replace(',', '.'));
            if (mg <= firstMg) { return Double.parseDouble(factorFiData.get(0)[1].replace(',', '.')); }
            int lastIndex = factorFiData.size() - 1;
            double lastMg = Double.parseDouble(factorFiData.get(lastIndex)[0].replace(',', '.'));
            if (mg >= lastMg) { return Double.parseDouble(factorFiData.get(lastIndex)[1].replace(',', '.')); }
            for (int i = 0; i < lastIndex; i++) {
                double mg_1 = Double.parseDouble(factorFiData.get(i)[0].replace(',', '.'));
                double fi_1 = Double.parseDouble(factorFiData.get(i)[1].replace(',', '.'));
                double mg_2 = Double.parseDouble(factorFiData.get(i + 1)[0].replace(',', '.'));
                double fi_2 = Double.parseDouble(factorFiData.get(i + 1)[1].replace(',', '.'));
                if (mg >= mg_1 && mg <= mg_2) {
                    return fi_1 + ((mg - mg_1) / (mg_2 - mg_1)) * (fi_2 - fi_1);
                }
            }
        } catch (NumberFormatException e) { e.printStackTrace(); }
        return 1.0;
    }

    private double logInterpolate(double x, double x1, double y1, double x2, double y2) {
        if (x1 <= 0 || y1 <= 0 || x2 <= 0 || y2 <= 0) {
            return y1 + (x - x1) * (y2 - y1) / (x2 - x1);
        }
        double logX = Math.log(x);
        double logX1 = Math.log(x1);
        double logY1 = Math.log(y1);
        double logX2 = Math.log(x2);
        double logY2 = Math.log(y2);
        if (Math.abs(logX2 - logX1) < 1e-9) { return y1; }
        double logY = logY1 + (logX - logX1) * (logY2 - logY1) / (logX2 - logX1);
        return Math.exp(logY);
    }

    private double getRatingAtRpm(RpmKwPoint[] dataPoints, double rpm) {
        if (dataPoints == null || dataPoints.length == 0 || rpm <= 0) return Double.NaN;
        if (rpm < dataPoints[0].rpm) {
            if (dataPoints.length < 2) return dataPoints[0].kw;
            return logInterpolate(rpm, dataPoints[0].rpm, dataPoints[0].kw, dataPoints[1].rpm, dataPoints[1].kw);
        }
        RpmKwPoint lastPoint = dataPoints[dataPoints.length - 1];
        if (rpm > lastPoint.rpm) {
            if (dataPoints.length < 2) return lastPoint.kw;
            RpmKwPoint secondLastPoint = dataPoints[dataPoints.length - 2];
            return logInterpolate(rpm, secondLastPoint.rpm, secondLastPoint.kw, lastPoint.rpm, lastPoint.kw);
        }
        for (int i = 0; i < dataPoints.length - 1; i++) {
            RpmKwPoint p1 = dataPoints[i];
            RpmKwPoint p2 = dataPoints[i + 1];
            if (rpm >= p1.rpm && rpm <= p2.rpm) {
                return logInterpolate(rpm, p1.rpm, p1.kw, p2.rpm, p2.kw);
            }
        }
        return Double.NaN;
    }

    private String getPrefix(int index) {
        String[] prefixes = {"1 torón", "2 torones", "3 torones"};
        if (index >= 0 && index < prefixes.length) { return prefixes[index]; }
        return "";
    }

    private LinkedHashMap<String, String> collectDataForPdf_Cadenas() {
        LinkedHashMap<String, String> data = new LinkedHashMap<>();

        data.put("HEADER_DATOS", "Datos de Cálculo");
        data.put("DATO_Potencia Nominal", this.potenciaNominalStr);
        data.put("DATO_Factor de Servicio", this.factorServicioStr);
        data.put("DATO_Ne", this.neRpmStr);
        data.put("DATO_Ns", this.nsRpmStr);
        data.put("DATO_mg", this.mgStr);
        data.put("DATO_Piñón Z1", this.z1Str);
        data.put("DATO_Piñón Z2", this.z2Str);
        data.put("DATO_Tipo de Cadena", this.tipoCadenaNombre);
        data.put("DATO_Tipo de Lubricación", this.tipoLubricacion);

        data.put("DATO_Potencia de Diseño", tvPotenciaDiseno.getText().toString());
        // Factores al PDF (aunque ocultos en pantalla)
        data.put("DATO_Factor fz", df.format(this.fz));
        data.put("DATO_Factor fi", df.format(this.fi));

        data.put("DRAW_IMAGES_HERE", "");
        data.put("SPACER", "10");

        data.put("HEADER_SELECCION", "Seleccion de la Cadena");
        data.put("HEADER_ALTERNATIVAS", "Alternativas de Cadena (Norma Americana)");

        for (int i = 0; i < 4; i++) {
            boolean altVisible = layoutAlternativas[i].getVisibility() == View.VISIBLE;
            if (altVisible) {
                addAlternativaDataToMap(data, i, "ALT" + i + "_");
                data.put("ALT" + i + "_SPACER", "10");
            }
        }

        if (tvTituloSegundaIteracion.getVisibility() == View.VISIBLE) {
            data.put("HEADER_SI", "Segunda Iteración (Re-selección)");
        }

        for (int i = 0; i < 4; i++) {
            boolean siVisible = layoutsSegundaIteracion[i].getVisibility() == View.VISIBLE;
            if (siVisible) {
                addSegundaIteracionDataToMap(data, i, "SI" + i + "_");
                data.put("SI" + i + "_SPACER", "10");
            }
        }

        return data;
    }

    // Variable de instancia para formateo en collectData (puedes crearla local si prefieres)
    private final DecimalFormat df = new DecimalFormat("#.##");

    private void addAlternativaDataToMap(LinkedHashMap<String, String> data, int i, String prefix) {
        int dataCounter = 0;
        data.put(prefix + "HEADER", textViewsCadenas[i].getText().toString());

        data.put(prefix + "DATA_" + dataCounter++, textViewsCodigoAnsi[i].getText().toString());
        data.put(prefix + "DATA_" + dataCounter++, textViewsAp[i].getText().toString());
        data.put(prefix + "DATA_" + dataCounter++, textViewsApWarning[i].getText().toString()); // Nuevo: Aviso en PDF
        data.put(prefix + "DATA_" + dataCounter++, textViewsFa[i].getText().toString());
        data.put(prefix + "DATA_" + dataCounter++, textViewsVelocidad[i].getText().toString());
        data.put(prefix + "DATA_" + dataCounter++, textViewsFl[i].getText().toString());
        data.put(prefix + "DATA_" + dataCounter++, textViewsFg[i].getText().toString());
        data.put(prefix + "DATA_" + dataCounter++, textViewsPc[i].getText().toString());
        data.put(prefix + "VALIDACION", textViewsValidacion[i].getText().toString());

        if (!textViewsAr[i].getText().toString().isEmpty()) data.put(prefix + "DATA_" + dataCounter++, textViewsAr[i].getText().toString());

        if (!textViewsF[i].getText().toString().isEmpty()) data.put(prefix + "DATA_" + dataCounter++, textViewsF[i].getText().toString());
        if (!textViewsFdyn[i].getText().toString().isEmpty()) data.put(prefix + "DATA_" + dataCounter++, textViewsFdyn[i].getText().toString());
        if (!textViewsFf[i].getText().toString().isEmpty()) data.put(prefix + "DATA_" + dataCounter++, textViewsFf[i].getText().toString());
        if (!textViewsFgTotal[i].getText().toString().isEmpty()) data.put(prefix + "DATA_" + dataCounter++, textViewsFgTotal[i].getText().toString());
        if (!textViewsXUniones[i].getText().toString().isEmpty()) data.put(prefix + "DATA_" + dataCounter++, textViewsXUniones[i].getText().toString());

        // --- CONDICIÓN CORREGIDA: Solo mostrar Paso (p) si RESISTE ---
        if (!textViewsDimTitulo[i].getText().toString().isEmpty()) {
            String titulo = textViewsCadenas[i].getText().toString();
            double p = extractPitchFromName(titulo);
            DecimalFormat dfLocal = new DecimalFormat("#.##");
            data.put(prefix + "DATA_" + dataCounter++, "Paso (p): " + dfLocal.format(p) + " mm");

            // Y luego las dimensiones
            data.put(prefix + "DATA_TITLE", textViewsDimTitulo[i].getText().toString());
            data.put(prefix + "DATA_" + dataCounter++, textViewsDimDo[i].getText().toString());
            data.put(prefix + "DATA_" + dataCounter++, textViewsDimDa[i].getText().toString());
            data.put(prefix + "DATA_" + dataCounter++, textViewsDimDf[i].getText().toString());
            data.put(prefix + "DATA_" + dataCounter++, textViewsDimB1[i].getText().toString());
        }
    }

    private void addSegundaIteracionDataToMap(LinkedHashMap<String, String> data, int i, String prefix) {
        int dataCounter = 0;
        data.put(prefix + "HEADER", textViewsSiCadena[i].getText().toString());

        data.put(prefix + "DATA_" + dataCounter++, textViewsSiCodigoAnsi[i].getText().toString());
        data.put(prefix + "DATA_" + dataCounter++, textViewsSiAp[i].getText().toString());
        data.put(prefix + "DATA_" + dataCounter++, textViewsSiApWarning[i].getText().toString()); // Nuevo: Aviso en PDF
        data.put(prefix + "DATA_" + dataCounter++, textViewsSiFa[i].getText().toString());
        data.put(prefix + "DATA_" + dataCounter++, textViewsSiV[i].getText().toString());
        data.put(prefix + "DATA_" + dataCounter++, textViewsSiFl[i].getText().toString());
        data.put(prefix + "DATA_" + dataCounter++, textViewsSiFg[i].getText().toString());
        data.put(prefix + "DATA_" + dataCounter++, textViewsSiPc[i].getText().toString());
        data.put(prefix + "VALIDACION", textViewsSiValidacion[i].getText().toString());

        if (!textViewsSiAr[i].getText().toString().isEmpty()) data.put(prefix + "DATA_" + dataCounter++, textViewsSiAr[i].getText().toString());
        if (!textViewsSiF[i].getText().toString().isEmpty()) data.put(prefix + "DATA_" + dataCounter++, textViewsSiF[i].getText().toString());
        if (!textViewsSiFdyn[i].getText().toString().isEmpty()) data.put(prefix + "DATA_" + dataCounter++, textViewsSiFdyn[i].getText().toString());
        if (!textViewsSiFf[i].getText().toString().isEmpty()) data.put(prefix + "DATA_" + dataCounter++, textViewsSiFf[i].getText().toString());
        if (!textViewsSiFgTotal[i].getText().toString().isEmpty()) data.put(prefix + "DATA_" + dataCounter++, textViewsSiFgTotal[i].getText().toString());
        if (!textViewsSiXUniones[i].getText().toString().isEmpty()) data.put(prefix + "DATA_" + dataCounter++, textViewsSiXUniones[i].getText().toString());

        // --- CONDICIÓN CORREGIDA: Solo mostrar Paso (p) si RESISTE ---
        if (!textViewsSiDimTitulo[i].getText().toString().isEmpty()) {
            String titulo = textViewsSiCadena[i].getText().toString();
            double p = extractPitchFromName(titulo);
            DecimalFormat dfLocal = new DecimalFormat("#.##");
            data.put(prefix + "DATA_" + dataCounter++, "Paso (p): " + dfLocal.format(p) + " mm");

            data.put(prefix + "DATA_TITLE", textViewsSiDimTitulo[i].getText().toString());
            data.put(prefix + "DATA_" + dataCounter++, textViewsSiDimDo[i].getText().toString());
            data.put(prefix + "DATA_" + dataCounter++, textViewsSiDimDa[i].getText().toString());
            data.put(prefix + "DATA_" + dataCounter++, textViewsSiDimDf[i].getText().toString());
            data.put(prefix + "DATA_" + dataCounter++, textViewsSiDimB1[i].getText().toString());
        }
    }

    private void openPdf(File file) {
        if (!file.exists()) {
            Toast.makeText(this, "El archivo PDF no existe.", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri pdfUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No hay aplicación para ver PDF instalada.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("OpenPDF_Cadenas", "Error al abrir PDF", e);
            Toast.makeText(this, "Error al abrir PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}