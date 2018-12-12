//-- select-class Thia Summer 1992 8
//-- add-item new project description 100
//
//
//--   select title, type, description, points, t_id from assignment
//--  join type USING(t_id)
//--  join section USING(sec_id)
//--  where sec_id='318' and type='project'
//--  order by(type);
//
//-- update item set recieved=38 from section where section.sec_id='"+activeSecId+"' and type='"+type+"';";
//-- Select * from section join type USING(sec_id)
//-- where section.sec_id='318' and type='test' and title='Senior Developer'
//
//-- update assignment
//-- set description='new!!', points=100
//-- from section join type USING(sec_id)
//-- where section.sec_id='318' and type='test' and title='Senior Developer';
//
//--  select title, type, description, points, t_id from assignment
//--  join type USING(t_id)
//--  join section USING(sec_id)
//--  where sec_id='318' and type='test' and title='Senior Developer'
//--  order by(type);
//
//
//--  select title, type, description, points, t_id from assignment
//--  join type USING(t_id)
//--  join section USING(sec_id)
//--  where sec_id='318' and type='test'
//--  order by(type);
//
//--  Select t_id from type Join section using(sec_id) where sec_id='318' and type='test' LIMIT 1;
//-- Select t_id from type Join section using(sec_id) where sec_id='318' and type='test' LIMIT 1
//--  insert into assignment (description, title, points, t_id)
//--  values ('desc', 'title', 49, 267);
//
//--  insert into assignment (description, title, points, t_id)
//--  Select 'desc', 'title', 48, t_id from type Join section using(sec_id)
//--  where sec_id='318' and type='test' LIMIT 1;
//
//-- select-class Thia Summer 1992 8
//-- add-item new project description 100
//
//
// select title, type, description, points, t_id from assignment
// join type USING(t_id)
// join section USING(sec_id)
// where sec_id='318' and type='project'
// order by(type);


package edu.boisestate.cs410.charity;

