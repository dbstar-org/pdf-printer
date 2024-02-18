package io.github.dbstarll.pdf.printer;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.printing.PDFPageable;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

public class PdfPrinter {
    public static void main(String[] args) throws IOException, PrinterException {
        final File file = new File("/Users/dbstar/Documents/双面打印测试.pdf");

        try (PDDocument document = Loader.loadPDF(file)) {
            try (PDDocument next = new PDDocument()) {
                final PDPageTree pages = document.getPages();
                final int total = pages.getCount();
                int i = 0;
                while (total - i >= 4) {
                    add(next, pages.get(i), pages.get(i + 1), pages.get(i + 2), pages.get(i + 3));
                    i += 4;
                }
                if (i < total) {
                    final PDPage[] last = new PDPage[4];
                    for (int j = i; j < total; j++) {
                        last[j - i] = pages.get(j);
                    }
                    for (int j = total - i; j < 4; j++) {
                        last[j] = new PDPage();
                    }
                    add(next, last);
                }
                print(next);
            }
        }
    }

    private static void add(final PDDocument doc, final PDPage... pages) {
        doc.addPage(pages[3]);
        doc.addPage(pages[0]);
        doc.addPage(pages[1]);
        doc.addPage(pages[2]);
    }

    private static void print(final PDDocument doc) throws PrinterException {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPageable(new PDFPageable(doc));
        if (job.printDialog()) {
            job.print();
        }
    }
}
