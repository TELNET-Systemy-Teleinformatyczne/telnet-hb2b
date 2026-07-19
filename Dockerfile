# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-11 AS build

ARG HB2B_BRANCH=telnet
ARG MAVEN_ARGS="-DskipTests"

WORKDIR /workspace

COPY . telnet-hb2b/
COPY --from=generic-utils . telnet-hb2b-generic-utils/
COPY --from=file-backend . telnet-hb2b-file-backend/

RUN if [ -d telnet-hb2b/.git ]; then git -C telnet-hb2b checkout "$HB2B_BRANCH"; fi

# Build shared dependencies first.
RUN mvn -B -ntp -f telnet-hb2b-generic-utils/pom.xml ${MAVEN_ARGS} install

# Break the test-scope dependency cycle between holodeckb2b-ebms3as4 and
# file-backend. Tests are skipped, so this placeholder is only needed for Maven
# dependency resolution and is overwritten by the real file-backend build below.
RUN mkdir -p /tmp/empty \
    && jar --create --file /tmp/file-backend-placeholder.jar -C /tmp/empty . \
    && mvn -B -ntp install:install-file \
        -DgroupId=org.holodeckb2b.extensions \
        -DartifactId=file-backend \
        -Dversion=2.0.0-telnet \
        -Dpackaging=jar \
        -Dfile=/tmp/file-backend-placeholder.jar

# Build and install Holodeck B2B modules first because file-backend depends on
# their local artifacts. The distribution is skipped here because it depends on
# file-backend.
RUN mvn -B -ntp -f telnet-hb2b/pom.xml ${MAVEN_ARGS} -pl '!modules/holodeckb2b-distribution' install

RUN mvn -B -ntp -f telnet-hb2b-file-backend/pom.xml ${MAVEN_ARGS} install

# Build the final Holodeck B2B distribution after all local dependencies are
# available.
RUN mvn -B -ntp -f telnet-hb2b/pom.xml ${MAVEN_ARGS} -pl modules/holodeckb2b-distribution -am package

RUN mkdir -p /workspace/runtime \
    && cd /workspace/runtime \
    && jar -xf /workspace/telnet-hb2b/modules/holodeckb2b-distribution/target/holodeckb2b-distribution-*.zip

FROM eclipse-temurin:11-jre AS runtime

ENV HB2B_HOME=/opt/holodeckb2b

WORKDIR ${HB2B_HOME}
COPY --from=build /workspace/runtime/holodeckb2b-7.0.0/ ${HB2B_HOME}/

RUN chmod +x ${HB2B_HOME}/bin/*.sh \
    && sed -i 's/\r$//' ${HB2B_HOME}/bin/*.sh \
    && mkdir -p ${HB2B_HOME}/logs

EXPOSE 8080

ENTRYPOINT ["/bin/sh", "/opt/holodeckb2b/bin/startServer.sh"]
