GCJ = gcj

all: lessc

lessc: js.o less.o
	$(GCJ) \
		--main=jnode.JNode \
		-Djnode.JNode.main=bin/lessc \
		-Djnode.JNode.scriptLoader=ClassLoaderScriptLoader \
		--classpath=lib/js-no-xmlbeans.jar:src \
		-o lessc \
		`find src -name '*.java'` js.o less.o 

less.o: examples/less.jar
	$(GCJ) -c --classpath=lib/js-no-xmlbeans.jar -o less.o examples/less.jar

js.o: lib/js-no-xmlbeans.jar 
	$(GCJ) -c --classpath=lib/js-no-xmlbeans.jar -o js.o lib/js-no-xmlbeans.jar
	
clean:
	rm -f js.o less.o lessc
