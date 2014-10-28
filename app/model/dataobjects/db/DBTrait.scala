package model.dataobjects.db

import java.sql.{Connection, DriverManager}

trait DBTrait {
  // Quick way to load the driver
  Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance

  def getConnectionByUrl(url: String) = {
    DriverManager.getConnection(url, "db2admin", "db2@dm1n")
  }

  def getConnection(name: String) = {
    getConnectionByUrl("jdbc:db2://localhost:50000/ooofoooh")
  }

  /*
  def withConnection[A](block: Connection => A): A = {
    withConnection("default")
  } */

  def withConnection[A](name: String)(block: Connection => A): A = {
    val connection = getConnection(name)
    try {
      block(connection)
    } finally {
      connection.close()
    }
  }

  def withTransaction[A](name: String)(block: Connection => A): A = {
    withConnection(name) { connection =>
      try {
        connection.setAutoCommit(false)
        val r = block(connection)
        connection.commit()
        r
      } catch {
        case e: Throwable => connection.rollback(); throw e
      }
    }
  }
}

