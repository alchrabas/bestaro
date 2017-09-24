package bestaro.common.util

import Numeric.Implicits._

object MathUtil {

  def avg[T: Numeric](xs: Iterable[T]): Double = {
    xs.sum.toDouble() / xs.size
  }

  def variance[T: Numeric](xs: Iterable[T]): Double = {
    val avg = MathUtil.avg(xs)

    xs.map(_.toDouble).map(a => math.pow(a - avg, 2)).sum / xs.size
  }

  def stdDev[T: Numeric](xs: Iterable[T]): Double = math.sqrt(variance(xs))
}
