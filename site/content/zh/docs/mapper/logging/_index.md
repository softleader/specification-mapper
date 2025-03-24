---
title: Logging
weight: 80
description: > 
  物件轉換過程的日誌輸出
---

在 Logging 等級中設定 `tw.com.softleader.data.jpa.spec.SpecMapper=debug`, 會在物件轉換成 Spec 的過程中印出更多資訊, 可以有效的幫助查找問題, 輸出類似如:

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

### Logger Name

從上面範例的第一行可以看到, 預設的 Logger Name 是使用 `SpecMapper`, 這可以方便你統一的設定, 如果你喜歡根據轉換的物件來控制和設定 Logging, 我們提供了另一種策略, 將 Logger Name 改成使用目標物件的 Logger 來輸出

通過設定 `ASTWriterFactory` 來調整策略, 你可以調整例如:

```java
var mapper = SpecMapper.builder()
      .defaultResolvers()
      // 預設為 ASTWriterFactory.domain()
      .astWriterFactory(ASTWriterFactory.impersonation()) 
      .build();
```

輸出會類似:

```
DEBUG 20297 --- [           main] my.package.CustomerCriteria  : --- Spec AST ---
...
```
