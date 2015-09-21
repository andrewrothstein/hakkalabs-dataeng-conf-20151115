/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Source: https://github.com/apache/spark/blob/v1.5.0/examples/src/main/scala/org/apache/spark/examples/mllib/MovieLensALS.scala
 */


import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
 * Common utility to read and parse MovieLens data (http://grouplens.org/datasets/movielens/).
 */
object MovieLens {

  case class Rating(userId: Int, movieId: Int, rating: Float, timestamp: Long)

  object Rating {
    def parseRating(str: String): Rating = {
      val fields = str.split("\\s+")
      assert(fields.size == 4, str)
      Rating(fields(0).toInt, fields(1).toInt, fields(2).toFloat, fields(3).toLong)
    }
  }

  case class Movie(movieId: Int, title: String, release: String, videoRelease: String, url: String, genres: Seq[String])

  object Movie {
    def parseMovie(str: String): Movie = {
      val fields = str.split("\\|")
      assert(fields.size == 5 + 19)
      Movie(fields(0).toInt, fields(1), fields(2), fields(3), fields(4), fields.takeRight(19))
    }
  }

  def loadRatings(@transient sc: SparkContext, path: String): RDD[Rating] = {
    sc.textFile(path).map(Rating.parseRating)
  }

  def loadMovies(@transient sc: SparkContext, path: String): RDD[Movie] = {
    sc.textFile(path).map(Movie.parseMovie)
  }
}
