# Pool3D
3D zero-gravity pool using Java3D.

This is a 3D zero-gravity pool game that I developed initially as a final
project for a Computational Geometry class using a [simple rendering
engine](https://github.com/CSUDallas/Geometry2014) that our class created. This
changed in [this
commit](https://github.com/McBrainy/Pool3D/commit/52c39a775719225524d4cbf515d199f9576e92b2),
when I switched to Java3D. While the important work of math, physics, and
graphics is done, the game still needs the addition of pockets and simple game
mechanics to be fully playable. (At present, it is more of a physics
simulation.)

## Controls

- Use WASD to slide the camera vertically or horizontally.
- Use the arrow keys to move forward and backward or rotate the camera.
- Press Q to switch to shooting mode.
  - In cue stick mode, use either WASD or the arrow keys to rotate around the
	cue ball.
  - Use spacebar to strike the cue ball and return to normal mode.
  - Use Q again to return to normal mode.

## Requirements

To run, you need JRE 1.8.0 or better. To build from source, you need JDK at
least 1.8.0 and Maven.

## Building the Project

First, download the Java3D jars from
[here](http://jogamp.org/deployment/java3d/1.6.0-pre12/jogamp-java3d.7z). Then,
after unzipping the archive, `cd` to the directory where you unzipped it and run
these commands:
```
mvn install:install-file -DgroupId=java3d -DartifactId=vecmath \
  -Dpackaging=jar -Dversion=1.6.0 -Dfile=vecmath.jar -DgeneratePom=true
mvn install:install-file -DgroupId=java3d -DartifactId=j3d-core-utils \
  -Dpackaging=jar -Dversion=1.6.0 -Dfile=j3dutils.jar -DgeneratePom=true
mvn install:install-file -DgroupId=java3d -DartifactId=j3d-core \
  -Dpackaging=jar -Dversion=1.6.0 -Dfile=j3dcore.jar -DgeneratePom=true
```
This installs the Java3D jars in your Maven local repository. You can now build
the project with typical Maven commands, such as `mvn compile`, run in the same
directory as the pom.xml. Running `mvn package` will also build a jar with
dependencies using the Maven assembly plugin. Note, however, that the pom.xml
file is not located in the repository root, but in `Pool3D/Pool3D`.
