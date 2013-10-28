GCJ = gcj

lessc: js.o
	cd src && $(GCJ) --main=jnode.JNode --classpath=../lib/js.jar:. -o ../lessc `find . -name '*.java'` ../js.o ../examples/less.jar

js.o: lib/js.jar
	gcj -c --classpath=lib/xbean.jar -o js.o lib/js.jar
	
clean:
	rm -f js.o lessc