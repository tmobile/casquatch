NAME=springconfigserver
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

CREATE TABLE $NAME.configuration (
    application text,
    profile text,
    label text,
    key text,
    value text,
    PRIMARY KEY ((application, profile), label, key)
) WITH CLUSTERING ORDER BY (label ASC, key ASC);
EOF
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
