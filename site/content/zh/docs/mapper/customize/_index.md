---
title: Customize Specs
weight: 70
---

延續 [Extending @Spec](/docs/mapper/simple/#extending-spec) 章節範例, 進階一點現在我們希望可以將 Entity Class 設計成可以配置, 這樣才能在 Customer 以外的 Entity 都可以使用!

要完成這需求我們需要在 Annotation 中定義更多參數, 因此 Simple @Spec 不適用了, 我們需要的是定義新的 Annotation, 完整的程式碼如下:

首先我們定義 Annotation:

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface MaxCreatedTime {

  Class<?> from();
}
```

接著我們要撰寫負責處理 `@MaxCreatedTime` 的邏輯, 透過實作 `SpecificationResolver` 來擴充:

```java
public class MaxCreatedTimeSpecificationResolver implements SpecificationResolver {

  @Override
  public boolean supports(Databind databind) { 
    // 這邊告訴 SpecMapper 什麼時候要使用此 resolver
    return databind.getField().isAnnotationPresent(MaxCreatedTime.class);
  }

  @Override
  public Specification<Object> buildSpecification(Context context, Databind databind) {
    var def = databind.getField().getAnnotation(MaxCreatedTime.class);
    return databind.getFieldValue()
        .map(value -> subquery(def.from(), value.toString()))
        .orElse(null);
  }

  Specification<Object> subquery(Class<?> entityClass, String by) {
    // 以下提供 subquery 的實作, 僅供參考
    return (root, query, builder) -> {
      var subquery = query.subquery(LocalDateTime.class);
      var subroot = subquery.from(entityClass);
      subquery.select(
        builder.greatest(subroot.get("createdTime").as(LocalDateTime.class))
      ).where(builder.equal(root.get(by), subroot.get(by)));
      return builder.equal(root.get("createdTime"), subquery);
    };
  }
}
```

接著我們在 `SpecMapper` 建構時加入此 resolver:

```java
var mapper = SpecMapper.builder()
      .defaultResolvers()
      .resolver(new MaxCreatedTimeSpecificationResolver())
      .build();
```

最後我們定義 POJO 及進行  `Specification` 的轉換:

```java
@Data
public class CustomerCriteria {

  @MaxCreatedTime(from = Customer.class)
  String maxBy;
}

var criteria = new CustomerCriteria();
criteria.setMaxBy("firstname");

var spec = mapper.toSpec(criteria, Customer.class);
repository.findAll(spec);
```

執行的 SQL 會類似:

```
... where customer0_.created_time=(
  select max(customer1_.created_time) from customer customer1_ 
  where customer0_.firstname=customer1_.firstname
)
```
