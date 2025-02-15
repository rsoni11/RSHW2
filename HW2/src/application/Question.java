//Created by Rhea Soni
package application;

public class Question {
    private int id;
    private String title;
    private String description;
    private String author;
   

    public Question(int id, String title, String description, String author) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
      
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
  

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
   
    
    
}
