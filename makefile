help:
	@echo "Usage:"
	@echo "make [task]"
	@echo "  tasks:"
	@echo "    - license-report: generate dependencies license report to license-report.html"
	@echo "    - build-jar: execute gradlew clean and than build, result in dgrv4_Gateway_serv/build/libs/digiRunner-$(shell cat dgrv4_Gateway_serv/src/main/resources/version.txt).jar"
	@echo "    - java-run: run digiRunner-$(shell cat dgrv4_Gateway_serv/src/main/resources/version.txt).jar using java -jar command"
	@echo "    - build-image: build docker image"
	@echo "    - run-container: run docker container"
license-report:
	@rm -f .tmp/license-report.html || true
	@rm -rf build/licenses || true
	@echo '{"allowedLicenses": []}' | jq > .tmp/allowed-licenses.json
	@sh gradlew -p . checkLicense || true
	@cat build/licenses/dependencies-without-allowed-license.json | jq '.dependenciesWithoutAllowedLicenses[].moduleLicense' | sort | uniq | jq --slurp '.' | jq '{allowedLicenses:[{moduleLicense:.[],moduleName:".*"}]}' > .tmp/allowed-licenses.json
	@sh gradlew -p . checkLicense
	@rm .tmp/allowed-licenses.json
	@mv build/licenses/index.html .tmp/license-report.html
	@rm -rf build/licenses
	@echo "output file: $(shell realpath .tmp/license-report.html)"
build-jar:
	@sh gradlew :dgrv4_Gateway_serv:clean
	@sh gradlew :dgrv4_Gateway_serv:build
java-run:
	java -jar dgrv4_Gateway_serv/build/libs/digiRunner-$(shell cat dgrv4_Gateway_serv/src/main/resources/version.txt).jar --digiRunner.token.key-store.path=$(shell pwd)/dgrv4_Gateway_serv/keys
build-image:
	@docker build -t digirunner .
run-container:
	@docker run -p 18080:18080 digirunner