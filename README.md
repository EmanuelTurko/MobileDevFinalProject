# RecipeHub 🍲

RecipeHub is a social Android app written in Kotlin that lets users share, browse, and rate recipes — like Instagram for food lovers. It supports both online and offline functionality using Firebase and a local Room (DAO) database.

---

## 📱 Features
- Upload recipes with images, ingredients, and instructions
- Rate other users’ recipes from 1 to 5 stars
- Browse recipes by popularity or newest
- Save and view recipes offline with Room database
- Firebase authentication and cloud storage
- Modern UI with Material Design components and spinners

---

## 🛠 Tech Stack
- Kotlin (Android)
- Firebase (Auth, Firestore, Storage)
- Room (DAO) for local storage
- Material Design (Buttons, Cards, Spinners, etc.)
- Coroutines for async operations

---

## 🚀 How to Run

   1. Clone the repo

    git clone https://github.com/EmanuelTurko/RecipeHub.git
    cd RecipeHub

   2. Open the project in Android Studio

   3. Set up Firebase

      - Go to Firebase Console

      - Create a new Firebase project
  
      - Register your Android app using the package name from AndroidManifest.xml

      - Download the google-services.json config file

      - Place the file inside the /app directory

   4. Build and run the app on an emulator or Android device
