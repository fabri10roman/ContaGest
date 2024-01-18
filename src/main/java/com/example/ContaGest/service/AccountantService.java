package com.example.ContaGest.service;

import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.model.InvoiceModel;
import com.example.ContaGest.repository.InvoiceRepository;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountantService {

    private final InvoiceRepository invoiceRepository;

    public byte[] getPDF(String clientCI, int month, int year) {

        List<InvoiceModel> invoices = invoiceRepository.findByClientIdAndMonthAndYear(clientCI, month, year);
        List<byte[]> imgs = invoices.stream().map(InvoiceModel::getImg).toList();

        if (imgs.isEmpty()) {
            throw new ResourceNotFoundException(String.format("Invoices of the client with CI %s in month %s and year %s not found", clientCI, month, year));
        }

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(byteArrayOutputStream);

            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.addNewPage();

            Document document = new Document(pdfDocument);

            for (byte[] img : imgs) {
                ImageData imageData = ImageDataFactory.create(img);
                Image image = new Image(imageData);
                float width = pdfDocument.getDefaultPageSize().getWidth() - document.getLeftMargin() - document.getRightMargin();
                float height = pdfDocument.getDefaultPageSize().getHeight() - document.getTopMargin() - document.getBottomMargin();
                image.scaleToFit(width, height);
                Div div = new Div();
                div.setVerticalAlignment(VerticalAlignment.MIDDLE);
                div.setHorizontalAlignment(HorizontalAlignment.CENTER);
                div.add(image);
                div.setMarginTop(20);
                document.add(div);
            }
            document.close();
            return byteArrayOutputStream.toByteArray();
        }catch (Exception e){
            throw new IllegalStateException("Error to generate PDF");
        }
    }




}
