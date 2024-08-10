package db

data class DatabaseConfig(
  val jdbcUrl: String,
  val username: String,
  val password: String,
  val maximumPoolSize: Int = 10,
  val transactionIsolation: String = "TRANSACTION_READ_COMMITTED",
)
