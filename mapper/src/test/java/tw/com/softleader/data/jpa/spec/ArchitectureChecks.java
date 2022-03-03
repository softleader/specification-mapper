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

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeJars;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.SimpleSpecification;

@AnalyzeClasses(packagesOf = SpecCodec.class, importOptions = {
    DoNotIncludeTests.class,
    DoNotIncludeJars.class
})
class ArchitectureChecks {

  @ArchTest
  static final ArchRule layerChecks = layeredArchitecture()
      .layer("Domain").definedBy(SimpleSpecification.class.getPackage().getName())
      .layer("Infra").definedBy(SpecCodec.class.getPackage().getName())
      .layer("Annotation").definedBy(Spec.class.getPackage().getName())

      .whereLayer("Annotation").mayOnlyBeAccessedByLayers("Infra")
      .whereLayer("Domain").mayOnlyBeAccessedByLayers("Infra", "Annotation")
      .whereLayer("Infra").mayNotBeAccessedByAnyLayer();
}
