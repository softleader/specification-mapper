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
  String firstName;
}
```

這樣我們就可以做 Specification 的轉換了:

```java
var criteria = new CustomerCriteria();
criteria.setFirstName("Hello")

var mapper = SpecMapper.builder().build();
var specification = mapper.toSpec(criteria);
```

得到 `Specification` 後, 我們可以依照原本的方式去資料庫查詢, 例如透過 Spring Data JPA 的 repository:

```java
customerRepository.findAll(specification);

// 執行語法將會是: 
// select ... from Customer c where c.firstName like '%Hello%'
```

## Simple Specifications

使用 `@Spec` 定義 `Specification` 的實作, 預設是 [`Equal`](#equal): 

```java
@Spec
@Spec(Equal.class) // are same
String firstName;
```

對應的 entity path 預設會使用 field name, 你也可以透過設定 `@Spec#path` 或是 `@javax.persistence.Column` 來改變

```java
@Spec(path = "...") // 最優先使用
@Column(name = "...")  // 第二順位使用
String firstName; // 最後使用
```
`@Spec` 中可定義的 `Specification` 實作支援了所有 `SimpleSpecification` 的 subclasses, 以下依序介紹:

### Equal

### Like, EndingWith, StartingWith

### GreaterThan, GreaterThanOrEqualTo

### LessThan, LessThanOrEqualTo

## Composition

## Join

## Join Fetch
