
JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
				$(JC) $(JFLAGS) $*.java

CLASSES = \
	      SimpleGui3C.java \
				MyDrawPanel.java \
				TwoButtons.java \
				SimpleAnimation.java \
				CalcPanel.java

all: classes

classes: $(CLASSES:.java=.class)

clean:
				$(RM) *.class
