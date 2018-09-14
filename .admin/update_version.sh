#!/bin/bash
#
# Helper script to update the version number
#

OLD_VERSION=$1
NEW_VERSION=$2

sed -i 's/'$OLD_VERSION'/'$NEW_VERSION'/g' pom.xml
sed -i 's/'$OLD_VERSION'/'$NEW_VERSION'/g' pom_install.xml
sed -i 's/'$OLD_VERSION'/'$NEW_VERSION'/g' springconfigserver/pom.xml
sed -i 's/'$OLD_VERSION'/'$NEW_VERSION'/g' cassandradriver/pom.xml
sed -i 's/'$OLD_VERSION'/'$NEW_VERSION'/g' cassandradriver-ee/pom.xml
sed -i 's/'$OLD_VERSION'/'$NEW_VERSION'/g' cassandragenerator/pom.xml
sed -i 's/'$OLD_VERSION'/'$NEW_VERSION'/g' cassandragenerator/src/main/resources/templates/pom_models.ftl
sed -i 's/'$OLD_VERSION'/'$NEW_VERSION'/g' install.sh
