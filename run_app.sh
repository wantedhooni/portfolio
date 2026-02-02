git pull
./gradlew clean
./gradlew api-server:bootjar
chmod +x ./api-server/build/libs/*
sudo lsof -t -i TCP:8080 -sTCP:LISTEN | xargs -r sudo kill -9
export LOGSTASH_ENABLE=true
nohup java -jar ./api-server/build/libs/api-server-0.1.0.jar > api-server.log 2>&1 &