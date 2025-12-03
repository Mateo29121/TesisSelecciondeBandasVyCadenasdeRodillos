package com.example.selecciondebnadasvycadenasderodillos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Clase auxiliar para generar un reporte en PDF con los resultados del cálculo.
 * VERSIÓN ROBUSTA: Una sola página larga.
 * NUEVO: Formato de UI (imita la app) en lugar de "llave: valor".
 * ACTUALIZADO: Añadida capacidad para dibujar imágenes (simbología y curva).
 * VERSIÓN SIMPLIFICADA: Eliminada la lógica de columnas de texto.
 */
public class PdfGenerator {

    private Context context;
    private LinkedHashMap<String, String> data;
    private static final String TAG = "PdfGenerator_UI";

    // Márgenes y dimensiones de página
    private static final int PAGE_WIDTH = 595; // Ancho A4
    private static final int PAGE_HEIGHT = 3500; // Altura personalizada extra larga
    private static final int MARGIN_LEFT = 40;
    private static final int MARGIN_TOP = 40;
    private static final int MARGIN_RIGHT = 40;
    private int contentWidth;

    private int y; // Posición Y actual en el lienzo

    // --- MÚLTIPLES ESTILOS DE PINTURA ---
    private Paint paintReportTitle; // "Reporte de Selección..."
    private Paint paintDate;        // Fecha
    private Paint paintHeaderDatos; // "Datos de Cálculo"
    private Paint paintDataKey;     // "Potencia Nominal: "
    private Paint paintDataValue;   // "10 Kw"
    private Paint paintAltHeader;   // "1 torón: 120 P 38.1 mm"
    private Paint paintAltData;     // "a/p = 74 uniones"
    private Paint paintDataTitle;   // "Cálculo de Dimensiones..."
    private Paint paintValidationResiste;
    private Paint paintValidationNoResiste;
    private Paint paintLinea;
    private Paint paintImage;       // Para dibujar bitmaps
    private Paint paintImageTitle;  // Título para las imágenes

    // --- BITMAPS PARA IMÁGENES ---
    private Bitmap bmpSimbologia;
    private Bitmap bmpCurva;

    public PdfGenerator(Context context, LinkedHashMap<String, String> data) {
        this.context = context;
        this.data = data;
        this.y = MARGIN_TOP;
        this.contentWidth = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

        // Título del Reporte (Grande, Bold)
        paintReportTitle = new Paint();
        paintReportTitle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintReportTitle.setTextSize(18);
        paintReportTitle.setColor(Color.BLACK);

        // Fecha (Normal, Derecha)
        paintDate = new Paint();
        paintDate.setTextSize(10);
        paintDate.setColor(Color.BLACK);
        paintDate.setTextAlign(Paint.Align.RIGHT);

        // Header (Ej: "Datos de Cálculo", "Alternativa 1")
        paintHeaderDatos = new Paint();
        paintHeaderDatos.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintHeaderDatos.setTextSize(14);
        paintHeaderDatos.setColor(Color.BLACK);

        // Clave de Dato simple (Ej: "Potencia Nominal: ")
        paintDataKey = new Paint();
        paintDataKey.setTextSize(10);
        paintDataKey.setColor(Color.DKGRAY); // Gris oscuro

        // Valor de Dato simple (Ej: "10 Kw")
        paintDataValue = new Paint();
        paintDataValue.setTextSize(10);
        paintDataValue.setColor(Color.BLACK);

        // Header de Alternativa (Ej: "1 torón: 120 P 38.1 mm")
        paintAltHeader = new Paint();
        paintAltHeader.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintAltHeader.setTextSize(12);
        paintAltHeader.setColor(Color.BLACK);

        // Línea de dato de Alternativa (Ej: "a/p = 74 uniones")
        paintAltData = new Paint();
        paintAltData.setTextSize(10);
        paintAltData.setColor(Color.BLACK); // Negro, como en la app

        // Título de sub-sección (Ej: "Cálculo de Dimensiones...")
        paintDataTitle = new Paint();
        paintDataTitle.setTextSize(10);
        paintDataTitle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        paintDataTitle.setColor(Color.DKGRAY);

        // Validación OK (Verde)
        paintValidationResiste = new Paint();
        paintValidationResiste.setTextSize(10);
        paintValidationResiste.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintValidationResiste.setColor(Color.rgb(0, 100, 0)); // Verde oscuro

        // Validación ERROR (Rojo)
        paintValidationNoResiste = new Paint();
        paintValidationNoResiste.setTextSize(10);
        paintValidationNoResiste.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintValidationNoResiste.setColor(Color.rgb(192, 0, 0)); // Rojo oscuro

        // Línea separadora
        paintLinea = new Paint();
        paintLinea.setColor(Color.LTGRAY);
        paintLinea.setStrokeWidth(1);

        // Paint para Bitmaps
        paintImage = new Paint(Paint.FILTER_BITMAP_FLAG);

        // Título para Imágenes
        paintImageTitle = new Paint();
        paintImageTitle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintImageTitle.setTextSize(12);
        paintImageTitle.setColor(Color.BLACK);
        paintImageTitle.setTextAlign(Paint.Align.CENTER); // ¡Importante para centrar!

        // Cargar Bitmaps
        try {
            bmpSimbologia = BitmapFactory.decodeResource(context.getResources(), R.drawable.simbologia_cadenas);
            bmpCurva = BitmapFactory.decodeResource(context.getResources(), R.drawable.curva_cadenas);
            Log.d(TAG, "Imágenes cargadas correctamente.");
        } catch (Exception e) {
            Log.e(TAG, "Error al cargar imágenes desde drawable." +
                    " Asegúrate que 'simbologia_cadenas.png' y 'curva_cadenas.png' estén en res/drawable", e);
            bmpSimbologia = null;
            bmpCurva = null;
        }
    }

