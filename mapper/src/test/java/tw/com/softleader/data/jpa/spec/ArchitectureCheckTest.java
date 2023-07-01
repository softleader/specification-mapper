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
package tw.com.softleader.data.jpa.spec;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeJars;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import com.tngtech.archunit.lang.ArchRule;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.SimpleSpecification;

import static com.tngtech.archunit.base.DescribedPredicate.doNot;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static com.tngtech.archunit.core.domain.JavaModifier.ABSTRACT;
import static com.tngtech.archunit.core.domain.properties.HasModifiers.Predicates.modifier;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.*;

@AnalyzeClasses(packagesOf = SpecCodec.class, importOptions = {
    DoNotIncludeTests.class,
    DoNotIncludeJars.class
})
class ArchitectureCheckTest {

  static final DescribedPredicate<JavaClass> BUILDER = simpleNameEndingWith("Builder");

  static final String DOMAIN = "Domain";
  static final String INFRA = "Infra";
  static final String ANNOTATION = "Annotation";

  static final String DOMAIN_PACKAGE = SimpleSpecification.class.getPackage().getName();
  static final String INFRA_PACKAGE = SpecCodec.class.getPackage().getName();
  static final String ANNOTATION_PACKAGE = Spec.class.getPackage().getName();

  @ArchTest
  static final ArchRule layerDependency = layeredArchitecture()
      .consideringAllDependencies()
      .layer(DOMAIN).definedBy(DOMAIN_PACKAGE)
      .layer(INFRA).definedBy(INFRA_PACKAGE)
      .layer(ANNOTATION).definedBy(ANNOTATION_PACKAGE)
      .whereLayer(ANNOTATION).mayOnlyBeAccessedByLayers(INFRA)
      .whereLayer(DOMAIN).mayOnlyBeAccessedByLayers(INFRA, ANNOTATION)
      .whereLayer(INFRA).mayNotBeAccessedByAnyLayer();

  @ArchTest
  static final ArchRule domainClassesShouldBePublic = classes()
      .that().resideInAPackage(DOMAIN_PACKAGE)
      .and(doNot(modifier(ABSTRACT)))
      .should().bePublic();

  @ArchTest
  static final ArchRule annotationClassesShouldBePublic = classes()
      .that().resideInAPackage(ANNOTATION_PACKAGE)
      .should().bePublic();

  @ArchTest
  static final ArchRule classesExceptSpecMapperShouldNotBePublicResideInInfra = classes()
      .that().resideInAPackage(INFRA_PACKAGE)
      .and(doNot(INTERFACES.or(BUILDER).or(assignableTo(SpecMapper.class))))
      .should().notBePublic();

  @ArchTest
  static final ArchTests generalCodingRules = ArchTests.in(GeneralCodingRules.class);

  static class GeneralCodingRules {

    @ArchTest
    static final ArchRule noClassesShouldThrowGenericExceptions = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    @ArchTest
    static final ArchRule noClassesShouldUseJodaTime = NO_CLASSES_SHOULD_USE_JODATIME;

    @ArchTest
    static final ArchRule noClassesShouldUseFieldInjection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    @ArchTest
    static final ArchRule noClassesShouldUseJavaUtilLogging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
  }
}
