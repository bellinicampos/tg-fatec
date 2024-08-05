package br.gov.sp.cps.fatecmm

// APACHE FLINK LIBRARIES
import org.apache.flink.streaming.api.scala._
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation

// JSON HANDLING LIBRARY
import play.api.libs.json._

// JAVA LIBRARIES FOR WORKING WITH TIME
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate}

/**
 * User deserialization class, the input is a json like string
 */
class UserDeserializer extends DeserializationSchema[User] {
  override def deserialize(message: Array[Byte]): User = {
    val jsonString = Json.parse(message)

    // EXTRACTING FIELDS FROM JSON
    val id = (jsonString \ "id").as[Long]
    val clientId = (jsonString \ "clientId").as[Long]
    val firstName = (jsonString \ "firstName").as[String]
    val lastName = (jsonString \ "lastName").as[String]
    val birthday_temp = (jsonString \ "birthday").as[String]
    val cpf = (jsonString \ "cpf").as[String]
    val city = (jsonString \ "city").as[String]
    val email = (jsonString \ "email").as[String]
    val mobile_number = (jsonString \ "mobile_number").as[String]

    // PARSING STRING TO DATE
    val birthday = LocalDate.parse(birthday_temp, DateTimeFormatter.ISO_DATE)

    // CONSTRUCTOR
    User(id, clientId, firstName, lastName, birthday, cpf, city, email, mobile_number)
  }
  override def isEndOfStream(nextElement: User): Boolean = false
  override def getProducedType: TypeInformation[User] = implicitly[TypeInformation[User]]
}

/**
 * Company deserialization class, the input is a json like string
 */
class CompanyDeserializer extends DeserializationSchema[Company] {
  override def deserialize(message: Array[Byte]): Company = {
    val jsonString = Json.parse(message)

    // EXTRACTING FIELDS FROM JSON
    val id = (jsonString \ "id").as[Long]
    val fantasyName = (jsonString \ "fantasyName").as[String]
    val document = (jsonString \ "document").as[String]
    val city = (jsonString \ "city").as[String]
    val uf = (jsonString \ "uf").as[String]
    val country = (jsonString \ "country").as[String]
    val domain = (jsonString \ "domain").as[String]
    val voucher = (jsonString \ "voucher").as[String]
    val sector = (jsonString \ "sector").as[String]
    val createdAt_temp = (jsonString \ "createdAt").as[String]
    val updatedAt_temp = (jsonString \ "updatedAt").as[String]

    // PARSING STRING TO INSTANT
    val createdAt = Instant.parse(createdAt_temp)
    val updatedAt = Instant.parse(updatedAt_temp)

    // CONSTRUCTOR
    Company(
      id, fantasyName, document, city, uf, country,
      domain, voucher, sector, createdAt, updatedAt
    )
  }
  override def isEndOfStream(nextElement: Company): Boolean = false
  override def getProducedType: TypeInformation[Company] = implicitly[TypeInformation[Company]]
}

/**
 * Voucher deserialization class, the input is a json like string
 */
class VoucherDeserializer extends DeserializationSchema[GenericProduct] {
  override def deserialize(message: Array[Byte]): GenericProduct = {
    val jsonString = Json.parse(message)

    // EXTRACTING FIELDS FROM JSON
    val voucherCode = (jsonString \ "voucherCode").as[String]
    val clientId = (jsonString \ "clientId").as[Long]
    val userId = (jsonString \ "userId").as[Long]
    val modal = (jsonString \ "modal").as[String]
    val createdAt_temp = (jsonString \ "createdAt").as[String]
    val createdAt = Instant.parse(createdAt_temp)
    val amountPaid = (jsonString \ "amountPaid").as[Double]
    val currency = (jsonString \ "currency").as[String]

    modal match {

      case "FLIGHT" =>
        val cia = (jsonString \ "cia").as[String]
        val classType = (jsonString \ "classType").as[String]
        val hasLuggage = (jsonString \ "hasLuggage").as[Boolean]
        val iataCodeOrigin = (jsonString \ "iataOrigin").as[String]
        val cityOrigin = (jsonString \ "cityOrigin").as[String]
        val departureDate_temp = (jsonString \ "departureDate").as[String]
        val departureDate = Instant.parse(departureDate_temp)
        val iataCodeDestiny = (jsonString \ "iataDestiny").as[String]
        val cityDestiny = (jsonString \ "cityDestiny").as[String]
        val arrivalDate_temp = (jsonString \ "arrivalDate").as[String]
        val arrivalDate = Instant.parse(arrivalDate_temp)
        // CONSTRUCTOR
        FlightProduct(
          voucherCode, clientId, userId, modal, createdAt, amountPaid, currency, cia, classType,
          hasLuggage, iataCodeOrigin, cityOrigin, departureDate, iataCodeDestiny, cityDestiny, arrivalDate
        )

      case "HOTEL" =>
        val hotelName = (jsonString \ "hotelName").as[String]
        val numNights = (jsonString \ "numNights").as[Int]
        val numPeople = (jsonString \ "numPeople").as[Int]
        val numStars = (jsonString \ "numStars").as[Int]
        val dailyPrice = (jsonString \ "dailyPrice").as[Double]
        val hasBreakfast = (jsonString \ "hasBreakfast").as[Boolean]
        val city = (jsonString \ "city").as[String]
        val state = (jsonString \ "state").as[String]
        val country = (jsonString \ "country").as[String]
        val checkinDate_temp = (jsonString \ "checkinDate").as[String]
        val checkinDate = LocalDate.parse(checkinDate_temp, DateTimeFormatter.ISO_DATE)
        val checkoutDate_temp = (jsonString \ "checkoutDate").as[String]
        val checkoutDate = LocalDate.parse(checkoutDate_temp, DateTimeFormatter.ISO_DATE)

        // CONSTRUCTOR
        HotelProduct(
          voucherCode, clientId, userId, modal, createdAt, amountPaid, currency, hotelName, numNights,
          numPeople, numStars, dailyPrice, hasBreakfast, city, state, country, checkinDate, checkoutDate
        )

      case "EXPENSE" =>
        val category = (jsonString \ "category").as[String]
        // CONSTRUCTOR
        ExpenseProduct(voucherCode, clientId, userId, modal, createdAt, amountPaid, currency, category)
    }

  }
  override def isEndOfStream(nextElement: GenericProduct): Boolean = false
  override def getProducedType: TypeInformation[GenericProduct] = implicitly[TypeInformation[GenericProduct]]
}