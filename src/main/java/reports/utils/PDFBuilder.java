package reports.utils;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilidad para construir PDFs con iText 7 replicando la estética de BookTech.
 * Implementa un conjunto mínimo de métodos necesarios para encabezado/pie/tabla/secciones.
 */
public class PDFBuilder {

    private final Document document;
    private final PdfFont fontNormal;
    private final PdfFont fontBold;

    public PDFBuilder(String rutaArchivo) {
        try {
            File file = new File(rutaArchivo);
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();

            PdfWriter writer = new PdfWriter(rutaArchivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            this.document = new Document(pdfDoc, PageSize.A4);
            this.document.setMargins(20, 20, 20, 20);

            this.fontNormal = PdfFontFactory.createFont();
            this.fontBold = PdfFontFactory.createFont();
        } catch (Exception e) {
            throw new RuntimeException("Error creando PDF: " + e.getMessage(), e);
        }
    }

    public PDFBuilder agregarTitulo(String titulo) {
        Paragraph p = new Paragraph(titulo)
                .setFont(fontBold)
                .setFontSize(22)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8);
        document.add(p);
        return this;
    }

    public PDFBuilder agregarSubtitulo(String subtitulo) {
        Paragraph p = new Paragraph(subtitulo)
                .setFont(fontNormal)
                .setFontSize(13)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(12);
        document.add(p);
        return this;
    }

    public PDFBuilder agregarParrafo(String texto) {
        document.add(new Paragraph(texto).setFont(fontNormal).setFontSize(11).setMarginBottom(8));
        return this;
    }

    public PDFBuilder agregarParrafoIndentado(String texto) {
        Paragraph p = new Paragraph(texto).setFont(fontNormal).setFontSize(11).setMarginLeft(15).setMarginBottom(6);
        document.add(p);
        return this;
    }

    public PDFBuilder agregarSeccion(String titulo) {
        Paragraph p = new Paragraph(titulo)
                .setFont(fontBold)
                .setFontSize(14)
                .setFontColor(ColorConstants.BLACK)
                .setMarginTop(10)
                .setMarginBottom(6);
        document.add(p);
        return this;
    }

    public PDFBuilder agregarLineaDetalle(String etiqueta, String valor) {
        Text t1 = new Text(etiqueta + ": ").setFont(fontBold);
        Text t2 = new Text(valor == null ? "" : valor).setFont(fontNormal);
        Paragraph p = new Paragraph().add(t1).add(t2).setFontSize(11).setMarginBottom(2);
        document.add(p);
        return this;
    }

    public PDFBuilder agregarEspacio(float puntos) {
        Paragraph p = new Paragraph(" ").setMarginTop(puntos);
        document.add(p);
        return this;
    }

    public PDFBuilder agregarEncabezadoInstitucional(String institucion) {
        return agregarEncabezadoInstitucional(institucion, null, null);
    }

    public PDFBuilder agregarEncabezadoInstitucional(String institucion, String sede, String recursoEscudo) {
        try {
            float[] widths = {1f, 4f};
            Table header = new Table(widths).useAllAvailableWidth();

            // Columna izquierda: escudo (opcional)
            Cell left = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
            if (recursoEscudo != null) {
                Image escudo = cargarImagenRecurso(recursoEscudo, 60);
                if (escudo != null) left.add(escudo);
            }
            header.addCell(left);

            // Columna derecha: textos
            Paragraph title = new Paragraph(institucion == null ? "" : institucion)
                    .setFont(fontBold).setFontSize(16).setTextAlignment(TextAlignment.LEFT);
            Paragraph sedeP = new Paragraph(sede == null ? "" : sede)
                    .setFont(fontNormal).setFontSize(11).setFontColor(ColorConstants.GRAY);
            Cell right = new Cell().setBorder(Border.NO_BORDER);
            right.add(title);
            if (sede != null && !sede.trim().isEmpty()) right.add(sedeP);

            header.addCell(right);
            document.add(header);
            agregarEspacio(6);
        } catch (Exception e) {
            // Continuar sin encabezado si ocurre un error de recurso
        }
        return this;
    }

    public PDFBuilder agregarLineaCiudadFecha(String ciudad) {
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd", new Locale("es", "CO")));
        String texto = (ciudad == null || ciudad.isEmpty()) ? fecha : (ciudad + ", " + fecha);
        Paragraph p = new Paragraph(texto)
                .setFont(fontNormal).setFontSize(10)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(10);
        document.add(p);
        return this;
    }

    public PDFBuilder agregarAsunto(String asunto) {
        Paragraph p = new Paragraph(asunto)
                .setFont(fontBold).setFontSize(13)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(10);
        document.add(p);
        return this;
    }

    public Table crearTabla(float[] anchos, String[] encabezados) {
        Table t = new Table(UnitValue.createPercentArray(anchos)).useAllAvailableWidth();
        if (encabezados != null) {
            for (String e : encabezados) {
                Cell c = new Cell().add(new Paragraph(e).setFont(fontBold))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER);
                t.addHeaderCell(c);
            }
        }
        return t;
    }

    public PDFBuilder agregarFilaTabla(Table tabla, String[] valores) {
        if (tabla == null || valores == null) return this;
        for (String v : valores) {
            Cell c = new Cell().add(new Paragraph(v == null ? "" : v).setFont(fontNormal).setFontSize(10));
            tabla.addCell(c);
        }
        return this;
    }

    public PDFBuilder agregarTabla(Table tabla) {
        if (tabla != null) document.add(tabla);
        return this;
    }

    public PDFBuilder agregarFooterConFecha() {
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Paragraph p = new Paragraph("Generado el "+fecha)
                .setFont(fontNormal).setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(15);
        document.add(p);
        return this;
    }

    public void construir() {
        document.close();
    }

    private Image cargarImagenRecurso(String recurso, float width) {
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(recurso);
            if (is == null) return null;
            // Compatible con Java 8: leer el stream manualmente
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] bytes = buffer.toByteArray();

            ImageData imgData = ImageDataFactory.create(bytes);
            Image img = new Image(imgData);
            img.setAutoScale(true);
            if (width > 0) img.setWidth(width);
            return img;
        } catch (Exception e) {
            return null;
        } finally {
            if (is != null) {
                try { is.close(); } catch (Exception ignore) {}
            }
        }
    }
}
