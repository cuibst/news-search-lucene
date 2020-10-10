FROM maven:3.6.3-openjdk-15

ENV HOME=/opt/news-search-lucene
WORKDIR $HOME

# install dependency only
COPY . $HOME
RUN mvn -q -s settings.xml dependency:resolve

RUN mvn -q -s settings.xml clean package -DskipTests

EXPOSE 80

CMD java -jar target/news-search-lucene.jar
