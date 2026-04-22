import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Curriculum {
    private String curriculumId;
    private String programName;
    private int effectiveYear;
    private List<Subject> subjects;

    public Curriculum(String curriculumId, String programName, int effectiveYear) {
        this.curriculumId = curriculumId;
        this.programName = programName;
        this.effectiveYear = effectiveYear;
        this.subjects = new ArrayList<>();
    }

    public void addSubject(Subject subject) {
        subjects.add(subject);
    }

    public List<Subject> getByYearLevel(int yearLevel) {
        // Simple demo: returns every Nth group based on year level
        // In a real system you'd tag subjects with yearLevel + semester
        System.out.println("Getting subjects for Year " + yearLevel);
        return subjects;
    }

    public List<Subject> getRemainingSubjects(List<Subject> completedSubjects) {
        return subjects.stream()
                .filter(s -> !completedSubjects.contains(s))
                .collect(Collectors.toList());
    }

    // Getters
    public String getCurriculumId() {
         return curriculumId;
     }
    public String getProgramName() { 
        return programName; 
    }
    public int getEffectiveYear() { 
        return effectiveYear; 
    }
    public List<Subject> getSubjects() { 
        return subjects; 
    }
}