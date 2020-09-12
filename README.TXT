1. Move the seppuku jar to ./lib
2. In build.gradle, change:
   compile files("lib/seppuku-1.12.2-2.5.jar")
   to whatever the jar file is called
3. $./gradlew setupDecompWorkspace
4. $./gradlew build
5. External module will be in ./build/libs/seppuku-rainbowhud-X.X.jar
6. Move to the modules folder
