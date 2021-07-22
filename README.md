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
