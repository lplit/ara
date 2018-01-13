SRC=$(shell find ./ -name "*.java")
BIN=$(shell find ./ -name "*.class")
DIR_PEERSIM="/media/sf_Dropbox/UPMC/Mats/ARA/proj/peersim-1.0.5/"
CFG=$(shell find ./ -name "cfg_initial.txt")
JCC=javac
JFLAGS= -cp .:$(DIR_PEERSIM)/* $(SRC)
LINER=java -cp .:$(DIR_PEERSIM)/* peersim.Simulator



all : $(BIN)
	clear
	javac $(JFLAGS)

run : $(BIN)
	$(LINER) $(CFG) >> "$(CFG).results"

$(BIN) : $(SRC)
	$(JCC) $(JFLAGS)

clean :
	@echo "Cleaning..."
	@echo "Removing class files..."
	@rm -fv `find . -name "*.class"`
	@echo "Cleaning up after emacs..."
	@rm -fv  `find . -name "*~"`
	@echo "Clean done!"

.PHONY: all clean run
