GCJ = gcj

ant-launcher.o: lib/ant-launcher.jar
	gcj -c -o ant-launcher.o lib/ant-launcher.jar
	
ant.o: lib/ant.jar ant-launcher.o
	gcj -c -o ant.o --classpath=lib/ant-launcher.jar lib/ant.jar 

xbean.o: lib/xbean.jar ant.o
	gcj -c -o xbean.o --classpath=lib/ant.jar lib/xbean.jar 

lessc: js.o
	cd src && $(GCJ) --main=jnode.JNode --classpath=../lib/js.jar:. -o lessc.o `find . -name '*.java'` ../js.o ../examples/less.jar

js.o: lib/js.jar
	gcj -c --classpath=lib/xbean.jar -o js.o lib/js.jar