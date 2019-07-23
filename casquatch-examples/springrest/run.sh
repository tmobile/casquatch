NAME=springrest
PORT="90"$(( ( RANDOM % 50 )  + 42 ))
GENERATOR=../../casquatch-generator/target/casquatch-generator-2.0-SNAPSHOT.jar

echo "---------------------------------------------"
echo "Starting Docker (30 second pause to startup)"
echo "---------------------------------------------"
echo "Docker will be started with cassandra on 127.0.0.1:$PORT"
docker kill $NAME
docker run --rm  -p $PORT:9042 -d --name $NAME -d cassandra:latest
sleep 30

echo "---------------------------------------------"
echo "Installing CQL"
echo "---------------------------------------------"
docker exec -i $NAME cqlsh <<EOF
CREATE KEYSPACE $NAME WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE $NAME.table_name (
    key_one int,
    key_two int,
    col_one text,
    col_two text,
    PRIMARY KEY (key_one, key_two)
);
EOF

echo "---------------------------------------------"
echo "Configure project"
echo "---------------------------------------------"
sed -i .old "s/127.0.0.1:90../127.0.0.1:$PORT/" src/main/resources/application.conf
rm src/main/resources/application.conf.old

echo "---------------------------------------------"
echo "Generate Entity"
echo "---------------------------------------------"
java -Dconfig.file=src/main/resources/application.conf -jar $GENERATOR

mvn clean test spring-boot:run
