JAR=stanford.jar
REPO=docker.lappsgrid.org
GROUP=nlp
NAME=stanford
IMAGE=$(GROUP)/$(NAME)
TAG=$(REPO)/$(IMAGE)
VERSION=$(shell cat VERSION)

jar:
	mvn package
	cp target/$(JAR) src/main/docker/

clean:
	mvn clean
	if [ -e src/main/docker/$(JAR) ] ; then rm src/main/docker/$(JAR) ; fi

docker:
	if [ ! -e src/main/docker/$(JAR) ] ; then cp target/$(JAR) src/main/docker ; fi
	if [ target/$(JAR) -nt src/main/docker/$(JAR) ] ; then cp target/$(JAR) src/main/docker ; fi
	cd src/main/docker && docker build -t $(IMAGE) .
	docker tag $(IMAGE) $(TAG)

run:
	java -jar target/stanford.jar

debug:
	docker run -it -p 11111:11111 -p 5672:5672 -e RABBIT_USERNAME=$(RABBIT_USERNAME) -e RABBIT_PASSWORD=$(RABBIT_PASSWORD) --name $(NAME) $(IMAGE) /bin/bash

start:
	docker run -d -p 11111:11111 -p 5672:5672 -e "RABBIT_USERNAME=$(RABBIT_USERNAME)" -e "RABBIT_PASSWORD=$(RABBIT_PASSWORD)" --name $(NAME) $(IMAGE)

stop:
	docker rm -f $(NAME)

push:
	docker push $(TAG)

tag:
	docker tag $(IMAGE) $(TAG):$(VERSION)
	docker push $(TAG):$(VERSION)

update:
	curl -X POST http://129.114.17.83:9000/api/webhooks/61eb1457-74b1-4968-abae-977b36681876

