package com.shocktrade.topologies

import com.ldaniels528.broadway.core.actors.FileReadingActor
import com.ldaniels528.broadway.core.actors.FileReadingActor.{CopyText, Delimited}
import com.ldaniels528.broadway.core.actors.kafka.avro.KafkaAvroPublishingActor
import com.ldaniels528.broadway.server.etl.BroadwayTopology
import com.ldaniels528.broadway.server.etl.BroadwayTopology.Implicits._
import com.shocktrade.actors.{KafkaConstants, KeyStatisticsLookupActor}

/**
 * ShockTrade Key Statistics Import Topology
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class KeyStatsImportTopology() extends BroadwayTopology("Key Statistics Import") with KafkaConstants {

  onStart { resource =>
    // create a file reader actor to read lines from the incoming resource
    val fileReader = addActor(new FileReadingActor())

    // create a Kafka publishing actor for stock quotes
    val keyStatsPublisher = addActor(new KafkaAvroPublishingActor(keyStatsTopic, brokers))

    // create a stock quote lookup actor
    val keyStatsLookup = addActor(new KeyStatisticsLookupActor(keyStatsPublisher))

    // start the processing by submitting a request to the file reader actor
    fileReader ! CopyText(resource, keyStatsLookup, handler = Delimited("[\t]"))
  }
}
