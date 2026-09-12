# Self Payroll — PWA + Android APK

This package contains:
- A static/offline-first PWA at the repository root.
- An Android WebView wrapper in `android/`.
- GitHub Pages deployment workflow: `.github/workflows/deploy-pages.yml`.
- GitHub Actions APK build workflow: `android/.github/workflows/build-apk.yml`.

The APK loads the same website files from Android assets, so it works without an internet connection for the app's local features.

For GitHub:
1. Upload the root website files to the repository root.
2. Keep the `android/` folder exactly as included.
3. Commit to `main`.
4. Pages uses the root website workflow.
5. Actions builds a debug APK and stores it as an Actions artifact.

Note: website data is browser-local/offline data. It is not automatically synchronized between browsers/devices.
