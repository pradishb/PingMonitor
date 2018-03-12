package pingmonitor;

import javax.xml.transform.Result;
import java.sql.*;

public class SQLiteJDBCDriverConnection {
    private static PreparedStatement insertPingStmt;
    private static PreparedStatement insertLossStmt;
    private static PreparedStatement getRangeValueStmt;

    private static Connection conn;
    /**
     * Connect to a sample database
     */
    public static void connect() {
        conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:data.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

            // SQL statement for creating a new table
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS pings (\n"
                    + "	id integer PRIMARY KEY,\n"
                    + "	time timestamp NOT NULL,\n"
                    + "	ping integer NOT NULL\n"
                    + ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS loss (\n"
                    + "	id integer PRIMARY KEY,\n"
                    + "	time timestamp NOT NULL\n"
                    + ");");

            System.out.println("Tables has been created.");

            insertPingStmt = conn.prepareStatement(
                    "INSERT INTO pings(time, ping)" +
                            " VALUES (?, ?)");

            insertLossStmt = conn.prepareStatement("INSERT INTO loss(time) VALUES (?)");

            getRangeValueStmt = conn.prepareStatement("SELECT AVG(ping) AS value FROM pings WHERE time > ? and time < ?");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insertPing(Timestamp time, int ping){
        try {
            insertPingStmt.setTimestamp(1, time);
            insertPingStmt.setInt(2, ping);
            insertPingStmt.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void insertLoss(Timestamp time){
        try {
            insertLossStmt.setTimestamp(1, time);
            insertLossStmt.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static int getRangeValue(Timestamp start, Timestamp end){
        try {
            getRangeValueStmt.setTimestamp(1, start);
            getRangeValueStmt.setTimestamp(2, end);
            ResultSet rs = getRangeValueStmt.executeQuery();
            return rs.getInt("value");
        }
        catch (SQLException e){
            e.printStackTrace();
            return -1;
        }

    }
}
