package pingmonitor;

import javax.xml.transform.Result;
import java.sql.*;

public class SQLiteJDBCDriverConnection {
    private static PreparedStatement insertPingStmt;
    private static PreparedStatement insertLossStmt;

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
                    + "	pingId text NOT NULL\n"
                    + ");");

            System.out.println("Tables has been created.");

            insertPingStmt = conn.prepareStatement(
                    "INSERT INTO pings( time, ping)" +
                            " VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);

            insertLossStmt = conn.prepareStatement("INSERT INTO loss(pingId) VALUES (?)");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
//            try {
//                if (conn != null) {
//                    conn.close();
//                }
//            } catch (SQLException ex) {
//                System.out.println(ex.getMessage());
//            }
        }
    }

    public static int insertPing(Timestamp time, int ping){
        try {
            insertPingStmt.setTimestamp(1, time);
            insertPingStmt.setInt(2, ping);
            insertPingStmt.executeUpdate();

            ResultSet rs = insertPingStmt.getGeneratedKeys();

            return rs.getInt(1);
        }
        catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean insertLoss(int pingId){
        try {
            insertLossStmt.setInt(1, pingId);
            insertLossStmt.executeUpdate();
            return true;
        }
        catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
}
