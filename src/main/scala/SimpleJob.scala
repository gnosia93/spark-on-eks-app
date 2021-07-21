import java.util.Properties

import org.apache.spark.scheduler.{SparkListener, SparkListenerApplicationEnd}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object SimpleJob extends Serializable {

  def main(args: Array[String]) = {

    /* RDD
      val conf = new SparkConf().setAppName("SimpleJob").setMaster("local")
      val sc = new SparkContext(conf)
    */

    val spark = SparkSession.builder().appName("SimpleJob")
      //    .config("spark.master", "spark://ec2-13-125-199-100.ap-northeast-2.compute.amazonaws.com:7077")
          .config("spark.master", "local")
          .config("driver-memory", "300m")
          .config("executor-memory", "1g")
          .getOrCreate()

    spark.sparkContext.addSparkListener(new SparkListener() {
      override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit = {
        println("shutting down hook..")
      }
    })


    val df = spark.read.format("csv")
      .load("hdfs://localhost:9000/tmp/spark-test.csv")
    //  .load("hdfs://ec2-13-125-199-100.ap-northeast-2.compute.amazonaws.com:8020/tmp/airflow")
      .toDF("code", "message", "date")


    df.printSchema()
    val count = df.count().intValue()

    // pg_ctl -D /usr/local/var/postgres restart
    // psql -h localhost -U airline -d airline_db
    // create table tbl_airflow_dummy_cnt (cnt int);
    // save count result to table
    val schema = StructType( Array(
        StructField("cnt", IntegerType, nullable = false)
    ))
    val result = Seq(Row(count))
    val rdd = spark.sparkContext.parallelize(result)
    val countDF = spark.sqlContext.createDataFrame(rdd, schema)

    val props = new Properties()
    props.put("user", "airline")
    props.put("password", "airline")

    countDF.write
        .mode("append")
        .jdbc("jdbc:postgresql://localhost:5432/airline_db", "tbl_airflow_dummy_cnt", props)

  }
}


// https://spark.apache.org/docs/latest/running-on-yarn.html
// https://jx2lee.github.io/hadoop-troubleshoot_hadoop_client/
// https://blog.yannickjaquier.com/hadoop/setup-spark-and-intellij-on-windows-to-access-a-remote-hadoop-cluster.html
// https://www.programmersought.com/article/69443764843/    이것인듯.