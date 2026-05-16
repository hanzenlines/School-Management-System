import java.util.HashMap;
import java.util.Map;

public class FacultyEvaluation {

    private String evalId;
    private String studentId;       // who submitted the eval
    private String teacherId;       // who is being evaluated
    private Map<String, Integer> ratings; 
    private String comments;
    private String semester;
    private boolean isAnonymous;


    // constructor
    public FacultyEvaluation(String evalId, String studentId, String teacherId,
                              String semester, boolean isAnonymous) {
        this.evalId = evalId;
        this.studentId = studentId;
        this.teacherId = teacherId;
        this.semester = semester;
        this.isAnonymous = isAnonymous;
        this.ratings = new HashMap<>();
        this.comments = "";
    }

  
    // methods

    // adds a rating category and score
    public void addRating(String category, int score) {
        ratings.put(category, score);
    }

    // gets average of all ratings
    public float getAverageScore() {
        if (ratings.isEmpty()) return 0;
        int total = 0;
        for (int score : ratings.values()) {
            total += score;
        }
        return (float) total / ratings.size();
    }

    public void displayInfo() {
        System.out.println("========================================");
        System.out.println("  FACULTY EVALUATION");
        System.out.println("========================================");
        System.out.println("  Eval ID   : " + evalId);
        System.out.println("  Teacher   : " + teacherId);
        System.out.println("  Student   : " + (isAnonymous ? "Anonymous" : studentId));
        System.out.println("  Semester  : " + semester);
        System.out.println("  Ratings   : " + ratings);
        System.out.println("  Average   : " + getAverageScore());
        System.out.println("  Comments  : " + (comments.isEmpty() ? "None" : comments));
        System.out.println("========================================");
    }

    // getters and Setters

    public String getEvalId() { return evalId; }

    public String getStudentId() { return studentId; }

    public String getTeacherId() { return teacherId; }

    public Map<String, Integer> getRatings() { return ratings; }

    public String getComments() { return comments; }

    public void setComments(String comments) { this.comments = comments; }

    public String getSemester() { return semester; }

    public boolean isAnonymous() { return isAnonymous; }

    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }
}
