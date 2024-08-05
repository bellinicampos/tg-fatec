package br.gov.sp.cps.fatecmm

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.util.Collector

object SplitStream {
  // OUTPUT TAGS
  val flightTag = new OutputTag[GenericProduct]("flight-tag")
  val hotelTag = new OutputTag[GenericProduct]("hotel-tag")
  val expenseTag = new OutputTag[GenericProduct]("expense-tag")
}

class SplitStream extends ProcessFunction[GenericProduct, GenericProduct] {
  import SplitStream._

  override def processElement(
                               voucher: GenericProduct,
                               ctx: ProcessFunction[GenericProduct, GenericProduct]#Context,
                               out: Collector[GenericProduct]
                             ): Unit = {
    if (voucher.modal == "FLIGHT") {
      ctx.output(flightTag, voucher)
    } else if (voucher.modal == "HOTEL") {
      ctx.output(hotelTag, voucher)
    } else if (voucher.modal == "EXPENSE") {
      ctx.output(expenseTag, voucher)
    } else {
      out.collect(voucher)
    }
  }
}