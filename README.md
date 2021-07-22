* sbt 사용법
https://joswlv.github.io/2017/08/06/howtousesbt/



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

* sbt-assembly plugin 추가
```
$ vi project/assembly.sbt
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")
```

* build.sbt 에 provided scope 추가 (provided 는 실행시에 runtime 이 해당 라이브러리를 제공)
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

```
ubuntu@ip-10-1-1-187:~/sparkapp$ spark-submit --master local target/scala-2.12/sparkapp-assembly-0.1.jar 1 2
```
