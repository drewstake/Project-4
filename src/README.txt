Instructions:

1. Change directory to the "src" folder:
   cd src

2. Generate Lexer.java from Lexer.flex using JFlex:
   java -jar jflex-1.6.1.jar Lexer.flex

3. Compile all Java files and store the .class files in the "class" folder:
   javac -d ../class *.java

4. To run a test, change directory to the "class" folder and use the following command syntax:
   java Program ../testcases/<test_case_name>.minc > ../actual_outputs/<test_case_name>.txt

   For example, to run the test case "fail_01":
   java Program ../testcases/fail_01.minc > ../actual_outputs/fail_01.txt