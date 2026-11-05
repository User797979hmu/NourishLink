package com.nourishlink.app.data.local

import androidx.room.withTransaction
import com.nourishlink.app.util.IsoTime
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Fills a brand-new database with the demo accounts and 12 realistic Gauteng surplus listings,
 * so the app is fully usable the first time it launches — no server, no setup.
 *
 * Demo accounts (password for all of them: Password1):
 *   donor@nourishlink.demo        Woolworths Sandton   (donor)
 *   pnp@nourishlink.demo          Pick n Pay Rosebank  (donor)
 *   beneficiary@nourishlink.demo  Thandi Mokoena       (beneficiary)
 *   soweto@nourishlink.demo       Soweto Care Foundation (beneficiary)
 *
 * Safe to call any number of times: it only seeds when there are no users yet.
 */
class DatabaseSeeder(private val db: AppDatabase) {

    private val mutex = Mutex()
    @Volatile private var done = false

    suspend fun ensureSeeded() {
        // App starts clean on a fresh installation.
        // Users populate their own data when donors contribute surplus food.
        done = true
    }

    private suspend fun seed() {
        db.withTransaction {
            val userDao = db.userDao()
            val listingDao = db.listingDao()
            val notificationDao = db.notificationDao()
            val created = IsoTime.now()

            fun demoUser(
                name: String, email: String, role: String, org: String, phone: String,
                picture: String, lat: Double, lng: Double, address: String,
                points: Int, meals: Int, kg: Int, badges: List<String>
            ) = UserEntity(
                name = name, email = email, passwordHash = DEMO_PASSWORD_HASH, authProvider = "local",
                role = role, organisation = org, phone = phone, profilePicture = picture,
                latitude = lat, longitude = lng, address = address,
                impactPoints = points, mealsRedistributed = meals, kgSaved = kg,
                badges = UserEntity.joinBadges(badges), createdAt = created
            )

            val woolworthsId = userDao.insert(
                demoUser(
                    "Woolworths Sandton", "donor@nourishlink.demo", "donor", "Woolworths Sandton",
                    "+27 11 784 1000", "https://images.unsplash.com/photo-1578916171728-46686eac8d58",
                    -26.1076, 28.0567, "Sandton City Shopping Centre, Sandton, 2196",
                    420, 150, 60, listOf("First Donation", "10 Donations", "50kg Saved")
                )
            ).toInt()

            val pnpId = userDao.insert(
                demoUser(
                    "Pick n Pay Rosebank", "pnp@nourishlink.demo", "donor", "Pick n Pay Stores",
                    "+27 11 447 2100", "https://images.unsplash.com/photo-1542838132-92c53300491e",
                    -26.1465, 28.0436, "Rosebank Mall, 50 Bath Ave, Rosebank, 2196",
                    280, 95, 38, listOf("First Donation", "10 Donations")
                )
            ).toInt()

            val thandiId = userDao.insert(
                demoUser(
                    "Thandi Mokoena", "beneficiary@nourishlink.demo", "beneficiary", "Ubuntu Community Shelter",
                    "+27 82 555 0192", "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
                    -26.2041, 28.0473, "Ubuntu Shelter, 12 Commissioner St, Johannesburg, 2001",
                    180, 60, 24, listOf("First Collection", "Community Hero")
                )
            ).toInt()

            val sowetoId = userDao.insert(
                demoUser(
                    "Soweto Care Foundation", "soweto@nourishlink.demo", "beneficiary", "Soweto Youth & Care Center",
                    "+27 11 982 3000", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
                    -26.2485, 27.8540, "Vilakazi Street, Orlando West, Soweto, 1804",
                    350, 120, 48, listOf("First Collection", "10 Collections", "50kg Saved")
                )
            ).toInt()

            fun listing(
                donorId: Int, title: String, category: String, description: String, servings: Int,
                lat: Double, lng: Double, address: String, hoursAhead: Int, vendor: String,
                dietary: List<String>
            ) = ListingEntity(
                donorId = donorId, title = title, category = category, description = description,
                quantityServings = servings, latitude = lat, longitude = lng, address = address,
                collectBy = IsoTime.hoursFromNow(hoursAhead), status = "available",
                vendorName = vendor, dietaryFlags = dietary.joinToString(", "),
                distanceKm = 0.0, createdAt = created, updatedAt = created
            )

            val listings = listOf(
                listing(woolworthsId, "Fresh Organic Produce Box", "produce",
                    "Crisp apples, bananas, spinach, carrots and sweet potatoes in good condition.",
                    20, -26.1076, 28.0567, "Sandton City, Johannesburg", 5, "Woolworths Sandton",
                    listOf("vegan", "vegetarian", "gluten_free", "halal")),
                listing(woolworthsId, "Warm Chicken Curry & Basmati Rice", "cooked_meals",
                    "Freshly prepared chicken curry portions with rice from deli counter.",
                    25, -26.1140, 28.0600, "Rivonia Road, Sandton", 3, "Woolworths Deli Sandton",
                    listOf("halal", "nut_free")),
                listing(woolworthsId, "Artisan Sourdough & Croissants", "bakery",
                    "Fresh surplus sourdough loaves, butter croissants, and muffins.",
                    15, -26.1010, 28.0525, "Morningside, Sandton", 8, "Woolworths Bakery Sandton",
                    listOf("vegetarian", "halal", "nut_free")),
                listing(pnpId, "Fresh Milk, Yoghurt & Cheese Crates", "dairy",
                    "Full cream milk bottles, strawberry yoghurt, and cheddar cheese blocks.",
                    30, -26.1465, 28.0436, "Rosebank Mall, Rosebank", 12, "Pick n Pay Rosebank",
                    listOf("vegetarian", "gluten_free", "halal")),
                listing(pnpId, "Pantry Staples & Canned Soups", "packaged",
                    "Canned baked beans, vegetable soups, pasta packs, and maize meal bags.",
                    40, -26.1480, 28.0410, "Oxford Road, Rosebank", 48, "Pick n Pay Express",
                    listOf("vegetarian", "vegan", "halal")),
                listing(pnpId, "Rotisserie Roast Chicken Portions", "cooked_meals",
                    "Hot rotisserie roast chicken halves ready for distribution.",
                    18, -26.1910, 28.0335, "Braamfontein, Johannesburg", 4, "PnP Daily Deli Braamfontein",
                    listOf("halal", "gluten_free", "nut_free")),
                listing(woolworthsId, "Fresh Citrus & Melon Fruit Trays", "produce",
                    "Pre-cut watermelon slices, oranges, and grape packs.",
                    16, -26.2485, 27.8540, "Vilakazi Street, Soweto", 6, "Woolworths Soweto Express",
                    listOf("vegan", "vegetarian", "gluten_free", "halal")),
                listing(pnpId, "Vegetable Lasagne & Garlic Bread", "cooked_meals",
                    "Trays of baked spinach and ricotta lasagne with garlic rolls.",
                    22, -26.0125, 28.1278, "Waterfall City, Midrand", 7, "Pick n Pay Midrand",
                    listOf("vegetarian", "halal", "nut_free")),
                listing(woolworthsId, "Sandwich Platter & Wrap Selection", "cooked_meals",
                    "Assorted egg mayo, cheese & tomato, and grilled chicken wraps.",
                    14, -25.7479, 28.2293, "Menlyn Maine, Pretoria", 5, "Woolworths Menlyn Pretoria",
                    listOf("halal", "vegetarian", "nut_free")),
                listing(pnpId, "Greek Yoghurt & Berry Parfait Pots", "dairy",
                    "Chilled berry parfait pots with honey and granola.",
                    12, -26.1715, 28.0401, "Parktown, Johannesburg", 10, "PnP Express Parktown",
                    listOf("vegetarian", "halal")),
                listing(woolworthsId, "Assorted Dinner Rolls & Buns", "bakery",
                    "Bags of seeded burger buns, soft white rolls, and sesame rolls.",
                    25, -26.0920, 27.9980, "Ferndale, Randburg", 14, "Woolworths Ferndale",
                    listOf("vegan", "vegetarian", "halal", "nut_free")),
                listing(pnpId, "High Protein Nutritional Shakes", "packaged",
                    "Cartons of chocolate and vanilla protein beverages.",
                    20, -26.1550, 28.0680, "Houghton, Johannesburg", 72, "Pick n Pay Houghton",
                    listOf("vegetarian", "gluten_free", "halal"))
            )

            val listingIds = listingDao.insertAll(listings).map { it.toInt() }

            // A few "new surplus near you" alerts so the Notifications tab isn't empty on first run.
            val alertListingIndexes = listOf(0, 1, 5)
            val minutesAgo = listOf(12, 35, 90)
            val alerts = mutableListOf<NotificationEntity>()
            for (beneficiaryId in listOf(thandiId, sowetoId)) {
                alertListingIndexes.forEachIndexed { i, index ->
                    val l = listings[index]
                    alerts.add(
                        NotificationEntity(
                            userId = beneficiaryId,
                            title = "New surplus near you",
                            body = "${l.title} — ${l.quantityServings} servings, collect by ${l.collectBy}",
                            listingId = listingIds[index],
                            createdAt = IsoTime.minutesAgo(minutesAgo[i])
                        )
                    )
                }
            }
            notificationDao.insertAll(alerts)
        }
    }

    companion object {
        /** PBKDF2 hash of the demo password "Password1" (see PasswordHasher for the format). */
        private const val DEMO_PASSWORD_HASH =
            "pbkdf2_sha256\$100000\$PXBL2p1eMGgbWolSOTJH5Q==\$eoEizVQ1zHkwt2XpOCavBeXYG5YEg1HTFsK9D6VNQsk="
    }
}
