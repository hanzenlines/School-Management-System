import java.util.ArrayList;
import java.util.List;

public class Subject {
    private String subjectCode;
    private String title;
    private int units;
    private List<Subject> prerequisites;
    private String description;

    public Subject(String subjectCode, String title, int units, String description) {
        this.subjectCode = subjectCode;
        this.title = title;
        this.units = units;
        this.description = description;
        this.prerequisites = new ArrayList<>();
    }

    public void addPrerequisite(Subject subject) {
        prerequisites.add(subject);
    }

    public List<Subject> getPrerequisites() {
        return prerequisites;
    }

    public boolean checkEligibility(List<Subject> completedSubjects) {
        return completedSubjects.containsAll(prerequisites);
    }

    // Getters
    public String getSubjectCode() { return subjectCode; }
    public String getTitle() { return title; }
    public int getUnits() { return units; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return subjectCode + " - " + title + " (" + units + " units)";
    }
}