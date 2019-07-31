cd ../../
mvn -pl cassandradriver -P cluster-filter-tests clean package
docker cp cassandradriver/target/CassandraDriver-1.4-SNAPSHOT-test-jar-with-dependencies.jar casquatch-cluster-tests_seed_node_1:/tests.jar
docker exec -it casquatch-cluster-tests_seed_node_1 java -jar /tests.jar
