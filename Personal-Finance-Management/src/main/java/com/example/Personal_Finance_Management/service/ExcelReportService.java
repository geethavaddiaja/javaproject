package com.example.Personal_Finance_Management.service;

import com.example.Personal_Finance_Management.ExpenceRepository.TransactionRepo;
import com.example.Personal_Finance_Management.entity.Transaction;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ExcelReportService {

    @Autowired
    TransactionRepo transactionRepo;

    public void generateExcelReport(HttpServletResponse response) throws IOException {

        List<Transaction> transactions = transactionRepo.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Transactions");


        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Type", "Amount", "Category", "Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(createHeaderCellStyle(workbook));
        }


        int rowIndex = 1;
        for (Transaction transaction : transactions) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(transaction.getId());
            row.createCell(1).setCellValue(transaction.getType());
            row.createCell(2).setCellValue(transaction.getAmount().doubleValue());
            row.createCell(3).setCellValue(transaction.getCategory());
            row.createCell(4).setCellValue(transaction.getDate().toString());
        }


        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=transactions.xlsx");


        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();


        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        return style;
    }
}
