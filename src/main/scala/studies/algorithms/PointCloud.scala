package studies.algorithms

import java.util.Random

import org.apache.commons.math3.analysis.MultivariateFunction
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer.{Sigma, PopulationSize}
import org.apache.commons.math3.optim._
import org.apache.commons.math3.random.RandomGeneratorFactory

import scala.math._

case class PointCloud(points: List[Vector2d]) {
  def centerByMean: PointCloud = {
    PointCloud(points.map(_ - mean))
  }

  lazy val mean: Vector2d = points.reduce(_ + _) / points.length

  def squareErrorTo(other: PointCloud): Double = {
    def vectorPowerToTwo(v: Vector2d) = v.x * v.x + v.y * v.y

    def deltaToClosestPointInCloud(point: Vector2d, cloud: PointCloud): Double = {
      cloud.points.map(otherPoint => vectorPowerToTwo(point - otherPoint)).min
    }

    val thisMinDeltas = this.points.map(p => deltaToClosestPointInCloud(p, other))
    val otherMinDeltas = other.points.map(p => deltaToClosestPointInCloud(p, this))

    (thisMinDeltas.sum + otherMinDeltas.sum) / points.length
  }

  def downsample(samples: Int = 100): PointCloud = {
    new PointCloud(util.Random.shuffle(points).take(samples))
  }

  def width = lengthAt(_.x)
  def height = lengthAt(_.y)

  private def lengthAt(func: Vector2d => Double): Double = {
    val values = points.map(func)
    values.max - values.min
  }

  def alignByStandardDeviation(other: PointCloud): PointCloud = {
    val scale = other.standardDeviation /: this.standardDeviation
    PointCloud(points.map(_ *: scale))
  }

  lazy val standardDeviation: Vector2d = {
    val devs = points.map(p => {
      Vector2d(pow(p.x - mean.x, 2), pow(p.y - mean.y, 2))
    })

    val variance = devs.reduce(_ + _) / devs.length.toDouble

    Vector2d(sqrt(variance.x), sqrt(variance.y))
  }

  def toImagePixelArray(pointColor: Int, emptyColor: Int): Array[Int] = {
    // FIXME redesign the implementation
    val pixels = Array.fill((width * height).toInt)(emptyColor)

    for (point <- points) {
      val position: Int = (width * point.y + point.x).toInt
      pixels(position) = pointColor
    }

    pixels
  }

  def runCMAES(model: PointCloud): CMAESGuess = {
    val optimizer = new CMAESOptimizer(
      10,     // Maximal number of iterations.
      0.0001,  // Whether to stop if objective function value is smaller than stopFitness.
      true,    // isActiveCMA. Chooses the covariance matrix update method.
      5,       // Number of initial iterations, where the covariance matrix remains diagonal.
      3,       // Determines how often new random objective variables are generated in case they are out of bounds.
      RandomGeneratorFactory.createRandomGenerator(new Random()), // Random generator.
      false,   // Whether statistic data is collected.
      new SimpleValueChecker(0.001, 0.001) // Convergence checker.
    )

    val objectiveFunction = new ObjectiveFunction(new MultivariateFunction {
      override def value(p1: Array[Double]): Double = {
        val guess = CMAESGuess.fromDoubleArray(p1)
        val newCloud = transformByCMAESGuess(guess)
        newCloud.squareErrorTo(model)
      }
    })

    val result = optimizer.optimize(
      objectiveFunction,
      new MaxEval(100000),
      new InitialGuess(CMAESGuess.initialGuess.toDoubleArray),
      new SimpleBounds(CMAESGuess.lowerBounds.toDoubleArray, CMAESGuess.upperBounds.toDoubleArray),
      new Sigma(CMAESGuess.sigma.toDoubleArray),
      new PopulationSize(9)
    )

    println(result)

    // TODO real return value
    CMAESGuess.initialGuess
  }

  def transformByCMAESGuess(guess: CMAESGuess): PointCloud = {
    val translatedPoints = this.points.map(_ + guess.translation)
    val scaledPoints = translatedPoints.map(_ *: guess.scale)
    PointCloud(scaledPoints)
  }
}

object PointCloud {
  def fromImagePixelArray(pixels: Array[Int], imageWidth: Int, pointColor: Int): PointCloud = {
    new PointCloud(
      pixels
        .zipWithIndex
        .filter { case (value, _) => value == pointColor }
        .map { case (_, index) => new Vector2d((index % imageWidth).toDouble, (index / imageWidth).toDouble) }
        .toList
    )
  }
}