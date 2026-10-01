# Smart Pantry Manager

Smart Pantry Manager is a Java Android application that records the ingredients a user has at home and suggests only recipes that can be prepared immediately. A recipe qualifies when every required ingredient is present in a compatible unit and at or above the required quantity.

## Core features

- Create, read, update and delete pantry items.
- Store ingredient name, quantity, unit and an optional expiry date.
- Persist all pantry records locally between app sessions.
- Seed 20 recipes on first database creation.
- Suggest recipes using strict all-ingredient and sufficient-quantity matching.
- Normalize simple plural names such as `tomatoes` and `tomato`.
- Convert compatible units, including kg to g, L to ml and tablespoons to ml.
- Display a recipe's full ingredient list and method.
- Store persistent settings for expiry warnings and the default input unit.
- Navigate between Pantry, Suggested Recipes and Settings with bottom navigation.

The app deliberately contains no Google Maps, GPS, payments or Play Store publishing integration because those features are outside the assignment scope.

## Database choice

The project uses Android's `SQLiteOpenHelper`. SQLite is suitable because the app is offline-first, its data is relational, and it does not require an account, network connection or external backend. The schema separates ingredients, aliases, pantry records, recipes and recipe requirements. Foreign keys preserve valid relationships, while first-run recipe seeding happens during database creation.

## Requirements

- Android Studio with a bundled JDK 17-compatible runtime
- Android SDK Platform 35
- Android SDK Build-Tools and Platform-Tools
- Android Emulator with a suitable system image, or a USB-debugging Android device

## Setup and run

1. Clone or download this repository.
2. Open the `SmartPantryManager` folder in Android Studio.
3. Allow Android Studio to download the Gradle 8.9 distribution and project dependencies.
4. If prompted, install Android SDK Platform 35 from **Tools > SDK Manager**.
5. Create a virtual device in **Tools > Device Manager**, or connect a physical Android device with USB debugging enabled.
6. Select the `app` run configuration and click **Run**.

No API key, server, Firebase project or database configuration is required.

## Verification commands

From the project folder on Windows:

```powershell
.\gradlew.bat clean testDebugUnitTest assembleDebug lintDebug
.\gradlew.bat connectedDebugAndroidTest
```

The first command runs local matching tests, builds a debug APK and performs Android lint checks. The second command requires a running emulator or connected Android device and tests the SQLite seed and CRUD flow.

## Strict matching rule

`RecipeMatcher` builds a total for every normalized pantry ingredient and measurement dimension. It converts compatible units to base quantities before checking each recipe requirement. The method returns `false` immediately when an ingredient is missing, has an incompatible measurement type, or is below the required quantity. Partial matches never enter the Suggested Recipes list.

Examples:

- Pantry `0.5 kg tomatoes` satisfies a requirement for `500 g tomato`.
- Pantry `2 tbsp oil` satisfies a requirement for `30 ml oil`.
- Pantry `499 g tomato` does not satisfy a requirement for `500 g tomato`.
- Four of five required ingredients never qualify as a strict match.

## Project structure

```text
app/src/main/java/za/ac/richfield/smartpantry/
├── data/       SQLite schema, seeding and repository CRUD
├── model/      Pantry and recipe domain objects
├── ui/         Activities and RecyclerView adapters
└── util/       Name normalization, unit conversion and matching
```

Top-level screens:

```text
PantryActivity
├── AddEditIngredientActivity
├── SuggestedRecipesActivity
│   └── RecipeDetailActivity
└── SettingsActivity
```

## Evidence files

- `docs/TEST_PLAN.md` contains functional, persistence and matching test cases.
- `docs/SCREENSHOT_CHECKLIST.md` lists the real running-app screenshots required by the report.
- `app/src/test` contains local unit tests for normalization, unit conversion and strict matching.
- `app/src/androidTest` contains on-device SQLite seeding and CRUD tests.

## Accessibility and usability

The app uses labelled inputs, readable type, consistent navigation, large Material buttons, empty-state feedback, deletion confirmation and inline validation. Pantry lists refresh in `onResume()` so changes appear immediately after returning from the Add/Edit screen.
