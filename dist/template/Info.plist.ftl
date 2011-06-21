<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple Computer//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>CFBundleAllowMixedLocalizations</key>
	<string>true</string>
	<key>CFBundleDevelopmentRegion</key>
	<string>English</string>
	<key>CFBundleExecutable</key>
	<string>JavaApplicationStub</string>
	<key>CFBundleIconFile</key>
	<string>rbnb.icns</string>
	<key>CFBundleIdentifier</key>
	<string>com.robonobo</string>
	<key>CFBundleInfoDictionaryVersion</key>
	<string>6.0</string>
	<key>CFBundleName</key>
	<string>robonobo</string>
	<key>CFBundlePackageType</key>
	<string>APPL</string>
	<key>CFBundleShortVersionString</key>
	<string>${version}</string>
	<key>CFBundleSignature</key>
	<string>RBNB</string>
	<key>CFBundleURLTypes</key>
	<array>
		<dict>
			<key>CFBundleURLIconFile</key>
			<string>robonobo.icns</string>
			<key>CFBundleURLName</key>
			<string>robonobo action</string>
			<key>CFBundleURLSchemes</key>
			<array>
				<string>rbnb</string>
			</array>
		</dict>
	</array>
	<key>CFBundleVersion</key>
	<string>robonobo ${version}</string>
	<key>Java</key>
	<dict>
		<key>ClassPath</key>
		<string>$JAVAROOT/robonobo-${version}.jar:/System/Library/Java</string>
		<key>JVMVersion</key>
		<string>1.5.0+</string>
		<key>MainClass</key>
		<string>com.robonobo.Robonobo</string>
		<key>Properties</key>
		<dict>
			<key>apple.laf.useScreenMenuBar</key>
			<string>true</string>
			<key>java.library.path</key>
			<string>$JAVAROOT</string>
			<key>java.net.preferIPv4Stack</key>
			<string>true</string>
		</dict>
		<key>VMOptions</key>
		<array>
			<string>-splash:../rbnb-splash.png</string>
			<string>-client</string>
			<string>-Xmx128m -XX:MaxPermSize=48m</string>
		</array>
		<key>WorkingDirectory</key>
		<string>$APP_PACKAGE/Contents/Resources/Java</string>
	</dict>
</dict>
</plist>
