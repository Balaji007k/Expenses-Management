package com.enterprise.expensetracker.core.database

import android.content.Context
import com.enterprise.expensetracker.core.common.dispatchers.CoroutineDispatchers
import com.enterprise.expensetracker.core.database.dao.BudgetMongoDao
import com.enterprise.expensetracker.core.database.dao.ExpenseMongoDao
import com.enterprise.expensetracker.core.database.dao.ItemCatalogMongoDao
import com.enterprise.expensetracker.core.database.dao.UserMongoDao
import com.enterprise.expensetracker.core.database.entity.BudgetMongoEntity
import com.enterprise.expensetracker.core.database.entity.ExpenseMongoEntity
import com.enterprise.expensetracker.core.database.entity.ItemCatalogMongoEntity
import com.enterprise.expensetracker.core.database.entity.UserMongoEntity
import com.enterprise.expensetracker.core.remoteconfig.FirebaseConfigManager
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.bson.Document
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext

/**
 * Enterprise Production MongoDB Manager Implementation.
 * Connects directly to MongoDB Atlas replica set with offline-first reactive caching.
 * Synchronizes Expenses, Users, Family/Member Budgets, and Global Item Catalog.
 */
@Singleton
class MongoDbManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseConfigManager: FirebaseConfigManager,
    private val dispatchers: CoroutineDispatchers
) : MongoDbManager {

    private val _connectionState = MutableStateFlow(MongoConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<MongoConnectionState> = _connectionState.asStateFlow()

    private val _activeConnectionUri = MutableStateFlow("")
    override val activeConnectionUri: StateFlow<String> = _activeConnectionUri.asStateFlow()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private var mongoClient: MongoClient? = null
    private var mongoDatabase: MongoDatabase? = null
    private var expensesCollection: MongoCollection<Document>? = null
    private var usersCollection: MongoCollection<Document>? = null
    private var budgetsCollection: MongoCollection<Document>? = null
    private var catalogCollection: MongoCollection<Document>? = null

    // Thread-safe in-memory & persistent cache stores
    private val localStore = ConcurrentHashMap<String, ExpenseMongoEntity>()
    private val _expensesFlow = MutableStateFlow<List<ExpenseMongoEntity>>(emptyList())

    private val localUsersStore = ConcurrentHashMap<String, UserMongoEntity>()
    private val _usersFlow = MutableStateFlow<List<UserMongoEntity>>(emptyList())

    private val localBudgetsStore = ConcurrentHashMap<String, BudgetMongoEntity>()
    private val _budgetsFlow = MutableStateFlow<List<BudgetMongoEntity>>(emptyList())

    private val localCatalogStore = ConcurrentHashMap<String, ItemCatalogMongoEntity>()
    private val _catalogFlow = MutableStateFlow<List<ItemCatalogMongoEntity>>(emptyList())

    init {
        seedInitialCatalog()
        seedInitialBudgets()

        // Observe dynamic connection changes from Firebase Remote Config safely
        CoroutineScope(dispatchers.io).launch {
            try {
                firebaseConfigManager.dynamicConfig.collect { config ->
                    if (config.dbConnectionUri.isNotBlank() && config.dbConnectionUri != _activeConnectionUri.value) {
                        _activeConnectionUri.value = config.dbConnectionUri
                        connectWithUri(config.dbConnectionUri)
                    }
                }
            } catch (t: Throwable) {
                _connectionState.value = MongoConnectionState.ERROR
            }
        }
    }

    private fun seedInitialCatalog() {
        val initialItems = listOf(
            ItemCatalogMongoEntity("tomato", "Tomato", "groceries_and_food", null, "kg", 40.0, 1.0, System.currentTimeMillis()),
            ItemCatalogMongoEntity("onion", "Onion", "groceries_and_food", null, "kg", 35.0, 1.0, System.currentTimeMillis()),
            ItemCatalogMongoEntity("potato", "Potato", "groceries_and_food", null, "kg", 30.0, 1.0, System.currentTimeMillis()),
            ItemCatalogMongoEntity("milk", "Milk", "groceries_and_food", null, "litre", 32.0, 1.0, System.currentTimeMillis()),
            ItemCatalogMongoEntity("bread", "Bread", "groceries_and_food", null, "packet", 45.0, 1.0, System.currentTimeMillis()),
            ItemCatalogMongoEntity("eggs", "Eggs", "groceries_and_food", null, "piece", 7.0, 6.0, System.currentTimeMillis()),
            ItemCatalogMongoEntity("rice", "Basmati Rice", "groceries_and_food", null, "kg", 110.0, 5.0, System.currentTimeMillis()),
            ItemCatalogMongoEntity("cooking_oil", "Cooking Oil", "groceries_and_food", null, "litre", 145.0, 1.0, System.currentTimeMillis()),
            ItemCatalogMongoEntity("apples", "Apples", "groceries_and_food", null, "kg", 180.0, 1.0, System.currentTimeMillis()),
            ItemCatalogMongoEntity("bananas", "Bananas", "groceries_and_food", null, "dozen", 60.0, 1.0, System.currentTimeMillis()),
            ItemCatalogMongoEntity("tea_leaves", "Tea Powder", "groceries_and_food", null, "packet", 120.0, 1.0, System.currentTimeMillis()),
            ItemCatalogMongoEntity("detergent", "Washing Powder", "home_and_utilities", null, "kg", 160.0, 1.0, System.currentTimeMillis()),
            ItemCatalogMongoEntity("toothpaste", "Toothpaste", "healthcare", null, "piece", 95.0, 1.0, System.currentTimeMillis())
        )
        for (item in initialItems) {
            localCatalogStore[item._id] = item
        }
        publishCatalogUpdate()
    }

    private fun seedInitialBudgets() {
        if (localBudgetsStore.isEmpty()) {
            val familyBudget = BudgetMongoEntity(
                _id = "family_primary_head_budget",
                familyId = "family_primary",
                userId = null,
                userName = "Whole Family",
                scope = "FAMILY",
                baseAmount = 50000.0,
                periodType = "MONTHLY",
                updatedAt = System.currentTimeMillis()
            )
            localBudgetsStore[familyBudget._id] = familyBudget
            publishBudgetsUpdate()
        }
    }

    override val expenseDao: ExpenseMongoDao = object : ExpenseMongoDao {
        override fun getAllExpensesFlow(): Flow<List<ExpenseMongoEntity>> = _expensesFlow

        override fun getExpensesByCategoryFlow(categoryId: String): Flow<List<ExpenseMongoEntity>> {
            return _expensesFlow.map { list ->
                list.filter { it.categoryId.equals(categoryId, ignoreCase = true) }
            }
        }

        override suspend fun getExpenseById(id: String): ExpenseMongoEntity? = withContext(dispatchers.io) {
            localStore[id]
        }

        override suspend fun upsertExpense(entity: ExpenseMongoEntity) {
            withContext(dispatchers.io) {
                // Also update catalog item automatically if this is a named commodity
                saveItemToCatalogIfApplicable(entity)

                try {
                    val col = ensureExpensesCollection()
                    val doc = expenseEntityToDocument(entity)
                    col.replaceOne(
                        Filters.eq("_id", entity._id),
                        doc,
                        ReplaceOptions().upsert(true)
                    )
                    val synced = entity.copy(syncState = "SYNCED")
                    localStore[entity._id] = synced
                    publishUpdate()
                    android.util.Log.i("MongoDbManager", "Successfully written expense '${entity.title}' (${entity.amount}) to Atlas!")
                } catch (t: Throwable) {
                    android.util.Log.e("MongoDbManager", "Failed writing expense '${entity.title}' to Atlas: ${t.message}", t)
                    val pending = entity.copy(syncState = "PENDING_SYNC")
                    localStore[entity._id] = pending
                    publishUpdate()
                }
            }
        }

        override suspend fun upsertExpenses(entities: List<ExpenseMongoEntity>) {
            withContext(dispatchers.io) {
                try {
                    val col = ensureExpensesCollection()
                    entities.forEach { entity ->
                        saveItemToCatalogIfApplicable(entity)
                        val doc = expenseEntityToDocument(entity)
                        col.replaceOne(
                            Filters.eq("_id", entity._id),
                            doc,
                            ReplaceOptions().upsert(true)
                        )
                        localStore[entity._id] = entity.copy(syncState = "SYNCED")
                    }
                    publishUpdate()
                } catch (t: Throwable) {
                    entities.forEach { entity ->
                        localStore[entity._id] = entity.copy(syncState = "PENDING_SYNC")
                    }
                    publishUpdate()
                }
            }
        }

        override suspend fun deleteExpenseById(id: String) {
            withContext(dispatchers.io) {
                val col = ensureExpensesCollection()
                col.deleteOne(Filters.eq("_id", id))
                localStore.remove(id)
                publishUpdate()
            }
        }

        override suspend fun getPendingSyncExpenses(): List<ExpenseMongoEntity> {
            return withContext(dispatchers.io) {
                localStore.values.filter { it.syncState != "SYNCED" }
            }
        }

        override suspend fun markAsSynced(id: String) {
            withContext(dispatchers.io) {
                localStore[id]?.let { existing ->
                    localStore[id] = existing.copy(syncState = "SYNCED")
                    publishUpdate()
                }
            }
        }

        override suspend fun clearAll() {
            withContext(dispatchers.io) {
                localStore.clear()
                publishUpdate()
            }
        }
    }

    override val userDao: UserMongoDao = object : UserMongoDao {
        override fun getAllUsersFlow(): Flow<List<UserMongoEntity>> = _usersFlow.asStateFlow()

        override fun getFamilyMembersFlow(familyId: String): Flow<List<UserMongoEntity>> {
            return _usersFlow.map { list ->
                list.filter { it.familyId == familyId }
            }
        }

        override suspend fun getUserById(id: String): UserMongoEntity? = withContext(dispatchers.io) {
            localUsersStore[id] ?: run {
                val col = ensureUsersCollection()
                val doc = col.find(Filters.eq("_id", id)).firstOrNull()
                doc?.let { d ->
                    val entity = docToUserEntity(d)
                    localUsersStore[id] = entity
                    entity
                }
            }
        }

        override suspend fun getUserByEmail(email: String): UserMongoEntity? = withContext(dispatchers.io) {
            val clean = email.trim().lowercase()
            localUsersStore.values.firstOrNull { it.email.equals(clean, ignoreCase = true) } ?: run {
                val col = ensureUsersCollection()
                val doc = col.find(Filters.eq("email", clean)).firstOrNull()
                doc?.let { d ->
                    val entity = docToUserEntity(d)
                    localUsersStore[entity._id] = entity
                    entity
                }
            }
        }

        override suspend fun upsertUser(entity: UserMongoEntity) {
            withContext(dispatchers.io) {
                val col = ensureUsersCollection()
                val doc = userEntityToDocument(entity)
                col.replaceOne(
                    Filters.eq("_id", entity._id),
                    doc,
                    ReplaceOptions().upsert(true)
                )
                localUsersStore[entity._id] = entity
                publishUsersUpdate()
                android.util.Log.i("MongoDbManager", "Successfully written user ${entity.email} to Atlas users collection!")
            }
        }

        override suspend fun upsertUsers(entities: List<UserMongoEntity>) {
            withContext(dispatchers.io) {
                entities.forEach { entity ->
                    localUsersStore[entity._id] = entity
                    try {
                        val col = ensureUsersCollection()
                        col.replaceOne(
                            Filters.eq("_id", entity._id),
                            userEntityToDocument(entity),
                            ReplaceOptions().upsert(true)
                        )
                    } catch (_: Throwable) {}
                }
                publishUsersUpdate()
            }
        }

        override suspend fun deleteUserById(id: String) {
            withContext(dispatchers.io) {
                localUsersStore.remove(id)
                publishUsersUpdate()
                try {
                    val col = ensureUsersCollection()
                    col.deleteOne(Filters.eq("_id", id))
                } catch (_: Throwable) {}
            }
        }

        override suspend fun clearAll() {
            withContext(dispatchers.io) {
                localUsersStore.clear()
                publishUsersUpdate()
            }
        }
    }

    override val budgetDao: BudgetMongoDao = object : BudgetMongoDao {
        override fun getAllBudgetsFlow(): Flow<List<BudgetMongoEntity>> = _budgetsFlow.asStateFlow()

        override fun getFamilyBudgetFlow(familyId: String): Flow<BudgetMongoEntity?> {
            return _budgetsFlow.map { list ->
                list.firstOrNull { it.familyId == familyId && it.scope.equals("FAMILY", ignoreCase = true) }
            }
        }

        override fun getMemberBudgetFlow(familyId: String, userId: String): Flow<BudgetMongoEntity?> {
            return _budgetsFlow.map { list ->
                list.firstOrNull { it.familyId == familyId && it.userId == userId && it.scope.equals("INDIVIDUAL", ignoreCase = true) }
            }
        }

        override suspend fun getBudgetById(id: String): BudgetMongoEntity? = withContext(dispatchers.io) {
            localBudgetsStore[id]
        }

        override suspend fun upsertBudget(entity: BudgetMongoEntity) {
            withContext(dispatchers.io) {
                localBudgetsStore[entity._id] = entity
                publishBudgetsUpdate()
                try {
                    val col = ensureBudgetsCollection()
                    col.replaceOne(
                        Filters.eq("_id", entity._id),
                        budgetEntityToDocument(entity),
                        ReplaceOptions().upsert(true)
                    )
                    android.util.Log.i("MongoDbManager", "Successfully saved budget ${entity._id} (${entity.baseAmount}) to Atlas!")
                } catch (t: Throwable) {
                    android.util.Log.e("MongoDbManager", "Failed writing budget to Atlas: ${t.message}", t)
                }
            }
        }

        override suspend fun deleteBudgetById(id: String) {
            withContext(dispatchers.io) {
                localBudgetsStore.remove(id)
                publishBudgetsUpdate()
                try {
                    val col = ensureBudgetsCollection()
                    col.deleteOne(Filters.eq("_id", id))
                } catch (_: Throwable) {}
            }
        }
    }

    override val itemCatalogDao: ItemCatalogMongoDao = object : ItemCatalogMongoDao {
        override fun getAllCatalogItemsFlow(): Flow<List<ItemCatalogMongoEntity>> = _catalogFlow.asStateFlow()

        override suspend fun searchCatalog(query: String): List<ItemCatalogMongoEntity> = withContext(dispatchers.io) {
            val q = query.trim().lowercase()
            if (q.isBlank()) return@withContext localCatalogStore.values.sortedByDescending { it.updatedAt }
            localCatalogStore.values.filter { it.name.lowercase().contains(q) }
        }

        override suspend fun getCatalogItemById(id: String): ItemCatalogMongoEntity? = withContext(dispatchers.io) {
            localCatalogStore[id]
        }

        override suspend fun upsertCatalogItem(entity: ItemCatalogMongoEntity) {
            withContext(dispatchers.io) {
                localCatalogStore[entity._id] = entity
                publishCatalogUpdate()
                try {
                    val col = ensureCatalogCollection()
                    col.replaceOne(
                        Filters.eq("_id", entity._id),
                        catalogEntityToDocument(entity),
                        ReplaceOptions().upsert(true)
                    )
                } catch (t: Throwable) {
                    android.util.Log.e("MongoDbManager", "Failed writing catalog item to Atlas: ${t.message}", t)
                }
            }
        }

        override suspend fun deleteCatalogItem(id: String) {
            withContext(dispatchers.io) {
                localCatalogStore.remove(id)
                publishCatalogUpdate()
                try {
                    val col = ensureCatalogCollection()
                    col.deleteOne(Filters.eq("_id", id))
                } catch (_: Throwable) {}
            }
        }
    }

    private fun saveItemToCatalogIfApplicable(expense: ExpenseMongoEntity) {
        val cleanName = expense.title.trim()
        if (cleanName.isBlank()) return
        val slug = cleanName.lowercase().replace(" ", "_")
        val existing = localCatalogStore[slug]
        val updated = ItemCatalogMongoEntity(
            _id = slug,
            name = cleanName,
            categoryId = expense.categoryId,
            imageUrl = expense.receiptUrl ?: existing?.imageUrl,
            defaultUnit = expense.unit.ifBlank { existing?.defaultUnit ?: "item" },
            lastPrice = expense.amount,
            lastQuantity = expense.quantity,
            updatedAt = System.currentTimeMillis()
        )
        localCatalogStore[slug] = updated
        publishCatalogUpdate()
        try {
            catalogCollection?.replaceOne(
                Filters.eq("_id", slug),
                catalogEntityToDocument(updated),
                ReplaceOptions().upsert(true)
            )
        } catch (_: Throwable) {}
    }

    private fun publishUpdate() {
        _expensesFlow.value = localStore.values.sortedByDescending { it.timestamp }
    }

    private fun publishUsersUpdate() {
        _usersFlow.value = localUsersStore.values.toList()
    }

    private fun publishBudgetsUpdate() {
        _budgetsFlow.value = localBudgetsStore.values.toList()
    }

    private fun publishCatalogUpdate() {
        _catalogFlow.value = localCatalogStore.values.sortedBy { it.name }
    }

    override suspend fun initialize(appId: String, apiKey: String?) {
        val uri = firebaseConfigManager.dynamicConfig.value.dbConnectionUri
        if (uri.isNotBlank()) {
            connectWithUri(uri)
        }
    }

    override suspend fun connectWithUri(uri: String): Result<Unit> {
        return withContext(dispatchers.io) {
            try {
                _connectionState.value = MongoConnectionState.CONNECTING

                // Normalize srv:// into direct replica set endpoints to bypass Android JNDI limitations
                val normalizedUri = normalizeAtlasUri(uri)
                val connectionString = ConnectionString(normalizedUri)

                val settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .applyToClusterSettings {
                        it.serverSelectionTimeout(8, TimeUnit.SECONDS)
                    }
                    .applyToSocketSettings {
                        it.connectTimeout(8, TimeUnit.SECONDS)
                        it.readTimeout(8, TimeUnit.SECONDS)
                    }
                    .applyToSslSettings {
                        it.enabled(true)
                        it.context(SSLContext.getDefault())
                    }
                    .build()

                try {
                    mongoClient?.close()
                } catch (_: Throwable) {}

                val client = MongoClients.create(settings)
                mongoClient = client

                val dbName = connectionString.database ?: "ExpensesManagement"
                val database = client.getDatabase(dbName)
                mongoDatabase = database

                // Actively verify connection to MongoDB Atlas
                val pingDoc = database.runCommand(Document("ping", 1))
                android.util.Log.i("MongoDbManager", "Successfully connected and pinged MongoDB Atlas: ${pingDoc.toJson()}")

                // Ensure all 4 collections explicitly exist in MongoDB Atlas
                try {
                    val existingCols = database.listCollectionNames().toList()
                    if (!existingCols.any { it.equals("users", ignoreCase = true) }) {
                        database.createCollection("users")
                    }
                    if (!existingCols.any { it.equals("Expense", ignoreCase = true) || it.equals("expenses", ignoreCase = true) }) {
                        database.createCollection("Expense")
                    }
                    if (!existingCols.any { it.equals("budgets", ignoreCase = true) }) {
                        database.createCollection("budgets")
                    }
                    if (!existingCols.any { it.equals("item_catalog", ignoreCase = true) }) {
                        database.createCollection("item_catalog")
                    }
                } catch (_: Throwable) {}

                val allCols = database.listCollectionNames().toList()
                val expenseColName = if (allCols.contains("Expense")) "Expense" else if (allCols.contains("expenses")) "expenses" else "Expense"
                expensesCollection = database.getCollection(expenseColName)
                usersCollection = database.getCollection("users")
                budgetsCollection = database.getCollection("budgets")
                catalogCollection = database.getCollection("item_catalog")

                _connectionState.value = MongoConnectionState.CONNECTED

                // Trigger initial pull and push synchronization
                triggerSync()

                Result.success(Unit)
            } catch (t: Throwable) {
                android.util.Log.e("MongoDbManager", "connectWithUri error: ${t.message}", t)
                _connectionState.value = MongoConnectionState.ERROR
                Result.failure(t)
            }
        }
    }

    private fun normalizeAtlasUri(rawUri: String): String {
        if (!rawUri.startsWith("mongodb+srv://")) return rawUri

        return try {
            val withoutScheme = rawUri.removePrefix("mongodb+srv://")
            val atIndex = withoutScheme.indexOf('@')
            if (atIndex == -1) return rawUri

            val userInfo = withoutScheme.substring(0, atIndex)
            val rest = withoutScheme.substring(atIndex + 1)

            val slashIndex = rest.indexOf('/')
            val host = if (slashIndex != -1) rest.substring(0, slashIndex) else rest.substringBefore('?')
            val dbAndParams = if (slashIndex != -1) rest.substring(slashIndex + 1) else "ExpensesManagement"
            val dbName = dbAndParams.substringBefore('?').ifEmpty { "ExpensesManagement" }

            val hosts = mutableListOf<String>()

            // Resolve SRV records using Google DNS-over-HTTPS
            try {
                val srvUrl = "https://dns.google/resolve?name=_mongodb._tcp.$host&type=SRV"
                val request = Request.Builder().url(srvUrl).build()
                val response = okHttpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""

                val json = JSONObject(body)
                if (json.has("Answer")) {
                    val answers = json.getJSONArray("Answer")
                    for (i in 0 until answers.length()) {
                        val ans = answers.getJSONObject(i)
                        val data = ans.optString("data", "")
                        val parts = data.split(" ")
                        if (parts.size >= 4) {
                            val port = parts[2]
                            val target = parts[3].trimEnd('.')
                            hosts.add("$target:$port")
                        }
                    }
                }
            } catch (_: Throwable) {}

            var txtOptions = "authSource=admin&replicaSet=atlas-12mqms-shard-0"
            try {
                val txtUrl = "https://dns.google/resolve?name=$host&type=TXT"
                val request = Request.Builder().url(txtUrl).build()
                val response = okHttpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""

                val json = JSONObject(body)
                if (json.has("Answer")) {
                    val answers = json.getJSONArray("Answer")
                    for (i in 0 until answers.length()) {
                        val ans = answers.getJSONObject(i)
                        val data = ans.optString("data", "").trim('"')
                        if (data.contains("replicaSet") || data.contains("authSource")) {
                            txtOptions = data
                        }
                    }
                }
            } catch (_: Throwable) {}

            if (hosts.isNotEmpty()) {
                val hostList = hosts.joinToString(",")
                "mongodb://$userInfo@$hostList/$dbName?ssl=true&$txtOptions"
            } else {
                val fallbackHosts = "ac-cbt44uz-shard-00-00.fithesf.mongodb.net:27017,ac-cbt44uz-shard-00-01.fithesf.mongodb.net:27017,ac-cbt44uz-shard-00-02.fithesf.mongodb.net:27017"
                "mongodb://$userInfo@$fallbackHosts/$dbName?ssl=true&authSource=admin&replicaSet=atlas-12mqms-shard-0"
            }
        } catch (t: Throwable) {
            val fallbackHosts = "ac-cbt44uz-shard-00-00.fithesf.mongodb.net:27017,ac-cbt44uz-shard-00-01.fithesf.mongodb.net:27017,ac-cbt44uz-shard-00-02.fithesf.mongodb.net:27017"
            val userInfo = rawUri.removePrefix("mongodb+srv://").substringBefore('@')
            "mongodb://$userInfo@$fallbackHosts/ExpensesManagement?ssl=true&authSource=admin&replicaSet=atlas-12mqms-shard-0"
        }
    }

    override suspend fun triggerSync(): Result<Unit> {
        return withContext(dispatchers.io) {
            _connectionState.value = MongoConnectionState.SYNCING
            try {
                // 1. Sync Expenses
                val collection = expensesCollection
                if (collection != null) {
                    try {
                        collection.deleteMany(
                            Filters.`in`("createdByUserId", "head_user_001", "member_user_002", "member_user_003")
                        )
                    } catch (_: Throwable) {}

                    // Push pending local expenses
                    val pending = expenseDao.getPendingSyncExpenses()
                    for (entity in pending) {
                        val doc = expenseEntityToDocument(entity)
                        collection.replaceOne(
                            Filters.eq("_id", entity._id),
                            doc,
                            ReplaceOptions().upsert(true)
                        )
                        expenseDao.markAsSynced(entity._id)
                    }

                    // Pull remote expenses
                    val remoteCursor = collection.find().limit(300).iterator()
                    while (remoteCursor.hasNext()) {
                        val doc = remoteCursor.next()
                        val id = doc.getString("_id") ?: doc.getObjectId("_id")?.toHexString() ?: continue
                        val creatorId = doc.getString("createdByUserId") ?: ""
                        if (creatorId in listOf("head_user_001", "member_user_002", "member_user_003")) {
                            continue
                        }
                        val remoteEntity = docToExpenseEntity(doc)
                        localStore[id] = remoteEntity
                    }
                    publishUpdate()
                }

                // 2. Sync Users
                val usersCol = usersCollection
                if (usersCol != null) {
                    try {
                        usersCol.deleteMany(
                            Filters.`in`("_id", "head_user_001", "member_user_002", "member_user_003")
                        )
                        localUsersStore.remove("head_user_001")
                        localUsersStore.remove("member_user_002")
                        localUsersStore.remove("member_user_003")
                    } catch (_: Throwable) {}

                    for (entity in localUsersStore.values) {
                        if (entity._id in listOf("head_user_001", "member_user_002", "member_user_003")) continue
                        val userDoc = userEntityToDocument(entity)
                        usersCol.replaceOne(
                            Filters.eq("_id", entity._id),
                            userDoc,
                            ReplaceOptions().upsert(true)
                        )
                    }

                    val userCursor = usersCol.find().limit(50).iterator()
                    while (userCursor.hasNext()) {
                        val doc = userCursor.next()
                        val id = doc.getString("_id") ?: continue
                        if (id in listOf("head_user_001", "member_user_002", "member_user_003")) continue
                        localUsersStore[id] = docToUserEntity(doc)
                    }
                    publishUsersUpdate()
                }

                // 3. Sync Budgets
                val budgetsCol = budgetsCollection
                if (budgetsCol != null) {
                    for (entity in localBudgetsStore.values) {
                        budgetsCol.replaceOne(
                            Filters.eq("_id", entity._id),
                            budgetEntityToDocument(entity),
                            ReplaceOptions().upsert(true)
                        )
                    }
                    val budgetCursor = budgetsCol.find().limit(50).iterator()
                    while (budgetCursor.hasNext()) {
                        val doc = budgetCursor.next()
                        val id = doc.getString("_id") ?: continue
                        localBudgetsStore[id] = docToBudgetEntity(doc)
                    }
                    publishBudgetsUpdate()
                }

                // 4. Sync Item Catalog
                val catCol = catalogCollection
                if (catCol != null) {
                    for (entity in localCatalogStore.values) {
                        catCol.replaceOne(
                            Filters.eq("_id", entity._id),
                            catalogEntityToDocument(entity),
                            ReplaceOptions().upsert(true)
                        )
                    }
                    val catCursor = catCol.find().limit(100).iterator()
                    while (catCursor.hasNext()) {
                        val doc = catCursor.next()
                        val id = doc.getString("_id") ?: continue
                        localCatalogStore[id] = docToCatalogEntity(doc)
                    }
                    publishCatalogUpdate()
                }

                _connectionState.value = MongoConnectionState.CONNECTED
                Result.success(Unit)
            } catch (t: Throwable) {
                android.util.Log.e("MongoDbManager", "Connection to MongoDB Atlas failed: ${t.message}", t)
                _connectionState.value = MongoConnectionState.ERROR
                Result.failure(t)
            }
        }
    }

    private suspend fun ensureUsersCollection(): MongoCollection<Document> {
        var col = usersCollection
        if (col == null) {
            val uri = firebaseConfigManager.dynamicConfig.value.dbConnectionUri
            if (uri.isNotBlank()) {
                val connRes = connectWithUri(uri)
                if (connRes.isFailure) {
                    val ex = connRes.exceptionOrNull()
                    throw (ex ?: IllegalStateException("Could not establish connection to MongoDB Atlas"))
                }
                col = usersCollection
            }
        }
        return col ?: throw IllegalStateException("MongoDB Atlas users collection is not available. Please verify network.")
    }

    private suspend fun ensureExpensesCollection(): MongoCollection<Document> {
        var col = expensesCollection
        if (col == null) {
            val uri = firebaseConfigManager.dynamicConfig.value.dbConnectionUri
            if (uri.isNotBlank()) {
                val connRes = connectWithUri(uri)
                if (connRes.isFailure) {
                    val ex = connRes.exceptionOrNull()
                    throw (ex ?: IllegalStateException("Could not establish connection to MongoDB Atlas"))
                }
                col = expensesCollection
            }
        }
        return col ?: throw IllegalStateException("MongoDB Atlas expenses collection is not available. Please verify network.")
    }

    private suspend fun ensureBudgetsCollection(): MongoCollection<Document> {
        var col = budgetsCollection
        if (col == null) {
            val uri = firebaseConfigManager.dynamicConfig.value.dbConnectionUri
            if (uri.isNotBlank()) {
                connectWithUri(uri)
                col = budgetsCollection
            }
        }
        return col ?: throw IllegalStateException("MongoDB Atlas budgets collection is not available.")
    }

    private suspend fun ensureCatalogCollection(): MongoCollection<Document> {
        var col = catalogCollection
        if (col == null) {
            val uri = firebaseConfigManager.dynamicConfig.value.dbConnectionUri
            if (uri.isNotBlank()) {
                connectWithUri(uri)
                col = catalogCollection
            }
        }
        return col ?: throw IllegalStateException("MongoDB Atlas catalog collection is not available.")
    }

    private fun docToUserEntity(doc: Document): UserMongoEntity {
        val id = doc.getString("_id") ?: doc.getObjectId("_id")?.toHexString().orEmpty()
        return UserMongoEntity(
            _id = id,
            name = doc.getString("name").orEmpty(),
            email = doc.getString("email").orEmpty(),
            passwordHash = doc.getString("passwordHash").orEmpty(),
            role = doc.getString("role") ?: "HEAD_OF_HOUSEHOLD",
            relationship = doc.getString("relationship") ?: "HEAD",
            familyId = doc.getString("familyId") ?: "family_primary",
            authProvider = doc.getString("authProvider") ?: "PASSWORD",
            avatarUrl = doc.getString("avatarUrl"),
            createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun userEntityToDocument(entity: UserMongoEntity): Document {
        return Document("_id", entity._id)
            .append("name", entity.name)
            .append("email", entity.email)
            .append("passwordHash", entity.passwordHash)
            .append("role", entity.role)
            .append("relationship", entity.relationship)
            .append("familyId", entity.familyId)
            .append("authProvider", entity.authProvider)
            .append("avatarUrl", entity.avatarUrl)
            .append("createdAt", entity.createdAt)
            .append("updatedAt", entity.updatedAt)
    }

    private fun docToExpenseEntity(doc: Document): ExpenseMongoEntity {
        val id = doc.getString("_id") ?: doc.getObjectId("_id")?.toHexString().orEmpty()
        return ExpenseMongoEntity(
            _id = id,
            ownerId = doc.getString("ownerId") ?: "user_default",
            title = doc.getString("title") ?: "",
            amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0,
            categoryId = doc.getString("categoryId") ?: "misc",
            currencyCode = doc.getString("currencyCode") ?: "INR",
            timestamp = (doc.get("timestamp") as? Number)?.toLong() ?: System.currentTimeMillis(),
            notes = doc.getString("notes"),
            receiptUrl = doc.getString("receiptUrl"),
            isRecurring = doc.getBoolean("isRecurring") ?: false,
            isAiPredicted = doc.getBoolean("isAiPredicted") ?: false,
            syncState = "SYNCED",
            updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
            createdByUserId = doc.getString("createdByUserId") ?: "user_default",
            createdByUserName = doc.getString("createdByUserName") ?: "Head of Household",
            createdByRelationship = doc.getString("createdByRelationship") ?: "Head",
            familyId = doc.getString("familyId") ?: "family_primary",
            quantity = (doc.get("quantity") as? Number)?.toDouble() ?: 1.0,
            unit = doc.getString("unit") ?: "item",
            scope = doc.getString("scope") ?: "FAMILY",
            stockStatus = doc.getString("stockStatus") ?: "IN_STOCK",
            stockEndedAt = (doc.get("stockEndedAt") as? Number)?.toLong(),
            lastRestockedAt = (doc.get("lastRestockedAt") as? Number)?.toLong(),
            restockReason = doc.getString("restockReason"),
            quantityRemainingBeforeRestock = (doc.get("quantityRemainingBeforeRestock") as? Number)?.toDouble()
        )
    }

    private fun expenseEntityToDocument(entity: ExpenseMongoEntity): Document {
        val doc = Document("_id", entity._id)
            .append("ownerId", entity.ownerId)
            .append("title", entity.title)
            .append("amount", entity.amount)
            .append("categoryId", entity.categoryId)
            .append("currencyCode", entity.currencyCode)
            .append("timestamp", entity.timestamp)
            .append("notes", entity.notes)
            .append("receiptUrl", entity.receiptUrl)
            .append("isRecurring", entity.isRecurring)
            .append("isAiPredicted", entity.isAiPredicted)
            .append("updatedAt", entity.updatedAt)
            .append("createdByUserId", entity.createdByUserId)
            .append("createdByUserName", entity.createdByUserName)
            .append("createdByRelationship", entity.createdByRelationship)
            .append("familyId", entity.familyId)
            .append("quantity", entity.quantity)
            .append("unit", entity.unit)
            .append("scope", entity.scope)
            .append("stockStatus", entity.stockStatus)

        entity.stockEndedAt?.let { doc.append("stockEndedAt", it) }
        entity.lastRestockedAt?.let { doc.append("lastRestockedAt", it) }
        entity.restockReason?.let { doc.append("restockReason", it) }
        entity.quantityRemainingBeforeRestock?.let { doc.append("quantityRemainingBeforeRestock", it) }
        return doc
    }

    private fun docToBudgetEntity(doc: Document): BudgetMongoEntity {
        val id = doc.getString("_id") ?: UUID.randomUUID().toString()
        return BudgetMongoEntity(
            _id = id,
            familyId = doc.getString("familyId") ?: "family_primary",
            userId = doc.getString("userId"),
            userName = doc.getString("userName"),
            scope = doc.getString("scope") ?: "FAMILY",
            baseAmount = (doc.get("baseAmount") as? Number)?.toDouble() ?: 50000.0,
            periodType = doc.getString("periodType") ?: "MONTHLY",
            updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun budgetEntityToDocument(entity: BudgetMongoEntity): Document {
        val doc = Document("_id", entity._id)
            .append("familyId", entity.familyId)
            .append("scope", entity.scope)
            .append("baseAmount", entity.baseAmount)
            .append("periodType", entity.periodType)
            .append("updatedAt", entity.updatedAt)
        entity.userId?.let { doc.append("userId", it) }
        entity.userName?.let { doc.append("userName", it) }
        return doc
    }

    private fun docToCatalogEntity(doc: Document): ItemCatalogMongoEntity {
        val id = doc.getString("_id") ?: UUID.randomUUID().toString()
        return ItemCatalogMongoEntity(
            _id = id,
            name = doc.getString("name") ?: "",
            categoryId = doc.getString("categoryId") ?: "groceries_and_food",
            imageUrl = doc.getString("imageUrl"),
            defaultUnit = doc.getString("defaultUnit") ?: "item",
            lastPrice = (doc.get("lastPrice") as? Number)?.toDouble() ?: 0.0,
            lastQuantity = (doc.get("lastQuantity") as? Number)?.toDouble() ?: 1.0,
            updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun catalogEntityToDocument(entity: ItemCatalogMongoEntity): Document {
        val doc = Document("_id", entity._id)
            .append("name", entity.name)
            .append("categoryId", entity.categoryId)
            .append("defaultUnit", entity.defaultUnit)
            .append("lastPrice", entity.lastPrice)
            .append("lastQuantity", entity.lastQuantity)
            .append("updatedAt", entity.updatedAt)
        entity.imageUrl?.let { doc.append("imageUrl", it) }
        return doc
    }

    override suspend fun disconnect() {
        withContext(dispatchers.io) {
            try {
                mongoClient?.close()
            } catch (_: Throwable) {}
            mongoClient = null
            expensesCollection = null
            usersCollection = null
            budgetsCollection = null
            catalogCollection = null
            mongoDatabase = null
            _connectionState.value = MongoConnectionState.DISCONNECTED
        }
    }
}
