package model.dataobjects.json

import java.io.StringWriter

import com.fasterxml.jackson.databind.ObjectMapper

object JSONifier {

  def toJSON(someObject : AnyRef): String = {
    val mapper: ObjectMapper = new ObjectMapper
    val sw: StringWriter = new StringWriter
    mapper.writeValue(sw, someObject)
    return sw.toString
  }

}
