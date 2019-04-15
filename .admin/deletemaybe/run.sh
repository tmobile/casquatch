cd ../../

mvn -q -pl cassandradriver clean install

mvn -q -pl cassandradriver -P cluster-tests clean package
docker cp cassandradriver/target/CassandraDriver-1.4-SNAPSHOT-test-jar-with-dependencies.jar casquatch-cluster-tests_seed_node_1:/cassandra-driver-cluster-tests.jar
docker exec -it casquatch-cluster-tests_seed_node_1 java -jar /cassandra-driver-cluster-tests.jar

mvn -q -pl cassandradriver-ee -P cluster-tests clean package
docker cp cassandradriver-ee/target/CassandraDriver-EE-1.4-SNAPSHOT-test-jar-with-dependencies.jar casquatch-cluster-tests_search_1:/cassandra-driver-ee-cluster-tests.jar
docker exec -it casquatch-cluster-tests_search_1 java -jar /cassandra-driver-ee-cluster-tests.jar
