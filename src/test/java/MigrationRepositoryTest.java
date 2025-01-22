
import org.example.model.MasterChangeLog;
import org.example.model.Migration;
import org.example.repository.MigrationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

public class MigrationRepositoryTest {
    @Test
    public void testReadChanges() {

        Migration migration = new Migration();
        migration.setFilename("first.json");
        migration.setAuthor("pavel");
        migration.setDescription("first change");
        migration.setStatements(List.of(
                "CREATE TABLE person (id INT PRIMARY KEY, name VARCHAR(50))",
                "CREATE INDEX idx_person_name ON person(name)"
        ));

        Migration migrationToCheck = MigrationRepository.readAllChanges().stream()
                .filter(obj->obj.getFilename().equals(migration.getFilename()))
                .findFirst().orElse(null);

        Assertions.assertEquals(true , Objects.equals(migrationToCheck, migration), "migration have to be the same");

    }

    @Test
    public void testReadMaster(){
        MigrationRepository migrationRepository = new MigrationRepository();

        MasterChangeLog master = new MasterChangeLog();
        master.setChanges(List.of(
                "first.json"
        ));

        MasterChangeLog masterToCheck = MigrationRepository.readMasterChanges();

        Assertions.assertEquals(true , Objects.equals(master, masterToCheck), "masters have to be the same");

    }
}
