---
title: Combine Specs
weight: 30
---

在 POJO 中, 可以使用 `@And` 或 `@Or` 來組合一個物件中的多個 Specification, 組合的預設是 `@And`.

## Combine on Classes

`@And` 或 `@Or` 可以宣告在 Class 上, 例如:

```java
@Or // 若沒定義預設就是 @And
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  @Spec(Like.class)
  String lastname;
}
```

執行的 SQL 將會是:

```
... where x.firstname like %?% or x.lastname like %?% 
```

## Combine on Fields

你也可以將 `@And` 或 `@Or` 註記在欄位上來控制單獨一個欄位要怎麼跟其他欄位組合. 舉個例子如下:

```java
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  @Spec(Like.class)
  String lastname;

  @Or
  @Spec(value = After.class, not = true)
  LocalDate birthday;
}
```

執行的 SQL 將會是:

```
... where (x.firstname like ?) and (x.lastname like ?) or x.birthday<=?
```
{{% alert title="Important" color="warning" %}}
欄位是依照宣告的順序組合的, 而 SQL 也是有[運算子優先順序](https://docs.microsoft.com/zh-tw/sql/t-sql/language-elements/operator-precedence-transact-sql?view=sql-server-ver15)的, 需注意兩者的配合的結果是否符合你的期望
{{% /alert %}}

例如, 將上面的例子調整欄位順序:

```java
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  @Or
  @Spec(value = After.class, not = true)
  LocalDate birthday;
  
  @Spec(Like.class)
  String lastname;
}
```

執行的 SQL 將會是:

```
... where (x.firstname like ? or x.birthday<=?) and (x.lastname like ?)
```
