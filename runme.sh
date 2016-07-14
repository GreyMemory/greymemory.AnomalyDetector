
# build greymemory jars
ant -f ../greymemory.AnomalyDetector -Dnb.internal.action.name=rebuild clean jar

# create lib folder
mkdir ./dist/lib

# copy greymemory.jar
cp ../greymemory/dist/greymemory.jar ./dist/lib

# copy greymemory.jar dependencies
cp ../greymemory/lib/* ./dist/lib

# copy greymemory.AnomalyDetecor.jar dependencies
cp ./lib/* ./dist/lib

