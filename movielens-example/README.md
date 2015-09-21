Example Project for `sbt-spark-submit` plugin that streamlines the process of Spark application 
development and deployment

Available tasks:

- `sbt getData` downloads the MovieLens 100k data and unzip them under the project directory
- `sbt sparkMovieLensALS` runs ALS algorithm to perform CF under `ml` project
- `sbt sparkMovieLensForce` generate the JSON file for force diagram under `vis` project

Note: I ran into stackoverflow exception when running ALS under local mode. 
Workaround for me is to add `SBT_OPTS=-Xss1m`, which I feel absolutely disgusted suggesting!
