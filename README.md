# arborext
_arborext_ is a Java tool that copies a git repository and creates a
[GXL](https://userpages.uni-koblenz.de/~ist/GXL/index.php) file for every
commit, that can be read by [SEE](https://see.uni-bremen.de).

## Usage
```bash
java -jar arborext.jar -p <git|svn|hg> -r <repository url>
```

Will clone the given repository to **tmprepo** and create
a batch of numbered GXL files in the **current directory**.
**tmprepo** will be deleted afterwards.

## Building
You need a working installation of [Mavan](https://maven.apache.org/) for this build.

From the project root it is as simple as:
```bash
mvn package
```
then.

This will create a folder _target/_ which contains two .jar
files; one with and the other without the bundled dependencies.
