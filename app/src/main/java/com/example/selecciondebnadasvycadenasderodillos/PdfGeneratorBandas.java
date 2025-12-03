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
 * Clase auxiliar para generar un reporte en PDF con los resultados del cálculo de Bandas V.
 * OPTIMIZADA: Carga imágenes redimensionadas para generación rápida (< 2 seg).
 */
public class PdfGeneratorBandas {

    private Context context;
    private LinkedHashMap<String, String> data;
    private static final String TAG = "PdfGeneratorBandas";

    // Márgenes y dimensiones de página (A4 Alargado para scroll vertical infinito visual)
    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 3500;
    private static final int MARGIN_LEFT = 40;
    private static final int MARGIN_TOP = 40;
    private static final int MARGIN_RIGHT = 40;
    private int contentWidth;

    private int y; // Posición Y actual en el lienzo (cursor de escritura)

    // Estilos de Pintura (Paints)
    private Paint paintReportTitle;
    private Paint paintDate;
    private Paint paintHeaderDatos;
    private Paint paintDataKey;
    private Paint paintDataValue;
    private Paint paintAltHeader;
    private Paint paintAltData;
    private Paint paintDataTitle;
    private Paint paintValidationResiste;
    private Paint paintValidationNoResiste;
    private Paint paintLinea;
    private Paint paintIter2Header;

    // Estilos para Imágenes
    private Paint paintImage;
    private Paint paintImageTitle;

    // Bitmaps
    private Bitmap bmpTerminologia;
    private Bitmap bmpDimensiones;

    public PdfGeneratorBandas(Context context, LinkedHashMap<String, String> data) {
        this.context = context;
        this.data = data;
        this.y = MARGIN_TOP;
        this.contentWidth = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

        initPaints();
        loadAndOptimizeImages();
    }

    private void initPaints() {
        // Título del Reporte
        paintReportTitle = new Paint();
        paintReportTitle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintReportTitle.setTextSize(18);
        paintReportTitle.setColor(Color.BLACK);

        // Fecha
        paintDate = new Paint();
        paintDate.setTextSize(10);
        paintDate.setColor(Color.BLACK);
        paintDate.setTextAlign(Paint.Align.RIGHT);

        // Encabezados de Sección
        paintHeaderDatos = new Paint();
        paintHeaderDatos.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintHeaderDatos.setTextSize(14);
        paintHeaderDatos.setColor(Color.BLACK);

        // Clave de Dato (ej: "Potencia:")
        paintDataKey = new Paint();
        paintDataKey.setTextSize(10);
        paintDataKey.setColor(Color.DKGRAY);

        // Valor de Dato
        paintDataValue = new Paint();
        paintDataValue.setTextSize(10);
        paintDataValue.setColor(Color.BLACK);

        // Título de Alternativa
        paintAltHeader = new Paint();
        paintAltHeader.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintAltHeader.setTextSize(12);
        paintAltHeader.setColor(Color.BLACK);

        // Texto de Datos de Alternativa
        paintAltData = new Paint();
        paintAltData.setTextSize(10);
        paintAltData.setColor(Color.BLACK);

        // Subtítulos (ej: "Cálculos de Ingeniería")
        paintDataTitle = new Paint();
        paintDataTitle.setTextSize(10);
        paintDataTitle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        paintDataTitle.setColor(Color.DKGRAY);

        // Título de Iteración 2
        paintIter2Header = new Paint();
        paintIter2Header.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
        paintIter2Header.setTextSize(11);
        paintIter2Header.setColor(Color.rgb(0, 0, 100));

        // Validaciones
        paintValidationResiste = new Paint();
        paintValidationResiste.setTextSize(10);
        paintValidationResiste.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintValidationResiste.setColor(Color.rgb(0, 100, 0));

        paintValidationNoResiste = new Paint();
        paintValidationNoResiste.setTextSize(10);
        paintValidationNoResiste.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintValidationNoResiste.setColor(Color.rgb(192, 0, 0));

        // Líneas
        paintLinea = new Paint();
        paintLinea.setColor(Color.LTGRAY);
        paintLinea.setStrokeWidth(1);

        // Imágenes
        paintImage = new Paint(Paint.FILTER_BITMAP_FLAG);

        paintImageTitle = new Paint();
        paintImageTitle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintImageTitle.setTextSize(12);
        paintImageTitle.setColor(Color.BLACK);
        paintImageTitle.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * Carga y redimensiona las imágenes para evitar lag en la generación del PDF.
     */
    private void loadAndOptimizeImages() {
        try {
            // Ancho objetivo: 300px es suficiente para media página A4 en calidad PDF pantalla
            int targetWidth = 720;

            // 1. Cargar Terminología
            Bitmap originalTerm = BitmapFactory.decodeResource(context.getResources(), R.drawable.terminologia_bandas);
            if (originalTerm != null) {
                float aspectRatio = (float) originalTerm.getHeight() / originalTerm.getWidth();
                int targetHeight = (int) (targetWidth * aspectRatio);
                bmpTerminologia = Bitmap.createScaledBitmap(originalTerm, targetWidth, targetHeight, true);

                if (bmpTerminologia != originalTerm) originalTerm.recycle(); // Liberar memoria
            }

            // 2. Cargar Dimensiones
            Bitmap originalDim = BitmapFactory.decodeResource(context.getResources(), R.drawable.dimensiones_bandasv);
            if (originalDim != null) {
                float aspectRatio = (float) originalDim.getHeight() / originalDim.getWidth();
                int targetHeight = (int) (targetWidth * aspectRatio);
                bmpDimensiones = Bitmap.createScaledBitmap(originalDim, targetWidth, targetHeight, true);

                if (bmpDimensiones != originalDim) originalDim.recycle(); // Liberar memoria
            }

            Log.d(TAG, "Imágenes cargadas y optimizadas correctamente.");

        } catch (Exception e) {
            Log.e(TAG, "Error al cargar imágenes. Verifica que existan en res/drawable.", e);
            bmpTerminologia = null;
            bmpDimensiones = null;
        }
    }

    public File createPdf() {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        try {
            // 1. Encabezado
            drawHeader(canvas);

            // 2. Imágenes (Antes de los datos)
            drawImages(canvas);

            // 3. Datos del HashMap
            for (Map.Entry<String, String> entry : data.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key.equals("HEADER_DATOS") || key.equals("HEADER_ALTERNATIVAS")) {
                    y += 10;
                    canvas.drawText(value, MARGIN_LEFT, y, paintHeaderDatos);
                    y += 20;
                }
                else if (key.startsWith("DATO_")) {
                    String label = key.substring(5).replace("_", " ") + ": ";
                    canvas.drawText(label, MARGIN_LEFT + 10, y, paintDataKey);
                    // Aumentamos el margen para el valor para que "Distancia necesaria..." quepa bien
                    canvas.drawText(value, MARGIN_LEFT + 140, y, paintDataValue);
                    y += 14;
                }
                else if (key.endsWith("_HEADER")) {
                    if (key.contains("_SI_")) {
                        y += 5;
                        canvas.drawLine(MARGIN_LEFT + 10, y, contentWidth + MARGIN_LEFT - 10, y, paintLinea);
                        y += 15;
                        canvas.drawText(value, MARGIN_LEFT + 10, y, paintIter2Header);
                    } else {
                        canvas.drawText(value, MARGIN_LEFT, y, paintAltHeader);
                    }
                    y += 16;
                }
                else if (key.contains("_DATA_TITLE")) {
                    y += 5;
                    canvas.drawText(value, MARGIN_LEFT + 10, y, paintDataTitle);
                    y += 14;
                }
                else if (key.contains("_DATA")) {
                    int indent = key.contains("_SI_") ? 25 : 10;
                    canvas.drawText(value, MARGIN_LEFT + indent, y, paintAltData);
                    y += 14;
                }
                else if (key.contains("SPACER")) {
                    y += Integer.parseInt(value);
                    if (!key.contains("_SI_")) { // Línea divisoria entre tarjetas principales
                        y += 5;
                        paintLinea.setStrokeWidth(2);
                        paintLinea.setColor(Color.GRAY);
                        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, paintLinea);
                        paintLinea.setStrokeWidth(1);
                        paintLinea.setColor(Color.LTGRAY);
                        y += 15;
                    }
                }
            }

            document.finishPage(page);

            File file = getPdfFile();
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();

            return file;

        } catch (Exception e) {
            Log.e(TAG, "Error generando PDF", e);
            e.printStackTrace();
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }

