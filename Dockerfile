FROM maven:3-jdk-8-slim

ENV HOME=/opt/news-search-lucene
WORKDIR $HOME

# install dependency only
COPY . $HOME
RUN mvn -q -s settings.xml dependency:resolve

RUN mvn -q -s settings.xml package -DskipTests

EXPOSE 80

CMD java -jar target/news-search-lucene.jar --spring.profiles.active=prod
