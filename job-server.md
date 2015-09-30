---
title: "Accessible Big Data"
author: "David Durst"
date: '2015-09-24'
---

Financial Modeling Group - Advanced Data Analytics team (FMGADA) is the large scale data science arm of FMG within BlackRock Solutions. We focus on applying "Big Data", data sets that contain many points and rapidly acquire more, to financial problems such as building behavioral models for mortgages. Apache Spark is our primary tool for managing the the computational power and storage capacity necessary to handle these data sets.

## Big Data's Accessibility Problem

Typical Big Data tools sacrifice accessibility for power and scalability. The tools can handle data sets too large and computations too complex for a single computer by distributing them across clusters of machines. However, big data tools provide rudimentary interfaces for users to interact with the powerful backends. CLIs like Spark's spark-shell are more complicated and have fewer features than the tools that business users are currently comfortable with, such as Excel. Excel enables users to load data into a table, process it, and make visualizations using simple GUIs. The spark-shell terminal requires the user to understand the Scala language and the implementation details of Spark to load and process data. Additionally, the user must develop external visualization systems. This rudimentary interface is too complicated for business users.

<div class="centered">
  <img src="https://raw.githubusercontent.com/bbouille/start-spark/master/src/spark-shell-standalone.png" width="600px" /> <br />
</div>

The failings of big data's user interfaces limit its adoption. Business users fail to understand the tools and therefore do not want to use them in their daily lives. Instead, they stay within their comfort zone: small data sets in Excel. More accessible interfaces will lower the barrier to entry, help users understand why they need big data, and drive adoption.

## User Friendly Big Data

The Scatterplot Matrix (SPLOM) visualization demonstrates the effectiveness of user friendly interfaces for big data systems. Unlike terminals, the interactive visualization enables business users to understand data without knowing Scala or Spark. The users can look at combinations of scatterplots to understand the distribution of the data in all relevant dimensions. The colors show how a k-means clustering analysis breaks down the data set into subsets of related points. Finally, the buttons above the visualization provide the functionality necessary to alter the visualization to suit the needs of the user, such as adding or removing categories from the matrix.

<div class="centered">
  <img src="images/3/scatterplot.png" width="600px" /> <br />
</div>

The resample button demonstrates how user friendly visualizations enable users to interact with a Spark cluster without knowing the details of Spark or Scala. The data in the SPLOM is a random sample of a larger data set. The resample button resamples the underlying data set and draws a new set of points in the SPLOM. It does this by firing a job on the cluster to select a new random sample. When the cluster finishes, the webpage automatically draws the new sample on the screen.  This is necessary to ensure that the user sees random samples that are representative of the underlying data set. A single sample may be skewed and present an incorrect picture. The ability to resample on the fly enables the user to leverage the full power of big data systems in an intuitive, visual manner.

## The Tech Behind The User Friendly Interface
The resample button at the top of the visualization enables the user to perform complicated operations on the cluster without knowing any of the technical details. When pressing the button, an AJAX request is sent to a server program known as the Spark Job Server. The Job Server processes the request and runs the job on the Spark cluster. After the job finishes, the Job Server gets the result of the job and places it in a location that is accessible by another AJAX call. The website long polls the Job Server to get this result, a new random sample of data points, and renders the result.

Follow the below steps to spin up your own copy of this stack on EC2:
<ol>
<li>Clone the repo at https://github.com/David-Durst/spark-jobserver</li>
<li>Setup AWS CLI</li>
<li>Copy config/user-ec2-settings.sh.template to config/user-ec2-settings.sh and modify to fit your environment</li>
<li>Run bin/ec2_deploy.sh</li>
<li>Run bin/ec2_example.sh</li>
<li>Open the scatterplot matrix in the web browser with the URL printed by the script</li>
</ol>

To learn more about the Spark Job Server, please visit the Github repo at https://github.com/spark-jobserver/spark-jobserver.