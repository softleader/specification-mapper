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

specification-mapper 是一套 [Specifications](https://docs.spring.io/spring-data/jpa/reference/jpa/specifications.html) 的產生器, 它讀取了 Object 中的欄位, 配合欄位上 Annotation 的定義, 來動態的建立查詢條件!

另外 [specification-mapper-starter](/docs/starter) 提供了 Spring Boot 的整合, 讓你可以零配置的在 Spring apps 中使用, 使用 Spring boot 的應用程式可以參考看看!
