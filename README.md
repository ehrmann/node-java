jnode
=====

Java implementation of the node.js launcher

The inspiration for this project was being annoyed that lessc *requires* an installation of Node.js;
it felt a bit like the YUI compressor requiring Tomcat.

At least for now, this is a Java implementation of the Node.js API with just enough progress to run lessc.
And it's gcj-compatible, so it sucessfully builds down to a native executable. To do that, run

```
make
```

And you'll have a ```lessc``` executable in the current directory.  This has been tested with gcj 4.6.3
on Ubuntu and gcj 4.7.3 on Cygwin.  While it generally works with gcj, it exposes some bugs in gcj's JIT.
These issues don't seem to happen with the Hotspot and Oracle's JRE.
