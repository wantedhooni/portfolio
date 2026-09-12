

### com.internetitem.logback.elasticsearch.ElasticsearchAppender 방식
```
// https://mvnrepository.com/artifact/com.internetitem/logback-elasticsearch-appender
// implementation("com.internetitem:logback-elasticsearch-appender:1.6")


    <!-- ===================== -->
    <!-- Elasticsearch Appender -->
    <!-- ===================== -->
    <appender name="ELASTIC" class="com.internetitem.logback.elasticsearch.ElasticsearchAppender">

        <!-- ES endpoint -->
        <url>http://localhost:9200/_bulk</url>
        <!-- index -->
        <index>${APP_NAME:-spring-app}</index>

        <headers>
            <header>
                <name>Content-Type</name>
                <value>application/json</value>
            </header>
        </headers>


        <!-- 비동기 설정 -->
        <includeMdc>true</includeMdc>
        <maxQueueSize>10000</maxQueueSize>
        <maxRetries>3</maxRetries>
        <sleepTime>1000</sleepTime>
    </appender>


```