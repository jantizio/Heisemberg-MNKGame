JC = javac
JR = java

MNKGAME =  mnkgame
PLAYER = player
TESTER = boneless
CLASS = classes
LIB = lib

MAIN_CLASS = $(MNKGAME).MNKGame
TESTER_CLASS = $(MNKGAME).MNKPlayerTester
PLAYER_CLASS = $(PLAYER).NostroPlayer
OPPONENT_CLASS = $(TESTER).OurPlayer

ifeq ($(OS),Windows_NT)
	detected_OS := Windows
	RM = rmdir /s /q
	SEP = ;
else
	detected_OS := $(shell uname)
	RM = rm -rf
	SEP = :
endif

RUNTIME_OPTIONS = -cp $(CLASS)$(SEP)$(LIB)/* -Xmx8G
CHANGE_DIR = cd "./$(CLASS)/$(SRC)"

MNK = 3 3 3
rep = 10

build:
	$(JC) -d $(CLASS) $(PLAYER)/*.java

full-build:
	$(JC) -d $(CLASS) $(MNKGAME)/*.java && $(JC) -d $(CLASS) $(PLAYER)/*.java

vshuman:
	$(JR) $(RUNTIME_OPTIONS) (MAIN_CLASS) $(MNK) $(PLAYER_CLASS)

test1move:
	$(JR) $(RUNTIME_OPTIONS) $(TESTER_CLASS) $(MNK) $(PLAYER_CLASS) $(OPPONENT_CLASS) -r $(rep)

test2move:
	$(JR) $(RUNTIME_OPTIONS) $(TESTER_CLASS) $(MNK) $(OPPONENT_CLASS) $(PLAYER_CLASS) -r $(rep)

test1moveV:
	$(JR) $(RUNTIME_OPTIONS) $(TESTER_CLASS) $(MNK) $(PLAYER_CLASS) $(OPPONENT_CLASS) -v

test2moveV:
	$(JR) $(RUNTIME_OPTIONS) $(TESTER_CLASS) $(MNK) $(OPPONENT_CLASS) $(PLAYER_CLASS) -v

complete-test:
	$(JR) $(RUNTIME_OPTIONS) player.Tester

clean:
	$(RM) $(CLASS)\$(PLAYER)

full-clean:
	$(RM) $(CLASS)\
