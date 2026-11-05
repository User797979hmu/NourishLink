# NourishLink — Android app

Kotlin, Views + ViewBinding, Room (on-device database). One app, no backend: press Run and it works,
online or offline.

## Run it

1. Android Studio → **Open** → this folder (`android/NourishLink`).
2. Let Gradle sync, then press **Run ▶**. Requires Android Studio with JDK 17 (bundled in recent versions).

On first launch the app creates its database and loads demo accounts and 12 realistic Gauteng surplus
listings. Nothing else to install or start.

## Demo accounts

All use the password `Password1`.

| Role        | Email                        | Name / organisation        |
|-------------|------------------------------|----------------------------|
| Donor       | donor@nourishlink.demo       | Woolworths Sandton         |
| Donor       | pnp@nourishlink.demo         | Pick n Pay Rosebank        |
| Beneficiary | beneficiary@nourishlink.demo | Thandi Mokoena (shelter)   |
| Beneficiary | soweto@nourishlink.demo      | Soweto Care Foundation     |

You can also register your own account, or tap **Continue as Guest**.
To reset all data back to the demo state: uninstall the app (or *Clear storage* in Android settings) and run again.

## How it maps to the brief

| Requirement | Where it's implemented |
|---|---|
| Register & log in, encrypted password | `ui/auth/*`, `data/repository/AuthRepository.kt`. Passwords are salted and hashed with PBKDF2-HMAC-SHA256 (`util/PasswordHasher.kt`) — the plain text is never stored. The signed-in session is kept in `SessionManager` using `EncryptedSharedPreferences` (AES-256). |
| Change settings | `ui/settings/SettingsFragment` — profile fields, language, notification and leaderboard toggles; saved to the on-device database. |
| Database | `data/local/*` — Room database holding users, listings, claims, messages and notifications. `DatabaseSeeder` loads the demo data. |
| Surplus feed | `ui/feed/FeedFragment` + `ListingRepository.filterAndRank` — category / radius / search filters, ranked by distance + urgency (closest and soonest-to-expire first). |
| Reserve a listing | `ClaimRepository.reserve` — saves the claim and marks the listing reserved in one transaction, so it can't be reserved twice. Works with no connection at all. |
| Logistics messages | `ListingDetailActivity` + `MessageRepository`. |
| Notifications | `NotificationRepository`; `MainActivity` shows a system notification (every 20 s while the app is open) when a new in-app notification arrives, e.g. "Reservation confirmed". |
| Impact & leaderboard | `ui/impact/ImpactFragment` + `AuthRepository.getLeaderboard`. |
| Multi-language (isiZulu, Xitsonga, English) | `util/LocaleHelper.kt` + `res/values-zu`, `res/values-ts`; switchable at runtime from Profile. |

## Notes

- Google Sign-In and Firebase push were removed: both need an online service to verify against.
  Notifications are delivered from the on-device database instead.
- The isiZulu/Xitsonga translations haven't been reviewed by a fluent speaker — worth a check before submitting.
