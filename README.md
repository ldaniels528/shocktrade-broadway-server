ShockTrade Server
=================
ShockTrade Server is the *replacement* processing back-end of the ShockTrade.com web site, which is currently implemented
via Storm. ShockTrade Server is being built atop of Broadway (https://github.com/ldaniels528/broadway) as a distributed
Actor-based processing system.

## The ShockTrade Server Topology

The proceeding example is a Broadway topology performs the following flow:

* Extracts stock symbols from a tabbed-delimited file.
* Retrieves stock quotes for each symbol.
* Converts the stock quotes to <a href="http://avro.apache.org/" target="avro">Avro</a> records.
* Publishes each Avro record to a Kafka topic (shocktrade.quotes.yahoo.avro)

Below is the Broadway topology that implements the flow described above:

```scala
class StockQuoteImportTopology() extends BroadwayTopology("Stock Quote Import") with KafkaConstants {

  onStart { resource =>
    // create a file reader actor to read lines from the incoming resource
    val fileReader = addActor(new FileReadingActor())

    // create a Kafka publishing actor for stock quotes
    val quotePublisher = addActor(new KafkaAvroPublishingActor(quotesTopic, brokers))

    // create a stock quote lookup actor
    val quoteLookup = addActor(new StockQuoteLookupActor(quotePublisher))

    // start the processing by submitting a request to the file reader actor
    fileReader ! CopyText(resource, quoteLookup, handler = Delimited("[\t]"))
  }
}
```

**NOTE:** The `KafkaAvroPublishingActor` and `FileReadingActor` actors are builtin components of Broadway.

The class below is an optional custom actor that will perform the stock symbol look-ups and then pass an Avro-encoded
record to the Kafka publishing actor (a built-in component).

```scala
class StockQuoteLookupActor(target: BWxActorRef)(implicit ec: ExecutionContext) extends Actor {
  private val parameters = YFStockQuoteService.getParams(
    "symbol", "exchange", "lastTrade", "tradeDate", "tradeTime", "ask", "bid", "change", "changePct",
    "prevClose", "open", "close", "high", "low", "volume", "marketCap", "errorMessage")

  override def receive = {
    case OpeningFile(resource) =>
      ResourceTracker.start(resource)

    case ClosingFile(resource) =>
      ResourceTracker.stop(resource)

    case TextLine(resource, lineNo, line, tokens) =>
      tokens.headOption foreach { symbol =>
        YahooFinanceServices.getStockQuote(symbol, parameters) foreach { quote =>
          val builder = com.shocktrade.avro.CSVQuoteRecord.newBuilder()
          AvroConversion.copy(quote, builder)
          target ! builder.build()
        }
      }

    case message =>
      unhandled(message)
  }
}
```

```scala
trait KafkaConstants {
  val eodDataTopic = "shocktrade.eoddata.yahoo.avro"
  val keyStatsTopic = "shocktrade.keystats.yahoo.avro"
  val quotesTopic = "shocktrade.quotes.yahoo.avro"

  val zkHost = "dev501:2181"
  val brokers = "dev501:9091,dev501:9092,dev501:9093,dev501:9094,dev501:9095,dev501:9096"
}
```

And an XML file to describe how files will be mapped to the topology:

```xml
<topology-config>

    <topology id="QuoteImportTopology" class="com.shocktrade.topologies.StockQuoteImportTopology" />

    <location id="CSVQuotes" path="/Users/ldaniels/broadway/incoming/csvQuotes">
        <feed match="exact" name="AMEX.txt" topology-ref="QuoteImportTopology" />
        <feed match="exact" name="NASDAQ.txt" topology-ref="QuoteImportTopology" />
        <feed match="exact" name="NYSE.txt" topology-ref="QuoteImportTopology" />
        <feed match="exact" name="OTCBB.txt" topology-ref="QuoteImportTopology" />
    </location>

</topology-config>
```