import com.budhash.cliche.Command;
import com.budhash.cliche.ShellFactory;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class GradeBook {
    private final Connection db;
    private static Class activeClass;
    private static Class prevClass;
    private static int activeSecId;
    private static int activeSecNum;

    public GradeBook(Connection cxn) {
        db = cxn;
    }

    public static void main(String[] args) throws IOException, SQLException {
        String dbUrl = args[0];
        activeSecId = -1;
        activeClass = new Class("name", "term", -1, "description");
        prevClass = new Class("name", "term", -1, "description");
//        prevClass.copy(activeClass);
        try (Connection cxn = DriverManager.getConnection("jdbc:" + dbUrl)) {
            GradeBook shell = new GradeBook(cxn);
            ShellFactory.createConsoleShell("grades", "", shell)
                    .commandLoop();
        }
    }

// in:   new-class Nathaniel Spring 2006 1 desc
// not:  new-class Nathaniel Spring 2007 1 desc
// not:  new-class Nathaniel Spring 2008 1 desc

//    select-class Nathaniel : Spring 2008 & success
//    select-class Nathaniel Spring 2007 : Spring 2007 & fail
//    select-class Nathaniel Fall 1995 86 & success


    //START CLASS---------------------------------------------------------------------------------------------------------
    @Command
    public void selectClass(String pName) throws SQLException {
        selectClass(pName, "None", -1, -1);
    }

    @Command
    public void selectClass(String pName, String pTerm, int pYear) throws SQLException {
        selectClass(pName, pTerm, pYear, -1);
    }

    @Command
    public void selectClass(String pName, String pTerm, int pYear, int sNumber) throws SQLException {
        int numSec = 0;
        int c_id = findClass(pName, pTerm, pYear);
        if (c_id == -1) {
            System.out.println("We didn't find a class");
            return; //aka we faild
        }
        numSec = selectSection(c_id, sNumber);
        if (numSec != 1 && sNumber == -1) {
            activeClass.copy(prevClass);
            return; //didn't find a single section
        }
        prevClass.copy(activeClass);
        System.out.println(activeClass.toString() + "\tsection: " + activeSecNum + "\n\tid:" + activeSecId);
    }

    @Command
    public void showClass() {
        if (activeClass.getYear() != -1) {
            System.out.println(activeClass.toString() + "\tsection: " + activeSecNum + "\n\tid:" + activeSecId);
        } else {
            System.out.println("No active class");
        }
    }

    @Command
    public int selectSection(int c_id) throws SQLException {
        return selectSection(c_id, -1);
    }


    @Command
    public int selectSection(int c_id, int sNumber) throws SQLException {
        int sec_id = -1;
        int numSec = 0;
        String queryCheck;
        if (sNumber == -1) {
            queryCheck =
                    "select * from section where c_id='" + c_id + "'";
        } else {
            queryCheck =
                    "select * from section where c_id='" + c_id + "' and number='" + sNumber + "'";
        }
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sec_id = rs.getInt("sec_id");
                    activeSecId = sec_id;
                    activeSecNum = rs.getInt("number");
                    numSec++;
                }
            }
        }
        return numSec;
    }

    // select-class Nathaniel
    @Command
    public int findClass(String pName) throws SQLException {
        return findClass(pName, "None", -1);
    }

    @Command
    public int findClass(String pName, String pTerm, int pYear) throws SQLException {
        int c_id = -1;
        int maxTerm = -1; // 0 = spring, 1 = summer,  2 = fall
        int currTerm = -1;
        int year = -1;
        String queryCheck;

        if (pTerm.equals("None")) {
            queryCheck =
                    " select * from class where name='" + pName + "'" +
                            " ORDER BY year DESC  LIMIT 3";
        } else {
            queryCheck =
                    " select * from class where name='" + pName + "'" +
                            " and term='" + pTerm + "'" +
                            " and year='" + pYear + "'" +
                            " ORDER BY year DESC  LIMIT 3";
        }
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (c_id == -1) {
                        year = rs.getInt("year");//first part of query returns oldest year
                    }
                    if (rs.getString("term").toLowerCase().equals("spring")) {
                        currTerm = 0;
                    } else if (rs.getString("term").toLowerCase().equals("summer")) {
                        currTerm = 1;
                    } else if (rs.getString("term").toLowerCase().equals("fall")) {
                        currTerm = 2;
                    }
                    if (maxTerm < currTerm && year <= rs.getInt("year")) { //if we find a new max
                        c_id = rs.getInt("c_id");
                        activeClass.setC_id(c_id);
                        activeClass.setName(rs.getString("name"));
                        activeClass.setTerm(rs.getString("term"));
                        activeClass.setYear(rs.getInt("year"));
                        activeClass.setDescription(rs.getString("description"));
                        maxTerm = currTerm;
                    }
                }
            }
        }
        return c_id;
    }

    @Command
    public void newClass(String pName, String pTerm, int pYear, int pSection, String pDescription) throws SQLException {
        int c_id = classCreateOrId(pName, pTerm, pYear, pDescription);
        newSection(pSection, c_id);
    }

    @Command
    public int classCreateOrId(String pName, String pTerm, int pYear, String pDescription) throws SQLException {
        Boolean any = false;
        int c_id = -1;
        String queryCheck =
                "SELECT * from class where name='" + pName + "' " +
                        "and term='" + pTerm + "' " +
                        "and year='" + pYear + "'";
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("Class already exists, selecting it");
                    any = true;
                    c_id = rs.getInt("c_id");
                    activeClass.setC_id(c_id);
                    activeClass.setName(rs.getString("name"));
                    activeClass.setTerm(rs.getString("term"));
                    activeClass.setYear(rs.getInt("year"));
                    activeClass.setDescription(rs.getString("description"));
                }
            }
        }

        if (!any) {
            System.out.println("adding a new class");
            String query =
                    "insert into class (name, term, year, description) \n" +
                            "values ('" + pName + "', '" + pTerm + "', " + pYear + ", '" + pDescription + "')";

            try (PreparedStatement stmt = db.prepareStatement(query)) {
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        c_id = rs.getInt("c_id");
                        activeClass.setC_id(c_id);
                        activeClass.setName(rs.getString("name"));
                        activeClass.setTerm(rs.getString("term"));
                        activeClass.setYear(rs.getInt("year"));
                        activeClass.setDescription(rs.getString("description"));
                    }
                }
            }
        }
        return c_id;
    }

    @Command
    public void newSection(int number, int c_id) throws SQLException {
        Boolean alreadyASection = false;
        int sec_id = -1;
        String queryCheck =
                "SELECT * from section where number='" + number + "'" +
                        " and c_id='" + c_id + "'";
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("A section already exists");
                    alreadyASection = true;
                    sec_id = rs.getInt("sec_id");
                    activeSecId = sec_id;
                    activeSecNum = rs.getInt("number");
                }
