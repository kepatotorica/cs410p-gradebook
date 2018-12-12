package edu.boisestate.cs410.charity;

import com.budhash.cliche.Command;
import com.budhash.cliche.ShellFactory;

import java.io.IOException;
import java.sql.*;

public class GradeBook {
    private final Connection db;
    private static Class activeClass;
    private static Class prevClass;
    private int activeSecId;
    private int activeSecNum;
    public GradeBook(Connection cxn) {
        db = cxn;
    }

    public static void main(String[] args) throws IOException, SQLException {
        String dbUrl = args[0];
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
        if(c_id == -1){
            System.out.println("We didn't find a class");
            return; //aka we faild
        }
        numSec = selectSection(c_id, sNumber);
            if (numSec != 1 && sNumber == -1) {
                activeClass.copy(prevClass);
                return; //didn't find a single section
            }
        prevClass.copy(activeClass);
        System.out.println(activeClass.toString() + " section: " + activeSecNum);
    }

    @Command
    public void showClass(){
        if(activeClass.getYear() != -1) {
            System.out.println(activeClass.toString() + " section: " + activeSecNum);
        }else{
            System.out.println("No active class");
        }
    }

    @Command
    public int selectSection(int c_id) throws SQLException {
        return selectSection(c_id, -1);
    }


    @Command
    public int selectSection(int c_id , int sNumber) throws SQLException {
        int sec_id = -1;
        int numSec = 0;
        String queryCheck;
        if(sNumber == -1) {
            queryCheck =
                    "select * from section where c_id='" + c_id + "'";
        }else{
            queryCheck =
                    "select * from section where c_id='" + c_id + "' and number='" + sNumber + "'";
        }
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sec_id  = rs.getInt("sec_id");
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

        if(pTerm.equals("None")) {
            queryCheck =
                    " select * from class where name='" + pName + "' ORDER BY year DESC  LIMIT 3";
        }else{
            queryCheck =
                    " select * from class where name='" + pName + "' and term='" + pTerm + "' and year='" + pYear + "' ORDER BY year DESC  LIMIT 3";
        }
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if(c_id == -1){
                        year = rs.getInt("year");//first part of query returns oldest year
                    }
                    if(rs.getString("term").toLowerCase().equals("spring")){
                        currTerm = 0;
                    }else if(rs.getString("term").toLowerCase().equals("summer")){
                        currTerm = 1;
                    }else if(rs.getString("term").toLowerCase().equals("fall")){
                        currTerm = 2;
                    }
                    if(maxTerm < currTerm && year <= rs.getInt("year")) { //if we find a new max
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
                "SELECT * from class where name='" + pName + "' and term='" + pTerm + "' and year='" + pYear + "'";
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

        if(!any) {
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
                "SELECT * from section where number='" + number + "' and c_id='" + c_id + "'";
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("A section already exists");
                    alreadyASection = true;
                    sec_id  = rs.getInt("sec_id");
                    activeSecId = sec_id;
                    activeSecNum = rs.getInt("number");
                }
//                System.out.println("any matches? " + alreadyASection );
            }
        }

        if(!alreadyASection) {
            System.out.println("Adding a new section");
            String query =
                    "insert into section (number, c_id) values (" + number + ", " + c_id + ")";
            try (PreparedStatement stmt = db.prepareStatement(query)) {
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        sec_id  = rs.getInt("sec_id");
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
    // show-categories
    @Command
    public void showCategories() throws SQLException {
        String queryCheck;
        String type;
        double weight;
            queryCheck =
                    "select type, weight from type join section using(sec_id) where sec_id="+activeSecId+"";

        System.out.println("TYPE\t\t\t|WEIGHT");
        System.out.println("========================");
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    type = rs.getString("type");
                    weight = rs.getDouble("weight");
                    if(type.length() >= "extra credit".length()) {
                        System.out.println(type + "\t|" + weight * 100 + "%");
                    }else{
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
                "SELECT * from type where type='"+ type +"'";
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("This category already exists, updating weight");
                    String updateQuery = "update type set weight="+weight+" from section where section.sec_id='"+activeSecId+"' and type='"+type+"';";
                    alreadyACat = true;
                    System.out.print(updateQuery);
                    try (PreparedStatement s = db.prepareStatement(updateQuery)) {
                        s.executeUpdate();
                    }
                }
            }
        }

        if(!alreadyACat) {
            System.out.println("Adding a new category");
            String query =
                    "insert into type (type, weight, sec_id) values ('"+type+"', "+weight+", "+activeSecId+");";
            try (PreparedStatement stmt = db.prepareStatement(query)) {
                stmt.executeUpdate();
            }
        }
    }