    /**
     * Crea y guarda el documento PDF.
     * @return El archivo PDF generado, o null si falla.
     */
    public File createPdf() {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        try {
            // --- 1. Dibujar el Encabezado (actualiza 'y') ---
            drawHeader(canvas);

            // --- 2. Iterar sobre los datos y dibujarlos según su clave ---
            // Bucle de dibujo simple y secuencial
            for (Map.Entry<String, String> entry : data.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                // Lógica de Dibujo Normal (si NO estamos en modo columna)
                // --- CORRECCIÓN: Añadidas HEADER_SELECCION y HEADER_ALTERNATIVAS ---
                if (key.equals("HEADER_DATOS") || key.equals("HEADER_SELECCION") ||
                        key.equals("HEADER_ALTERNATIVAS") || key.equals("HEADER_SI")) {
                    y += 10;
                    canvas.drawText(value, MARGIN_LEFT, y, paintHeaderDatos);
                    y += 20;
                }
                else if (key.equals("DRAW_IMAGES_HERE")) {
                    drawImages(canvas);
                }
                else if (key.startsWith("DATO_")) {
                    String label = key.substring(5).replace("_", " ") + ": ";
                    canvas.drawText(label, MARGIN_LEFT + 10, y, paintDataKey);
                    canvas.drawText(value, MARGIN_LEFT + 120, y, paintDataValue);
                    y += 14;
                }
                else if (key.endsWith("_HEADER")) {
                    canvas.drawText(value, MARGIN_LEFT, y, paintAltHeader);
                    y += 16;
                }
                else if (key.contains("_DATA_TITLE")) {
                    y += 5;
                    canvas.drawText(value, MARGIN_LEFT + 10, y, paintDataTitle);
                    y += 14;
                }
                else if (key.contains("_DATA")) {
                    canvas.drawText(value, MARGIN_LEFT + 10, y, paintAltData);
                    y += 14;
                }
                else if (key.contains("_VALIDACION")) {
                    Paint paintToUse = value.startsWith("Resiste") ? paintValidationResiste : paintValidationNoResiste;
                    canvas.drawText(value, MARGIN_LEFT + 10, y, paintToUse);
                    y += 14;
                }
                else if (key.contains("SPACER")) {
                    y += Integer.parseInt(value);
                }
                // Las claves de control de columna (START_COLUMN...) simplemente se ignoran
            }

            document.finishPage(page);
            Log.d(TAG, "PDF finalizado.");

            // --- 4. Guardar el archivo ---
            File file = getPdfFile();
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            Log.d(TAG, "PDF guardado.");

            return file;

        } catch (Exception e) { // Capturar CUALQUIER error
            Log.e(TAG, "Error CRÍTICO al generar PDF", e);
            e.printStackTrace();
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }

        try {
            if (document != null) {
                document.close();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error al cerrar documento después de error", ex);
        }
        return null;
    }

    /**
     * Dibuja el encabezado en el lienzo proporcionado.
     */
    private void drawHeader(Canvas canvas) {
        canvas.drawText("Reporte de Selección de Cadenas", MARGIN_LEFT, y + 35, paintReportTitle);
        String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawText(fecha, PAGE_WIDTH - MARGIN_RIGHT, y + 35, paintDate);
        y += 60;
        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, paintLinea);
        y += 20;
    }

