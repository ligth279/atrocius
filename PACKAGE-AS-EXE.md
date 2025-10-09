# Packaging JavaFX App as Windows EXE

This project can be packaged as a native Windows EXE installer using the jpackage tool (included with JDK 14+). This will bundle your app and a Java runtime, so users do not need to install Java separately.

## Steps (Windows, JDK 17+ required)

1. **Build the JAR**
   - Run: `mvn clean package`
   - The JAR will be in `target/scheduler-1.0-SNAPSHOT.jar`

2. **Run jpackage**
   - Example command:
     ```cmd
     jpackage \
       --type exe \
       --input target \
       --name SchedulerFX \
       --main-jar scheduler-1.0-SNAPSHOT.jar \
       --main-class com.example.SchedulerFX \
       --icon src/main/resources/icons/calendar-blue.png \
       --java-options "-Dprism.order=sw" \
       --win-shortcut \
       --win-menu \
       --win-dir-chooser \
       --win-menu-group "SchedulerFX" \
       --win-upgrade-uuid "YOUR-UUID-HERE"
     ```
   - Replace `YOUR-UUID-HERE` with a random UUID (use `uuidgen` or any online generator).

3. **Installer Output**
   - The EXE installer will be created in the current directory.

## Notes
- You can customize the installer name, icon, and menu group.
- The generated EXE will work on any Windows machine (no Java required).
- You can re-run these steps any time you update your app.

---

For advanced options, see the [jpackage documentation](https://docs.oracle.com/en/java/javase/17/docs/specs/man/jpackage.html).
