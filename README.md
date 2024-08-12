Project Guy

Prerequisites
1.	Android Studio Version: Recommended 2022.1.1 or higher.
2.	Android SDK Version: Ensure you have SDK 33 (or the latest stable version).
3.	Java Development Kit (JDK): Version 11 or higher.
4.	Gradle: Integrated within Android Studio, but ensure you have the latest version compatible with your Android Studio setup.
   
Installation Steps
1.	Clone the Repository:
  o Use Git to clone the project:
git clone https://github.com/PaytonNess/Al_Guardian_Guy_Project.git
  o	Navigate into the project directory:
bash cd Al_Guardian_Guy_Project

3.	Import the Project into Android Studio:
  o	Open Android Studio and select "Open an existing Android Studio project."
  o	Navigate to the cloned repository and open it.
  o	Android Studio will automatically sync the project using Gradle.

4.	Build the Project:
  o	Once the project is loaded, Android Studio will sync the Gradle files. After the sync, build the project by selecting "Build > Make Project" from the menu or by pressing Ctrl + F9.

5.	Set Up an Emulator:
  o	Create an emulator via AVD Manager with the SDK version 33 or higher.
  o	Alternatively, set up a physical device with USB or wireless debugging enabled.
6.	Run the Project:
  o	You can run the project on an Android emulator or a physical device by selecting the "Run" option in Android Studio.
  o	Ensure that your environment is set up for Android development, including setting up an Android Virtual Device (AVD) or connecting a physical device with USB debugging enabled.
Recommendation: We recommend using a physical device with USB debugging enabled to avoid issues with the emulator.

Running the Project from Command Line
1.	Build the APK:
  o	From the root directory of the project, run the following command to build the APK:
./gradlew assembleDebug

3.	Install the APK on a Device:
  o	Use the Android Debug Bridge (ADB) to install the APK on a connected device:
adb install -r app/build/outputs/apk/debug/app-debug.apk

Additional Scenarios:
•	You could record a YouTube video of someone saying climate change is a hoax and ask if it is true.
•	Another option could be to record a troubling text conversation and ask what you should do about it.

Libraries Used: 
Android
  o Retrofit:  For networking and API communication.
  o Mobile FFmpeg: Enables media processing features.

Backend
  o Node.js: Server-side runtime environment.
  o Vercel: Cloudplatform for deployment.
  o Google Generative AI:  Uploading multimodal prompts and messaging with Gemini 1.5 Pro.

Usage
Navigation: Record your screen and internal audio using the overlay. It will be uploaded, converted to text, and immediately deleted. Have a conversation about it with Gemini.
Key Interactions: Resize the overlay by dragging the bottom or right sides. Use the button on the bottom to perform the apps actions. Use the buttons on the top to return to the record screen or close the window.

Future Enhancements
Record only what the region within the overlay, currently the entire screen in recorded. 
Due to Vercels file size limit, recordings are currently limited to 15-20 seconds.

