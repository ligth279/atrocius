# iSmart Schedule

**⚠️ This project is still in development. Use without the author’s permission is not permitted.**

iSmart Schedule is a smart weekly scheduler for Windows, featuring timeline visualizations, theming, and a bundled installer.

## Features

- Calendar and daily timeline views with animated highlights
- Task, event, and work planners with hour + minute selectors
- Dark/light theme toggle
- Windows installer with bundled JRE (no need for separate Java install)

## How to Install

1. Download the latest installer from the [Releases](https://github.com/YourUsername/YourRepo/releases) page or from your distribution link.
2. Double-click the `iSmart-Schedule-Setup-2.0.0.exe` file.
3. Follow the on-screen instructions to complete the installation.
4. Launch iSmart Schedule from the Start Menu or Desktop shortcut.
## VERSION
  ** 1.5.0 **
     - imporved dark mode!
     - improved slot system for more flexiblity
  ** 2.0.0 **
     - improved ui/ux with more color coding and better design
     - improved timetable generator logic
     - notifcation!


## How to Build (for developers)

1. Make sure you have Java 21+ and Maven 3.9+ installed.
2. Clone this repository.
3. Run:
    ```bash
    mvn clean package
    ```
4. To create a Windows installer (requires NSIS 3.11+):
    ```cmd
    cd dist
    "C:\Program Files (x86)\NSIS\makensis.exe" installer.nsi
    ```

## License

See [LICENSE.txt](LICENSE.txt) for details.

---

**Note:** This software is under active development. Do not use, distribute, or modify without explicit permission from the author.