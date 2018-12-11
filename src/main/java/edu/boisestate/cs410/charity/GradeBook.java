package edu.boisestate.cs410.charity;

import com.budhash.cliche.Command;
import com.budhash.cliche.ShellFactory;

import java.io.IOException;
import java.sql.*;

public class GradeBook {
    private final Connection db;

    public GradeBook(Connection cxn) {
        db = cxn;
    }

    public static void main(String[] args) throws IOException, SQLException {
        String dbUrl = args[0];
        try (Connection cxn = DriverManager.getConnection("jdbc:" + dbUrl)) {
            GradeBook shell = new GradeBook(cxn);
            ShellFactory.createConsoleShell("grades", "", shell)
                    .commandLoop();
        }
    }

// in:   new-class Nathaniel Spring 2006 1 desc
// not:  new-class Nathaniel Spring 2007 1 desc
    @Command
    public void newClass(String pName, String pTerm, int pYear, int pSection, String pDescription) throws SQLException {

        int c_id = classCreateOrId(pName, pTerm, pYear, pDescription);
        newSection(pSection, c_id);

    }

    @Command
    public int classCreateOrId(String pName, String pTerm, int pYear, String pDescription) throws SQLException {
        System.out.println("adding a new class");
        Boolean any = false;
        int c_id = -1;
        String queryCheck =
                "SELECT * from class where name='" + pName + "' and term='" + pTerm + "' and year='" + pYear + "'";
        try (PreparedStatement stmt = db.prepareStatement(queryCheck)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    any = true;
                    c_id = rs.getInt("c_id");
                }
                System.out.println("any matches? " + any );
            }
        }

        if(!any) {
            System.out.println("in the add");
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
                    alreadyASection = true;
                    sec_id  = rs.getInt("sec_id");
                }
                System.out.println("any matches? " + alreadyASection );
            }
        }

        if(!alreadyASection) {
            System.out.println("adding a new section");
            String query =
                    "insert into section (number, c_id) values (" + number + ", " + c_id + ")";

            try (PreparedStatement stmt = db.prepareStatement(query)) {
                stmt.executeUpdate();
            }
        }
    }

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
}