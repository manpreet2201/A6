
import java.util.Properties;

public class MyIdentity {

    public static void setIdentity(Properties prop) {
      prop.setProperty("database", "singh4");
      prop.setProperty("user", "singh4");  // CSID for bluenose
      prop.setProperty("password", "B00853930"); //Banner ID (which is your mysql password)
    }
}
