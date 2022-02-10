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

這樣我們就可以做 Specification 的轉換了:

```java
var criteria = new CustomerCriteria();
criteria.setFirstname("Hello")

var mapper = SpecMapper.builder().build();
var specification = mapper.toSpec(criteria);
```

得到 `Specification` 後, 我們可以依照原本的方式去資料庫查詢, 例如透過 Spring Data JPA 的 repository:

```java
customerRepository.findAll(specification);

// 執行 SQL 將會是: 
// select ... from Customer c where c.firstname like '%Hello%'
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

## Specification Resolvers

`SpecMapper` 在建構時, 會自動的加入多個預設的 `SpecificationResolver`, 這些 Resolvers 負責將讀取到欄位及 Annotation  轉換成 *Specification*, 以下介紹各種 Resolver 及對應的 Annotation 使用方式

### Simple Specification Resolver

使用 `@Spec` 定義 `Specification` 的實作, 預設是 [`Equal`](#equal): 

```java
@Spec
@Spec(Equals.class) // are same
```

對應的 entity path 預設會使用 field name, 你也可以透過設定 `@Spec#path` 來改變

```java
@Spec(path = "...") // 優先使用
String firstname; // 最後(預設)使用
```
`@Spec` 中可定義的 class 清單如下:

| Sample | Supported field type | JPQL snippet |
|---|---|---|
| `@Spec(Equals.class) String firstname;` | *Any* | `... where x.firstname = ?` |
| `@Spec(Not.class) String firstname;` | *Any* | `... where x.firstname <> ?` |
| `@Spec(Like.class) String firstname;` | *String* | `... where x.firstname like %?%` |
| `@Spec(StartingWith.class) String firstname;` | *String* | `... where x.firstname like %?` |
| `@Spec(EndingWith.class) String firstname;` | *String* | `... where x.firstname not like ?%` |
| `@Spec(NotLike.class) String firstname;` | *String* | `... where x.firstname not like %?%` |
| `@Spec(In.class) Set<String> firstname;` | *Iterable of Any* | `... where x.firstname in (?, ?, ...)` |
| `@Spec(NotIn.class) Set<String> firstname;` | *Iterable of Any* | `... where x.firstname not in (?, ?, ...)` |
| `@Spec(Between.class) List<Integer> age;` | *Iterable of Comparable* <br> (Expected exact **2 elements** in *Iterable*)| `... where x.age between ? and ?` |
| `@Spec(GreaterThan.class) Integer age;` | *Comparable* | `... where x.age > ?` |
| `@Spec(GreaterThanEqual.class) Integer age;` | *Comparable* | `... where x.age >= ?` |
| `@Spec(LessThan.class) Integer age;` | *Comparable* | `... where x.age < ?` |
| `@Spec(LessThanEqual.class) Integer age;` | *Comparable* | `... where x.age <= ?` |

#### Customize @Spec

*@Spec* 是可以很容易擴充的, 只要實作了 `SimpleSpecification<T>` 並提供規定的 Constructor, 這些 class 就可以被定義在 *@Spec* 中

例如, 我有個 *Customer* Table, 有以下欄位: 

- *firstname (String)* - 人名, 可重複
- *createdTime (LocalDateTime)* - 建立時間, 不可重複

我希望可以找出每個人名中, 建立時間為最新的那本資料! 且我打算透過一個 subquery 來完成這需求, 完整的範例如下:

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

上面完成的 `MaxCustomerCreatedTime` 就可以被應用在 *@Spec* 中了, 接著我們定義 POJO 及進  `Specification` 的轉換:

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
select ... from customer customer0_ 
where customer0_.created_time=(
  select max(customer1_.created_time) from customer customer1_ 
  where customer0_.firstname=customer1_.firstname
)
```

### Composition Specification Resolver

### Join Specification Resolver

### Join Fetch Specification Resolver

### Customize Specification Resolver

延續 [Customize @Spec](#customize-spec) 章節範例, 進階一點現在我們希望可以將 Entity Class 設計成可以配置, 這樣才能在 Customer 以外的 Entity 都可以使用!

要完成這需求我們需要在 Annotation 中定義更多參數, 因此 Simple @Spec 不適用了, 我們需要的是定義新的 Annotation 及擴充 Resolver, 完整的程式碼如下:

首先我們定義 Annotation:

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface MaxCreatedTime {

  Class<?> from();
}
```

接著我們實作 `SpecificationResolver` 來擴充:

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
select ... from customer customer0_ 
where customer0_.created_time=(
  select max(customer1_.created_time) from customer customer1_ 
  where customer0_.firstname=customer1_.firstname
)
```

## Limitation
