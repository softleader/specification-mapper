# specification-mapper

```xml
<dependency>
  <groupId>tw.com.softleader.data</groupId>
  <artifactId>specification-mapper</artifactId>
</dependency>
```

specification-mapper 是一套 [Specifications](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications) 的產生器, 它讀取了 Object 中的欄位, 配合欄位上的 Annotation 的定義, 來協助建立動態的查詢條件!

另外 [specification-mapper-starter](../starter) 提供了 Spring Boot 的整合, 讓你可以零配置的在 Spring apps 中使用, 使用 Spring boot 的應用程式可以參考看看!

## Getting Started

我們需要的是建立 `SpecMapper` 實例, 這是最重要的 Class 也是所有 Spec 操作的 API 入口:

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
... where c.firstname like '%Hello%'
```

### Skipping Strategy

在 POJO 中沒有掛任何 Spec Annotation 或值為 *null* 的欄位, 在轉換的過程中都將會忽略, 如:

```java
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  String lastname;
}

var mapper = SpecMapper.builder().build();
customerRepository.findAll(mapper.toSpec(new CustomerCriteria()));
```

以上最後執行的 SQL 將不會有任何過濾條件!

## Simple Specifications

使用 `@Spec` 定義 `Specification` 的實作, 預設是 `Equals`: 

```java
@Spec
@Spec(Equals.class) // are same
```

對應的 entity path 預設會使用 field name, 你也可以透過設定 `@Spec#path` 來改變

```java
@Spec(path = "...") // 優先使用
String firstname; // 最後(預設)使用
```

### Built-in Simple @Spec

以下是內建的 `@Spec` 的類型清單:

| Spec | Supported field type | Sample | JPQL snippet |
|---|---|---|---|
| `Equals` | *Any*  | `@Spec(Equals.class) String firstname;` | `... where x.firstname = ?` |
| `Between` | *Iterable of Comparable* <br> (Expected exact **2 elements** in *Iterable*) | `@Spec(Between.class) List<Integer> age;` | `... where x.age between ? and ?` |
| `LessThan` | *Comparable* | `@Spec(LessThan.class) Integer age;` | `... where x.age < ?` |
| `LessThanEqual` | *Comparable* | `@Spec(LessThanEqual.class) Integer age;` | `... where x.age <= ?` |
| `GreaterThan` | *Comparable* | `@Spec(GreaterThan.class) Integer age;` | `... where x.age > ?` |
| `GreaterThanEqual` | *Comparable* | `@Spec(GreaterThanEqual.class) Integer age;` | `... where x.age >= ?` |
| `After` | *Comparable* | `@Spec(After.class) LocalDate startDate;` | `... where x.startDate > ?` |
| `Before` | *Comparable* | `@Spec(Before.class) LocalDate startDate;` | `... where x.startDate < ?` |
| `Null` | *Boolean* | `@Spec(Null.class) Boolean age;` | `... where x.age is null` *(if true)* <br> `... where x.age not null` *(if false)* |
| `NotNull` | *Boolean* | `@Spec(NotNull .class) Boolean age;` | `... where x.age not null` *(if true)* <br> `... where x.age is null` *(if false)* |
| `Like` | *String* | `@Spec(Like.class) String firstname;` | `... where x.firstname like %?%` |
| `NotLike` | *String* | `@Spec(NotLike.class) String firstname;` | `... where x.firstname not like %?%` |
| `StartingWith` | *String* | `@Spec(StartingWith.class) String firstname;` | `... where x.firstname like ?%` |
| `EndingWith` | *String*| `@Spec(EndingWith.class) String firstname;` | `... where x.firstname like %?` |
| `Not` | *Any*| `@Spec(Not.class) String firstname;` | `... where x.firstname <> ?` |
| `In` | *Iterable of Any* | `@Spec(In.class) Set<String> firstname;` | `... where x.firstname in (?, ?, ...)` |
| `NotIn` | *Iterable of Any* | `@Spec(NotIn.class) Set<String> firstname;` | `... where x.firstname not in (?, ?, ...)` |
| `True` | *Boolean* | `@Spec(True.class) Boolean active;` | `... where x.active = true` *(if true)* <br> `... where x.active = false` *(if false)* |
| `False` | *Boolean* | `@Spec(False.class) Boolean active;` | `... where x.active = false` *(if true)* <br> `... where x.active = true` *(if false)* |