    /**
     * Dibuja las imágenes cargadas (Simbología y Curva) en el lienzo.
     * Dibuja las imágenes una al lado de la otra en dos columnas
     * con ancho variable (porcentaje)
     */
    private void drawImages(Canvas canvas) {
        if (bmpSimbologia == null && bmpCurva == null) {
            Log.w(TAG, "No se dibujaron imágenes, no se pudieron cargar.");
            return;
        }

        y += 10;

        int gap = 10;

        // --- AQUÍ PUEDES MODIFICAR LOS TAMAÑOS ---
        float percentCol1 = 0.25f; // 25% para la simbología
        float percentCol2 = 0.75f; // 75% para la curva

        int columnWidth1 = (int) ((contentWidth - gap) * percentCol1);
        int columnWidth2 = (int) ((contentWidth - gap) * percentCol2);

        float centerX_Col1 = MARGIN_LEFT + (columnWidth1 / 2.0f);
        float centerX_Col2 = MARGIN_LEFT + columnWidth1 + gap + (columnWidth2 / 2.0f);

        int startY_for_row = y;
        int bottom_Image1 = y;
        int bottom_Image2 = y;

        if (bmpSimbologia != null) {
            y = startY_for_row;

            canvas.drawText("Tabla de terminos", centerX_Col1, y, paintImageTitle);
            y += 18;

            int origWidth = bmpSimbologia.getWidth();
            int origHeight = bmpSimbologia.getHeight();
            int newHeight = (int) ((float) origHeight * ((float) columnWidth1 / (float) origWidth));

            Rect destRect = new Rect(MARGIN_LEFT, y, MARGIN_LEFT + columnWidth1, y + newHeight);
            canvas.drawBitmap(bmpSimbologia, null, destRect, paintImage);

            bottom_Image1 = y + newHeight;
        }

        if (bmpCurva != null) {
            y = startY_for_row;

            canvas.drawText("Diagrama de potencia", centerX_Col2, y, paintImageTitle);
            y += 18;

            int origWidth = bmpCurva.getWidth();
            int origHeight = bmpCurva.getHeight();
            int newHeight = (int) ((float) origHeight * ((float) columnWidth2 / (float) origWidth));

            int left_Col2 = MARGIN_LEFT + columnWidth1 + gap;
            Rect destRect = new Rect(left_Col2, y, left_Col2 + columnWidth2, y + newHeight);
            canvas.drawBitmap(bmpCurva, null, destRect, paintImage);

            bottom_Image2 = y + newHeight;
        }

        y = Math.max(bottom_Image1, bottom_Image2) + 20;
    }

    /**
     * Crea un nombre de archivo único y obtiene el File object.
     */
    private File getPdfFile() {
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (directory == null) {
            Log.w(TAG, "getExternalFilesDir(DOCUMENTS) es nulo. Usando caché.");
            directory = context.getExternalCacheDir();
        }
        if (directory == null) {
            Log.e(TAG, "¡Incluso el directorio de caché es nulo! Usando filesDir.");
            directory = context.getFilesDir();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "ReporteCadena_" + timeStamp + ".pdf";

        return new File(directory, fileName);
    }
}