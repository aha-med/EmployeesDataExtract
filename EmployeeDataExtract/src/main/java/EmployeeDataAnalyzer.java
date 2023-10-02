import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EmployeeDataAnalyzer {
    public static void main(String[] args) {
        String excelFilePath = "C:/Users/badhu/Downloads/Assignment_Timecard.xlsx";

        try (FileInputStream excelFile = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(excelFile)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming the data is on the first sheet

            List<EmployeeRecord> employeeRecords = parseEmployeeData(sheet);

            // Analyze and print employees who meet the criteria
            analyzeAndPrintEmployees(employeeRecords);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<EmployeeRecord> parseEmployeeData(Sheet sheet) {
        List<EmployeeRecord> employeeRecords = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                // Skip the header row
                continue;
            }

            String positionID = row.getCell(0).getStringCellValue();
            String positionStatus = row.getCell(1).getStringCellValue();
            Date timeIn;
            Date timeOut;
            try {
                Cell timeInCell = row.getCell(2);
                Cell timeOutCell = row.getCell(3);

                if (timeInCell == null || timeOutCell == null) {
                    // Skip rows with empty date cells
                    continue;
                }

                if (timeInCell.getCellType() == CellType.STRING) {
                    String timeInStr = timeInCell.getStringCellValue().trim(); // Trim leading/trailing spaces
                    if (!timeInStr.isEmpty()) {
                        timeIn = dateFormat.parse(timeInStr);
                    } else {
                        // Handle empty date cell
                        continue;
                    }
                } else if (DateUtil.isCellDateFormatted(timeInCell)) {
                    timeIn = timeInCell.getDateCellValue();
                } else {
                    // Handle the case when the cell doesn't contain a valid date
                    // You can log a message or take appropriate action here
                    continue; // Skip rows with invalid date formats
                }

                if (timeOutCell.getCellType() == CellType.STRING) {
                    String timeOutStr = timeOutCell.getStringCellValue().trim();
                    if (!timeOutStr.isEmpty()) {
                        timeOut = dateFormat.parse(timeOutStr);
                    } else {
                        // Handle empty date cell
                        continue;
                    }
                } else if (DateUtil.isCellDateFormatted(timeOutCell)) {
                    timeOut = timeOutCell.getDateCellValue();
                } else {
                    // Handle the case when the cell doesn't contain a valid date
                    // You can log a message or take appropriate action here
                    continue; // Skip rows with invalid date formats
                }

            } catch (ParseException e) {
                e.printStackTrace();
                continue; // Skip rows with invalid date formats
            }


            double timecardHours = parseTimeToHours(row.getCell(4).getStringCellValue());

            // Parse payCycleStartDate and payCycleEndDate columns
            Date payCycleStartDate;
            Date payCycleEndDate;
            try {
                Cell payCycleStartDateInCell=row.getCell(5);
                Cell payCycleEndDateInCell=row.getCell(6);
               if(payCycleStartDateInCell.getCellType()==CellType.STRING) {
                   payCycleStartDate = dateFormat.parse(row.getCell(6).getStringCellValue());
               }
               else if(DateUtil.isCellDateFormatted(payCycleStartDateInCell)){
                   payCycleStartDate=payCycleStartDateInCell.getDateCellValue();
               }
               else{
                   continue;
               }


                if(payCycleEndDateInCell.getCellType()==CellType.STRING) {
                    payCycleEndDate = dateFormat.parse(row.getCell(6).getStringCellValue());
                }
                else if(DateUtil.isCellDateFormatted(payCycleEndDateInCell)){
                    payCycleEndDate=payCycleEndDateInCell.getDateCellValue();
                }
                else{
                    continue;
                }

            } catch (ParseException e) {
                e.printStackTrace();
                continue; // Handle invalid date formats or empty date cells here
            }

            String employeeName = row.getCell(7).getStringCellValue();
            String fileNumber = row.getCell(8).getStringCellValue();

            EmployeeRecord employeeRecord = new EmployeeRecord(positionID, positionStatus, timeIn, timeOut,
                    timecardHours, payCycleStartDate, payCycleEndDate, employeeName, fileNumber);
            employeeRecords.add(employeeRecord);
        }
        return employeeRecords;
    }


    private static double parseTimeToHours(String timeStr) {
        String[] parts = timeStr.split(":");
        if (parts.length == 2) {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours + (minutes / 60.0);
        }
        return 0.0;
    }

    private static void analyzeAndPrintEmployees(List<EmployeeRecord> employeeRecords) {
        for (int i = 0; i < employeeRecords.size(); i++) {
            EmployeeRecord currentEmployee = employeeRecords.get(i);
            if (meetsConsecutiveDays(employeeRecords, i, 7) ||
                    meetsShiftTimeGap(employeeRecords, i, 1.0, 10.0) ||
                    currentEmployee.getTimecardHours() > 14.0) {
                System.out.println("Employee Name: " + currentEmployee.getEmployeeName() +
                        ", Position: " + currentEmployee.getPositionID());
            }
        }
    }

    private static boolean meetsConsecutiveDays(List<EmployeeRecord> employeeRecords, int index, int days) {
        if (index < days - 1) {
            return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance();
        Date endDate = employeeRecords.get(index).getPayCycleEndDate();
        calendar.setTime(endDate);
        calendar.add(Calendar.DAY_OF_MONTH, -days);

        Date startDate = calendar.getTime();
        String startDateStr = dateFormat.format(startDate);

        for (int i = index; i >= index - days + 1; i--) {
            if (!dateFormat.format(employeeRecords.get(i).getPayCycleEndDate()).equals(startDateStr)) {
                return false;
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            startDate = calendar.getTime();
            startDateStr = dateFormat.format(startDate);
        }

        return true;
    }

    private static boolean meetsShiftTimeGap(List<EmployeeRecord> employeeRecords, int index,
                                             double minHours, double maxHours) {
        if (index > 0) {
            EmployeeRecord currentEmployee = employeeRecords.get(index);
            EmployeeRecord previousEmployee = employeeRecords.get(index - 1);
            double hoursBetweenShifts = (currentEmployee.getTimeIn().getTime() -
                    previousEmployee.getTimeOut().getTime()) / (1000.0 * 60 * 60);
            return hoursBetweenShifts > minHours && hoursBetweenShifts < maxHours;
        }
        return false;
    }
}