    @Command
    public void showItems() throws SQLException {
        String queryCheck;
        String type;
        String weight;
        int tot_points;
        int rec_points;

        queryCheck =
                "select type, weight from type join section using(sec_id) where sec_id="+activeSecId+"";

        System.out.println("TYPE\t\t\t|WEIGHT");
        System.out.println("========================");
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
//                    type = rs.getString("type");
//                    weight = rs.getDouble("weight");
//                        System.out.println(type + "\t\t\t|" + weight * 100 + "%");
                }
            }
        }
    }
//select type as category, title, rec_points, tot_points  from assignment join type USING(t_id) join section USING(sec_id) where sec_id='318'
//order by(type)
    @Command
    public void addItem(String type, double weight) throws SQLException {
        Boolean alreadyACat = false;
        int sec_id = -1;
        String queryCheck =
                "SELECT * from type where type='"+ type +"'";
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("This item already exists, updating weight");
                    String updateQuery = "update type set weight="+weight+" from section where section.sec_id='"+activeSecId+"' and type='"+type+"';";
                    alreadyACat = true;
                    System.out.print(updateQuery);
                    try (PreparedStatement s = db.prepareStatement(updateQuery)) {
                        s.executeUpdate();
                    }
                }
            }
        }

        if(!alreadyACat) {
            System.out.println("Adding a new item");
            String query =
                    "insert into type (type, weight, sec_id) values ('"+type+"', "+weight+", "+activeSecId+");";
            try (PreparedStatement stmt = db.prepareStatement(query)) {
                stmt.executeUpdate();
            }
        }
    }
// END CATEGORIES---------------------------------------------------------------------------------------------------------

// START STUDENTS---------------------------------------------------------------------------------------------------------
    @Command
    public void newStudent(String username, int stuId, String name) throws SQLException {
        Boolean alreadyIsStudent = false;
        String queryCheck = "Select * from student join enrolled using (stu_id) join section using (sec_id) Where stu_id = 'stuId'";
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alreadyIsStudent = true;
                }
            }
        }
        Boolean alreadyInSection = false;
        if (alreadyIsStudent){
            String queryCheck2 = "Select * from student join enrolled using (stu_id) join section using (sec_id) Where stu_id = 'stuId' AND sec_id = 'activeSecId'";
            try (PreparedStatement stmt = db.prepareStatement(queryCheck2)){
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()){
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
        if (!alreadyInSection){
            String insertQuery = "Insert into enrolled (stu_id,sec_id) values (stuId,activeSecId)";
            try (PreparedStatement stmt = db.prepareStatement(insertQuery)){
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
        String fName = "";
        String lName = "";
        String query;
        if(search.equals("")){
            query =
                "SELECT * from student";
        }else{
            query =
                    "SELECT * from student WHERE f_name ILIKE ('%' ||'\" + search  + \"'|| '%') OR l_name ILIKE ('%' ||'\" + search  + \"'|| '%')username ILIKE ('%' ||'" + search  + "'|| '%') ORDER BY l_name";
        }
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fName = rs.getString("f_name");
                    lName = rs.getString("l_name");
                    System.out.printf("%s, %s%n", lName, fName);
                }
            }
        }
    }
}
// END STUDENTS---------------------------------------------------------------------------------------------------------