//                System.out.println("any matches? " + alreadyASection );
            }
        }

        if (!alreadyASection) {
            System.out.println("Adding a new section");
            String query =
                    "insert into section (number, c_id)" +
                            " values (" + number + ", " + c_id + ")";
            try (PreparedStatement stmt = db.prepareStatement(query)) {
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        sec_id = rs.getInt("sec_id");
                        activeSecId = sec_id;
                        activeSecNum = rs.getInt("number");
                    }
                }
            }

        }
    }


//END CLASS---------------------------------------------------------------------------------------------------------

    //START CATEGORIES---------------------------------------------------------------------------------------------------------
    // select-class Thia Summer 1992 8
    // add-item new project description 100
    // show-categories
    @Command
    public void showCategories() throws SQLException {
        if (activeClass.getYear() == -1) {
            System.out.println("No active class");
            return;
        }
        String queryCheck;
        String type;
        double weight;
        queryCheck =
                "select type, weight from type join section using(sec_id) " +
                        "where sec_id=" + activeSecId + "";

        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("TYPE\t\t\t|WEIGHT");
                System.out.println("========================");
                while (rs.next()) {
                    type = rs.getString("type");
                    weight = rs.getDouble("weight");
                    if (type.length() >= "extra credit".length()) {
                        System.out.println(type + "\t|" + weight * 100 + "%");
                    } else {
                        System.out.println(type + "\t\t\t|" + weight * 100 + "%");
                    }
                }
            }
        }
    }

    //  add-category a 1
    @Command
    public void addCategory(String type, double weight) throws SQLException {
        Boolean alreadyACat = false;
        int sec_id = -1;
        String queryCheck =
                "SELECT * from type where type='" + type + "'";
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("This category already exists, updating weight");
                    String updateQuery =
                            "update type set weight=" + weight + "" +
                                    " from section" +
                                    " where section.sec_id='" + activeSecId + "' " +
                                    "and type='" + type + "';";
                    alreadyACat = true;
                    System.out.print(updateQuery);
                    try (PreparedStatement s = db.prepareStatement(updateQuery)) {
                        s.executeUpdate();
                    }
                }
            }
        }

        if (!alreadyACat) {
            System.out.println("Adding a new category");
            String query =
                    "insert into type (type, weight, sec_id)" +
                            " values ('" + type + "', " + weight + ", " + activeSecId + ");";
            try (PreparedStatement stmt = db.prepareStatement(query)) {
                stmt.executeUpdate();
            }
        }
    }

    @Command
    public void showItems() throws SQLException {
        if (activeClass.getYear() == -1) {
            System.out.println("No active class");
            return;
        }
        String queryCheck;
        String type;
        String title;
        int points;

        queryCheck =
                "select type, title, points from assignment" +
                        " join type USING(t_id)" +
                        " join section USING(sec_id) where sec_id='" + activeSecId + "'" +
                        " order by(type)";


        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\tcategory\t|\ttitle\t|\tpoints");
                System.out.println("================================================");
                while (rs.next()) {
                    type = rs.getString("type");
                    title = rs.getString("title");
                    points = rs.getInt("points");
                    System.out.println(type + "\t\t|" + title + "\t\t|" + points);
                }
            }
        }
    }

    //The instructions are not clear on what each of these arguments is to be
    @Command
    public void addItem(String title, String type, String description, int points) throws SQLException {
        Boolean alreadyAnItem = false;
        int sec_id = -1;
        String queryCheck =
                "select title, type, description, points from assignment " +
                        "join type USING(t_id) " +
                        "join section USING(sec_id) " +
                        "where sec_id='" + activeSecId + "' " +
                        "and type='" + type + "' and title='" + title + "' " +
                        "order by(type)";
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("This item already exists, updating it instead");
                    String updateQuery =
                            "update assignment " +
                            "set description='" + description + "', points=" + points + " " +
                            "from section join type USING(sec_id) " +
                            "where section.sec_id='" + activeSecId + "' " +
                            "and type='" + type + "' and title='" + title + "' ";
                    alreadyAnItem = true;
                    try (PreparedStatement s = db.prepareStatement(updateQuery)) {
                        s.executeUpdate();
                    }
                }
            }
        }

        if (!alreadyAnItem) {
            System.out.println("Adding a new item");
            String query =
                    "insert into assignment (description, title, points, t_id) " +
                            "Select '" + description + "', '" + title + "', " + points + ", " +
                            "t_id from type Join section using(sec_id) " +
                            "where sec_id='" + activeSecId + "' " +
                            "and type='" + type + "' LIMIT 1";
//            System.out.println(query);
            try (PreparedStatement stmt = db.prepareStatement(query)) {
                stmt.executeUpdate();
            }
        }
    }
