JC = javac
JR = java

SRC =  mnkgame
CLASS = classes

MAIN_CLASS = mnkgame.MNKGame
TESTER_CLASS = mnkgame.MNKPlayerTester
PLAYER_CLASS = mnkgame.NostroPlayer

CHANGE_DIR = cd "./$(CLASS)/$(SRC)"

ifeq ($(OS),Windows_NT)
	detected_OS := Windows
	RM = rmdir /s /q
else
	detected_OS := $(shell uname)
	RM = rm -rf
endif

MNK = 3 3 3
rep = 10

build:
	$(JC) -cp ".." -d $(CLASS) $(SRC)/*.java

vshuman:
	$(CHANGE_DIR) && $(JR) -cp ".." $(MAIN_CLASS) $(MNK) $(PLAYER_CLASS)

test1move:
	$(CHANGE_DIR) && $(JR) -cp ".." $(TESTER_CLASS) $(MNK) $(PLAYER_CLASS) mnkgame.QuasiRandomPlayer -r $(rep)

test2move:
	$(CHANGE_DIR) && $(JR) -cp ".." $(TESTER_CLASS) $(MNK) mnkgame.QuasiRandomPlayer $(PLAYER_CLASS) -r $(rep)

1movetestverbose:
	$(CHANGE_DIR) && $(JR) -cp ".." $(TESTER_CLASS) $(MNK) $(PLAYER_CLASS) mnkgame.QuasiRandomPlayer -v

2movetestverbose:
	$(CHANGE_DIR) && $(JR) -cp ".." $(TESTER_CLASS) $(MNK) mnkgame.QuasiRandomPlayer $(PLAYER_CLASS) -v

clean:
	$(RM) $(CLASS)\