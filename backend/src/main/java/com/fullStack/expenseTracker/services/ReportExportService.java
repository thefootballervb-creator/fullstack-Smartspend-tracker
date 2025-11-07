package com.fullStack.expenseTracker.services;

import com.fullStack.expenseTracker.models.Transaction;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class ReportExportService {

    public byte[] toPdf(String title, List<Transaction> transactions) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);

            addHeader(table, "Date");
            addHeader(table, "Category");
            addHeader(table, "Description");
            addHeader(table, "Amount");

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            transactions.forEach(t -> {
                table.addCell(t.getDate() == null ? "" : fmt.format(t.getDate()));
                table.addCell(t.getCategory() == null ? "" : t.getCategory().getCategoryName());
                table.addCell(t.getDescription() == null ? "" : t.getDescription());
                table.addCell(String.valueOf(t.getAmount()));
            });

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF: " + e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private void addHeader(PdfPTable table, String text) {
        PdfPCell header = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        header.setPadding(5f);
        table.addCell(header);
    }

    public byte[] toExcel(String sheetName, List<Transaction> transactions) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet(sheetName);

            int rowIdx = 0;
            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Date");
            header.createCell(1).setCellValue("Category");
            header.createCell(2).setCellValue("Description");
            header.createCell(3).setCellValue("Amount");

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (Transaction t : transactions) {
                Row r = sheet.createRow(rowIdx++);
                Cell c0 = r.createCell(0);
                c0.setCellValue(t.getDate() == null ? "" : fmt.format(t.getDate()));
                r.createCell(1).setCellValue(t.getCategory() == null ? "" : t.getCategory().getCategoryName());
                r.createCell(2).setCellValue(t.getDescription() == null ? "" : t.getDescription());
                r.createCell(3).setCellValue(t.getAmount());
            }

            for (int i = 0; i < 4; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generating Excel: " + e.getMessage(), e);
            throw new RuntimeException("Failed to generate Excel: " + e.getMessage(), e);
        }
    }
}


