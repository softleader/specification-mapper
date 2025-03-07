---
title: Logging
weight: 80
---

To set `tw.com.softleader.data.jpa.spec.SpecMapper` to logging level _debug_, which prints more information during the object-to-spec conversion process:

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

If you prefer to control and set the Logger based on the converted objects, we offer another strategy. By setting the `ASTWriterFactory`, you can switch to using the Logger of the target object:

```java
var mapper = SpecMapper.builder()
      .defaultResolvers()
      .astWriterFactory(ASTWriterFactory.impersonation())
      .build();
```
