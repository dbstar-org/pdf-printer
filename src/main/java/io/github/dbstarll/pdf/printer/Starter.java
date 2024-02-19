package io.github.dbstarll.pdf.printer;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Starter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Starter.class);

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: pdf-printer <input pdf file>");
            System.exit(1);
        }

        final File input = new File(args[0]);
        final File output = getOutputFile(input);
        LOGGER.info("input: {}", input);
        LOGGER.info("output: {}", output);

        doubleFaced(input, output);
    }

    private static File getOutputFile(final File input) {
        final int index = input.getName().lastIndexOf('.');
        if (index >= 0) {
            final String filename = input.getName().substring(0, index);
            final String ext = input.getName().substring(index + 1);
            return new File(input.getParent(), filename + "-双面二折页." + ext);
        } else {
            return new File(input.getParent(), input.getName() + "-双面二折页.pdf");
        }
    }

    private static void doubleFaced(final File input, final File output) throws IOException {
        try (PDDocument document = Loader.loadPDF(input)) {
            order(document);
            rotate(document);
            overLay(document, output);
        }
    }

    private static void order(final PDDocument document) {
        final PDPageTree pages = document.getPages();
        final int total = pages.getCount();
        int i = 0;
        while (total - i >= 4) {
            final PDPage first = pages.get(i);
            final PDPage four = pages.get(i + 3);
            pages.remove(four);
            pages.insertBefore(four, first);
            i += 4;
        }
        if (i < total) {
            pages.insertBefore(new PDPage(), pages.get(i));
        }
    }

    private static void rotate(final PDDocument document) throws IOException {
        final PDPageTree pages = document.getPages();
        for (int i = 0, total = pages.getCount(); i < total; i++) {
            final PDPage page = pages.get(i);
            try (PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.PREPEND, false, false)) {
                final PDRectangle cropBox = page.getCropBox();
                float ty = (cropBox.getLowerLeftY() + cropBox.getUpperRightY()) / 2;
                float scale = cropBox.getUpperRightX() / cropBox.getUpperRightY();
                float upperY = i % 2 == 0 ? cropBox.getUpperRightY() : ty;
                cs.transform(Matrix.getRotateInstance(Math.toRadians(-90), cropBox.getLowerLeftX(), upperY));
                cs.transform(Matrix.getScaleInstance(scale, scale));
            }
        }
    }

    private static void overLay(final PDDocument document, final File output) throws IOException {
        final int total = document.getNumberOfPages();
        try (PDDocument second = new PDDocument()) {
            for (int i = 0; i < total; i++) {
                if (i % 2 == 1) {
                    second.importPage(document.getPage(i));
                }
            }
            for (int i = total - 1; i >= 0; i--) {
                if (i % 2 == 1) {
                    document.removePage(i);
                }
            }
            try (Overlay overLay = new Overlay()) {
                overLay.setInputPDF(document);
                overLay.setAllPagesOverlayPDF(second);
                overLay.overlay(new HashMap<>()).save(output);
            }
        }
    }
}
