package br.gov.sp.cps.fatecmm

// APACHE FLINK LIBRARIES
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.functions.co.{BroadcastProcessFunction, KeyedBroadcastProcessFunction}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Try}
// KAFKA CONNECTOR LIBRARIES
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
// LOG CONFIGURATIONS
import ch.qos.logback.classic.{Level, LoggerContext}
import org.slf4j.{Logger, LoggerFactory}

object ProcessingKafka2 {

  // SETTING THE LEVEL OF LOG
  private val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  private val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)
  rootLogger.setLevel(Level.ERROR) // Set the log level to (DEBUG, INFO, WARN, ERROR)

  // DEFINING A FLINK STREAMING ENVIRONMENT
  val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

  // METHOD THAT CREATES A KAFKA SOURCE
  private def createKafkaSource[T](
                            topic: String,
                            groupId: String,
                            deserializer: DeserializationSchema[T]
                          ): KafkaSource[T] = {
    KafkaSource.builder[T]()
      .setBootstrapServers("localhost:9092")
      .setTopics(topic)
      .setGroupId(groupId)
      .setStartingOffsets(OffsetsInitializer.latest())
      .setValueOnlyDeserializer(deserializer)
      .build()
  }

  // METHOD THAT CREATES KAFKA CONSUMERS
  private def kafkaConsumers(): Unit = {

    // CREATE A KAFKA SOURCE FOR USERS TOPIC
    val usersTopicKafka: KafkaSource[User] = createKafkaSource[User](
      "users",
      "users-group",
      new UserDeserializer()
    )

    // CREATE A USER STREAM FROM THE SOURCE CREATED PREVIOUSLY
    val userStream: DataStream[User] = env.fromSource(
      usersTopicKafka,
      WatermarkStrategy.noWatermarks(),
      "Kafka Source Topic Users"
    )

//    val keyedUserStream: KeyedStream[User, Long] = userStream.keyBy(_.id)

    // CREATE A KAFKA SOURCE FOR COMPANIES TOPIC
    val companiesTopicKafka = createKafkaSource[Company](
      "companies",
      "companies-group",
      new CompanyDeserializer()
    )

    // CREATE A COMPANY STREAM FROM THE SOURCE CREATED PREVIOUSLY
    val companyStream: DataStream[Company] = env.fromSource(
      companiesTopicKafka,
      WatermarkStrategy.noWatermarks(),
      "Kafka Source Topic Companies"
    )

    // TRANSFORM COMPANY IN A KEYED STREAM BY ID
    val keyedCompanyStream: KeyedStream[Company, Long] = companyStream.keyBy(_.id)

    // MAP DESCRIPTOR FOR USER
    val broadcastUserStateDescriptor = new MapStateDescriptor[Long, User]("broadcastUserState", classOf[Long], classOf[User])

    // BROADCAST USER STATE
    val broadcastUserStream = userStream.broadcast(broadcastUserStateDescriptor)

    // CONNECT COMPANY WITH USER STREAMS
    val connectedStreams = keyedCompanyStream.connect(broadcastUserStream)

    // PROCESS THE CONNECTED STREAMS JOINING THEM BY COMPANY ID
    val resultStream = connectedStreams.process(
      new KeyedBroadcastProcessFunction[Long, Company, User, String] {

        val UserStateDescriptor = new MapStateDescriptor[Long, User]("broadcastUserState", classOf[Long], classOf[User])

        override def processBroadcastElement(
                                              user: User,
                                              ctx: KeyedBroadcastProcessFunction[Long, Company, User, String]#Context,
                                              out: Collector[String]
                                            ): Unit = {
          // fetch the broadcast state = distributed variable
          val userState = ctx.getBroadcastState(UserStateDescriptor)
          // update the state
          userState.put(user.clientId, user)
        }

        override def processElement(
                                     company: Company,
                                     ctx: KeyedBroadcastProcessFunction[Long, Company, User, String]#ReadOnlyContext,
                                     out: Collector[String]
                                   ): Unit = {

          val tryUser: Try[User] = Try(ctx.getBroadcastState(UserStateDescriptor).get(company.id))

          tryUser match {
            case Success(user) if user != null => out.collect(s"company.fantasyName: ${company.fantasyName}, userName: ${user.firstName} ${user.lastName}")
            case Success(_) => out.collect(s"company.fantasyName: ${company.fantasyName}, userName: Unknown")
            case Failure(exception) => out.collect(s"Error retrieving user for company ${company.id}: ${exception.getMessage}")
          }

        }
      }
    )

    resultStream.print()

    env.execute()
  }

  def main(args: Array[String]): Unit = {
    kafkaConsumers()
  }
}
