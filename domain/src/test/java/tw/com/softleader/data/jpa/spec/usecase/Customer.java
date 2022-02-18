/*
 * Copyright © 2022 SoftLeader (support@softleader.com.tw)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tw.com.softleader.data.jpa.spec.usecase;

import static javax.persistence.CascadeType.ALL;
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
  @OneToMany(fetch = LAZY, cascade = ALL)
  @JoinColumn(name = "customer_id")
  Set<Badge> badges;

  @Singular
  @OneToMany(fetch = LAZY, cascade = ALL)
  @JoinColumn(name = "order_id")
  Set<Order> orders;
}
