# kotlin-minimal-rdms

Add-on to [kotlin-minimal-server](https://github.com/thesurlydev/kotlin-minimal-server) for relational database support.

## Usage

### Configuration
```kotlin
val dbConfig = DatabaseConfig(
  jdbcUrl = "jdbc:postgresql://${getEnv("POSTGRES_HOST")}:5433/${getEnv("POSTGRES_DB")}",
  username = getEnv("POSTGRES_USER")!!,
  password = getEnv("POSTGRES_PASSWORD")!!
)
val databaseManager = DatabaseManager(dbConfig)
val personRepository = PersonRepository(databaseManager)
```

### Schema

```sql
create table if not exists people
(
    id       uuid primary key default gen_random_uuid(),
    name     varchar(255) not null,
    age      int          not null
);
```

### Repository
```kotlin
class PersonRepository(private val databaseManager: DatabaseManager) : Repository<UUID, Person> {
  override fun findAll(): List<Person> {
    val sql = "SELECT * FROM people"
    return Querier.executeQuery(databaseManager, sql, ::person)
  }

  override fun findById(id: UUID): Person? {
    val sql = "SELECT * FROM people WHERE id = :id"
    val params = mapOf("id" to id)
    return Querier.queryWithParams(databaseManager, sql, params, ::maybePerson)
  }

  override fun delete(id: UUID): Boolean {
    val sql = "DELETE FROM people WHERE id = :id"
    val params = mapOf("id" to id)
    return Querier.updateObject(databaseManager, sql, params) > 0
  }

  private fun maybePerson(resultSet: ResultSet): Person? = when {
    resultSet.next() -> person(resultSet)
    else -> null
  }

  private fun person(resultSet: ResultSet) = Person(
    resultSet.getString("id"),
    resultSet.getString("name"),
    resultSet.getInt("age")
  )
}
```

### Gradle (Kotlin DSL)

```kotlin
implementation("dev.surly:kotlin-minimal-rdms:0.1.0")
```

### Gradle (Groovy DSL)

```groovy
implementation 'dev.surly:kotlin-minimal-rdms:0.1.0'
```

### Maven

```xml
<dependency>
    <groupId>dev.surly</groupId>
    <artifactId>kotlin-minimal-rdms</artifactId>
    <version>0.1.0</version>
</dependency>
```

