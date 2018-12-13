package edu.boisestate.cs410.gradebook;

/**
 * @author Kepat, awlec
 */
public class Class {
    private int c_id;
    private String name;
    private String term;
    private int year;
    private String description;

    public Class(String name, String term, int year, String description){
        this.name = name;
        this.term = term;
        this.year = year;
        this.description = description;
    }

    public int getC_id() {
        return c_id;
    }

    public void setC_id(int c_id) {
        this.c_id = c_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void copy(Class copy){
        this.name = copy.getName();
        this.term = copy.getTerm();
        this.year = copy.getYear();
        this.description = copy.getDescription();
    }

    public String toString(){
        return "\n\n\tName: <" + name + ">\n\tterm: <" + term + ">\n\tyear: <" + year + ">\n\tdescription: <" + description + ">\n";
    }
}