//SELECT * from student WHERE f_name LIKE searchQ OR l_name LIKE 'searchQ' OR username LIKE 'searchQ' ORDER BY l_name"
//    @Command
//    public void findDonor(String donorName) throws SQLException {
//        System.out.println("searching for author matching " + donorName);
//        String query =
//                "SELECT donor_id, donor_name, sum(amount) AS total " +
//                        "FROM gift JOIN gift_fund_allocation USING(gift_id) " +
//                        "JOIN donor USING(donor_id) " +
//                        "WHERE donor_name ILIKE ('%' ||'" + donorName  + "'|| '%') " +
//                        "GROUP BY donor_id, donor_name";
//        try (PreparedStatement stmt = db.prepareStatement(query)) {
//            try (ResultSet rs = stmt.executeQuery()) {
//                System.out.format("Donors matching %s:%n", donorName);
//                System.out.println("\t|------------------------------");
//                System.out.println("\t|Donor Name | Donor Id | Total");
//                System.out.println("\t|------------------------------");
//                while (rs.next()) {
//                    int aid = rs.getInt("donor_id");
//                    String name = rs.getString("donor_name");
//                    int amount = rs.getInt("total");
//                    System.out.format("\t|%s | %d | %d%n", name, aid, amount);
//                }
//            }
//        }
//    }
//
//    @Command
//    public void donorReport(String donorId, String year) throws SQLException {
//        totalGifts(donorId, year);
//        System.out.println();
//        fundsSupported(donorId, year);
//        System.out.println();
//        pastYears(donorId, year);
//    }
//
//    @Command
//    public void totalGifts (String donorId, String year) throws SQLException {
//        System.out.println("searching for gifts given during " + year);
//        String query =
//                "SELECT gift_id, gift_date, SUM(amount) as total, " +
//                        "COUNT(gift_id) as num_gifts_given FROM gift " +
//                        "JOIN gift_fund_allocation USING(gift_id) " +
//                        "JOIN donor USING(donor_id) " +
//                        "WHERE donor_id=" + donorId + " AND EXTRACT(year FROM gift_date)=" + year + " " +
//                        "GROUP BY gift_id, gift_date";
////	        "SELECT gift_id, gift_date, amount FROM gift " +
////	        "JOIN gift_fund_allocation USING(gift_id) " +
////	        "JOIN donor USING(donor_id) " +
////	        "WHERE donor_id=" + donorId + " AND EXTRACT(year FROM gift_date)=" + year + " GROUP BY gift_id, gift_date, amount";
//
//        try (PreparedStatement stmt = db.prepareStatement(query)) {
//            try (ResultSet rs = stmt.executeQuery()) {
//                System.out.println("\t|----------------------------------------------");
//                System.out.println("\t|Gift Id | Gift Date | Total | Num Gifts Given");
//                System.out.println("\t|----------------------------------------------");
//                while (rs.next()) {
//                    String giftId = rs.getString("gift_id");
//                    String giftDate = rs.getString("gift_date");
//                    int total = rs.getInt("total");
//                    int numGiftsGiven = rs.getInt("num_gifts_given");
//                    System.out.format("\t|%s | %s | %d | %d%n", giftId, giftDate, total, numGiftsGiven);
//
//                }
//            }
//        }
//    }
//
//    @Command
//    public void fundsSupported(String donorId, String year) throws SQLException {
//        System.out.println("searching for funds suppored during " + year);
//        String query =
//                "SELECT fund_name, sum(amount) AS total FROM gift " +
//                        "JOIN gift_fund_allocation USING(gift_id) " +
//                        "JOIN donor USING(donor_id) " +
//                        "JOIN fund USING(fund_id) " +
//                        "WHERE donor_id=" + donorId + " AND EXTRACT(year FROM gift_date)=" + year + " " +
//                        "GROUP BY fund_name";
//        try (PreparedStatement stmt = db.prepareStatement(query)) {
//            try (ResultSet rs = stmt.executeQuery()) {
//                System.out.println("\t|-------------------");
//                System.out.println("\t|Fund Name | Amount");
//                System.out.println("\t|-------------------");
//                while (rs.next()) {
//                    int amount = rs.getInt("total");
//                    String name = rs.getString("fund_name");
//                    System.out.format("\t|%s | %d%n", name, amount);
//                }
//            }
//        }
//    }
//
//    @Command
//    public void pastYears(String donorId, String year) throws SQLException {
//        System.out.println("searching for donations before " + year);
//        String query =
//                "SELECT EXTRACT(year FROM gift_date) AS year, SUM(amount) as total " +
//                        "FROM gift JOIN gift_fund_allocation USING(gift_id) " +
//                        "JOIN donor USING(donor_id) " +
//                        "WHERE donor_id=" + donorId + " AND EXTRACT(year FROM gift_date)<" + year + " " +
//                        "GROUP BY EXTRACT(year FROM gift_date)";
//        try (PreparedStatement stmt = db.prepareStatement(query)) {
//            try (ResultSet rs = stmt.executeQuery()) {
//                System.out.println("\t|--------------");
//                System.out.println("\t|Year | Amount");
//                System.out.println("\t|--------------");
//                while (rs.next()) {
//                    int yur = rs.getInt("year");
//                    int total = rs.getInt("total");
//                    System.out.format("\t|%s | %d%n", yur, total);
//                }
//            }
//        }
//    }
//
//    @Command
//    public void topDonors (String year) throws SQLException {
//        System.out.println("searching for the donors who gave the most during " + year);
//        String query =
//                "SELECT donor_name, SUM(amount) as total " +
//                        "FROM gift JOIN gift_fund_allocation USING(gift_id) " +
//                        "JOIN donor USING(donor_id) " +
//                        "WHERE EXTRACT(year FROM gift_date)=" + year + " " +
//                        "GROUP BY donor_name ORDER BY total DESC " +
//                        "LIMIT 10";
//
//
//        try (PreparedStatement stmt = db.prepareStatement(query)) {
//            try (ResultSet rs = stmt.executeQuery()) {
//                System.out.println("\t|-------------------");
//                System.out.println("\t|Donor Name | Total");
//                System.out.println("\t|-------------------");
//                while (rs.next()) {
//                    String name = rs.getString("donor_name");
//                    int total = rs.getInt("total");
//                    System.out.format("\t|%s | %d%n", name, total);
//
//                }
//            }
//        }
//    }
