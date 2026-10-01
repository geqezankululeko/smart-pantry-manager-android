# Smart Pantry Manager Test Plan

Record the device/emulator, Android version, test date and result for each test before submission.

| ID | Test | Procedure | Expected result |
|---|---|---|---|
| F01 | Empty pantry | Launch after clearing app data | Friendly empty-state text appears; no crash |
| F02 | Required name | Open Add Ingredient, leave name blank and tap Save | Inline name error; no row inserted |
| F03 | Positive quantity | Enter `0`, a negative value or blank quantity | Inline quantity error; no row inserted |
| F04 | Create and read | Add `Egg`, `2`, `count` | Item appears in Pantry with correct values |
| F05 | Update | Edit the egg quantity to `3` | Existing row changes; no duplicate row |
| F06 | Delete cancel | Tap Delete, then Cancel | Row remains |
| F07 | Delete confirm | Tap Delete, then confirm | Row disappears |
| F08 | Persistence | Add an item, close the app and reopen it | Item remains |
| M01 | Exact strict match | Add every required ingredient at exact quantity | Recipe appears |
| M02 | Missing ingredient | Remove one required ingredient | Recipe disappears |
| M03 | Insufficient quantity | Reduce one item below the requirement | Recipe disappears |
| M04 | Excess quantity | Increase all items above requirements | Recipe appears |
| M05 | Name normalization | Use `Tomatoes` where recipe stores `Tomato` | Ingredient matches |
| M06 | Mass conversion | Use `0.5 kg` where recipe needs `500 g` | Ingredient matches |
| M07 | Volume conversion | Use `1 l` where recipe needs `200 ml` | Ingredient matches |
| M08 | Spoon conversion | Use `2 tbsp` where recipe needs `30 ml` | Ingredient matches |
| M09 | Duplicate rows | Add two `250 g Tomato` rows for a 500 g requirement | Quantities aggregate and match |
| M10 | Incompatible units | Add `500 count Tomato` for a `500 g` requirement | Recipe remains excluded |
| R01 | Seed count | Open Settings after first launch | Exactly 20 recipes are reported |
| R02 | No duplicate seed | Close and reopen repeatedly | Recipe count stays at 20 |
| N01 | Bottom navigation | Visit Pantry, Recipes and Settings | Correct screen opens and selected tab updates |
| N02 | Recipe Intent | Tap a suggested recipe | Correct details and method appear |
| S01 | Settings persistence | Toggle expiry warnings and change default unit, then restart | Both choices remain |
| L01 | Lifecycle refresh | Add/edit an item and return to Pantry | RecyclerView shows current database data |
| L02 | Rotation | Rotate each screen if enabled on the test device | No duplicate row or crash |

## Automated checks

Run local tests and the debug build:

```powershell
.\gradlew.bat clean testDebugUnitTest assembleDebug lintDebug
```

With an emulator/device connected, run the SQLite integration tests:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```
