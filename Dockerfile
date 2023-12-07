FROM 		docker-registry.htrc.indiana.edu/tomcat9

MAINTAINER Data to Insight Center <d2i@indiana.edu>

# tomcat9 sets USER to tomcat, and WORKDIR to /opt/tomcat

USER root

# environment variables
ENV ARTIFACT_ID=rights-api
ENV TOMCAT_DIR=/opt/tomcat

# identity-helper.war should exist in the dir containing this
# Dockerfile
ADD target/$ARTIFACT_ID*.war $TOMCAT_DIR/webapps/$ARTIFACT_ID.war

RUN unzip -qq $TOMCAT_DIR/webapps/$ARTIFACT_ID.war -d $TOMCAT_DIR/webapps/$ARTIFACT_ID

RUN chown -R tomcat $TOMCAT_DIR/webapps/
RUN chmod 755 $TOMCAT_DIR/logs/


USER tomcat