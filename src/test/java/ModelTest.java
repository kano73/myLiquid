import org.example.model.Change;
import org.example.model.Migration;
import org.junit.jupiter.api.Test;

public class ModelTest {
    @Test
    public void testMigToCha() {
        Migration migration = new Migration();
        migration.setFilename("first");
        migration.setDescription("first");
        migration.setAuthor("first");

        Change change = migration.toChange();

        System.out.println(change);
    }
}
