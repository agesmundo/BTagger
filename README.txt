//To test the code follow these quick guide
//(I recommend to set up an Eclipse project)
//This test is done a small portion of the Conll-2000 corpus
//The task is Noun-Phrase-Chunking given as input Words and POS tags

//0 PULL GIT REPO
mkdir BTagger
cd BTagger
git init
git remote add BTagger git@github.com:agesmundo/BTagger
git pull BTagger master

//1 COMPILE:
cd src/main/java/bTagger
javac *
cd ..
java bTagger/BTagger
//here should output usage 

//2 TRAIN TEST:
java bTagger/BTagger -t MY_TRAIN ../resources/test/trainNP.txt ../resources/test/featureScriptNP.txt
//BTagger outputs in the current folder weights files for each different round called : MY_TRAIN.<id>.fea

//3 PREDICT & EVALUATE
java bTagger/BTagger -pe MY_TEST ../resources/test/testNP.txt MY_TRAIN.2.fea ../resources/test/featureScriptNP.txt
//the content of the file src/main/resources/test/logEvaluation should match the shell output
//for the full NP corpus visit the Conll2000 website: http://www.cnts.ua.ac.be/conll2000/chunking/

=======================================================================

COPYRIGHT AND LICENSE:

Copyright (c) 2009 by Andrea Gesmundo <andrea.gesmundo@gmail.com>

See the file LICENSE.txt for the licensing terms that this software is
released under. 
