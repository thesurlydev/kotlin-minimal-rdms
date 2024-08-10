package db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

class DatabaseManager(config: DatabaseConfig) {
  private val dataSource: HikariDataSource

  init {
    val hikariConfig = HikariConfig().apply {
      jdbcUrl = config.jdbcUrl
      username = config.username
      password = config.password
      maximumPoolSize = config.maximumPoolSize
      transactionIsolation = config.transactionIsolation
    }
    dataSource = HikariDataSource(hikariConfig)
  }

  fun getConnection(): Connection = dataSource.connection
}
