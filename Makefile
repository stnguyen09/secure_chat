JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	SecureChat.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
	
server:
	java SecureChat server 2014
	
client:
	java RSACracker client 2014 localhost steve