# myLiquid
## Конфигурационные параметры:

myliquid.jdbc.url – URL подключения к базе данных. 

myliquid.jdbc.username – Логин для базы данных. 

myliquid.jdbc.password – Пароль для базы данных. 

myliquid.git.link – Ссылка на Git-репозиторий. 

myliquid.git.token – Токен доступа к Git. 

myliquid.git.username – Имя пользователя Git. 

myliquid.changes.path – Путь к локальным миграциям (необязательно). 

myliquid.migration.level – Уровень миграции (pull_mig, push_mig, mig). 

myliquid.migration.version – Имя файла, на котором миграция остановится. 

## пример:
myliquid.jdbc.url=jdbc:postgresql://localhost:5432/myliquid

myliquid.jdbc.username=postgres

myliquid.jdbc.password=12345

myliquid.git.link=https://github.com/example/liquidMasterChangeLog

myliquid.git.token=ghp_ExampleToken123456789

myliquid.git.username=exampleUser

myliquid.changes.path=/path/to/local/changes

myliquid.migration.level=pull_mig

myliquid.migration.version=change2.json

## пример файла:
{
    "author": "pavel",
    "description": "Create and populate table1",
    "statements": [
        "CREATE TABLE table1 (id INT PRIMARY KEY, name VARCHAR(50), created_at TIMESTAMP)",
        "INSERT INTO table1 (id, name, created_at) VALUES (1, 'Row1', NOW())",
        "INSERT INTO table1 (id, name, created_at) VALUES (2, 'Row2', NOW())"
        ],
    "rollBack": [
    "DROP TABLE table1"
    ]
}
