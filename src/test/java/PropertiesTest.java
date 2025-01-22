import org.example.config.GetProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class PropertiesTest {
    @Test
    public void testProperties() {
        Properties properties = GetProperties.get();

        String jdbcUrl = properties.getProperty("myliquid.jdbc.url");
        String username = properties.getProperty("myliquid.jdbc.username");
        String password = properties.getProperty("myliquid.jdbc.password");
        String gitLink = properties.getProperty("myliquid.git.link");


        Assertions.assertEquals("jdbc:postgresql://localhost:5432/myliquid", jdbcUrl, "url not valid");
        Assertions.assertEquals("postgres", username, "username not valid");
        Assertions.assertEquals("12345", password, "password not valid");
        Assertions.assertEquals("https://github.com/kano73/liquidMasterChangeLog", gitLink, "link for git hub is not valid");
    }
}
