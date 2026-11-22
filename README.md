# Quick Files

1. Find-replace `com.rahul.quickfiles` with your own unique id (e.g. com.rahul.quickfiles) in:
   - app/build.gradle.kts (2 places)
   - app/src/main/res/xml/shortcuts.xml (8 places)
   - rename folder app/src/main/java/com.rahul.quickfiles to match
   - the package line in MainActivity.kt
2. Create an upload key (needs Java):
   keytool -genkeypair -v -keystore upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
   Keep upload.jks safe. Never upload it to GitHub.
3. Convert it to base64 and add these GitHub repo Secrets (Settings > Secrets and variables > Actions):
   KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
   (Linux/Mac: base64 -w0 upload.jks ; Windows: certutil -encode upload.jks out.txt)
4. Push to GitHub > Actions tab > run "Build" > download artifact "quickfiles"
   (.aab for Play Store, .apk for testing on your phone).
