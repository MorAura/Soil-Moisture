# Implementation Plan - Navigation and UI Dashboard

Implement the navigation and UI layer for the MoraAura SoilSense application using Jetpack Navigation 3, Material 3, and adaptive layouts.

## User Review Required

> [!IMPORTANT]
> The app will use Jetpack Navigation 3 as per instructions, which is different from the legacy NavController/NavHost approach.

## Proposed Changes

### [BLE Layer]
#### [MODIFY] [BleManager.kt](file:///C:/Users/LEGION/AndroidStudioProjects/SoilApp/app/src/main/java/com/example/soilapp/ble/BleManager.kt)
- Update to support manual device selection by exposing `foundDevices` list.
- Add public `connect(device: BluetoothDevice)` method.
- Update scan logic to not auto-connect.

### [Data Layer]
#### [MODIFY] [SoilSenseModels.kt](file:///C:/Users/LEGION/AndroidStudioProjects/SoilApp/app/src/main/java/com/example/soilapp/data/model/SoilSenseModels.kt)
- Add any missing state or data classes if needed (though existing ones seem sufficient for now).

### [UI Layer]
#### [MODIFY] [SoilSenseViewModel.kt](file:///C:/Users/LEGION/AndroidStudioProjects/SoilApp/app/src/main/java/com/example/soilapp/ui/SoilSenseViewModel.kt)
- Expose `foundDevices` from `BleManager`.
- Add `connect(device: BluetoothDevice)` method.
- Add navigation state handling using `NavBackStack`.

#### [NEW] [NavKey.kt](file:///C:/Users/LEGION/AndroidStudioProjects/SoilApp/app/src/main/java/com/example/soilapp/ui/navigation/NavKey.kt)
- Define serializable routes: `DeviceScan` and `Dashboard`.

#### [NEW] [DeviceScanScreen.kt](file:///C:/Users/LEGION/AndroidStudioProjects/SoilApp/app/src/main/java/com/example/soilapp/ui/screens/DeviceScanScreen.kt)
- Implement scanning UI with device list and filtering.
- Handle "SoilSense" filter.

#### [NEW] [DashboardScreen.kt](file:///C:/Users/LEGION/AndroidStudioProjects/SoilApp/app/src/main/java/com/example/soilapp/ui/screens/DashboardScreen.kt)
- Implement dashboard with metric cards.
- Implement alerting logic: Red color for out-of-bounds Moisture/Temperature.
- Add "Disconnect" button and navigation to history/thresholds (placeholders for now).

#### [MODIFY] [MainActivity.kt](file:///C:/Users/LEGION/AndroidStudioProjects/SoilApp/app/src/main/java/com/example/soilapp/MainActivity.kt)
- Replace basic UI with `NavDisplay` and adaptive scaffold.

#### [MODIFY] [Theme.kt](file:///C:/Users/LEGION/AndroidStudioProjects/SoilApp/app/src/main/java/com/example/soilapp/ui/theme/Theme.kt)
- Ensure nature-green aesthetic and dynamic color support.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure it compiles.

### Manual Verification
- Verify navigation between Scan and Dashboard.
- Verify alerting logic (Red text) when data is out of thresholds.
- Verify adaptive layout on different screen sizes (Preview).
