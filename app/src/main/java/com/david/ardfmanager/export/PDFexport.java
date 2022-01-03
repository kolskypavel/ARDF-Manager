package com.david.ardfmanager.export;

import android.graphics.pdf.PdfDocument;

public class PDFexport {
    private void generatePDF(){
        //Here the system loads propeties about the phone to make it responsible

        int pageWidth = 1100;
        int pageHeight = 600;

        PdfDocument export = new PdfDocument();
        PdfDocument.PageInfo info_pg1 = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();

        PdfDocument.Page pg1 =  export.startPage(info_pg1);

        export.finishPage(pg1);
    }
}
