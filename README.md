FHIR Reference Server
=====================

This is an implementation of the HAPI FHIR server library, customised to provide an online set of FHIR reference resources for national profiles, valuesets and conformance resources, published through the NHS Developer Network.



Notes:
------

To deploy this into the root context in tomcat, you will need to edit the context in tomcat's conf/server.xml:

```
<Context docBase="fhir" path="/" reloadable="true" source="org.eclipse.jst.jee.server:fhir"/>
```

Deploying in Docker
-------------------

If you have Docker installed, you can easily build and deploy this in Docker.

First, build the WAR file:

```bash
mvn install
```

Then create and start the Docker image:

```bash
./build.sh
./deploy.sh
```

