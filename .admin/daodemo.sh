docker kill dkr
.admin/docker $1
mvn -pl cassandradriver clean install
mvn -pl cassandragenerator clean package
cd cassandragenerator/target
java -jar CassandraGenerator-*.jar --output=tmp --keyspace=$1 --datacenter=datacenter1 --dao --overwrite=true --package
cd tmp
mkdir config
cat >> config/application.properties <<EOF
server.contextPath=/daodemo
server.port=8080
cassandraDriver.contactPoints=localhost
cassandraDriver.localDC=datacenter1
cassandraDriver.keyspace=$1
cassandraDriver.features.driverConfig=disabled
#logging.level.root=ERROR
#logging.level.com.tmobile.opensource.casquatch=TRACE
#logging.level.com.datastax.driver.core.RequestHandler=TRACE
EOF
mvn test
mvn spring-boot:run
