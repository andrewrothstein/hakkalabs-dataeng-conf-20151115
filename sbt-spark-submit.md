---
title: "sbt-spark-submit"
author: "Forest Fang"
date: '2015-09-24'
subtitle: Straight-through Build to Spark Analytics
---

Financial Modeling Group - Advanced Data Analytics team (FMGADA) is the large scale data science arm of FMG within BlackRock Solutions. We focus on building scalable behavioral models for mortgages as well as providing training and consulting for many other groups who use Hadoop platform for data ETL, OLAP, and Machine Learning.
Recently we have shifted our primary distributed computing toolchain to Apache Spark due to its performance and friendly programming interface in Scala.

> [Apache Spark<sup>TM</sup>](http://spark.apache.org/) is a fast and general engine for large-scale data processing.

The flexible execution engine and in-memory first computing principle allow Spark to achieve much better performance than traditional MapReduce. Since adopting Apache Spark, we are able to finish individual data science iteration within minutes compared to hours on TB sized datasets.

## Spark First World Problem

While we greatly enjoy the benefit of having the statically typed Scala language which is expressive and functional, the developer experience isn't nearly as perfect. Data science projects require a lot of iterations from data cleaning, feature extraction to model tuning. The performance of Spark is very impressive but submitting a Spark application can still be tedious.

<div class="centered">
  <img src="https://i.imgflip.com/r4xnp.jpg" width="300px" />
</div>


To submit a Spark application as an application developer, you need to

1. Create an uber JAR that contains all the dependencies
```
sbt myproject/assembly
```

2. Upload JAR to a host co-located Spark cluster
```
scp myproject/target/scala-2.10/vis-assembly-x.y.z-SNAPSHOT.jar my-awesome-spark-cluster:
```

3. The *spark-submit* script in Spark's bin directory is used to launch applications on a cluster.^[http://spark.apache.org/docs/latest/submitting-applications.html]
```
# Run on a Spark Standalone cluster in client deploy mode
$SPARK_HOME/bin/spark-submit \
  --class <super long fully qualified class name, e.g. org.apache.spark.examples.SparkPi> \
  --master spark://<who remember this Spark master ip address, e.g. 207.184.161.138>:7077 \
  --executor-memory 20G \
  --total-executor-cores 100 \
  --conf "<crazy but useful Spark configuration for tuning, e.g. spark.shuffle.memoryFraction=0.7>" \
  /path/to/application.jar \
  1000 \
  <other application arguments>
```

Notice that

- *spark-submit* commands are difficult to remember
- There is no easy way to store different pairs of application arguments (such as a generic visualization program on different dataset with different tuning parameters)
- Above all, none of the steps are instantaneous resulting significant downtime for developers especially if you do development and submit Spark applications more than a couple times per day.

<div class="centered">
  <img src="http://geekshumor.com/wp-content/uploads/2013/06/My-codes-compiling.png" width="300px" />^[Source: https://xkcd.com/303/]
</div>

As developers, we are pretty effective at multitasking when we can switch tasks every hour or so. However these steps takes more than a few seconds but never more than one minute or two resulting either a long blank wait
or delay in executing the following steps if you chose to do other things while waiting.

## *sbt-spark-submit* Plugin to Rescue

That's why we developed *[sbt-spark-submit](https://github.com/saurfang/sbt-spark-submit)* plugin to streamline this process. If you are not already familiar with *sbt*,

> [sbt](http://www.scala-sbt.org/) is an interactive build tool. Use Scala to define your tasks. Then run them in parallel from the shell.

Instead of [XML](http://www.lyberty.com/img/xml-sucks.gif), you have

- *build.sbt* which uses an elegant Scala DSL for build definition 
- *project/\*.scala* which leverage full power of Scala to configure the build and create custom tasks

Here is how to make this plugin change you life:

### Enable *sbt-spark-submit*

In `project/plugins.sbt` add

```scala
addSbtPlugin("com.github.saurfang" % "sbt-spark-submit" % "0.0.4")
```

to add plugin to your build.

In `project/SparkSubmit.scala` add

```scala
object SparkSubmit {
  lazy val settings =
    SparkSubmitSetting("sparkPi", Seq("--class", "SparkPi"))
}
```

to declare custom sbt task using *sbt-spark-submit*

In `build.sbt` add

```scala
SparkSubmit.settings: _*
```

to import the settings you just defined to project.

With these changes, we defined a *sbt* task called *sparkPi*, which replaces above three steps with simply `sbt sparkPi` command. It will build the JAR and run *spark-submit* for *sparkPi* in local mode with single button push.

### Enable *sbt-spark-submit* YARN mode

To submit *SparkPi* application to a cluster such as YARN running on Hadoop, we need to enable YARN mode plugin:

In `build.sbt`

```scala
//fill in default YARN settings
enablePlugins(SparkSubmitYARN)
//submit the assembly jar with all dependencies
sparkSubmitJar := assembly.value.getAbsolutePath
//now blend in the sparkSubmit settings
SparkSubmit.settings
```

Set `$HADOOP_CONF_DIR` to pick up YARN configuration so the plugin knows where is Hadoop namenode. Now `sbt sparkPi` will not only build the JAR but also run *spark-submit* using *yarn-cluster* as master. Spark application will run on your YARN cluster without your leaving your development environment.

### Override Settings with sbt

Suppose now we want to submit our application to a standalone cluster running on top of AWS EC2, we need to somehow figure out where is our Spark master. EC2 is very cost effective that you can bring Spark cluster up and down on-demand. However that also means your Spark master host will likely change in each restart. It is also bad form to hardcode and mix your deployment configuration with application code.

*sbt* build is a program in itself, meaning we can take advantage of all existing Java/Scala libraries to build the build. For example, we want to add *aws-java-sdk* to our build (not the project itself), we can do so in `project/plugins.sbt`:

```scala
libraryDependencies ++= Seq(
  "com.github.seratch" %% "awscala"          % "0.5.3" excludeAll ExclusionRule(organization = "com.amazonaws"),
  "com.amazonaws"      %  "aws-java-sdk-s3"  % "1.10.1",
  "com.amazonaws"      %  "aws-java-sdk-ec2" % "1.10.1"
)
```

Now in `project/SparkSubmit.scala`, we are able to query EC2 services directly and find address to our Spark master.

```scala
task.settings(sparkSubmitSparkArgs in task := {
  Seq(
    "--master", getMaster.map(i => s"spark://${i.publicDnsName}:6066").getOrElse(""),
    "--deploy-mode", "cluster",
    "--class", "SparkPi"
  )
}
...
def getMaster: Option[Instance] = {
  ec2.instances.find(_.securityGroups.exists(_.getGroupName == clusterName + "-master"))
}
```

For more information, please see detailed [example](https://github.com/saurfang/sbt-spark-submit/tree/master/examples/sbt-assembly-on-ec2) in the plugin repository.

## Conclusion

In this post, we have seen how *sbt-spark-submit* plugin can help you

- Streamline build, upload and submit into single task
- Codify *spark-submit* command settings
- Extend beyond basic capability by leveraging full functionality of Scala

It allows you to focus on your code change by automating the process of building and submitting Spark applications. We hope this plugin will save you tons of hours of productivity just like how it served us.

To learn more about *sbt-spark-submit* plugin, please visit the Github repo at https://github.com/saurfang/sbt-spark-submit.
You can also view our meetup slides at https://github.com/saurfang/nyc-spark-meetup
