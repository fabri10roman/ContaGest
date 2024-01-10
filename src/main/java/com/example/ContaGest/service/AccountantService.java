package com.example.ContaGest.service;

import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.model.InvoiceModel;
import com.example.ContaGest.repository.AccountantRepository;
import com.example.ContaGest.repository.InvoiceRepository;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


import java.io.FileNotFoundException;
import java.util.List;

@Service
@AllArgsConstructor
public class AccountantService {

    private final AccountantRepository accountantRepository;
    private final InvoiceRepository invoiceRepository;

    public void getPDF (String clientCI,int month,String path) throws FileNotFoundException {

        List<InvoiceModel> invoices = invoiceRepository.findByClientIdAndMonth(clientCI, month);
        List<byte[]> imgs = invoices.stream().map(InvoiceModel::getImg).toList();

        if (imgs.isEmpty()) throw new ResourceNotFoundException(String.format("Invoices with client CI %s and month %s not found",clientCI,month));

        PdfWriter pdfWriter = new PdfWriter(path);

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

    }



}
