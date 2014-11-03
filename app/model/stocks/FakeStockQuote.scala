package model.stocks

import java.lang
import java.util.Random

/**
 * Creates a randomly generated price based on the previous price
 */
class FakeStockQuote extends StockQuote {
  override def newPrice(symbol: String, lastPrice: lang.Double): lang.Double = {
    return lastPrice * (0.95 + (0.1 * new Random().nextDouble))
  }
}