        try {
            document.close();
        } catch (Exception ex) { }
        return null;
    }

    private void drawHeader(Canvas canvas) {
        canvas.drawText("Reporte de Selección de Bandas V", MARGIN_LEFT, y + 35, paintReportTitle);
        String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawText(fecha, PAGE_WIDTH - MARGIN_RIGHT, y + 35, paintDate);
        y += 60;
        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, paintLinea);
        y += 20;
    }

    private void drawImages(Canvas canvas) {
        if (bmpTerminologia == null && bmpDimensiones == null) return;

        y += 10;
        int gap = 10;

        // Proporciones: 40% Terminología, 60% Dimensiones
        float percentCol1 = 0.40f;
        float percentCol2 = 0.60f;

        int columnWidth1 = (int) ((contentWidth - gap) * percentCol1);
        int columnWidth2 = (int) ((contentWidth - gap) * percentCol2);

        float centerX_Col1 = MARGIN_LEFT + (columnWidth1 / 2.0f);

        int startY = y;
        int maxY = y;

        // 1. Terminología (Izquierda) con Título
        if (bmpTerminologia != null) {
            y = startY;
            canvas.drawText("Tabla de terminos", centerX_Col1, y, paintImageTitle);
            y += 18;

            int h = (int) ((float) bmpTerminologia.getHeight() * ((float) columnWidth1 / (float) bmpTerminologia.getWidth()));
            Rect dest = new Rect(MARGIN_LEFT, y, MARGIN_LEFT + columnWidth1, y + h);
            canvas.drawBitmap(bmpTerminologia, null, dest, paintImage);

            if (y + h > maxY) maxY = y + h;
        }

        // 2. Dimensiones (Derecha) SIN Título
        if (bmpDimensiones != null) {
            y = startY + 18; // Alineamos con la imagen izquierda (saltando su título)

            int left = MARGIN_LEFT + columnWidth1 + gap;
            int h = (int) ((float) bmpDimensiones.getHeight() * ((float) columnWidth2 / (float) bmpDimensiones.getWidth()));

            Rect dest = new Rect(left, y, left + columnWidth2, y + h);
            canvas.drawBitmap(bmpDimensiones, null, dest, paintImage);

            if (y + h > maxY) maxY = y + h;
        }

        y = maxY + 25; // Actualizar cursor Y para el texto siguiente
    }


    private File getPdfFile() {
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (directory == null) directory = context.getExternalCacheDir();
        if (directory == null) directory = context.getFilesDir();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return new File(directory, "ReporteBanda_" + timeStamp + ".pdf");
    }
}