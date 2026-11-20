# PlayCube Task Manager

A TV remote-friendly task manager with a per-app **×**, **multi-select / Close Selected**, and a persistent **Never Close** list. Based on [TV Recents](https://github.com/haf280/android-tv-button-mapper) (MIT license; original attribution in LICENSE).

## Features

- Keep the upstream recent-apps overlay and remote-button mapper.
- D-pad-focusable × at top right of each app card, with select and protect toggles below.
- **Close selected** stops only manually checked apps.
- **Never Close** manages a persistent protected-app list, including launchable installed apps.
- Protect Projectivy Launcher (`com.spocky.projengmenu`) and URL Router (`com.playcube.urlrouter`) by default.
- Close All, Close Selected, Menu and × respect protections. Also protect system apps, launchers, enabled accessibility services, keyboards and TV vendor components.
- No root, Shizuku, active wireless debugging, or network access needed after setup.

## Install

The package is `com.playcube.taskmanager`, separate from original TV Recents (`io.github.haf280.tvrecents`).

1. Open **Actions → Build PlayCube Task Manager APK** in this repo. Download and unzip the `playcube-task-manager-apk` artifact.
2. Install using your wireless ADB connection:
   ```cmd
   adb -s IP:PORT install -r playcube-task-manager.apk
   ```
3. Enable **PlayCube Task Manager** under TV **Settings → Accessibility**.
4. Disable **Recents** (the old app's Accessibility service) while testing to prevent competing key interception.
5. Open the app from Projectivy, test closing VLC with ×, and try Select / Close Selected and Never Close.
6. The existing remote *mapping functionality* is kept, but the *saved assignments* do not migrate between package IDs. Reconfigure remote mappings using **Buttons**.

## Limitations

- A recent-apps list is not a full running-process list.
- Force-stop automation depends on the TV's App info UI and can time out.
- This project has not yet been tested on your PlayCube hardware.
- Builds created separately by GitHub Actions use newly generated signing keys unless you supply a stable private signing setup. Those APKs cannot update one another in place. Don't commit signing keys.
- This won't fix the unrelated vendor `sdmc_bin` CPU load.

## Build

JDK 17, Android SDK platform 34 and build-tools 34.0.0. Run `bash build.sh`. Output: `out/playcube-task-manager.apk`.

MIT license, retain original copyright notice in LICENSE.
