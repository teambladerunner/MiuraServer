package model.games

abstract class Game {

  val progress = ???
  //TODO means we have to save game state - for example state of crossword filled

  val startTime = ???
  //TODO all games have a start

  val medal = ???
  //TODO all games will have a medal based on something - for example in crossword, based on time taken we can give g/s/b

  //TODO generate new - generate a new game when the player requests

  //TODO what is the target?

}
