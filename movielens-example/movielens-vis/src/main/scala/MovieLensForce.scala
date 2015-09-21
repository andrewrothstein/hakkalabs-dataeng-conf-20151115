import java.io.PrintWriter

import org.apache.spark.sql.{Column, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.catalyst.dsl.expressions._
import org.apache.spark.sql.catalyst.expressions._
import org.apache.spark.sql.functions._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

object MovieLensForce {
  case class Node(name: String, id: Int,group: Int)
  case class Link(source: Int, target: Int, value: Int)

  def main(args: Array[String]) {
    val sc = new SparkContext(new SparkConf())
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._
    val ratings = MovieLens.loadRatings(sc, args(0)).cache()
    val movies = MovieLens.loadMovies(sc, args(1)).cache()

    val mostRatedMovies = ratings
      .toDF()
      .groupBy("movieId")
      .agg(new Column(Count(1)) as 'count)
      .sort(desc("count"))
      .limit(50)
      .select("movieId")
      .collect()
      .map(_.getInt(0))

    val ratingGraph = ratings
      .filter(r => mostRatedMovies.contains(r.movieId))
      .groupBy(_.userId)
      .flatMap {
        case (_, userRatings) =>
          userRatings.flatMap {
            r1 =>
              userRatings
                .filter(_.movieId > r1.movieId)
                .map(r2 => (r1.movieId, r2.movieId))
          }
      }
      .countByValue()

    val nodes = movies
      .filter(m => mostRatedMovies.contains(m.movieId))
      .collect()
      .toSeq
      .map(m => Node(m.title, m.movieId, m.genres.indexOf("1")))
    val movieIds = nodes.map(_.id).zipWithIndex.toMap
    val links = ratingGraph.toSeq.map {
      case ((movie1, movie2), count) => Link(movieIds(movie1), movieIds(movie2), count.toInt)
    }

    implicit val formats = DefaultFormats
    val json = ("nodes" -> nodes.map(Extraction.decompose)) ~ ("links" -> links.map(Extraction.decompose))
    new PrintWriter(args(2) + "/movies.json") { write(pretty(render(json))); close() }

    sc.stop()
  }
}
