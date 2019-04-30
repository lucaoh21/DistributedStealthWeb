JFLAGS = -g
JC = javac
CLASSES = \
        RmiClient.java \
	RmiServer.java \
	RmiServerIntf.java
MY_INSTALL_DIR=httpcomponents-client-4.5.2/lib
CLASSPATH=.:httpcomponents-client-4.5.8/lib/httpclient-4.5.8.jar:httpcomponents-client-4.5.8/lib/httpcore-4.4.11.jar
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) -classpath ${CLASSPATH} $*.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class