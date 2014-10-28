package model.games

import scala.xml.Elem

class StocksCrossword extends Game with TimeBoxed {

  //TODO generate content (as XML? HTML?) if we generate html its like a controller
  // like XML then can be passed through rest call

  def generate(): Any = {
    fill
    val crossword =
      <crossword lengthAcross="15" lengthDown="15">
        {for (stockWord: (String, String, String, Integer, Integer, Integer) <- stockWords) {
        generateWord(stockWord)
      }}
      </crossword>
    return crossword
  }

  def generateWord(stockWord: (String, String, String, Integer, Integer, Integer)): Elem = {
    <word style={stockWord _3} hintId={stockWord._4.toString} line={stockWord._5.toString} offset={stockWord._6.toString}>
      <content>
        {stockWord _2}
      </content>
      <hint>
        {stockWord _1}
      </hint>
    </word>
  }

  var stockWords: List[(String, String, String, Integer, Integer, Integer)] = List()

  def fill = {
    //actually want to get from DB? or hard code here
    stockWords = stockWords :+("Sudden stock market collapse", "CRASH", "ACROSS", new Integer(1), new Integer(1), new Integer(0))
    stockWords = stockWords :+("Caribbean resort island", "ARUBA", "ACROSS", new Integer(6), new Integer(1), new Integer(6))
    stockWords = stockWords :+("___ Jones Average (stock market index)", "DOW", "ACROSS", new Integer(11), new Integer(1), new Integer(12))
    stockWords = stockWords :+("“America’s Got Talent” judge Mandel", "HOWIE", "ACROSS", new Integer(14), new Integer(2), new Integer(0))
    stockWords = stockWords :+("Brightened quickly, as the face: 2 wds.", "LITUP", "ACROSS", new Integer(15), new Integer(2), new Integer(6))
    stockWords = stockWords :+("“___ you ready?”", "ARE", "ACROSS", new Integer(16), new Integer(2), new Integer(12))
    stockWords = stockWords :+("Sound signaling the beginning of the stock market’s trading day: 2 wds.", "OPENINGBELL", "ACROSS", new Integer(17), new Integer(3), new Integer(0))
    stockWords = stockWords :+("Thus far", "YET", "ACROSS", new Integer(19), new Integer(3), new Integer(12))
    stockWords = stockWords :+("Get married to", "WED", "ACROSS", new Integer(20), new Integer(4), new Integer(0))
    stockWords = stockWords :+("Marsupial pal of Pooh", "ROO", "ACROSS", new Integer(21), new Integer(4), new Integer(4))
    stockWords = stockWords :+("State-run gambling games", "LOTTOS", "ACROSS", new Integer(22), new Integer(4), new Integer(9))
    stockWords = stockWords :+("People who simultaneously buy and sell in two separate financial markets in order to profit from their price differences, for short", "ARBS", "ACROSS", new Integer(24), new Integer(5), new Integer(5))
    stockWords = stockWords :+("Do damage to", "MAR", "ACROSS", new Integer(27), new Integer(5), new Integer(10))
    stockWords = stockWords :+("Oahu is one: Abbr.", "ISL", "ACROSS", new Integer(28), new Integer(6), new Integer(0))
    stockWords = stockWords :+("New York Stock Exchange, informally: 3 wds.", "THEBIGBOARD", "ACROSS", new Integer(31), new Integer(6), new Integer(4))
    stockWords = stockWords :+("Suffix for English county names", "SHIRE", "ACROSS", new Integer(35), new Integer(7), new Integer(0))
    stockWords = stockWords :+("“Your Money” channel", "CNN", "ACROSS", new Integer(37), new Integer(7), new Integer(7))
    stockWords = stockWords :+("Concept", "IDEA", "ACROSS", new Integer(38), new Integer(7), new Integer(10))
    stockWords = stockWords :+("Mourns for", "LAMENTS", "ACROSS", new Integer(39), new Integer(8), new Integer(0))
    stockWords = stockWords :+("Be a better marketer than", "OUTSELL", "ACROSS", new Integer(42), new Integer(8), new Integer(8))
    stockWords = stockWords :+("Surrounded by", "AMID", "ACROSS", new Integer(44), new Integer(9), new Integer(0))
    stockWords = stockWords :+("Countless years on end", "EON", "ACROSS", new Integer(45), new Integer(9), new Integer(5))
    stockWords = stockWords :+("Upper heart chambers", "ATRIA", "ACROSS", new Integer(47), new Integer(9), new Integer(10))
    stockWords = stockWords :+("Investment options consisting of a number of diversified securities: 2 wds.", "MUTUALFUNDS", "ACROSS", new Integer(48), new Integer(10), new Integer(0))
    stockWords = stockWords :+("___-fi movie", "SCI", "ACROSS", new Integer(52), new Integer(10), new Integer(12))
    stockWords = stockWords :+("No longer in general use: Abbr.", "OBS", "ACROSS", new Integer(53), new Integer(11), new Integer(2))
    stockWords = stockWords :+("“That’s not ___!” (parent’s admonition): 2 wds.", "ATOY", "ACROSS", new Integer(54), new Integer(11), new Integer(6))
    stockWords = stockWords :+("Like a swamp", "MARCHY", "ACROSS", new Integer(55), new Integer(12), new Integer(0))
    stockWords = stockWords :+("Was in charge of", "LED", "ACROSS", new Integer(58), new Integer(12), new Integer(8))
    stockWords = stockWords :+("“The Simpsons” character Disco ___", "STU", "ACROSS", new Integer(60), new Integer(12), new Integer(12))
    stockWords = stockWords :+("Stocks ___ bonds", "AND", "ACROSS", new Integer(63), new Integer(13), new Integer(0))
    stockWords = stockWords :+("Unprocessed goods, as grains and precious metals", "COMMODITIES", "ACROSS", new Integer(64), new Integer(13), new Integer(4))
    stockWords = stockWords :+("Memphis-to-Chicago dir.", "NNE", "ACROSS", new Integer(68), new Integer(14), new Integer(0))
    stockWords = stockWords :+("“Ran” director ___ Kurosawa", "AKIRA", "ACROSS", new Integer(69), new Integer(14), new Integer(4))
    stockWords = stockWords :+("One of the equal parts into which the stock of a corporation is divided", "SHARE", "ACROSS", new Integer(70), new Integer(14), new Integer(10))
    stockWords = stockWords :+("Corn-on-the-cob unit", "EAR", "ACROSS", new Integer(71), new Integer(15), new Integer(0))
    stockWords = stockWords :+("“I pass,” in a card game: 2 wds.", "NOBID", "ACROSS", new Integer(72), new Integer(15), new Integer(4))
    stockWords = stockWords :+("Church songsWords", "HYMNS", "ACROSS", new Integer(73), new Integer(15), new Integer(10))

    stockWords = stockWords :+("___ down (pig out)", "CHOW", "DOWN", new Integer(1), new Integer(1), new Integer(0))
    stockWords = stockWords :+("Lasso, e.g.", "ROPE", "DOWN", new Integer(2), new Integer(1), new Integer(1))
    stockWords = stockWords :+("Filled with wonder", "AWED", "DOWN", new Integer(3), new Integer(1), new Integer(2))
    stockWords = stockWords :+("Iniquity", "SIN", "DOWN", new Integer(4), new Integer(1), new Integer(3))
    stockWords = stockWords :+("Person named in a will", "HEIR", "DOWN", new Integer(5), new Integer(1), new Integer(4))
    stockWords = stockWords :+("Bill Clinton’s vice president: 2 wds.", "ALGORE", "DOWN", new Integer(6), new Integer(1), new Integer(6))
    stockWords = stockWords :+("Bit of BBQ meat", "RIB", "DOWN", new Integer(7), new Integer(1), new Integer(7))
    stockWords = stockWords :+("Sport-___ (off-road vehicle)", "UTE", "DOWN", new Integer(8), new Integer(1), new Integer(8))
    stockWords = stockWords :+("Person who believes that stock prices will increase", "BULL", "DOWN", new Integer(9), new Integer(1), new Integer(9))
    stockWords = stockWords :+("Self-assurance", "APLOMB", "DOWN", new Integer(10), new Integer(1), new Integer(10))
    stockWords = stockWords :+("Those who buy and sell stocks rapidly on the Internet to profit from momentary price fluctuations: 2 wds.", "DAYTRADERS", "DOWN", new Integer(11), new Integer(1), new Integer(12))
    stockWords = stockWords :+("Nabisco cookie", "OREO", "DOWN", new Integer(12), new Integer(1), new Integer(13))
    stockWords = stockWords :+("Moistens", "WETS", "DOWN", new Integer(13), new Integer(1), new Integer(14))
    stockWords = stockWords :+("Ark builder", "NOAH", "DOWN", new Integer(18), new Integer(3), new Integer(5))
    stockWords = stockWords :+("Follower of Lao-tzu", "TAOIST", "DOWN", new Integer(23), new Integer(4), new Integer(11))
    stockWords = stockWords :+("English TV network", "BBC", "DOWN", new Integer(25), new Integer(5), new Integer(7))
    stockWords = stockWords :+("Prefix meaning “Chinese”", "SINO", "DOWN", new Integer(26), new Integer(5), new Integer(8))
    stockWords = stockWords :+("Allah worshiper’s religion", "ISLAM", "DOWN", new Integer(28), new Integer(6), new Integer(0))
    stockWords = stockWords :+("Killer whale at SeaWorld", "SHAMU", "DOWN", new Integer(29), new Integer(6), new Integer(1))
    stockWords = stockWords :+("Instruction for an investment broker to buy or sell something at a specific price: 2 wds.", "LIMITORDER", "DOWN", new Integer(30), new Integer(6), new Integer(2))
    stockWords = stockWords :+("A dozen minus two", "TEN", "DOWN", new Integer(31), new Integer(6), new Integer(4))
    stockWords = stockWords :+("African antelope", "GNU", "DOWN", new Integer(32), new Integer(6), new Integer(9))
    stockWords = stockWords :+("Artifact from the past", "RELIC", "DOWN", new Integer(33), new Integer(6), new Integer(13))
    stockWords = stockWords :+("___ Lama (chief Tibetan monk)", "DALAI", "DOWN", new Integer(34), new Integer(6), new Integer(14))
    stockWords = stockWords :+("Copies over, as a soundtrack", "REDUBS", "DOWN", new Integer(36), new Integer(7), new Integer(3))
    stockWords = stockWords :+("___ Aviv, Israel", "TEL", "DOWN", new Integer(40), new Integer(8), new Integer(5))
    stockWords = stockWords :+("Long couch", "", "DOWN", new Integer(41), new Integer(8), new Integer(6))
    stockWords = stockWords :+("Professors’ helpers: Abbr.", "TAS", "DOWN", new Integer(43), new Integer(8), new Integer(10))
    stockWords = stockWords :+("Almond or pecan", "NUT", "DOWN", new Integer(46), new Integer(9), new Integer(7))
    stockWords = stockWords :+("Garbage barrel", "ASHCAN", "DOWN", new Integer(49), new Integer(10), new Integer(4))
    stockWords = stockWords :+("Not charging a sales commission, as 48-Across: Hyph.", "NOLOAD", "DOWN", new Integer(50), new Integer(10), new Integer(8))
    stockWords = stockWords :+("Colored, as Easter eggs", "DYED", "DOWN", new Integer(51), new Integer(10), new Integer(9))
    stockWords = stockWords :+("Lion’s neck hair", "MANE", "DOWN", new Integer(55), new Integer(12), new Integer(0))
    stockWords = stockWords :+("Actress Paquin", "ANNA", "DOWN", new Integer(56), new Integer(12), new Integer(1))
    stockWords = stockWords :+("Musician ___ Ono", "YOKO", "DOWN", new Integer(57), new Integer(12), new Integer(5))
    stockWords = stockWords :+("Plate", "DISH", "DOWN", new Integer(59), new Integer(12), new Integer(10))
    stockWords = stockWords :+("Thailand, formerly", "SIAM", "DOWN", new Integer(60), new Integer(12), new Integer(12))
    stockWords = stockWords :+("Sea bird", "TERN", "DOWN", new Integer(61), new Integer(12), new Integer(13))
    stockWords = stockWords :+("Takes advantage of", "USES", "DOWN", new Integer(62), new Integer(12), new Integer(14))
    stockWords = stockWords :+("1997 hit film starring Will Smith and Tommy Lee Jones, for short", "MIB", "DOWN", new Integer(65), new Integer(13), new Integer(6))
    stockWords = stockWords :+("Medical scan, briefly", "MRI", "DOWN", new Integer(66), new Integer(13), new Integer(7))
    stockWords = stockWords :+("“Your,” in the Bible", "THY", "DOWN", new Integer(67), new Integer(13), new Integer(11))
  }

  // gotta have a dictionary of words and hints
  // a way to generate a crossword out of them
  // 1. jumble the list
  // 2. pick a word by random - lay it down in the center
  // 3. choose cross-able letters (all for the first time)
  // 4. next word by random - cross any letter in the cross-able list
  // 5. put across
  // 6. update cross-able letters and positions
  // 7. next letter


}
