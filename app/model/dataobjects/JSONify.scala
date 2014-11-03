package model.dataobjects

import java.io.StringWriter

import com.fasterxml.jackson.databind.ObjectMapper

trait JSONify {

  def toJSON(): String = {
    val mapper: ObjectMapper = new ObjectMapper
    val sw: StringWriter = new StringWriter
    mapper.writeValue(sw, this)
    return sw.toString
  }

}
