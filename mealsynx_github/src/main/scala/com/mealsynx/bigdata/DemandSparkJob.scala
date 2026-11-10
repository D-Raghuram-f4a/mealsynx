package com.mealsynx.bigdata

object DemandSparkJob {

  case class HistoricalOrderRecord(
      orderId: String,
      canteenId: String,
      foodId: String,
      dayOfWeek: String,
      isEventDay: Boolean,
      temperatureC: Double,
      quantitySold: Int,
      quantityWasted: Int
  )

  def computeContextMultipliers(records: Seq[HistoricalOrderRecord]): Map[String, Double] = {
    if (records.isEmpty) return Map("NORMAL" -> 1.0, "EVENT_DAY" -> 1.15, "HOT_WEATHER" -> 1.25)

    val eventDayAvg = records.filter(_.isEventDay).map(_.quantitySold.toDouble).sum /
      math.max(1, records.count(_.isEventDay))
    val normalDayAvg = records.filterNot(_.isEventDay).map(_.quantitySold.toDouble).sum /
      math.max(1, records.count(!_.isEventDay))

    val eventMultiplier = if (normalDayAvg > 0) eventDayAvg / normalDayAvg else 1.15
    Map(
      "NORMAL_BASELINE" -> normalDayAvg,
      "EVENT_MULTIPLIER" -> math.round(eventMultiplier * 100.0) / 100.0
    )
  }
}