// END CATEGORIES---------------------------------------------------------------------------------------------------------

    // START STUDENTS---------------------------------------------------------------------------------------------------------
//    select-class Thia Summer 1992 8
    @Command
    public void newStudent(String username, int stuId, String name) throws SQLException {
        Boolean alreadyIsStudent = false;
        String queryCheck = "Select * from student join enrolled using (stu_id) join section using (sec_id) Where stu_id = '"+stuId+"'";
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alreadyIsStudent = true;
                }
            }
        }
        Boolean alreadyInSection = false;
        if (alreadyIsStudent) {
            String queryCheck2 = "Select * from student join enrolled using (stu_id) join section using (sec_id) Where stu_id = '"+stuId+"' AND sec_id = '"+activeSecId+"'";
            try (PreparedStatement stmt = db.prepareStatement(queryCheck2)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        alreadyInSection = true;
                    }
                }
            }
        } else {
            String[] fullName;
            fullName = name.split(", ");
            String query = "insert into student (stu_id, f_name, l_name, username) values ('" + stuId + "', '" + fullName[0] + "', '" + fullName[1] + "','" + username + "')";
            try (PreparedStatement stmt = db.prepareStatement(query)) {
                stmt.executeUpdate();
            }
        }
        if (!alreadyInSection) {
            String insertQuery = "Insert into enrolled (stu_id,sec_id) values (stuId,activeSecId)";
            try (PreparedStatement stmt = db.prepareStatement(insertQuery)) {
                stmt.executeUpdate();
            }
        }
    }

    @Command
    public void showStudents() throws SQLException {
        showStudents("");
    }

    @Command
    public void showStudents(String search) throws SQLException {
        if (activeClass.getYear() == -1) {
            System.out.println("No active class");
            return;
        }
        String fName = "";
        String lName = "";
        String uName = "";
        int stuId = -1;
        String query;
        if (search.equals("")) {
            query =
                    "SELECT * from student join enrolled using(stu_id) join section using(sec_id) where sec_id='"+activeSecId+"'";
        } else {
            query =
                    "SELECT * from student join enrolled using(stu_id) join section using(sec_id) WHERE (f_name ILIKE ('%' ||'\" + search  + \"'|| '%') OR l_name ILIKE ('%' ||'\" + search  + \"'|| '%')username ILIKE ('%' ||'" + search + "'|| '%')) and sec_id='"+activeSecId+"' ORDER BY l_name";
        }
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fName = rs.getString("f_name");
                    lName = rs.getString("l_name");
                    uName = rs.getString("username");
                    stuId = rs.getInt("stu_id");
                    System.out.printf("%d, %s (%s, %s)%n", stuId, uName, lName, fName);
                }
            }
        }
    }


