package br.gov.sp.cps.fatecmm

import java.time.{Instant, LocalDate}

abstract class GenericProduct(
  val voucherCode: String,
  clientId: Long,
  userId: Long,
  val modal: String,
  createdAt: Instant,
  amountPaid: Double,
  currency: String
)

case class ExpenseProduct(
  override val voucherCode: String,
  clientId: Long,
  userId: Long,
  override val modal: String,
  createdAt: Instant,
  amountPaid: Double,
  currency: String,
  category: String
) extends GenericProduct(voucherCode, clientId, userId, modal, createdAt, amountPaid, currency)

case class FlightProduct(
  override val voucherCode: String,
  clientId: Long,
  userId: Long,
  override val modal: String,
  createdAt: Instant,
  amountPaid: Double,
  currency: String,
  cia: String,
  classType: String,
  hasLuggage: Boolean,
  iataOrigin: String,
  cityOrigin: String,
  departureDate: Instant,
  iataDestiny: String,
  cityDestiny: String,
  arrivalDate: Instant
) extends GenericProduct(voucherCode, clientId, userId, modal, createdAt, amountPaid, currency)

case class HotelProduct(
  override val voucherCode: String,
  clientId: Long,
  userId: Long,
  override val modal: String,
  createdAt: Instant,
  amountPaid: Double,
  currency: String,
  hotelName: String,
  numNights: Int,
  numPeople: Int,
  numStars: Int,
  dailyPrice: Double,
  hasBreakfast: Boolean,
  city: String,
  state: String,
  country: String,
  checkinDate: LocalDate,
  checkoutDate: LocalDate
) extends GenericProduct(voucherCode, clientId, userId, modal, createdAt, amountPaid, currency)

case class User(
  id: Long,
  clientId: Long,
  firstName: String,
  lastName: String,
  birthday: LocalDate,
  cpf: String,
  city: String,
  email: String,
  mobile_number: String
)

case class Company(
  id: Long,
  fantasyName: String,
  document: String,
  city: String,
  uf: String,
  country: String,
  domain: String,
  voucher: String,
  sector: String,
  createdAt: Instant,
  updatedAt: Instant
)