> 為了方便已經熟悉 Spring Data JPA 的人使用, 以上名稱都是儘量跟著 [Query Methods](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation) 一樣

### Extending Simple @Spec

*@Spec* 是可以很容易擴充的, 只要實作了 `SimpleSpecification<T>` 並提供規定的 Constructor, 這些 class 就可以被定義在 *@Spec* 中

例如, 我有個 *Customer* Entity, 有以下欄位: 

- *firstname (String)* - 人名, 可重複
- *createdTime (LocalDateTime)* - 建立時間, 不可重複

我希望可以找出每個人名中, 建立時間為最新的那筆資料! 且我打算透過一個 subquery 來完成這需求, 完整的範例如下:

首先我們實作 `SimpleSpecification<T>`, 並提供規定的 Constructor:

```java
public class MaxCustomerCreatedTime extends SimpleSpecification<Customer> {

  // 這是規定必須提供的建構值
  public MaxCustomerCreatedTime(Context context, String path, Object value) {
    super(context, path, value);
  }

  @Override
  public Predicate toPredicate(Root<Customer> root,
    CriteriaQuery<?> query,
    CriteriaBuilder builder) {
    // 以下提供 subquery 的實作, 僅供參考
    var subquery = query.subquery(Long.class);
    var subroot = subquery.from(Customer.class);
    subquery.select(builder.max(subroot.get("createdTime")))
      .where(builder.equal(root.get((String) value), subroot.get((String) value)));
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

最終執行的 SQL 將會是:

```
... where customer0_.created_time=(
  select max(customer1_.created_time) from customer customer1_ 
  where customer0_.firstname=customer1_.firstname
)
```

## Combining Specs

透過 `@And` 或 `@Or` 可以組合多個 Specification, 透過在 POJO 的 class 層級定義可以變更組合邏輯, 組合的預設是 `@And`, 例如我想要改成 `@Or` 則:

```java
@Or // 若沒定義預設就是 and
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  @Spec(Like.class)
  String lastname;
}
```

這樣執行的 SQL 將會是:

```
... where c.firstname like %?% or c.lastname like %?% 
```

## Nested Specs

透過註記 `@NestedSpec` 在 Field 上, 就可以提醒 `SpecMapper` 往下一層物件 (Nested Object) 去組合 Specification,  例如:

```java
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  @NestedSpec
  CustomerAddress address;
}

@Or
@Data
public class CustomerAddress {

  @Spec
  String county;
  
  @Spec
  String city;
}
```

這樣執行的 SQL 將會是:

```
... where c.firstname like %?% and ( c.county=? or c.city=? )
```


## Join

## Join Fetch

## Customize Spec Annotation

延續 [Extending Simple @Spec](#extending-simple-spec) 章節範例, 進階一點現在我們希望可以將 Entity Class 設計成可以配置, 這樣才能在 Customer 以外的 Entity 都可以使用!

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
      var subquery = query.subquery(Long.class);
      var subroot = subquery.from(entityClass);
      subquery.select(builder.max(subroot.get("createdTime")))
          .where(builder.equal(root.get(by), subroot.get(by)));
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

最終執行的 SQL 將會是:

```
... where customer0_.created_time=(
  select max(customer1_.created_time) from customer customer1_ 
  where customer0_.firstname=customer1_.firstname
)
```

## Limitation

`SpecMapper` 在找 POJO 欄位時, 只會找當前 Class 的 Local Field, 而不去往上找 Hierarchy Classes 的 Field, 如果你共用的欄位想要用在多個 POJO, 請考慮使用 [Nested Specs](#nested-Specs) 方式
