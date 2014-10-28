package model.dataobjects.db

import controllers.Global

trait DBFacade {

  val springJDBCQueries: SpringJDBCQueries = Global.context.getBean("springJDBCQueries").asInstanceOf[SpringJDBCQueries]

}
