mvn versions:set -DnewVersion=1.0.1
mvn clean deploy -P release -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true -P release