# reaper-game
A game called Reaper. It relies on having the LWJGL3 library being included in the project. Additionally, in order to use the LWJGL3 library, the project must be run with the VM (terminal?) option:
-Djava.library.path="/home/murdock/NetBeansProjects/libs/lwjgl/native"
where the path is replaced with whatever is necessary. The compiled jar, however, should run on its by own automatically setting this property to the location of the included library as if the user has done it themselves through the terminal.
