package model.dataobjects.db

import controllers.Global

trait DBFacade {

  val view: SpringJDBCQueries = Global.context.getBean("springJDBCQueries").asInstanceOf[SpringJDBCQueries]

}
