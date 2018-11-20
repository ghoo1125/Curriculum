FROM jetty
COPY ./build/libs/curriculum-1.0.war $JETTY_BASE/webapps/ROOT.war
EXPOSE 8080
CMD java -jar $JETTY_HOME/start.jar
