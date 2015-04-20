cd ..
rm -rf test/matmul/output/*
java -cp ./lib/antlr-2.7.5.jar:./lib/cetus.jar:./lib/gcompiler.jar ece.ncsu.edu.gpucompiler.cuda.KernelDriver -cuda=cuda1_1 -outtype=cl -output=test/matmul_cl/output test/matmul_cl/matmul.c