# PetCare — Android Pet Care Management App

A native Android app (Kotlin) for managing pet profiles, care routines, task
checklists, SMS delegation of care duties, and geotagged pet-related places
(vets, groomers, parks, stores, shelters).

## Tech stack

- **Language:** Kotlin
- **Min SDK:** 24 (Android 7.0) · **Target/Compile SDK:** 34
- **Local database:** Room (SQLite) — pet, task, routine, location, and user data
  are all stored on-device
- **Authentication:** Local email/password accounts. Passwords are **never**
  stored in plain text — they're hashed with PBKDF2WithHmacSHA256 + a random
  per-user salt (see `utils/PasswordUtils.kt`). Session persistence is handled
  with `SharedPreferences` (`utils/SessionManager.kt`).
- **Maps / Geotagging:** Google Maps SDK for Android + Fused Location Provider
- **SMS delegation:** `SmsManager` + the system Contacts picker
- **Reminders:** `AlarmManager` + a `BroadcastReceiver`, rescheduled on boot
- **Images:** Camera (via `FileProvider`) or gallery picker, loaded with Coil

## ⚠️ Before you build: add a Google Maps API key

The Geotagging feature (Places tab, saved vets/groomers/parks/stores/shelters)
requires a Google Maps API key.

1. Go to the [Google Cloud Console](https://console.cloud.google.com/), create
   (or pick) a project, and enable the **Maps SDK for Android**.
2. Create an API key (restrict it to Android apps + your package name
   `com.petcare.app` for security).
3. Open `app/src/main/res/values/strings.xml` and replace:
   ```xml
   <string name="google_maps_api_key" translatable="false">YOUR_GOOGLE_MAPS_API_KEY_HERE</string>
   ```
   with your real key.

Without this, every screen works **except** the Places/Map tab, which will show
a blank/grey map.

## How to open the project

1. Unzip this project.
2. Open **Android Studio** (Giraffe/Koala or newer recommended) →
   **Open** → select the unzipped `PetCareApp` folder.
3. Let Gradle sync. The project ships with `gradlew`/`gradlew.bat` and
   `gradle-wrapper.properties` (Gradle 8.7, AGP 8.4.2, Kotlin 1.9.24); Android
   Studio will download the wrapper jar and all dependencies automatically the
   first time you sync — this needs an internet connection.
4. Add your Google Maps API key (see above).
5. Run on an emulator or device (API 24+). For the Maps screen on an emulator,
   use an image that includes Google Play services.

## Feature walkthrough

| # | Feature | Where to find it |
|---|---|---|
| 1 | Home / Splash screen with auto-redirect | Launches first; redirects straight to the dashboard if you're already logged in |
| 2 | Registration & Login | Full field validation (empty fields, email format, password strength, duplicate email), clear error messages, hashed local password storage, session persistence, Logout (Profile tab) |
| 3 | Pet profiles, edit, delete, mark tasks complete | **Pets** tab → tap a pet for details, edit/delete (cascades to its tasks & routines), tick/untick tasks with strikethrough + color change on completion |
| 4 | Pet care routines | From a pet's detail screen → **Add Routine**: category, daily/weekly schedule, day/time, supplies, notes, optional photo. Automatically generates an upcoming task checklist with reminders |
| 5 | SMS delegation | Pet detail screen → **Delegate via SMS**: pick which tasks to include, pick a contact or type a number, edit the auto-composed message, send |
| 6 | Geotagging (chosen "desirable" feature) | **Places** tab in the bottom navigation: map with markers for saved vet clinics, grooming salons, dog parks, pet supply stores and animal shelters. Add a place by tapping the mini-map, using your current GPS location, or searching an address. Link a place to a task/appointment. Tap a marker to view details, navigate, edit, or delete |

## Project structure

```
app/src/main/java/com/petcare/app/
├── data/
│   ├── local/          Room entities, DAOs, database, type converters
│   └── repository/     Repository layer between UI and Room
├── receiver/            AlarmManager reminder + boot-reschedule receivers
├── ui/
│   ├── splash/           Splash / auto-redirect
│   ├── auth/             Login, Register
│   ├── dashboard/        Bottom-nav host
│   ├── pet/              Pet list, detail, add/edit
│   ├── task/             All-tasks tab, add/edit task, checklist adapter
│   ├── routine/          Add/edit routine, routine adapter
│   ├── delegation/       SMS delegation screen
│   ├── map/              Geotagging map + add/edit location
│   └── profile/          Profile / logout
└── utils/                Validation, password hashing, session, SMS, images,
                           reminder scheduling, place-type labels
```

## Notes / things you may want to customize

- **App icon:** a simple placeholder paw-print icon is included
  (`res/mipmap-*/ic_launcher*`, `res/drawable/ic_launcher_foreground.xml`).
  Swap in your own branding via Android Studio's Image Asset tool if you'd
  like something more polished.
- **Firebase:** the spec allowed either Firebase Authentication or local
  hashed passwords — this project uses the local option so it builds and runs
  immediately with zero external configuration (besides the Maps key above).
  If you'd prefer Firebase Auth, you can swap `UserRepository` for a Firebase
  implementation without touching the rest of the app.
- **Exact-alarm reminders:** on Android 12+ users may need to grant "Alarms &
  reminders" permission in system settings for exact-time notifications; the
  app falls back to inexact delivery if it isn't granted.
