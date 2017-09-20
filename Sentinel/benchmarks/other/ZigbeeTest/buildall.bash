cwd=$(pwd)
cd ./../../..
make iotjava
cd benchmarks
make nocheck
cd $cwd
cd ../../packages/
# ./clean.bash
# ./build.bash
cd $cwd
javac -cp ./../../../bin:.:../../packages/build  *.java