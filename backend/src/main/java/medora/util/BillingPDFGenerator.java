package medora.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import medora.dto.BillingDetailDTO;
import medora.dto.BillingItemDTO;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BillingPDFGenerator {

    public static byte[] generateInvoicePDF(BillingDetailDTO billingDetail) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            document.setMargins(20, 20, 20, 20);

            // Header
            Paragraph title = new Paragraph("MEDICAL BILLING INVOICE")
                    .setFontSize(24)
                    .setBold();
            document.add(title);

            // Bill Information
            Paragraph billInfo = new Paragraph()
                    .add("Bill #: " + billingDetail.getBillId())
                    .add("\nDate: " + formatDate(billingDetail.getBillDate()))
                    .setFontSize(11);
            document.add(billInfo);

            document.add(new Paragraph("\n"));

            // Patient Information Section
            Paragraph patientHeader = new Paragraph("PATIENT INFORMATION")
                    .setBold()
                    .setFontSize(12);
            document.add(patientHeader);

            Paragraph patientInfo = new Paragraph()
                    .add("Name: " + billingDetail.getPatientName())
                    .add("\nEMBG: " + billingDetail.getPatientEmbg())
                    .add("\nPhone: " + billingDetail.getPatientPhone())
                    .setFontSize(10);
            document.add(patientInfo);

            document.add(new Paragraph("\n"));

            // Services Table
            Paragraph servicesHeader = new Paragraph("ITEMIZED SERVICES")
                    .setBold()
                    .setFontSize(12);
            document.add(servicesHeader);

            // Create table with 2 columns
            Table table = new Table(new float[]{5, 2});

            // Table header
            Cell descriptionHeader = new Cell().add(new Paragraph("Description").setBold());
            Cell costHeader = new Cell().add(new Paragraph("Cost").setBold());
            table.addCell(descriptionHeader);
            table.addCell(costHeader);

            // Add procedures
            if (billingDetail.getProcedures() != null && !billingDetail.getProcedures().isEmpty()) {
                Cell procedureCategory = new Cell(1, 2).add(new Paragraph("PROCEDURES").setBold());
                table.addCell(procedureCategory);

                for (BillingItemDTO procedure : billingDetail.getProcedures()) {
                    table.addCell(new Cell().add(new Paragraph(procedure.getDescription())));
                    table.addCell(new Cell().add(new Paragraph("$" + procedure.getCost())));
                }
            }

            // Add lab tests
            if (billingDetail.getLabTests() != null && !billingDetail.getLabTests().isEmpty()) {
                Cell labTestCategory = new Cell(1, 2).add(new Paragraph("LAB TESTS").setBold());
                table.addCell(labTestCategory);

                for (BillingItemDTO labTest : billingDetail.getLabTests()) {
                    table.addCell(new Cell().add(new Paragraph(labTest.getDescription())));
                    table.addCell(new Cell().add(new Paragraph("$" + labTest.getCost())));
                }
            }

            // Total row
            Cell totalLabel = new Cell().add(new Paragraph("TOTAL:").setBold());
            Cell totalAmount = new Cell().add(new Paragraph("$" + billingDetail.getTotalCost())
                    .setBold()
                    .setFontSize(12));
            table.addCell(totalLabel);
            table.addCell(totalAmount);

            document.add(table);

            document.add(new Paragraph("\n"));

            // Payment Status
            Paragraph paymentStatus = new Paragraph()
                    .add("Payment Status: " + billingDetail.getPaymentStatus())
                    .setFontSize(11);
            if ("PAID".equals(billingDetail.getPaymentStatus())) {
                paymentStatus.add("\nPayment Date: " + formatDate(billingDetail.getPaymentDate()));
            }
            document.add(paymentStatus);

            document.add(new Paragraph("\n\n"));

            // Footer
            Paragraph footer = new Paragraph("Thank you for your business.")
                    .setFontSize(10);
            document.add(footer);

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }

    private static String formatDate(LocalDate date) {
        if (date == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }
}