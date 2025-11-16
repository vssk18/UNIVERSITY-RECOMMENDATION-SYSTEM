JAVA_SRC := $(shell find src -name "*.java")
JAVA_OUT := out

all: run

compile:
	mkdir -p $(JAVA_OUT)
	javac -d $(JAVA_OUT) $(JAVA_SRC)

run: compile
	java -cp $(JAVA_OUT) cli.UniversityRecommendationSystem

summary:
	$(MAKE) -C tools/cpp_summary run
