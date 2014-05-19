cat $1 | while read e;
do
    echo "Testing $e"
    java Compiler "$e"
    echo ""
done