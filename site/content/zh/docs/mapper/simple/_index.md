---
title: Simple Specs
weight: 20
description: > 
  基本查詢條件
---

在 POJO 中, 你可以在 Field 上使用 `@Spec` 來定義 `Specification` 的實作, 預設是 `Equals`: 

```java
@Spec // 同等於 @Spec(Equals.class)
String firstname;
```

對應的 entity path 預設會使用 field name, 你也可以設定 `@Spec#path` 來改變

```java
@Spec(path = "...") // 有定義則優先使用
String firstname; // 預設使用欄位名稱
```

## Built-in @Spec

以下是內建的 `@Spec` 的類型清單:

| Spec | Supported field type | Sample | JPQL snippet |
|---|---|---|---|
| `Equals` | *Any*  | `@Spec(Equals.class) String firstname;` | `... where x.firstname = ?` |
| `NotEquals` | *Any*| `@Spec(NotEquals.class) String firstname;` | `... where x.firstname <> ?` |
| `Between` | *Iterable of Comparable* <br> (Expected exact **2 elements** in *Iterable*) | `@Spec(Between.class) List<Integer> age;` | `... where x.age between ? and ?` |
| `LessThan` | *Comparable* | `@Spec(LessThan.class) Integer age;` | `... where x.age < ?` |
| `LessThanEqual` | *Comparable* | `@Spec(LessThanEqual.class) Integer age;` | `... where x.age <= ?` |
| `GreaterThan` | *Comparable* | `@Spec(GreaterThan.class) Integer age;` | `... where x.age > ?` |
| `GreaterThanEqual` | *Comparable* | `@Spec(GreaterThanEqual.class) Integer age;` | `... where x.age >= ?` |
| `After` | *Comparable* | `@Spec(After.class) LocalDate startDate;` | `... where x.startDate > ?` |
| `Before` | *Comparable* | `@Spec(Before.class) LocalDate startDate;` | `... where x.startDate < ?` |
| `IsNull` | *Boolean* | `@Spec(IsNull.class) Boolean age;` | `... where x.age is null` *(if true)* <br> `... where x.age not null` *(if false)* |
| `NotNull` | *Boolean* | `@Spec(NotNull .class) Boolean age;` | `... where x.age not null` *(if true)* <br> `... where x.age is null` *(if false)* |
| `Like` | *String* | `@Spec(Like.class) String firstname;` | `... where x.firstname like %?%` |
| `NotLike` | *String* | `@Spec(NotLike.class) String firstname;` | `... where x.firstname not like %?%` |
| `StartingWith` | *String* | `@Spec(StartingWith.class) String firstname;` | `... where x.firstname like ?%` |
| `EndingWith` | *String*| `@Spec(EndingWith.class) String firstname;` | `... where x.firstname like %?` |
| `In` | *Iterable of Any* | `@Spec(In.class) Set<String> firstname;` | `... where x.firstname in (?, ?, ...)` |
| `NotIn` | *Iterable of Any* | `@Spec(NotIn.class) Set<String> firstname;` | `... where x.firstname not in (?, ?, ...)` |
| `True` | *Boolean* | `@Spec(True.class) Boolean active;` | `... where x.active = true` *(if true)* <br> `... where x.active = false` *(if false)* |
| `False` | *Boolean* | `@Spec(False.class) Boolean active;` | `... where x.active = false` *(if true)* <br> `... where x.active = true` *(if false)* |
| `HasLength` | *Boolean* | `@Spec(HasLength.class) Boolean firstname;` | `... where x.firstname is not null and character_length(x.firstname)>0` *(if true)* <br> `... where not(x.firstname is not null and character_length(x.firstname)>0)` *(if false)* |
| `HasText` | *Boolean* | `@Spec(HasText.class) Boolean firstname;` | `... where x.firstname is not null and character_length(trim(BOTH from x.firstname))>0` *(if true)* <br> `... where not(where x.firstname is not null and character_length(trim(BOTH from x.firstname))>0)` *(if false)* |

> 為了方便已經熟悉 Spring Data JPA 的人使用, 以上名稱都是儘量跟著 [Query Methods](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html#jpa.query-methods.query-creation) 一樣

## Negates @Spec

你可以使用 `@Spec#not` 來判斷反向條件, 預設是 `false`, 設定成 `true` 就會將結果做反向轉換.

例如, 我想要用 `Between` 找**不在**區間內的資料, 則範例如下:

```java
@Spec(value = Between.class, not = true)
Collection<Integer> age;
```

執行的 SQL 會類似:

```
... where x.age not between ? and ?
```

## Extending @Spec

*@Spec* 是可以很容易擴充的, 只要實作了 `SimpleSpecification<T>` 並提供規定的 Constructor, 這些 class 就可以被定義在 *@Spec* 中

例如, 我有個 *Customer* Entity, 有以下欄位: 

- *firstname (String)* - 人名, 可重複
- *createdTime (LocalDateTime)* - 建立時間, 不可重複

我希望可以找出每個人名中, 建立時間為最新的那筆資料! 且我打算撰寫一個 subquery 來完成這需求, 完整的範例如下:

首先我們實作 `SimpleSpecification<T>`, 並提供規定的 Constructor:

```java
public class MaxCustomerCreatedTime extends SimpleSpecification<Customer> {

  // 這是規定必須提供的建構值, 修飾詞不限定: public, protected, default, private 都支援
  protected MaxCustomerCreatedTime(Context context, String path, Object value) {
    super(context, path, value);
  }

  @Override
  public Predicate toPredicate(Root<Customer> root,
    CriteriaQuery<?> query,
    CriteriaBuilder builder) {
    // 以下提供 subquery 的實作, 僅供參考
    var subquery = query.subquery(LocalDateTime.class);
    var subroot = subquery.from(Customer.class);
    subquery.select(
      builder.greatest(subroot.get("createdTime").as(LocalDateTime.class))
    ).where(builder.equal(root.get((String) value), subroot.get((String) value)));
    return builder.equal(root.get("createdTime"), subquery);
  }
}
```

上面完成的 `MaxCustomerCreatedTime` 就可以被應用在 *@Spec* 中了, 接著我們定義 POJO 及進行 `Specification` 的轉換:

```java
@Data
public class CustomerCriteria {

  @Spec(MaxCustomerCreatedTime.class)
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
