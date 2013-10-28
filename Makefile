GCJ = gcj

lessc: js.o
	$(GCJ) --main=jnode.JNode --classpath=lib/js.jar:src -o lessc `find src -name '*.java'` js.o less.o xmlbeans.o

less.o: examples/less.jar
	gcj -c -o less.o less.jar

xmlbeans.o: lib/xmlbeans.jar
	$(GCJ) -c --classpath=lib/xmlbeans.jar -o xmlbeans.o lib/xmlbeans.jar

js.o: lib/js.jar xmlbeans.o
	gcj -c --classpath=lib/xmlbeans.jar:lib/js.jar -o js.o lib/js.jar
	
clean:
	rm -f js.o less.o xmlbeans.o lessc