//   stu: 839 a: 871
//    username vfantinina
//    grade new vfantinina 100
    @Command
    public void grade(String title, String username, int points) throws SQLException {
        Boolean alreadyAGrade = false;
        int sec_id = -1;
        String queryCheck =
                "select title, type, description, recieved from assignment " +
                "join type USING(t_id) " +
                "join section USING(sec_id) " +
                "join grade USING(a_id) " +
                "where sec_id='" + activeSecId + "' and title='" + title + "' " +
                "order by(type) ";

        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("This grade already exists, updating it instead");
                    String updateQuery =
                            "update grade set recieved="+points+" "+
                            "where g_id in (select g_id from assignment "+
                            "join type using(t_id) "+
                            "join section using(sec_id) "+
                            "join grade using(a_id) "+
                            "join student using(stu_id) "+
                            "where sec_id='"+activeSecId+"' and title='"+title+"' and username='"+username+"')";
//                    System.out.println(updateQuery);
//                            "update grade " +
//                            "set points=" + points + " " +
//                            "from section join type USING(sec_id) " +
//                            "where section.sec_id='" + activeSecId + "' and title='" + title + "' and username='" + username + "' ";
                    alreadyAGrade = true;
                    try (PreparedStatement s = db.prepareStatement(updateQuery)) {
                        s.executeUpdate();
                    }
                }
            }
        }

        if (!alreadyAGrade) {
            System.out.println("Adding a new item");
            String query =
                "insert into grade (stu_id, a_id, recieved) "+
                "Select stu_id, a_id, "+points+" from assignment "+
                "join type using(t_id) "+
                "join section using(sec_id) "+
                "join enrolled using(sec_id) "+
                "join student using(stu_id) "+
                "where sec_id='"+activeSecId+"' and title='"+title+"' and username='"+username+"' "+
                "LIMIT 1 ";

            System.out.print(query);
            try (PreparedStatement stmt = db.prepareStatement(query)) {
                stmt.executeUpdate();
            }
        }
    }

// END STUDENTS---------------------------------------------------------------------------------------------------------

// START REPORT---------------------------------------------------------------------------------------------------------
@Command
public void studentGrades(String username) throws SQLException {
    if (activeClass.getYear() == -1) {
        System.out.println("No active class");
        return;
    }

//    ArrayList<ArrayList<String>> types = new ArrayList<>();
    String pType = "";
    String query;
    String type;
    String title;
    String recieved;
    int pRec;
    int points;

    int stu_id = -1;

    query =
            "select stu_id from student " +
            "join enrolled using(stu_id) " +
            "join section using(sec_id) " +
            "where username='"+username+"' and sec_id='"+activeSecId+"'";



    try (PreparedStatement stmt = db.prepareStatement(query)) {
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
//                System.out.println("Found student in this class!");
                stu_id = rs.getInt("stu_id");
            }
        }
    }

    if(stu_id != -1){//if we found a matching user
        System.out.println("\nGrades for " + username + " in class " + activeClass.getName() + ":");
        query =
                "select username, type, title, recieved, points from assignment " +
                "join type using(t_id) " +
                "join enrolled using(sec_id) " +
                "join student using(stu_id) " +
                "left join grade using(a_id) " +
                "where username='"+username+"' and sec_id="+activeSecId+" " +
                "Order by type";

//                System.out.println(query);
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    type = rs.getString("type");
                    if(!pType.equals(type)){
                        System.out.println("\n" + type + ":");
                        System.out.printf("\t%-22s%-22s%-22s\n","Name","Recieved","Possible");
                        pType = type;
                    }
                    title = rs.getString("title");
                    points = rs.getInt("points");
                    recieved = rs.getString("recieved");

//                    System.out.println(username + ", " + type + ", " + title + ", "+recieved+", " + points);
                    if(recieved == null){
//                        System.out.println(title + ", NULL, " + points);
                        System.out.printf("\t%-22s%-22s%-22s\n",title,"NULL",points);
                    }else{
                        pRec = Integer.parseInt(recieved);
//                        System.out.println(title + ", " + pRec + ", " + points);
                        System.out.printf("\t%-22s%-22s%-22s\n",title,"NULL",points);
                    }


                }
            }
        }
//        gradeTotal("username");
    }else{
        System.out.println("No user " + username + " found in class " + activeClass.getName());
    }
}

    @Command
    public void gradebook(){
        if (activeClass.getYear() == -1) {
            System.out.println("No active class");
            return;
        }


        String query;
        query =
                "SELECT * from student join enrolled using(stu_id) join section using(sec_id) where sec_id='"+activeSecId+"'";

    }

    @Command
    public void gradeTotal(String username){

    }

// END REPORT---------------------------------------------------------------------------------------------------------
    @Command
    public void s() throws SQLException {
        //    username vfantinina
        selectClass("Thia", "Summer", 1992, 8);
        addItem("should","test", "desc",200);
        //    grade new vfantinina 100
//        grade("new","vfantinina",100);
        showStudents();
        //vfantinina
        studentGrades("vfantinina");
    }

}