JC = javac
JR = java

MNKGAME =  mnkgame
PLAYER = player
TESTER = tester
CLASS = classes

MAIN_CLASS = MNKGame
TESTER_CLASS = MNKPlayerTester
PLAYER_CLASS = NostroPlayer
OPPONENT_CLASS = OurPlayer

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
	$(JC) -d $(CLASS) $(PLAYER)/*.java

full-build:
	$(JC) -d $(CLASS) $(MNKGAME)/*.java && $(JC) -d $(CLASS) $(PLAYER)/*.java && $(JC) -d $(CLASS) $(TESTER)/*.java

vshuman:
	$(JR) -cp $(CLASS) $(MNKGAME).$(MAIN_CLASS) $(MNK) $(PLAYER).$(PLAYER_CLASS)

test1move:
	$(JR) -cp $(CLASS) $(MNKGAME).$(TESTER_CLASS) $(MNK) $(PLAYER).$(PLAYER_CLASS) $(TESTER).$(OPPONENT_CLASS) -r $(rep)

test2move:
	$(JR) -cp $(CLASS) $(MNKGAME).$(TESTER_CLASS) $(MNK) $(TESTER).$(OPPONENT_CLASS) $(PLAYER).$(PLAYER_CLASS) -r $(rep)

test1moveV:
	$(JR) -cp $(CLASS) $(MNKGAME).$(TESTER_CLASS) $(MNK) $(PLAYER).$(PLAYER_CLASS) $(TESTER).$(OPPONENT_CLASS) -v

test2moveV:
	$(JR) -cp $(CLASS) $(MNKGAME).$(TESTER_CLASS) $(MNK) $(TESTER).$(OPPONENT_CLASS) $(PLAYER).$(PLAYER_CLASS) -v

clean:
	$(RM) $(CLASS)\$(PLAYER)

full-clean:
	$(RM) $(CLASS)\