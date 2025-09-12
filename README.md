# BFH_APP1_MagnetDetector_SwipeSquad
# MagnetSensor Android App

**MagnetSensor** is a simple Android app that detects and displays the strength of magnetic fields near your device using its built-in magnetometer sensor.

## Features
- Real-time magnetic field detection
- Clean UI with a progress bar (0–100%) showing magnetic field strength
- Percentage display for quick reference

## Usage
1. **Install and run the app** on your Android device.
2. The main screen displays a progress bar representing the current magnetic field strength as a percentage.
3. The value updates in real-time as the sensor detects changes in the magnetic environment.

## Requirements
- An Android device with a magnetometer sensor (most modern smartphones support this)

## How it works
The app uses the magnetic field sensor (`Sensor.TYPE_MAGNETIC_FIELD`) to read X, Y, and Z components of the surrounding magnetic field. The total field strength is calculated and normalized to a 0–100% scale, which is visualized with a progress bar.

## Example UI

```
Magnetic Field Strength
[█████████████-----------] 57%

5.0 µT (5%)
Mesured in nano Tesla µT
```

## Notes
- For best results, keep the device away from strong magnets and metal surfaces unless intentionally testing.
- If the progress bar does not move, your device may not support magnetometer readings.

---
**Made for WIKO Power U10**
