package com.nativess.xmllayouttopdflibrary;

import android.graphics.Bitmap;

public interface PDFGenerator {

    boolean openPdfFile();

    boolean savePdf(int pdfLayout);

    boolean savePdf(Bitmap bitmap);

    boolean attachPdfToEmail();
}
