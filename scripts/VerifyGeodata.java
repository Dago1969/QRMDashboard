import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerifyGeodata {
    private static final Logger log = LoggerFactory.getLogger(VerifyGeodata.class);
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
                log.info("countries={}", result.getInt("countries"));
                log.info("regions={}", result.getInt("regions"));
                log.info("provinces={}", result.getInt("provinces"));
                log.info("cities={}", result.getInt("cities"));
            }
        }
    }
}