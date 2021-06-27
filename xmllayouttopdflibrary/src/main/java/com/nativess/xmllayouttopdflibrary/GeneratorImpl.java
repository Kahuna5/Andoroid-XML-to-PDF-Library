package com.nativess.xmllayouttopdflibrary;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GeneratorImpl implements Generator {

    private final PDFBuilder mPDFBuilder;
    private final Activity activity;

    GeneratorImpl(final Activity activity, final PDFBuilder builder) {
        this.mPDFBuilder = builder;
        this.activity = activity;
    }

    @Override
    public boolean openPdfFile() {
        final String filename = mPDFBuilder.getFilename();
        final String directoryPath = mPDFBuilder.getDirectoryPath();
        final File file = new File(directoryPath + filename);
        if (!file.exists()) {
            LogUtil.e("GeneratorImpl: openPdfFile: Exception: pdf does not exist.");
            return false;
        }
        final String AUTHORITY = BuildConfig.LIBRARY_PACKAGE_NAME + ".provider";
        final Uri uri = FileProvider.getUriForFile(activity, AUTHORITY, file);
        final Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
        pdfIntent.setDataAndType(uri, "application/pdf");
        pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pdfIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            activity.startActivity(pdfIntent);
        } catch (ActivityNotFoundException e) {
            LogUtil.e("Utils: openPdfFile: Exception: " + e);
            return false;
        }
        return true;
    }

    @Override
    public boolean savePdf(final int pdfLayout) {
        final View layoutView = getMeasuredViewFromLayoutId(pdfLayout);
        return savePdf(layoutView);
    }

    @Override
    public boolean savePdf(final View view) {
        final String filename = mPDFBuilder.getFilename();
        final String directoryPath = mPDFBuilder.getDirectoryPath();
        final PdfDocument pdfDocument = bitmapToPdf(viewToBitmap(view));
        if (pdfDocument != null) {
            final File file = new File(directoryPath + filename);

            //If folder does not exist, create folder.
            final File mediaStorageDir = new File(directoryPath);
            if (!mediaStorageDir.exists()) {
                boolean result = mediaStorageDir.mkdirs();
                LogUtil.d("GeneratorImpl: savePdf: mediaStorageDir.mkdirs: " + result);
            }

            // write the document content
            try {
                FileOutputStream fos = new FileOutputStream(file);
                if (file.exists()) {
                    pdfDocument.writeTo(fos);
                    LogUtil.d("Utils: bitmapToPdf: success");
                    fos.flush();
                    fos.close();
                    pdfDocument.close();
                    return true;
                }
            } catch (IOException e) {
                LogUtil.e("Utils: bitmapToPdf: Exception: " + e);
                pdfDocument.close();
                return false;
            }

            LogUtil.e("GeneratorImpl: savePdf: Exception: Unknown exception. " +
                    "Could not save pdf file to external storage. ");
            pdfDocument.close();
        } else {
            LogUtil.e("GeneratorImpl: savePdf: Exception: Pdf Document is null. ");
        }
        return false;
    }

    @Override
    public boolean attachPdfToEmail() {
        final String filename = mPDFBuilder.getFilename();
        final String directoryPath = mPDFBuilder.getDirectoryPath();
        final String email = mPDFBuilder.getEmail();
        final String emailSubject = mPDFBuilder.getEmailSubject();
        final String emailText = mPDFBuilder.getEmailText();

        final File file = new File(directoryPath + filename);
        final String AUTHORITY = BuildConfig.LIBRARY_PACKAGE_NAME + ".provider";
        final Uri uri = FileProvider.getUriForFile(activity, AUTHORITY, file);

        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("application/pdf");
        // the attachment
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        //Add mailto
        if (email != null && !email.isEmpty()) {
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        }
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailText);

        try {
            activity.startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            LogUtil.e("Utils: attachPdfToEmail: ActivityNotFoundException: " + e);
            return false;
        }

        return true;
    }

    //region Utils

    /**
     * Generates a PdfDocument from a bitmap image.
     *
     * @param bitmap, bitmap file.
     * @return PdfDocument.
     */
    private PdfDocument bitmapToPdf(final Bitmap bitmap) {
        if (bitmap != null) {
            final int pdfPageWidth = mPDFBuilder.getPdfPageWidth();
            final int pdfPageHeight = mPDFBuilder.getPdfPageHeight();
            final int pdfPageNumber = mPDFBuilder.getPdfPageNumber();

            /* GENERATE PDF */
            final PdfDocument document = new PdfDocument();
            final PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.
                    Builder(pdfPageWidth, pdfPageHeight, pdfPageNumber).create();

            final PdfDocument.Page page = document.startPage(pageInfo);

            final Canvas canvas = page.getCanvas();

            int centreX = (canvas.getWidth() - bitmap.getWidth()) / 2;
            int centreY = (canvas.getHeight() - bitmap.getHeight()) / 6;

            canvas.drawBitmap(bitmap, centreX, centreY, null);

            document.finishPage(page);

            return document;

        } else {
            LogUtil.e("GeneratorImpl: bitmapToPdf: Exception: Bitmap is null. ");
            return null;
        }
    }

    /**
     * Generates a bitmap from a View object.
     *
     * @param view, any view object.
     * @return Bitmap, bitmap image.
     */
    private Bitmap viewToBitmap(final View view) {

        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(),
                view.getMeasuredHeight());

        //Create the bitmap.
        final Bitmap clusterBitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(clusterBitmap);
        view.draw(canvas);

        return clusterBitmap;
    }

    /**
     * Returns a measured view from a layout id.
     *
     * @param pdfLayout, xml layout.
     * @return View, layout view.
     */
    private View getMeasuredViewFromLayoutId(final int pdfLayout) {
        final View invoiceLayout = LayoutInflater.from(activity).inflate(pdfLayout,
                null);

        //Measure the invoiceLayout view
        invoiceLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        invoiceLayout.layout(0, 0, invoiceLayout.getMeasuredWidth(),
                invoiceLayout.getMeasuredHeight());

        return invoiceLayout;
    }

    //endregion

}
