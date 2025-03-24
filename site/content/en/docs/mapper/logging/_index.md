---
title: Logging
weight: 80
description: > 
  Logs the object-to-spec conversion process
---

Setting logging level `tw.com.softleader.data.jpa.spec.SpecMapper=debug` will print more details during the object-to-Spec conversion process, which can be very helpful for troubleshooting. The output will look like this:

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

## Logger Name

From the first line of the example above, you can see that the default Logger Name is `SpecMapper`. This allows for centralized logging configuration. However, if you prefer to control and configure logging based on the converted object, we provide an alternative strategy: using the target object’s Logger for output.

You can adjust this strategy by configuring `ASTWriterFactory`, for example:

```java
var mapper = SpecMapper.builder()
      .defaultResolvers()
      // Default is ASTWriterFactory.domain()
      .astWriterFactory(ASTWriterFactory.impersonation())
      .build();
```

The output will then look like this:

```
DEBUG 20297 --- [           main] my.package.CustomerCriteria  : --- Spec AST ---
...
```
