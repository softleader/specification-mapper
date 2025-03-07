---
title: Starter
weight: 20
description: > 
  Spring Starter
---

{{< tabpane text=true >}}
  {{% tab "Maven" %}}
  ```xml
  <dependency>
    <groupId>tw.com.softleader.data.jakarta</groupId>
    <artifactId>specification-mapper-starter</artifactId>
    <version>${specification-mapper.version}</version>
  </dependency>
  ```
  {{% /tab %}}
  {{% tab "Java Module" %}}
  ```java
  requires specification.mapper;
  requires specification.mapper.starter;
  requires jakarta.persistence;
  ```
  {{% /tab %}}
  {{% tab "Logging" %}}
  ```yaml
  logging:
    level:
      tw.com.softleader.data.jpa.spec.starter: info
  ```
  {{% /tab %}}
{{< /tabpane >}}

The `specification-mapper-starter` integrates [specification-mapper](/docs/mapper) with [Spring Data JPA](https://spring.io/projects/spring-data-jpa) and provides a way to query by specifications.

Query by Spec (QBS) is a user-friendly querying approach that allows you to dynamically build query conditions using specifications. With the QBS interface, you can execute query statements easily.

## Getting Started

By adding the dependency in your `pom.xml` file, the `specification-mapper-starter` will automatically configure everything during the Spring Boot startup process, allowing you to start using it without any additional configuration. The starter includes the following features:

- Configuration for [Query By Spec](#query-by-spec)
- Registration of the default [`SpecMapper`](#default-specmapper)

The auto-configuration is enabled by default, and you can control it through the `spec.mapper.enabled` property in your application's properties file. To disable the auto-configuration, you can use the following configuration:

```yaml
spec:
  mapper:
    enabled: false
```
