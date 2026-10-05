# Wells Mic Android - build notes

The Android application is built automatically by the `Wells Mic Android APK` GitHub Actions workflow.

Current experimental target:
- Minimum Android: 8.0 (API 26)
- Reference device: Samsung SM-J410G running Android 8.1
- Build variant: debug
- Expected artifact: `Wells-Mic-v0.1-debug-apk`

The Android application must not modify USB tethering, RNDIS interfaces, network routes, or the phone's internet-sharing configuration.
