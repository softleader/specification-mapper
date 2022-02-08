package tw.com.softleader.data.jpa.spec.usecase;

import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.LAZY;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.springframework.data.annotation.CreatedDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Customer {

  @Id
  @GeneratedValue
  Long id;

  @Enumerated(EnumType.STRING)
  Gender gender;

  String name;

  LocalDate birthday;

  @CreatedDate
  LocalDateTime createdTime;

  Integer weight;
  int weightInt;
  long weightLong;
  float weightFloat;
  Double weightDouble;
  BigDecimal weightBigDecimal;

  boolean gold;
  Boolean goldObj;

  @Singular
  @OneToMany(fetch = LAZY, cascade = { PERSIST, REMOVE })
  @JoinColumn(name = "customer_id")
  Set<Badge> badges;

  @Singular
  @OneToMany(fetch = LAZY, cascade = { PERSIST, REMOVE })
  @JoinColumn(name = "order_id")
  Set<Order> orders;
}
