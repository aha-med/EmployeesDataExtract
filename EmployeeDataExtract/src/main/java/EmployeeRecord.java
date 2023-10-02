import java.util.Date;

class EmployeeRecord {
    private String positionID;
    private String positionStatus;
    private Date timeIn;
    private Date timeOut;
    private double timecardHours;
    private Date payCycleStartDate;
    private Date payCycleEndDate;
    private String employeeName;
    private String fileNumber;

    public EmployeeRecord(String positionID, String positionStatus, Date timeIn, Date timeOut,
                          double timecardHours, Date payCycleStartDate, Date payCycleEndDate,
                          String employeeName, String fileNumber) {
        this.positionID = positionID;
        this.positionStatus = positionStatus;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.timecardHours = timecardHours;
        this.payCycleStartDate = payCycleStartDate;
        this.payCycleEndDate = payCycleEndDate;
        this.employeeName = employeeName;
        this.fileNumber = fileNumber;
    }

    public String getPositionID() {
        return positionID;
    }

    public String getPositionStatus() {
        return positionStatus;
    }

    public Date getTimeIn() {
        return timeIn;
    }

    public Date getTimeOut() {
        return timeOut;
    }

    public double getTimecardHours() {
        return timecardHours;
    }

    public Date getPayCycleStartDate() {
        return payCycleStartDate;
    }

    public Date getPayCycleEndDate() {
        return payCycleEndDate;
    }

    public String getEmployeeName() {
        return employeeName;
    }
}