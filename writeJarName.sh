# This is a trick to get maven to tell us the project version so we can work out the filename of the JAR file it creates

version=`mvn help:evaluate -Dexpression=project.version | grep -v "^\["`
echo "Found version from Maven: $version"

filename="MakeHTML-$version.jar"
echo "Writing Jar filename: $filename"

echo $filename > target/jarName.txt
