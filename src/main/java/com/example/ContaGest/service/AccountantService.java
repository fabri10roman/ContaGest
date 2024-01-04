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
import com.itextpdf.layout.element.Image;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


import java.io.FileNotFoundException;
import java.util.List;

@Service
@AllArgsConstructor
public class AccountantService {

    private final AccountantRepository accountantRepository;
    private final InvoiceRepository invoiceRepository;

    public void getPDF (Long clientCI,int month,String path) throws FileNotFoundException {

        List<InvoiceModel> invoices = invoiceRepository.findByClientIdAndMonth(clientCI, month);
        List<byte[]> imgs = invoices.stream().map(InvoiceModel::getImg).toList();

        if (imgs.isEmpty()) throw new ResourceNotFoundException("No existen facturas en el mes seleccionado");

        PdfWriter pdfWriter = new PdfWriter(path);

        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        pdfDocument.addNewPage();

        Document document = new Document(pdfDocument);

        for (byte[] img : imgs) {
            ImageData imageData = ImageDataFactory.create(img);
            Image image = new Image(imageData);
            document.add(image);
        }

        document.close();

    }
}
