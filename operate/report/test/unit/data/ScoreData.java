package unit.data;

import java.math.BigDecimal;

public class ScoreData {

    public String courseName;
    
    public String studentName;
    
    public BigDecimal score;

    public ScoreData(String studentName, String courseName, BigDecimal score) {
        this.courseName = courseName;
        this.studentName = studentName;
        this.score = score;
    }
    
}
