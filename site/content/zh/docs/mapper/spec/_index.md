---
title: SpecMapper
weight: 10
description: > 
  操作入口
---

`SpecMapper` 是所有 Spec 操作的 API 入口, 首先我們需要建構實例:

```java
var mapepr = SpecMapper.builder().build();
```

接著我們定義封裝查詢條件的物件, 這是一個 POJO 即可, 如:

```java
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
}
```

這樣我們就可以做 `Specification` 的轉換了, 得到 `Specification` 後就可以依照原本的方式去資料庫查詢, 例如透過 Spring Data JPA 的 repository:

```java
var criteria = new CustomerCriteria();
criteria.setFirstname("Hello")

var mapper = SpecMapper.builder().build();
var specification = mapper.toSpec(criteria);

customerRepository.findAll(specification);
```

執行 SQL 將會是: 

```
... where x.firstname like '%Hello%'
```

## Skipping Strategy

在 POJO 中的欄位, 只要符合以下任一條件, 在轉換的過程中都將會忽略:

- 沒有掛任何 Spec Annotation 
- 值為 *null*
- 若 Type 為 *Iterable* 且值為 *empty*
- 若 Type 為 *Optional* 且值為 *empty*
- 若 Type 為 *CharSequence* 且長度為 *0*
- 若 Type 為 *Array* 且長度為 *0*
- 若 Type 為 *Map* 且值為 *empty*

例如, 將以下 POJO 建構後, 不 set 任何值就直接轉換成 `Specification` 及查詢

```java
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  String lastname = "Hello";
 
  @Spec
  String nickname = "";
     
  @Spec(GreaterThat.class)
  Optional<Integer> age = Optional.empty();
  
  @Spec(In.class)
  Collection<String> addresses = Arrays.asList();
}

var mapper = SpecMapper.builder().build();
customerRepository.findAll(mapper.toSpec(new CustomerCriteria()));
```

以上執行的 SQL 將不會有任何過濾條件!

{{% alert title="Note" color="info" %}}
如果你有使用 Builder Pattern, (e.g. Lombok's *@Builder*), 請特別注意 Builder 的 Default Value!
{{% /alert %}}

若你想要客製化跳脫邏輯, 可以實作 `SkippingStrategy`, 並在建構 `SpecMapper` 時傳入:

```java
var mapper = SpecMapper.builder()
      .defaultResolvers()
      .skippingStrategy(fieldValue -> {
        // 判斷是否要跳 field value, 回傳 boolean
      })
      .build();
```
