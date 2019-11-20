package com.nativess.xmllayouttopdflibrary;

import android.view.View;

public interface Generator {

    boolean openPdfFile();

    boolean savePdf(int pdfLayout);

    boolean savePdf(View view);

    boolean attachPdfToEmail();
}
