FROM openjdk:8u181
RUN mkdir -p /Users/aleksandrmutovkin/Pictures/captcha/
EXPOSE 8282/tcp
WORKDIR /app
COPY ./target/scala-2.12/PrimeScalaFriend-assembly-0.1.jar /app/PrimeScalaFriend-assembly-0.1.jar
CMD java -jar \/app\/PrimeScalaFriend-assembly-0.1.jar -server -Xmx2144 -XX:+UseG1GC