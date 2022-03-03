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
package tw.com.softleader.data.jpa.spec.repository.support;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.support.RepositoryFactoryCustomizer;

/**
 * RepositoryFactoryCustomizer injector, which somehow Spring Data doesn't do the injection...
 *
 * @author Matt Ho
 * @see RepositoryFactoryCustomizer
 */
@RequiredArgsConstructor
public class JpaRepositoryFactoryBeanPostProcessor implements BeanPostProcessor {

  final List<RepositoryFactoryCustomizer> customizers;

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    if (bean instanceof JpaRepositoryFactoryBean) {
      val factoryBean = (JpaRepositoryFactoryBean) bean;
      customizers.forEach(factoryBean::addRepositoryFactoryCustomizer);
    }
    return bean;
  }
}
