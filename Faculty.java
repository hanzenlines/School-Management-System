import java.util.ArrayList;
import java.util.List;

public class Faculty extends Account {

    private String employeeId;
    private String department;
    private List<String> assignedSubjects;

    public Faculty(String accountId, String password, String employeeId, String department) {
        super(accountId, password);
        this.employeeId = employeeId;
        this.department = department;
        this.assignedSubjects = new ArrayList<>();
    }

    public void addSubject(String subject) {
        assignedSubjects.add(subject);
        System.out.println("  [OK] Subject added: " + subject);
    }

    public void removeSubject(String subject) {
        if (assignedSubjects.remove(subject)) {
            System.out.println("  [OK] Subject removed: " + subject);
        } else {
            System.out.println("  [ERROR] Subject not found.");
        }
    }

    public void viewSubjects() {
        if (assignedSubjects.isEmpty()) {
            System.out.println("  No assigned subjects.");
        } else {
            System.out.println("  Assigned subjects:");
            for (int i = 0; i < assignedSubjects.size(); i++) {
                System.out.println("    " + (i + 1) + ". " + assignedSubjects.get(i));
            }
        }
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public List<String> getAssignedSubjects() {
        return assignedSubjects;
    }

    @Override
    public String toString() {
        return super.toString() + "\n" +
               "  Employee ID : " + employeeId + "\n" +
               "  Department  : " + department + "\n" +
               "  Subjects    : " + assignedSubjects;
    }
}