import java.util.Properties

import com.twitter.chill.Base64.InputStream
import org.apache.spark.scheduler.{SparkListener, SparkListenerApplicationEnd}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object SimpleJob extends Serializable {

  var HDFS_URL: String = null;          // source
  var JDBC_URL: String = null;          // target
  var spark: SparkSession = null;       // spark session

  /*
  // properties - https://okky.kr/article/38761
  def readConfig() = {
    val props = new Properties()
    try {
      // 클래스 패스로 부터 읽어올 경우 "/SimpleJob.properties" 로 표시
      val is = SimpleJob.getClass.getClassLoader.getResourceAsStream("SimpleJob.properties")
      props.load(is)
      jdbcUrl = props.getProperty("jdbc-url")
      hdfsUrl = props.getProperty("hdfs-url")
    }
    catch {
      case ex: Exception => println("SimpleJob.properties loading error.")
    }
  }
  */

  def readParam(args: Array[String]) = {
      HDFS_URL = args(0)
      JDBC_URL = args(1)

      println(s"source $HDFS_URL" )
      println(s"target $JDBC_URL" )
  }

  def initSpark() = {
    /* RDD
      val conf = new SparkConf().setAppName("SimpleJob").setMaster("local")
      val sc = new SparkContext(conf)
    */
    spark = SparkSession.builder().appName("SimpleJob")
      .config("spark.master", "local")
      .getOrCreate()

    spark.sparkContext.addSparkListener(new SparkListener() {
      override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit = {
        println("shutting down hook..")
      }
    })
  }

  def read() = {
    val df = spark.read.format("csv")
      .load(HDFS_URL)
      .toDF("code", "message", "date")

    df.count().intValue()
  }

  def save(count: Int) = {
    // save to jdbc
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
      .jdbc(JDBC_URL, "tbl_airflow_dummy_cnt", props)
  }

  def main(args: Array[String]) = {

    readParam(args)
    val spark = initSpark()
    val count = read()
    save(count)
  }
}

// pg_ctl -D /usr/local/var/postgres restart
// psql -h localhost -U airline -d airline_db
// create table tbl_airflow_dummy_cnt (cnt int);
// save count result to table


