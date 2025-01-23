import org.example.model.Migration;
import org.example.model.ChangeSet;
import org.junit.jupiter.api.Test;

public class ModelTest {
    @Test
    public void testMigToCha() {
        ChangeSet changeSet = new ChangeSet();
        changeSet.setFilename("first");
        changeSet.setDescription("first");
        changeSet.setAuthor("first");

        Migration migration = changeSet.toMigration();

        System.out.println(migration);
    }
}
