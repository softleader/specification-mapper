---
title: Logging
weight: 80
---

將 `tw.com.softleader.data.jpa.spec.SpecMapper` 設定為 *debug*, 會在物件轉換成 Spec 的過程中印出更多資訊, 可以有效的幫助查找問題, 如:

```
DEBUG 20297 --- [           main] t.c.softleader.data.jpa.spec.SpecMapper  : --- Spec AST ---
+-[CustomerCriteria]: my.package.CustomerCriteria
|  +-[CustomerCriteria.firstname]: @Spec(value=Equals, path=, not=false) -> Equals[path=name, value=matt]
|  +-[CustomerCriteria.address]: my.package.AddressCriteria (NestedSpecificationResolver)
|  |  +-[AddressCriteria.county]: @Spec(value=Equals, path=, not=false) -> null
|  |  +-[AddressCriteria.city]: @Spec(value=Equals, path=, not=false) -> Equals[path=name, value=Taipei]
|  \-[CustomerCriteria.address]: Conjunction[specs=[Equals[path=city, value=Taipei]]]
\-[CustomerCriteria]: Conjunction[specs=[Equals[path=name, value=matt], Conjunction[specs=[Equals[path=city, value=Taipei]]]]]
```

如果你喜歡根據轉換的物件來控制和設定 Logging, 我們提供了另一種策略, 通過設定 `ASTWriterFactory`, 可以改成使用目標物件的 Logger 來輸出:

```java
var mapper = SpecMapper.builder()
      .defaultResolvers()
      .astWriterFactory(ASTWriterFactory.impersonation())
      .build();
```
