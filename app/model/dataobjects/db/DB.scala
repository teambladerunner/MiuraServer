package model.dataobjects.db

import java.sql.Connection

object DB extends DBTrait {
  def withConnection[A](block: Connection => A): A = {
    this.withConnection("default")(block)
  }

  def withTransaction[A](block: Connection => A): A = {
    this.withTransaction("default")(block)
  }
}
