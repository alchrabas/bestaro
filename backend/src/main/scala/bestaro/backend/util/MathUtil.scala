package bestaro.backend.util

object MathUtil {

  def avg(xs: Iterable[Double]): Double = {
    xs.sum / xs.size
  }

  def variance(xs: Iterable[Double]): Double = {
    val avg = MathUtil.avg(xs)

    xs.map(a => math.pow(a - avg, 2)).sum / xs.size
  }

  def stdDev(xs: Iterable[Double]): Double = math.sqrt(variance(xs))
}
