# specification-mapper

```xml
<dependency>
  <groupId>tw.com.softleader.data</groupId>
  <artifactId>specification-mapper</artifactId>
  <version>last-release-version</version>
</dependency>
```

specification-mapper 是一套 [Specifications](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications) 的產生器, 它讀取了 Object 中的欄位, 配合欄位上 Annotation 的定義, 來動態的建立查詢條件!

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

在 POJO 中的欄位, 只要符合以下任一條件, 在轉換的過程中都將會忽略:

- 沒有掛任何 Spec Annotation 
- 若 Type 為 *Iterable* 且值為 *empty*
- 若 Type 為 *Optional* 且值為 *empty*
- 值為 *null*

例如, 將以下 POJO 建構後, 不 set 任何值就直接轉換成 `Specification` 及查詢

```java
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  String lastname = "Hello";
  
  @Spec(GreaterThat.class)
  Optional<Integer> age = Optional.empty();
  
  @Spec(In.class)
  Collection<String> addresses = List.of();
}

var mapper = SpecMapper.builder().build();
customerRepository.findAll(mapper.toSpec(new CustomerCriteria()));
```

以上執行的 SQL 將不會有任何過濾條件!

> 如果你有使用 Builder Pattern, (e.g. Lombok's *@Builder*), 請特別注意 Builder 的 Default Value!

## Simple Specifications

你可以在 Field 使用 `@Spec` 來定義 `Specification` 的實作, 預設是 `Equals`: 

```java
@Spec // 同等於 @Spec(Equals.class)
String firstname;
```

對應的 entity path 預設會使用 field name, 你也可以設定 `@Spec#path` 來改變

```java
@Spec(path = "...") // 有定義則優先使用
String firstname; // 預設使用欄位名稱
```

### Built-in Simple @Spec

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

> 為了方便已經熟悉 Spring Data JPA 的人使用, 以上名稱都是儘量跟著 [Query Methods](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation) 一樣

### Negates the @Spec

你可以使用 `@Spec#not` 來判斷反向條件, 預設是 `false`, 設定成 `true` 就會將結果做反向轉換.

例如, 我想要用 `Between` 找**不在**區間內的資料, 則範例如下:

```java
@Spec(value = Between.class, not = true)
Collection<Integer> age;
```

執行的 SQL 將會是:

```
... where x.age not between ? and ?
```

### Extending Simple @Spec

*@Spec* 是可以很容易擴充的, 只要實作了 `SimpleSpecification<T>` 並提供規定的 Constructor, 這些 class 就可以被定義在 *@Spec* 中

例如, 我有個 *Customer* Entity, 有以下欄位: 

- *firstname (String)* - 人名, 可重複
- *createdTime (LocalDateTime)* - 建立時間, 不可重複

我希望可以找出每個人名中, 建立時間為最新的那筆資料! 且我打算撰寫一個 subquery 來完成這需求, 完整的範例如下:

首先我們實作 `SimpleSpecification<T>`, 並提供規定的 Constructor:

```java
public class MaxCustomerCreatedTime extends SimpleSpecification<Customer> {

  // 這是規定必須提供的建構值, 且必須是 public
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

執行的 SQL 將會是:

```
... where customer0_.created_time=(
  select max(customer1_.created_time) from customer customer1_ 
  where customer0_.firstname=customer1_.firstname
)
```

## Combining Specs

你可以在 class 層級上使用 `@And` 或 `@Or` 來組合多個 Specification, 組合的預設是 `@And`.

例如我想要改成 `@Or`, 程式碼範例如下:

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
... where c.firstname like %?% or c.lastname like %?% 
```

## Nested Specs

你可以在 Field 上使用 `@NestedSpec` 來告知 `SpecMapper` 要往下一層物件 (Nested Object) 去組合 Specification,  這是沒有層級限制的, 可以一直往下找!

例如我有一個共用的 `AddressCriteria` POJO, 我就可以將它掛載到其他的 POJO 中, 程式碼範例如下:

```java
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  @NestedSpec
  AddressCriteria address;
}

@Or
@Data
public class AddressCriteria {

  @Spec
  String county;
  
  @Spec
  String city;
}
```

執行的 SQL 將會是:

```
... where c.firstname like %?% and ( c.county=? or c.city=? )
```


## Join

你可以在 Field 上使用 `@Join`  來過濾關聯 entity, 這些 entity 之間需要都先定義好關係, 例如:

```java
@Entity
class Customer {

  @OneToMany(cascade = ALL, fetch = LAZY)
  @JoinColumn(name = "order_id")
  private Collection<Order> orders;
}

@Entity
class Order {

  private String itemName;
}
```

如果你想要查詢買了指定東西的客戶, 則可以定義 POJO 如下:

```java
@Data
public class CustomerOrderCriteria {

  @Join(path = "orders", alias = "o")
  @Spec(path = "o.itemName", value = In.class)
  Collection<String> items;
}
```

執行的 SQL 將會是:

```
select distinc ... from customer customer0_ 
inner join orders orders1_ on customer0_.id=orders1_.order_id 
where orders1_.item_name in (? , ?)
```

為了比較符合大部分的使用情境, 預設的 Join type 是 `INNER`, 也會將結果排除重複 (*distinct*), 你可以設定 `@Join#joinType` 或 `@Join#distinct` 來改變, 如:

```java
@Join(joinType = JoinType.RIGHT, distinct = false)
```

### Multi Level Joins

你可以使用 `@Joins` 來定義多層級的 Join, 例如:

```java
@Entity
class Customer {

  @OneToMany(cascade = ALL, fetch = LAZY)
  @JoinColumn(name = "order_id")
  private Set<Order> orders;
}

@Entity
class Order {
    
  @ManyToMany(cascade = ALL, fetch = LAZY)
  private Set<Tag> tags;
}

@Entity
class Tag {

  private String name;
}
```

如果你想要查詢買了指定所屬類別的東西的客戶, 則可以定義 POJO 如下:

```java
@Data
class CustomerOrderTagCriteria {

  @Joins({
    @Join(path = "orders", alias = "o"),
    @Join(path = "o.tags", alias = "t")
  })
  @Spec(path = "t.name", value = In.class)
  Collection<String> tags;
}
```

執行的 SQL 將會是:

```
select distinct ... from customer customer0_ 
inner join orders orders1_ on customer0_.id=orders1_.order_id 
inner join orders_tags tags2_ on orders1_.id=tags2_.order_id 
inner join tag tag3_ on tags2_.tags_id=tag3_.id 
where 1=1 and (tag3_.name in (?))
```

**特別注意, Annotation 的處理是有順序性的, 因此必須依照 Join 的順序去定義 `@Joins`**

例如依照上面的情境, 下列的定義順序是錯誤的:

```java
@Data
class CustomerOrderTagCriteria {

  @Joins({
    @Join(path = "o.tags", alias = "t"), // "o" alias will be not exist during processing this @Join
    @Join(path = "orders", alias = "o")
  })
  @Spec(path = "t.name", value = In.class)
  Collection<String> tagNames;
}

```

## Join Fetch

你可以在 class 層級上使用 `@JoinFetch` 可以一次撈出所有 Lazy 的關聯資料, 例如:

```java
@Entity
class Customer {

  @OneToMany(fetch = LAZY, cascade = ALL)
  @JoinColumn(name = "order_id")
  private Collection<Order> orders;
}

@Entity
class Order {
  
  private String itemName;
}
```

如果你想在取得 Customer 時就順便 Join 出 Order, 則:

```java
@JoinFetch(paths = "orders")
@Data
class CustomerOrderCriteria {

  @Spec
  String name;
}
```

執行的 SQL 將會是:

```
select distinct 
  customer0_.* ...,
  orders1_.* ...
from customer customer0_ 
left outer join orders orders1_ on customer0_.id=orders1_.order_id 
where customer0_.name=?
```

為了比較符合大部分的使用情境, 預設的 Join type 是 `LEFT`, 也會將結果排除重複 (*distinct*), 你可以設定 `@FetchJoin#joinType` 或 `@FetchJoin#distinct` 來改變, 如:

```java
@FetchJoin(joinType = JoinType.RIGHT, distinct = false)
```

### Multi Level Fetch Joins

你可以使用 `@FetchJoins` 來定義多層級的 Fetch Join, 例如:

```java
@Entity
class Customer {

  @OneToMany(cascade = ALL, fetch = LAZY)
  @JoinColumn(name = "order_id")
  private Set<Order> orders;
}

@Entity
class Order {
    
  @ManyToMany(cascade = ALL, fetch = LAZY)
  private Set<Tag> tags;
}

@Entity
class Tag {

  private String name;
}
```

如果你想在取得 Customer 時就順便 Join 出 Order 及 Tag, 則:


```java
@JoinFetches({
  @JoinFetch(paths = "orders"),
  @JoinFetch(paths = "orders.tags")
})
@Data
class CustomerOrderTagCriteria {

  @Spec
  String name;
}
```

執行的 SQL 將會是:

```
select distinct 
  customer0_.* ...,
  orders1_.* ...,
  tags3_.* ...
from customer customer0_ 
left outer join orders orders1_ on customer0_.id=orders1_.order_id 
inner join orders orders2_ on customer0_.id=orders2_.order_id 
left outer join orders_tags tags3_ on orders2_.id=tags3_.order_id 
left outer join tag tag4_ on tags3_.tags_id=tag4_.id 
where 1=1 and customer0_.name=?
```

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

執行的 SQL 將會是:

```
... where customer0_.created_time=(
  select max(customer1_.created_time) from customer customer1_ 
  where customer0_.firstname=customer1_.firstname
)
```

## Limitation

`SpecMapper` 在找 POJO 欄位時, 只會找當前 Class 的 Local Field, 而不去往上找 Hierarchy Classes 的 Field, 如果你共用的欄位想要用在多個 POJO, 請考慮使用 [Nested Specs](#nested-Specs) 方式
