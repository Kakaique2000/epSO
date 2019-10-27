@break off
@echo off
mkdir .output 2> NUL
echo Compilando... 
javac Escalonador.java -d .output/ 
echo Executando...
java -classpath .output Escalonador
echo Execucao terminada, pressione qualquer tecla para sair
pause >NUL