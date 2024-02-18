package io.github.dbstarll.pdf.printer;

import io.github.dbstarll.utils.spring.boot.BootLauncher;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Starter extends BootLauncher {
    public static void main(final String[] args) {
        new Starter().run(null, "pdf-printer", args).close();
    }
}
