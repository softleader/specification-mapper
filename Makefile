# VERSION defines the release-version of the bundle.
VERSION ?=
JAVA ?=
SPRING_BOOT ?=

define java_version
$(if $(filter-out "",$(JAVA)),-D'java.version=$(JAVA)',)
endef

define spring_boot_version
$(if $(filter-out "",$(SPRING_BOOT)),-D'spring-boot.version=$(SPRING_BOOT)',)
endef

##@ General

help: ## Display this help.
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} /^[a-zA-Z_0-9-]+:.*?##/ { printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Develop

format: ## Format the source code.
	mvn validate -e

clean: ## Remove files generated at build-time.
	mvn clean -e

compile: clean  ## Clean and compile the source code.
	mvn compile -e $(call java_version) $(call spring_boot_version)

test: clean ## Clean and test the compiled code.
	mvn test -e $(call java_version) $(call spring_boot_version)

install: clean ## Install project to local repository w/o unit testing.
	mvn install -e -DskipTests -Prelease $(call java_version) $(call spring_boot_version)

spring-boot-version: ## Get current Spring Boot version
	@mvn help:evaluate -Dexpression=spring-boot.version -DforceStdout -q

bump-spring-boot: ## Bump Spring Boot version
ifeq ($(strip $(BOOT)),)
	$(error BOOT is required)
endif
	mvn versions:set-property -Dproperty=spring-boot.version -DnewVersion=$(BOOT)
	mvn versions:commit

##@ Delivery

version: ## Get current project version
	@mvn help:evaluate -Dexpression=project.version -DforceStdout -q

new-version: ## Update version.
ifeq ($(strip $(VERSION)),)
	$(error VERSION is required)
endif
	mvn versions:set -DnewVersion=$(VERSION)
	mvn versions:commit

release: ## Pack w/o unit testing, and deploy to remote repository.
	mvn clean deploy -e -Prelease -DskipTests
