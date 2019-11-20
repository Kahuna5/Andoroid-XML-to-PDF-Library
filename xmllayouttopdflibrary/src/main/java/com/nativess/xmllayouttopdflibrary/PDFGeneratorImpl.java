package com.nativess.xmllayouttopdflibrary;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PDFGeneratorImpl implements PDFGenerator {

    private PdfBuilder mPdfBuilder;
    private Activity activity;

    public PDFGeneratorImpl(final Activity activity, final PdfBuilder builder) {
        this.mPdfBuilder = builder;
        this.activity = activity;
    }

    @Override
    public boolean openPdfFile() {
        final String filename = mPdfBuilder.getFilename();
        final String directoryPath = mPdfBuilder.getDirectoryPath();
        final File file = new File(directoryPath + filename);
        if (!file.exists()) {
            LogUtil.e("PDFGeneratorImpl: openPdfFile: Exception: pdf does not exist.");
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
        final Bitmap bitmap = createClusterBitmap(pdfLayout);
        return savePdf(bitmap);
    }

    @Override
    public boolean savePdf(final Bitmap bitmap) {
        if (hasReadAndWritePermissions()) {
            final String filename = mPdfBuilder.getFilename();
            final String directoryPath = mPdfBuilder.getDirectoryPath();
            final PdfDocument pdfDocument = bitmapToPdf(bitmap);
            if (pdfDocument != null) {
                final File file = new File(directoryPath + filename);

                //If folder does not exist, create folder.
                final File mediaStorageDir = new File(directoryPath);
                if (!mediaStorageDir.exists()) {
                    boolean result = mediaStorageDir.mkdirs();
                    LogUtil.d("PDFGeneratorImpl: savePdf: mediaStorageDir.mkdirs: " + result);
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

                LogUtil.e("PDFGeneratorImpl: savePdf: Exception: Unknown exception. " +
                        "Could not save pdf file to external storage. ");
                pdfDocument.close();
                return false;
            } else {
                LogUtil.e("PDFGeneratorImpl: savePdf: Exception: Pdf Document is null. ");
                return false;
            }
        } else {
            LogUtil.e("PDFGeneratorImpl: savePdf: Exception: \n" +
                    "********************** \n" +
                    "PERMISSIONS NOT GRANTED!! \n" +
                    "********************** \n" +
                    "READ AND WRITE PERMISSIONS ARE REQUIRED TO SAVE PDF TO EXTERNAL STORAGE. " +
                    "********************** \n");
            return false;
        }
    }

    @Override
    public boolean attachPdfToEmail() {
        final String filename = mPdfBuilder.getFilename();
        final String directoryPath = mPdfBuilder.getDirectoryPath();
        final String email = mPdfBuilder.getEmail();
        final String emailSubject = mPdfBuilder.getEmailSubject();
        final String emailText = mPdfBuilder.getEmailText();

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
            final int pdfPageWidth = mPdfBuilder.getPdfPageWidth();
            final int pdfPageHeight = mPdfBuilder.getPdfPageHeight();
            final int pdfPageNumber = mPdfBuilder.getPdfPageNumber();

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
            LogUtil.e("PDFGeneratorImpl: bitmapToPdf: Exception: Bitmap is null. ");
            return null;
        }
    }

    /**
     * Generates a bitmap image from an XML layout.
     *
     * @param pdfLayout, xml layout.
     * @return Bitmap, image.
     */
    private Bitmap createClusterBitmap(final int pdfLayout) {
        final View invoiceLayout = LayoutInflater.from(activity).inflate(pdfLayout,
                null);

        //Measure the invoiceLayout view
        invoiceLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        invoiceLayout.layout(0, 0, invoiceLayout.getMeasuredWidth(),
                invoiceLayout.getMeasuredHeight());

        //Create the bitmap.
        final Bitmap clusterBitmap = Bitmap.createBitmap(invoiceLayout.getMeasuredWidth(),
                invoiceLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(clusterBitmap);
        invoiceLayout.draw(canvas);

        return clusterBitmap;
    }

    /**
     * Read an write permissions are required for saving a file to the external storage.
     * This method checks if permissions are granted.
     *
     * @return boolean, true if granted.
     */
    private boolean hasReadAndWritePermissions() {
        final boolean readPermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        final boolean writePermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        return readPermission && writePermission;
    }

    //endregion

}
