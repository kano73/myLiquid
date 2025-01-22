import org.example.model.Change;
import org.example.model.MasterChangeLog;
import org.example.model.Migration;
import org.example.repository.DatabaseRepository;
import org.example.repository.MigrationRepository;
import org.example.service.DatabaseServiceImplementation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

public class CompareMigAndChaTest {
    @Test
    public void testCompareMigAndCha() {
        DatabaseServiceImplementation dbService = new DatabaseServiceImplementation(new DatabaseRepository());
        List<Change> changes = dbService.getAllChanges();

        List<Migration> migrations = MigrationRepository.readAllChanges();

        List<Change> changesFromMigration = migrations.stream()
                .map(mig ->{
                    return mig.toChange();
                }).collect(Collectors.toList());

        System.out.println("hi");
        System.out.println(changesFromMigration);
        System.out.println("hi");
        System.out.println(changes);

        Assertions.assertNotEquals(changesFromMigration, changes);
    }
}
