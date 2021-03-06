package com.shocktrade.datacenter.narratives.securities

import java.lang.{Double => JDouble}

import com.ldaniels528.broadway.BroadwayNarrative
import com.mongodb.casbah.Imports.{DBObject => O}

/**
 * Provide commonly used functions for computing stock quote-related values
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait StockQuoteSupport {
  self: BroadwayNarrative =>

  def computeSpreadJava(high: Option[JDouble], low: Option[JDouble]):Option[JDouble] = {
    for {
      hi <- high
      lo <- low
    } yield if (lo != 0.0d) 100d * (hi - lo) / lo else 0.0d
  }

  def computeChangePctJava(prevClose: Option[JDouble], lastTrade: Option[JDouble]): Option[JDouble] = {
    for {
      prev <- prevClose
      last <- lastTrade
      diff = last - prev
    } yield (if (diff != 0) 100d * (diff / prev) else 0.0d): JDouble
  }

  def computeSpread(high: Option[Double], low: Option[Double]): Option[Double] = {
    for {
      hi <- high
      lo <- low
    } yield if (lo != 0.0d) 100d * (hi - lo) / lo else 0.0d
  }

  def computeChangePct(prevClose: Option[Double], lastTrade: Option[Double]): Option[Double] = {
    for {
      prev <- prevClose
      last <- lastTrade
      diff = last - prev
    } yield if (diff != 0) 100d * (diff / prev) else 0.0d
  }

}
