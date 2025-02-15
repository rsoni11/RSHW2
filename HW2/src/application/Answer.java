//Created by Rhea Soni
package application;

public class Answer {
    private int id;
    private int questionId;
    private String content;
    private String author;
    private boolean isSolution;

    public Answer(int id, int questionId, String content, String author, boolean isSolution) {
        this.id = id;
        this.questionId = questionId;
        this.content = content;
        this.author = author; // Ensure this is correctly assigned
        this.isSolution = isSolution;
    }
    
    public int getId() { return id; }
    public int getQuestionId() { return questionId; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public boolean isSolution() { return isSolution; }

    public void setContent(String content) { this.content = content; }
    public void setSolution(boolean solution) { isSolution = solution; }
}