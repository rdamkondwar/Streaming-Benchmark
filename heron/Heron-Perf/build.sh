echo "======================== Building Source Code ===========================\n"
mvn clean package
echo "======================== Copying files  ===========================\n"
cp target/classes/edu/wisc/streaming/* ~/.heron/examples/heron-examples/edu/wisc/streaming/

echo "======================== Making JAR ===========================\n"
CWD=`pwd`
cd ~/.heron/examples/heron-examples/
rm *.jar
jar cf run.jar *
cp run.jar `echo $CWD`
