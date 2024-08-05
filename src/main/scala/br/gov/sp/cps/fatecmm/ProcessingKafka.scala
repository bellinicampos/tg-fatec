package br.gov.sp.cps.fatecmm

// APACHE FLINK LIBRARIES
import org.apache.flink.streaming.api.scala._
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.DeserializationSchema
// KAFKA CONNECTOR LIBRARIES
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
// LOG CONFIGURATIONS
import ch.qos.logback.classic.{Level, LoggerContext}
import org.slf4j.{LoggerFactory, Logger}

object ProcessingKafka {

  // SETTING THE LEVEL OF LOG
  private val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  private val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)
  rootLogger.setLevel(Level.WARN) // Set the log level to (DEBUG, INFO, WARN, ERROR)

  // DEFINING A FLINK STREAMING ENVIRONMENT
  val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

  // METHOD THAT CREATES A KAFKA SOURCE
  private def createKafkaSource[T](
                                    topic: String,
                                    deserializer: DeserializationSchema[T]
                                  ): KafkaSource[T] = {

    KafkaSource.builder[T]()
      .setBootstrapServers("localhost:9092")
      .setTopics(topic)
      .setGroupId(s"$topic-group")
      .setStartingOffsets(OffsetsInitializer.latest())
      .setValueOnlyDeserializer(deserializer)
      .build()
  }

  // METHOD THAT CREATES KAFKA CONSUMERS
  private def kafkaConsumers(): Unit = {

    // CREATING INSTANCES OF KAFKA SOURCE
    val userKafkaSource: KafkaSource[User] = createKafkaSource[User]("users", new UserDeserializer())
    val companyKafkaSource: KafkaSource[Company] = createKafkaSource[Company]("companies", new CompanyDeserializer())
    val voucherKafkaSource: KafkaSource[GenericProduct]  = createKafkaSource[GenericProduct]("vouchers", new VoucherDeserializer())

    // CREATING INSTANCES OF DATA STREAMS
    val userStream: DataStream[User] = env.fromSource(userKafkaSource, WatermarkStrategy.noWatermarks(), "kafka users topic")
    val companyStream: DataStream[Company] = env.fromSource(companyKafkaSource, WatermarkStrategy.noWatermarks(), "kafka companies topic")
    val voucherStream: DataStream[GenericProduct] = env.fromSource(voucherKafkaSource, WatermarkStrategy.noWatermarks(), "kafka vouchers topic")

    // SPLITTING THE VOUCHER STREAM INTO ONE STREAM PER PRODUCT
    import SplitStream._
    val allVouchers: DataStream[GenericProduct] = voucherStream.process(new SplitStream)
    val flightVouchers: DataStream[FlightProduct] = allVouchers.getSideOutput(flightTag).map(_.asInstanceOf[FlightProduct])
    val hotelVouchers: DataStream[HotelProduct] = allVouchers.getSideOutput(hotelTag).map(_.asInstanceOf[HotelProduct])
    val expenseVouchers: DataStream[ExpenseProduct] = allVouchers.getSideOutput(expenseTag).map(_.asInstanceOf[ExpenseProduct])

    // KEYING THE STREAMS
    val keyedUserStream: KeyedStream[User, Long] = userStream.keyBy(_.id)
    val keyedCompanyStream: KeyedStream[Company, Long] = companyStream.keyBy(_.id)
    val keyedFlightVouchers: KeyedStream[FlightProduct, String] = flightVouchers.keyBy(_.voucherCode)
    val keyedHotelVouchers: KeyedStream[HotelProduct, String] = hotelVouchers.keyBy(_.voucherCode)
    val keyedExpenseVouchers: KeyedStream[ExpenseProduct, String] = expenseVouchers.keyBy(_.voucherCode)

    // PRINTING THE KEYED STREAMS
    keyedFlightVouchers.print()
    keyedHotelVouchers.print()
    keyedExpenseVouchers.print()
    keyedUserStream.print()
    keyedCompanyStream.print()

    // EXECUTING ALL TRANSFORMATIONS
    env.execute()
  }

  def main(args: Array[String]): Unit = {
    kafkaConsumers()
  }
}