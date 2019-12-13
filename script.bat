@echo off
del /Q bin         
javac -d bin src/*.java 
java -cp ./bin UrlReader        
pause  