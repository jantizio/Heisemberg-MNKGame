JC = javac
JR = java

SRC =  mnkgame
CLASS = classes

MAIN_CLASS = mnkgame.MNKGame
PLAYER_CLASS = mnkgame.NostroPlayer

CHANGE_DIR = cd "./$(CLASS)/$(SRC)"

ifeq ($(OS),Windows_NT)
	detected_OS := Windows
	RM = rmdir /s /q
else
	detected_OS := $(shell uname)
endif

MNK = 3 3 3

build:
	$(JC) -cp ".." -d $(CLASS) $(SRC)/*.java

run:
	$(CHANGE_DIR) && java -cp ".." $(MAIN_CLASS) $(MNK) mnkgame.RandomPlayer mnkgame.QuasiRandomPlayer

clean:
	$(RM) "./$(CLASS)/$(SRC)"