/*
 * Copyright © 2022 SoftLeader
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package tw.com.softleader.data.jpa.spec.usecase;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

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

  @Singular
  @ElementCollection(fetch = LAZY)
  @CollectionTable(name = "phones", joinColumns = @JoinColumn(name = "cust_id"))
  @MapKeyColumn(name = "carrier")
  @Column(name = "phone_number")
  Map<String, String> phones;

  @Singular
  @ManyToMany(cascade = {
      CascadeType.PERSIST,
      CascadeType.MERGE,
      CascadeType.DETACH,
      CascadeType.REFRESH
  }, fetch = FetchType.EAGER)
  @JoinTable(name = "CCUSTOMER_SCHOOL_MAPPING", joinColumns = {
      @JoinColumn(name = "CUSTOMER_ID", nullable = false)
  }, inverseJoinColumns = {
      @JoinColumn(name = "SCHOOL_ID", nullable = false)
  })
  Set<School> schools;
}
