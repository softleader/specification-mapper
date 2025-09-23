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

[![Maven Central](https://img.shields.io/maven-central/v/tw.com.softleader.data.jakarta/specification-mapper-starter?color=orange)](https://central.sonatype.com/artifact/tw.com.softleader.data.jakarta/specification-mapper-starter)

specification-mapper-starter 整合了 [specification-mapper](/docs/mapper) 及 [Spring Data JPA](https://spring.io/projects/spring-data-jpa), 並提供了 Query by Spec 的查詢方式等

Query by Spec (QBS) 是一個  user-friendly 的查詢方式, 可以動態的建立查詢條件 ([Specifications](https://docs.spring.io/spring-data/jpa/reference/jpa/specifications.html)), 透過 QBS interface 就可以執行查詢語句!

## Getting Started

只要在 `pom.xml` 中加入 dependency, 此 Starter 在 Spring Boot 啟動過程就會自動的配置一切, 讓你可以零配置的就開始使用, 包含了:

- [Query By Spec](/docs/starter/qbs/) 的設定
- 註冊預設的 [`SpecMapper`](/docs/starter/spec/)


自動配置預設是啟用的, 你可以透過 properties 中的 `spec.mapper.enabled` 控制, 如要關閉則:

```yaml
spec:
  mapper:
    enabled: false
```
