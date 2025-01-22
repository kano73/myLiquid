import org.example.repository.DatabaseRepository;
import org.example.service.DatabaseServiceImplementation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DBTest {
    @Test
    public void testDBConnection() {
        DatabaseRepository repo = new DatabaseRepository();
        DatabaseServiceImplementation dbService = new DatabaseServiceImplementation(repo);
        Assertions.assertDoesNotThrow(dbService::getAllChanges);
    }


}
