package com.example.moneymap.automation.reporting;

import com.example.moneymap.automation.model.TestCase;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class ExcelReportGenerator {

    public static void generateReports(List<TestCase> testCases, String outputDirectory) {
        new File(outputDirectory).mkdirs();

        // 1. Generate master report
        generateMasterReport(testCases, outputDirectory + "/Automation_Test_Report.xlsx");

        // 2. Generate Passed Tests report
        generateStatusReport(testCases, "PASSED", outputDirectory + "/Passed_Test_Cases.xlsx");

        // 3. Generate Failed Tests report
        generateStatusReport(testCases, "FAILED", outputDirectory + "/Failed_Test_Cases.xlsx");

        // 4. Generate Execution Summary report
        generateSummaryReport(testCases, outputDirectory + "/Execution_Summary.xlsx");
    }

    private static void generateMasterReport(List<TestCase> testCases, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Colors
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle passedStyle = createStatusStyle(workbook, IndexedColors.GREEN, IndexedColors.WHITE);
            CellStyle failedStyle = createStatusStyle(workbook, IndexedColors.RED, IndexedColors.WHITE);
            CellStyle skippedStyle = createStatusStyle(workbook, IndexedColors.GOLD, IndexedColors.BLACK);

            // Sheet 1: All Executed Tests
            Sheet allSheet = workbook.createSheet("Executed Test Cases");
            writeTestHeaders(allSheet, headerStyle);
            int rowIdx = 1;
            for (TestCase tc : testCases) {
                Row row = allSheet.createRow(rowIdx++);
                writeTestRow(row, tc, passedStyle, failedStyle, skippedStyle);
            }
            autoSizeColumns(allSheet, 6);

            // Sheet 2: Passed Tests
            Sheet passedSheet = workbook.createSheet("Passed Tests");
            writeTestHeaders(passedSheet, headerStyle);
            rowIdx = 1;
            for (TestCase tc : testCases) {
                if ("PASSED".equalsIgnoreCase(tc.getStatus())) {
                    Row row = passedSheet.createRow(rowIdx++);
                    writeTestRow(row, tc, passedStyle, failedStyle, skippedStyle);
                }
            }
            autoSizeColumns(passedSheet, 6);

            // Sheet 3: Failed Tests
            Sheet failedSheet = workbook.createSheet("Failed Tests");
            writeTestHeaders(failedSheet, headerStyle);
            rowIdx = 1;
            for (TestCase tc : testCases) {
                if ("FAILED".equalsIgnoreCase(tc.getStatus())) {
                    Row row = failedSheet.createRow(rowIdx++);
                    writeTestRow(row, tc, passedStyle, failedStyle, skippedStyle);
                }
            }
            autoSizeColumns(failedSheet, 6);

            // Sheet 4: Skipped Tests
            Sheet skippedSheet = workbook.createSheet("Skipped Tests");
            writeTestHeaders(skippedSheet, headerStyle);
            rowIdx = 1;
            for (TestCase tc : testCases) {
                if ("SKIPPED".equalsIgnoreCase(tc.getStatus())) {
                    Row row = skippedSheet.createRow(rowIdx++);
                    writeTestRow(row, tc, passedStyle, failedStyle, skippedStyle);
                }
            }
            autoSizeColumns(skippedSheet, 6);

            // Sheet 5: Execution Metrics
            Sheet metricsSheet = workbook.createSheet("Execution Metrics");
            writeMetricsSheet(metricsSheet, testCases, workbook);

            // Sheet 6: Defect Summary
            Sheet defectsSheet = workbook.createSheet("Defect Summary");
            writeDefectsSheet(defectsSheet, testCases, headerStyle);

            // Sheet 7: Pass Rate Summary
            Sheet passRateSheet = workbook.createSheet("Pass Rate Summary");
            writePassRateSheet(passRateSheet, testCases, headerStyle);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            System.out.println("Excel master report generated at: " + filePath);
        } catch (Exception e) {
            System.err.println("Failed to generate master Excel report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateStatusReport(List<TestCase> testCases, String targetStatus, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle passedStyle = createStatusStyle(workbook, IndexedColors.GREEN, IndexedColors.WHITE);
            CellStyle failedStyle = createStatusStyle(workbook, IndexedColors.RED, IndexedColors.WHITE);
            CellStyle skippedStyle = createStatusStyle(workbook, IndexedColors.GOLD, IndexedColors.BLACK);

            Sheet sheet = workbook.createSheet(targetStatus + " Tests");
            writeTestHeaders(sheet, headerStyle);

            int rowIdx = 1;
            for (TestCase tc : testCases) {
                if (targetStatus.equalsIgnoreCase(tc.getStatus())) {
                    Row row = sheet.createRow(rowIdx++);
                    writeTestRow(row, tc, passedStyle, failedStyle, skippedStyle);
                }
            }
            autoSizeColumns(sheet, 6);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (Exception e) {
            System.err.println("Failed to generate " + targetStatus + " Excel report: " + e.getMessage());
        }
    }

    private static void generateSummaryReport(List<TestCase> testCases, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Summary");
            writeMetricsSheet(sheet, testCases, workbook);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (Exception e) {
            System.err.println("Failed to generate Summary Excel report: " + e.getMessage());
        }
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static CellStyle createStatusStyle(Workbook workbook, IndexedColors bgColor, IndexedColors fgColor) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(bgColor.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);

        Font font = workbook.createFont();
        font.setColor(fgColor.getIndex());
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static void writeTestHeaders(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Test ID", "Module", "Test Name", "Priority", "Status", "Execution Time (ms)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private static void writeTestRow(Row row, TestCase tc, CellStyle passed, CellStyle failed, CellStyle skipped) {
        row.createCell(0).setCellValue(tc.getTestId());
        row.createCell(1).setCellValue(tc.getModule());
        row.createCell(2).setCellValue(tc.getName());
        row.createCell(3).setCellValue(tc.getPriority());
        
        Cell statusCell = row.createCell(4);
        statusCell.setCellValue(tc.getStatus());
        if ("PASSED".equalsIgnoreCase(tc.getStatus())) {
            statusCell.setCellStyle(passed);
        } else if ("FAILED".equalsIgnoreCase(tc.getStatus())) {
            statusCell.setCellStyle(failed);
        } else if ("SKIPPED".equalsIgnoreCase(tc.getStatus())) {
            statusCell.setCellStyle(skipped);
        }
        
        row.createCell(5).setCellValue(tc.getDurationMs());
    }

    private static void writeMetricsSheet(Sheet sheet, List<TestCase> testCases, Workbook workbook) {
        int total = testCases.size();
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        long totalTime = 0;

        for (TestCase tc : testCases) {
            totalTime += tc.getDurationMs();
            if ("PASSED".equalsIgnoreCase(tc.getStatus())) passed++;
            else if ("FAILED".equalsIgnoreCase(tc.getStatus())) failed++;
            else if ("SKIPPED".equalsIgnoreCase(tc.getStatus())) skipped++;
        }

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBold(true);
        titleStyle.setFont(titleFont);

        Row title = sheet.createRow(0);
        title.createCell(0).setCellValue("E2E Execution Metrics");
        title.getCell(0).setCellStyle(titleStyle);

        String[] labels = {"Metric", "Value"};
        Row header = sheet.createRow(2);
        header.createCell(0).setCellValue(labels[0]);
        header.createCell(1).setCellValue(labels[1]);
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        header.getCell(0).setCellStyle(headerStyle);
        header.getCell(1).setCellStyle(headerStyle);

        String[][] data = {
            {"Total Test Cases", String.valueOf(total)},
            {"Passed", String.valueOf(passed)},
            {"Failed", String.valueOf(failed)},
            {"Skipped", String.valueOf(skipped)},
            {"Pass Percentage", String.format("%.2f%%", (double) passed / total * 100)},
            {"Total Execution Duration (ms)", String.valueOf(totalTime)}
        };

        for (int i = 0; i < data.length; i++) {
            Row r = sheet.createRow(3 + i);
            r.createCell(0).setCellValue(data[i][0]);
            r.createCell(1).setCellValue(data[i][1]);
        }
        autoSizeColumns(sheet, 2);
    }

    private static void writeDefectsSheet(Sheet sheet, List<TestCase> testCases, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Test ID", "Module", "Test Name", "Error Message", "Screenshot Path"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (TestCase tc : testCases) {
            if ("FAILED".equalsIgnoreCase(tc.getStatus())) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(tc.getTestId());
                row.createCell(1).setCellValue(tc.getModule());
                row.createCell(2).setCellValue(tc.getName());
                row.createCell(3).setCellValue(tc.getActualResult());
                row.createCell(4).setCellValue(tc.getScreenshotPath());
            }
        }
        autoSizeColumns(sheet, 5);
    }

    private static void writePassRateSheet(Sheet sheet, List<TestCase> testCases, CellStyle headerStyle) {
        Map<String, int[]> moduleCounts = new HashMap<>(); // [total, passed]

        for (TestCase tc : testCases) {
            moduleCounts.putIfAbsent(tc.getModule(), new int[2]);
            int[] c = moduleCounts.get(tc.getModule());
            c[0]++; // total
            if ("PASSED".equalsIgnoreCase(tc.getStatus())) {
                c[1]++; // passed
            }
        }

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Module", "Total Tests", "Passed Tests", "Pass Rate"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Map.Entry<String, int[]> entry : moduleCounts.entrySet()) {
            Row row = sheet.createRow(rowIdx++);
            int[] c = entry.getValue();
            double rate = c[0] > 0 ? (double) c[1] / c[0] * 100 : 0.0;
            
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(c[0]);
            row.createCell(2).setCellValue(c[1]);
            row.createCell(3).setCellValue(String.format("%.2f%%", rate));
        }
        autoSizeColumns(sheet, 4);
    }

    private static void autoSizeColumns(Sheet sheet, int colCount) {
        for (int i = 0; i < colCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
