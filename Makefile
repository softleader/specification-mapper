# RELEASE defines the release-version of the bundle.
VERSION ?=
# See https://spring.io/projects/spring-boot#support
SPRING_BOOT_VERSIONS := 2.4.13 2.5.9 2.6.3
JAVA_VERSION := 11

##@ General

help: ## Display this help.
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} /^[a-zA-Z_0-9-]+:.*?##/ { printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Develop

format: ## Format the source code.
	mvn process-sources -e

clean: ## Remove files generated at build-time.
	mvn clean -e

compile: clean  ## Clean and compile the source code.
	mvn compile -e

test: clean ## Clean and test the compiled code.
	mvn test -e

matrix-test: $(SPRING_BOOT_VERSIONS) ## Clean and test the compiled code w/ multiple Spring Boot version.
$(SPRING_BOOT_VERSIONS):
	mvn clean test -e -D'format.skip=true' -D'spring-boot.version=$@' -D'java.version=$(JAVA_VERSION)'

install: clean ## Install project to local repository w/o unit testing.
	mvn install -e -DskipTests -Prelease

##@ Delivery

version: ## Get current project version
	mvn help:evaluate -Dexpression=project.version

new-version: ## Update version.
ifeq ($(strip $(VERSION)),)
	$(error VERSION is required)
endif
	mvn versions:set -DnewVersion=$(VERSION)
	mvn versions:commit

release: ## Pack w/o unit testing, and deploy to remote repository.
	mvn clean deploy -e -Prelease -DskipTests
