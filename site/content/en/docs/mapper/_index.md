---
title: Mapper
weight: 10
description: > 
  Core Domain
---

{{< tabpane text=true >}}
  {{% tab "Maven" %}}
  ```xml
  <dependency>
    <groupId>tw.com.softleader.data.jakarta</groupId>
    <artifactId>specification-mapper</artifactId>
    <version>${specification-mapper.version}</version>
  </dependency>
  ```
  {{% /tab %}}
  {{% tab "Java Module" %}}
  ```java
  requires specification.mapper;
  requires jakarta.persistence;
  ```
  {{% /tab %}}
  {{% tab "Logging" %}}
  ```yaml
  logging:
    level:
      tw.com.softleader.data.jpa.spec: info
  ```
  {{% /tab %}}
{{< /tabpane >}}

[![Maven Central](https://img.shields.io/maven-central/v/tw.com.softleader.data.jakarta/specification-mapper?color=orange)](https://central.sonatype.com/artifact/tw.com.softleader.data.jakarta/specification-mapper)

specification-mapper is a generator for [Specifications](https://docs.spring.io/spring-data/jpa/reference/jpa/specifications.html). It reads the fields from an object and dynamically creates query conditions based on the definitions of the fields' annotations.

In addition, [specification-mapper-starter](/docs/starter) provides integration with Spring Boot, allowing you to use it effortlessly in Spring apps without any configuration. We highly recommend checking it out if you are using a Spring Boot application!
