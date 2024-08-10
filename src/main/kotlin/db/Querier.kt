package db

import java.sql.ResultSet
import java.sql.Statement

class Querier {
  companion object {

    inline fun <reified T : Any> saveObject(
        databaseManager: DatabaseManager,
        sql: String,
        params: Map<String, Any?>,
        resultSetMapper: (ResultSet) -> T?,
    ): T? {
      val result = databaseManager.getConnection().use { conn ->
        val positionalSql = sql.replace(Regex(":(\\w+)")) { "?" }
        val stmt = conn.prepareStatement(positionalSql, Statement.RETURN_GENERATED_KEYS)

        val parameterIndexMap = buildParameterIndexMap(sql, params.keys)
        params.forEach { (name, value) ->
          val indexList = parameterIndexMap[name]
          indexList?.forEach { index ->
            stmt.setObject(index, value)
          }
        }

        stmt.executeUpdate()
        stmt.generatedKeys.use { generatedKeys ->
          if (generatedKeys.next()) {
            resultSetMapper(generatedKeys)
          } else {
            null
          }
        }
      }
      return result
    }

    fun updateObject(
        databaseManager: DatabaseManager,
        sql: String,
        params: Map<String, Any>,
    ): Int {
      return databaseManager.getConnection().use { conn ->
        val positionalSql = sql.replace(Regex(":(\\w+)")) { "?" }
        val stmt = conn.prepareStatement(positionalSql)

        val parameterIndexMap = buildParameterIndexMap(sql, params.keys)
        params.forEach { (name, value) ->
          val indexList = parameterIndexMap[name]
          indexList?.forEach { index ->
            stmt.setObject(index, value)
          }
        }

        stmt.executeUpdate()
      }
    }

    inline fun <T> executeQuery(
      databaseManager: DatabaseManager,
      query: String,
      resultSetMapper: (ResultSet) -> T,
    ): List<T> {
      val resultList = mutableListOf<T>()
      databaseManager.getConnection().use { conn ->
        conn.createStatement().use { stmt ->
          stmt.executeQuery(query).use { rs ->
            while (rs.next()) {
              resultList.add(resultSetMapper(rs))
            }
          }
        }
      }
      return resultList.toList()
    }

    inline fun <reified T : Any> queryWithParams(
      databaseManager: DatabaseManager,
      sql: String,
      params: Map<String, Any>,
      resultSetMapper: (ResultSet) -> T?,
    ): T? {
      val result = databaseManager.getConnection().use { conn ->
        // Convert named placeholders to positional placeholders and prepare the statement
        val positionalSql = sql.replace(Regex(":(\\w+)")) { "?" }
        val stmt = conn.prepareStatement(positionalSql)

        // Bind the named parameters to the prepared statement
        val parameterIndexMap = buildParameterIndexMap(sql, params.keys)
        params.forEach { (name, value) ->
          val indexList = parameterIndexMap[name]
          indexList?.forEach { index ->
            stmt.setObject(index, value)
          }
        }

        // Execute the query
        stmt.executeQuery().use { resultSet ->
          resultSetMapper(resultSet)
        }
      }
      return result
    }

    fun buildParameterIndexMap(sql: String, paramNames: Set<String>): Map<String, List<Int>> {
      val parameterIndexMap = mutableMapOf<String, MutableList<Int>>()
      var index = 1
      Regex(":(\\w+)").findAll(sql).forEach { matchResult ->
        val paramName = matchResult.groupValues[1]
        if (paramName in paramNames) {
          parameterIndexMap.getOrPut(paramName) { mutableListOf() }.add(index)
        }
        index++
      }
      return parameterIndexMap
    }
  }
}