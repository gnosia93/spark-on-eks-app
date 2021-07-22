## sbt 사용법 ##
* https://joswlv.github.io/2017/08/06/howtousesbt/

```
$ cd sparkapp
$ sbt clean compile package
```

## jar 실행 파일 만들기 ##

[문제점]
```
ubuntu@ip-10-1-1-187:~/sparkapp$ jar -tf target/scala-2.12/sparkapp_2.12-0.1.jar
META-INF/MANIFEST.MF
SimpleJob$.class
SimpleJob$$anon$1.class
SimpleJob.class

ubuntu@ip-10-1-1-187:~/sparkapp$ java -jar target/scala-2.12/sparkapp_2.12-0.1.jar
Error: A JNI error has occurred, please check your installation and try again
Exception in thread "main" java.lang.NoClassDefFoundError: org/apache/spark/sql/SparkSession
	at java.lang.Class.getDeclaredMethods0(Native Method)
	at java.lang.Class.privateGetDeclaredMethods(Class.java:2701)
	at java.lang.Class.privateGetMethodRecursive(Class.java:3048)
	at java.lang.Class.getMethod0(Class.java:3018)
	at java.lang.Class.getMethod(Class.java:1784)
	at sun.launcher.LauncherHelper.validateMainClass(LauncherHelper.java:650)
	at sun.launcher.LauncherHelper.checkAndLoadMain(LauncherHelper.java:632)
Caused by: java.lang.ClassNotFoundException: org.apache.spark.sql.SparkSession
	at java.net.URLClassLoader.findClass(URLClassLoader.java:382)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:418)
	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:352)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:351)
	... 7 more
```

[해결방법]

* https://alvinalexander.com/scala/sbt-how-build-single-executable-jar-file-assembly/

(1) sbt-assembly plugin 추가
```
$ vi project/assembly.sbt
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")
```

(2) build.sbt 에 provided scope 추가 (provided 는 실행시에 runtime 이 해당 라이브러리를 제공)
```
name := "sparkapp"

version := "0.1"

scalaVersion := "2.12.12"

libraryDependencies ++= Seq (
  "org.apache.spark" %% "spark-core" % "3.1.1" % "provided",
  "org.apache.spark" %% "spark-sql" % "3.1.1" % "provided" ,
  "org.postgresql" % "postgresql" % "42.2.23"

)
```


## 실행하기 ##

* local 로 실행하기
```
ubuntu@ip-10-1-1-187:~/sparkapp$ spark-submit \
--master local \
--driver-class-path /home/ubuntu/.ivy2/cache/org.postgresql/postgresql/jars/postgresql-42.2.23.jar \
--jars /home/ubuntu/.ivy2/cache/org.postgresql/postgresql/jars/postgresql-42.2.23.jar \
target/scala-2.12/sparkapp-assembly-0.1.jar \
hdfs://ec2-13-125-199-100.ap-northeast-2.compute.amazonaws.com:8020/tmp/airflow/ \
jdbc:postgresql://bigdata-postgres.cwhptybasok6.ap-northeast-2.rds.amazonaws.com:5432/airline_db 


ubuntu@ip-10-1-1-187:~/sparkapp$ psql -h bigdata-postgres.cwhptybasok6.ap-northeast-2.rds.amazonaws.com -U airline -d airline_db
Password for user airline:
psql (12.7 (Ubuntu 12.7-0ubuntu0.20.04.1), server 13.3)
WARNING: psql major version 12, server major version 13.
         Some psql features might not work.
SSL connection (protocol: TLSv1.2, cipher: ECDHE-RSA-AES256-GCM-SHA384, bits: 256, compression: off)
Type "help" for help.

airline_db=> \d
                List of relations
 Schema |         Name          | Type  |  Owner
--------+-----------------------+-------+---------
 public | tbl_airflow_dummy     | table | airline
 public | tbl_airflow_dummy_cnt | table | airline
(2 rows)

airline_db=> select * from tbl_airflow_dummy_cnt;
    cnt
-----------
 103968400
(1 row)
```

* yarn 모드로 실행하기

[$YARN_CONF_DIR/yarn-site.xml]
```
<configuration>
 <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>ec2-13-125-199-100.ap-northeast-2.compute.amazonaws.com</value>
    </property>
</configuration>
```

[실행하기]
```
ubuntu@ip-10-1-1-187:~/sparkapp$ spark-submit \
--master yarn \
--driver-class-path /home/ubuntu/.ivy2/cache/org.postgresql/postgresql/jars/postgresql-42.2.23.jar \
--jars /home/ubuntu/.ivy2/cache/org.postgresql/postgresql/jars/postgresql-42.2.23.jar \
target/scala-2.12/sparkapp-assembly-0.1.jar \
hdfs://ec2-13-125-199-100.ap-northeast-2.compute.amazonaws.com:8020/tmp/airflow/ \
jdbc:postgresql://bigdata-postgres.cwhptybasok6.ap-northeast-2.rds.amazonaws.com:5432/airline_db 


Exception in thread "main" org.apache.spark.SparkException: When running with master 'yarn' either HADOOP_CONF_DIR or YARN_CONF_DIR must be set in the environment.
	at org.apache.spark.deploy.SparkSubmitArguments.error(SparkSubmitArguments.scala:631)
	at org.apache.spark.deploy.SparkSubmitArguments.validateSubmitArguments(SparkSubmitArguments.scala:271)
	at org.apache.spark.deploy.SparkSubmitArguments.validateArguments(SparkSubmitArguments.scala:234)
	at org.apache.spark.deploy.SparkSubmitArguments.<init>(SparkSubmitArguments.scala:119)
	at org.apache.spark.deploy.SparkSubmit$$anon$2$$anon$3.<init>(SparkSubmit.scala:1022)
	at org.apache.spark.deploy.SparkSubmit$$anon$2.parseArguments(SparkSubmit.scala:1022)
	at org.apache.spark.deploy.SparkSubmit.doSubmit(SparkSubmit.scala:85)
	at org.apache.spark.deploy.SparkSubmit$$anon$2.doSubmit(SparkSubmit.scala:1039)
	at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:1048)
	at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
ubuntu@ip-10-1-1-187:~/sparkapp$
```
