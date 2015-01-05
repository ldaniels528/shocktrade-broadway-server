package com.shocktrade.narratives

import akka.actor.Actor
import com.ldaniels528.broadway.BroadwayNarrative
import com.ldaniels528.broadway.core.actors.Actors.Implicits._
import com.ldaniels528.broadway.core.actors.Actors._
import com.ldaniels528.broadway.core.actors.FileReadingActor
import com.ldaniels528.broadway.core.actors.FileReadingActor._
import com.ldaniels528.broadway.core.actors.kafka.avro.KafkaAvroPublishingActor
import com.ldaniels528.broadway.server.ServerConfig
import com.ldaniels528.trifecta.io.avro.AvroConversion
import com.shocktrade.helpers.ResourceTracker
import com.shocktrade.narratives.KeyStatsImportNarrative.KeyStatisticsLookupActor
import com.shocktrade.services.YahooFinanceServices

import scala.concurrent.ExecutionContext

/**
 * ShockTrade Key Statistics Import Narrative
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class KeyStatsImportNarrative(config: ServerConfig) extends BroadwayNarrative(config, "Key Statistics Import")
with KafkaConstants {

  onStart { resource =>
    // create a file reader actor to read lines from the incoming resource
    val fileReader = addActor(new FileReadingActor(config))

    // create a Kafka publishing actor for stock quotes
    val keyStatsPublisher = addActor(new KafkaAvroPublishingActor(keyStatsTopic, brokers))

    // create a stock quote lookup actor
    val keyStatsLookup = addActor(new KeyStatisticsLookupActor(keyStatsPublisher))

    // start the processing by submitting a request to the file reader actor
    fileReader ! CopyText(resource, keyStatsLookup, handler = Delimited("[\t]"))
  }
}

/**
 * ShockTrade Key Statistics Import Narrative Singleton
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object KeyStatsImportNarrative {

  /**
   * Key Statistics Lookup Actor
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class KeyStatisticsLookupActor(target: BWxActorRef)(implicit ec: ExecutionContext) extends Actor {
    override def receive = {
      case OpeningFile(resource) =>
        ResourceTracker.start(resource)

      case ClosingFile(resource) =>
        ResourceTracker.stop(resource)

      case TextLine(resource, lineNo, line, tokens) =>
        tokens.headOption foreach { symbol =>
          YahooFinanceServices.getKeyStatistics(symbol) foreach { keyStatistics =>
            val builder = com.shocktrade.avro.KeyStatisticsRecord.newBuilder()
            AvroConversion.copy(keyStatistics, builder)
            target ! builder.build()
          }
        }

      case message =>
        unhandled(message)
    }
  }

}
