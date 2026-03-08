import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class VerifyGeodata {
    public static void main(String[] args) throws Exception {
        String jdbcUrl = "jdbc:mysql://localhost:3306/QTMDashboard?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, "root", "dago");
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "select "
                             + "(select count(*) from country) as countries, "
                             + "(select count(*) from region) as regions, "
                             + "(select count(*) from province) as provinces, "
                             + "(select count(*) from city) as cities")) {
            if (result.next()) {
                System.out.println("countries=" + result.getInt("countries"));
                System.out.println("regions=" + result.getInt("regions"));
                System.out.println("provinces=" + result.getInt("provinces"));
                System.out.println("cities=" + result.getInt("cities"));
            }
        }
    }
}