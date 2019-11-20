package com.nativess.xmllayouttopdflibrary;

import android.app.Activity;
import android.view.View;

public class PDFGenerator implements Generator {
    private final GeneratorImpl pdfGenerator;

    public PDFGenerator(final Activity activity, final PDFBuilder builder) {
        pdfGenerator = new GeneratorImpl(activity, builder);
    }

    @Override
    public boolean openPdfFile() {
        return pdfGenerator.openPdfFile();
    }

    @Override
    public boolean savePdf(int pdfLayout) {
        return pdfGenerator.savePdf(pdfLayout);
    }

    @Override
    public boolean savePdf(View view) {
        return pdfGenerator.savePdf(view);
    }

    @Override
    public boolean attachPdfToEmail() {
        return pdfGenerator.attachPdfToEmail();
    }
}
