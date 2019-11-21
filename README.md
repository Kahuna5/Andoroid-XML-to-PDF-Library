# Andoroid-XML-to-PDF-Library
Convert an XML layout or a Any View to a PDF file.
[![](https://jitpack.io/v/Kahuna5/Andoroid-XML-to-PDF-Library.svg)](https://jitpack.io/#Kahuna5/Andoroid-XML-to-PDF-Library)

## To import this library in your Andorid Project add this dependency in your app module build.gradle file:
``` java
    implementation 'com.github.native-ss:xml-to-pdf:1.0.0'
```

## Create a PDF from an XML layout:
First create an instance of the PDFBuilder.
Calling the setters is optional.
All the neccessary fields for building a PDF have default values.

``` java
        PDFBuilder builder = new PDFBuilder()
                .setPageWidth(1300)
                .setDirectoryPath(stringPath)
                .setPageHeight(1800)
                .setEmail("dave@live.com")
                .setEmailSubject("Subject test")
                .setPdfFileName("testName")
                .setEmailText("email body");
```
After that call the PDFGenerator(Context, PDFBuilder).

``` java
PDFGenerator generator = new PDFGenerator(this, builder);
```

And save the PDF.
savePdf can take an int [the xml layout id]:

``` java
generator.savePdf(R.layout.my_layout);
```

 or a VIEW as a parameter:
``` java
final View invoiceLayout = LayoutInflater.from(this).inflate(R.layout.my_invoice_layout,
        null);

invoiceLayout.findViewById(R.id.invoice_due_date).setVisibility(View.GONE);
TextView tvHeader = invoiceLayout.findViewById(R.id.invoiceHeader);
tvHeader.setText("My Invoice Header");
        
generator.savePdf(invoiceLayout);
```
The PDF will be saved in the directoryPath that was set in the PDFBuilder.

Now you can open the PDF or attach it to an EMAIL;

``` java
        generator.openPdfFile();
        ---OR---
        generator.attachPdfToEmail();
```

NOTE:
PDFGenerator requires READ and WRITE permissions.
User will need to request this permissions before using the library.

