FROM asabino/wildfly:21.0.2.Final-jdk15

COPY ./WebServletSDP/target/WebServletSDP.war /opt/jboss/wildfly/standalone/deployments/

CMD["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]