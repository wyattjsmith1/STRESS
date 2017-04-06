-- MySQL dump 10.13  Distrib 5.7.17, for macos10.12 (x86_64)
--
-- Host: localhost    Database: apache_projects
-- ------------------------------------------------------
-- Server version	5.7.17

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ProjectMetrics_libraries`
--

DROP TABLE IF EXISTS `ProjectMetrics_libraries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProjectMetrics_libraries` (
  `ProjectMetrics_id` bigint(20) NOT NULL,
  `libraries` varchar(255) DEFAULT NULL,
  KEY `FKjq8y0cu7qcj2twvdn2d6r5srb` (`ProjectMetrics_id`),
  CONSTRAINT `FKjq8y0cu7qcj2twvdn2d6r5srb` FOREIGN KEY (`ProjectMetrics_id`) REFERENCES `ProjectMetrics` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ProjectMetrics_libraries`
--

LOCK TABLES `ProjectMetrics_libraries` WRITE;
/*!40000 ALTER TABLE `ProjectMetrics_libraries` DISABLE KEYS */;
INSERT INTO `ProjectMetrics_libraries` VALUES (21,'junit:junit:4.12'),(21,'easymock:org.easymock:3.4'),(21,'hamcrest-all:org.hamcrest:1.3'),(21,'bcel:org.apache.bcel:6.0'),(24,'commons-rng-client-api:org.apache.commons:1.0'),(24,'junit:junit:4.11'),(24,'commons-rng-simple:org.apache.commons:1.0'),(24,'jmh-core:org.openjdk.jmh:${jmh.version}'),(24,'commons-rng-sampling:org.apache.commons:1.0'),(24,'jmh-generator-annprocess:org.openjdk.jmh:${jmh.version}'),(30,'jacl:jacl:1.2.6'),(30,'tcljava:jacl:1.2.6'),(30,'js:rhino:1.7R2'),(30,'xalan:xalan:2.7.0'),(30,'junit:junit:3.8.2'),(30,'commons-jexl:commons-jexl:1.1'),(30,'jython:jython:2.1'),(39,'junit:junit:4.12'),(39,'xz:org.tukaani:1.6'),(39,'powermock-api-mockito:org.powermock:${powermock.version}'),(39,'powermock-module-junit4:org.powermock:${powermock.version}'),(50,'junit:junit:4.11'),(50,'JUnitParams:pl.pragmatists:1.0.4'),(59,'geronimo-spec-jta:geronimo-spec:1.0.1B-rc4'),(69,'addressing:org.apache.axis2:null'),(69,'commons-logging:commons-logging:${commons.logging.version}'),(69,'axis2-mtompolicy:org.apache.axis2:null'),(69,'axis2-kernel:org.apache.axis2:null'),(69,'httpcore:org.apache.httpcomponents:4.0-beta1'),(69,'junit:junit:3.8.2'),(86,'junit-dataprovider:com.tngtech.java:${cs.junit.dataprovider.version}'),(86,'javax.inject:javax.inject:1'),(86,'junit:junit:${cs.junit.version}'),(86,'powermock-api-mockito:org.powermock:${cs.powermock.version}'),(86,'hamcrest-all:org.hamcrest:${cs.hamcrest.version}'),(86,'spring-test:org.springframework:${org.springframework.version}'),(86,'mockito-all:org.mockito:${cs.mockito.version}'),(86,'powermock-module-junit4:org.powermock:${cs.powermock.version}'),(106,'junit:junit:4.12'),(107,'jackson-core:com.fasterxml.jackson.core:2.6.4'),(107,'groovy:org.codehaus.groovy:2.4.5'),(107,'junit:junit:4.11'),(107,'commons-io:commons-io:2.4'),(107,'commons-jexl:org.apache.commons:2.1.1'),(107,'commons-logging:commons-logging:1.1.3'),(107,'jackson-databind:com.fasterxml.jackson.core:2.6.4'),(124,'junit:junit:${junitVersion}'),(137,'junit:junit:${junit.version}'),(137,'assertj-core:org.assertj:${assertj.version}'),(143,'guava:com.google.guava:null'),(154,'commons-logging:commons-logging:1.1.1'),(154,'log4j:log4j:1.2.16'),(154,'commons-vfs:commons-vfs:1.0'),(154,'javaee-api:org.apache.openejb:5.0-2'),(154,'jsch:com.jcraft:0.1.42'),(154,'apache-log4j-extras:log4j:1.1'),(154,'xstream:xstream:1.1.2'),(154,'hsqldb:hsqldb:1.8.0.7'),(154,'jmdns:javax.jmdns:3.4.1'),(154,'junit:junit:3.8.1'),(154,'geronimo-jms_1.1_spec:org.apache.geronimo.specs:1.0'),(156,'mail:javax.mail:1.4.3'),(156,'javaee-api:org.apache.openejb:5.0-2'),(156,'junit:junit:3.8.2'),(156,'oro:oro:2.0.8'),(156,'geronimo-jms_1.1_spec:org.apache.geronimo.specs:1.0'),(162,'jmockit:com.googlecode.jmockit:1.3'),(162,'log4j-over-slf4j:org.slf4j:${dep.slf4j.version}'),(162,'slf4j-api:org.slf4j:${dep.slf4j.version}'),(162,'netty-handler:io.netty:4.0.27.Final'),(162,'de.huxhorn.lilith.logback.appender.multiplex-classic:de.huxhorn.lilith:0.9.44'),(162,'junit:junit:${dep.junit.version}'),(162,'commons-io:commons-io:2.4'),(162,'snappy-java:org.xerial.snappy:1.1.2.6'),(162,'kerb-core:org.apache.kerby:${kerby.version}'),(162,'jcl-over-slf4j:org.slf4j:${dep.slf4j.version}'),(162,'guava:com.google.guava:${dep.guava.version}'),(162,'kerb-simplekdc:org.apache.kerby:${kerby.version}'),(162,'jul-to-slf4j:org.slf4j:${dep.slf4j.version}'),(162,'logback-classic:ch.qos.logback:1.0.13'),(162,'kerb-client:org.apache.kerby:${kerby.version}'),(162,'mockito-core:org.mockito:1.9.5'),(167,'slf4j-api:org.slf4j:null'),(167,'junit:junit:${junit.version}'),(167,'mockito-all:org.mockito:${mockito.version}'),(167,'slf4j-log4j12:org.slf4j:null'),(167,'log4j:log4j:null'),(167,'hamcrest-all:org.hamcrest:1.3'),(167,'force-shading:org.apache.flink:1.3-SNAPSHOT'),(167,'jsr305:com.google.code.findbugs:null'),(167,'powermock-api-mockito:org.powermock:${powermock.version}'),(167,'powermock-module-junit4:org.powermock:${powermock.version}'),(172,'junit:junit:null'),(178,'junit:junit:null'),(192,'fulcrum-yaafi:org.apache.turbine:1.0.7'),(192,'velocity:org.apache.velocity:1.7'),(192,'fulcrum-localization:org.apache.fulcrum:1.0.6'),(192,'slf4j-log4j12:org.slf4j:1.7.10'),(192,'fulcrum-crypto:org.apache.fulcrum:1.0.7'),(192,'commons-logging:commons-logging:1.2'),(192,'junit:junit:4.12'),(192,'uadetector-resources:net.sf.uadetector:2014.10'),(192,'fulcrum-factory:org.apache.fulcrum:1.1.0'),(192,'fulcrum-mimetype:org.apache.fulcrum:1.0.5'),(192,'jabsorb:org.jabsorb:1.3.2'),(192,'log4j:log4j:1.2.17'),(192,'servlet-api:javax.servlet:2.5'),(192,'fulcrum-intake:org.apache.fulcrum:1.2.2'),(192,'jython:org.python:2.7.0'),(192,'fulcrum-cache:org.apache.fulcrum:1.1.0'),(192,'mockito-core:org.mockito:2.0.2-beta'),(192,'fulcrum-security-api:org.apache.fulcrum:1.1.0'),(192,'commons-lang:commons-lang:2.6'),(192,'commons-beanutils:commons-beanutils:1.9.2'),(192,'commons-email:org.apache.commons:1.4'),(192,'avalon-framework-api:org.apache.avalon.framework:4.3.1'),(192,'commons-configuration:commons-configuration:1.10'),(192,'torque-runtime:org.apache.torque:4.0'),(192,'jcl-over-slf4j:org.slf4j:1.7.10'),(192,'fulcrum-upload:org.apache.fulcrum:1.0.5'),(192,'fulcrum-parser:org.apache.fulcrum:1.0.3'),(192,'commons-io:commons-io:2.4'),(192,'slf4j-api:org.slf4j:1.7.10'),(192,'fulcrum-quartz:org.apache.fulcrum:1.1.0'),(192,'commons-codec:commons-codec:1.10'),(192,'hsqldb:org.hsqldb:2.2.9'),(192,'fulcrum-pool:org.apache.fulcrum:1.0.4'),(192,'fulcrum-xslt:org.apache.fulcrum:1.1.0'),(192,'ecs:ecs:1.4.2'),(192,'commons-collections:commons-collections:3.2.2'),(192,'fulcrum-security-memory:org.apache.fulcrum:1.1.0'),(198,'findbugs-annotations:com.github.stephenc.findbugs:null'),(198,'junit:junit:null'),(198,'log4j:log4j:null'),(198,'mockito-all:org.mockito:null'),(205,'werken-xpath:werken-xpath:0.9.4'),(205,'commons-lang:commons-lang:2.1'),(205,'antlr:antlr:2.7.5'),(205,'jdom:jdom:1.0'),(205,'velocity:velocity:1.5'),(205,'junit:junit:3.8.1'),(205,'commons-collections:commons-collections:3.1'),(205,'ant:ant:1.6'),(207,'commons-lang:commons-lang:2.1'),(207,'velocity:velocity:1.5'),(207,'junit:junit:3.8.1'),(207,'commons-collections:commons-collections:3.1'),(207,'ant:ant:1.6'),(212,'junit:junit:null'),(212,'servlet-api:javax.servlet:null'),(212,'slf4j-api:org.slf4j:null'),(212,'slf4j-log4j12:org.slf4j:null'),(212,'jcl-over-slf4j:org.slf4j:null'),(212,'mockito-all:org.mockito:null'),(224,'junit:junit:${junit.version}'),(224,'mockito-core:org.mockito:1.8.5'),(224,'xml-resolver:xml-resolver:1.2'),(224,'commons-io:commons-io:1.3.2'),(224,'commons-logging:commons-logging:1.2'),(242,'junit:junit:4.11'),(260,'junit:junit:4.12'),(260,'commons-httpclient:commons-httpclient:3.1'),(260,'easymock:org.easymock:3.4');
/*!40000 ALTER TABLE `ProjectMetrics_libraries` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-04-05 21:29:59
