package et.hotelbooking.app

import android.app.Application
import android.content.Context
import androidx.room.Room
import et.hotelbooking.app.data.local.AppDatabase
import et.hotelbooking.app.data.repo.*
import et.hotelbooking.app.data.seed.Seed
import et.hotelbooking.app.util.SettingsStore
import et.hotelbooking.app.util.UserListsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Manual dependency container. When the Django REST backend is ready, create Remote* implementations
 * of the repository interfaces (see data/remote/ApiContract.kt) and select them here when
 * BuildConfig.USE_REMOTE_API is true. The UI depends only on the interfaces.
 */
class AppContainer(context: Context, dbName: String? = "ethio_hotels.db") {
    val db: AppDatabase = (if (dbName == null) Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        else Room.databaseBuilder(context, AppDatabase::class.java, dbName)).fallbackToDestructiveMigration(true).build()
    val settings = SettingsStore(context)
    val lists = UserListsStore(context)
    private val notifier = Notifier(db)
    val auth: AuthRepository = LocalAuthRepository(db, settings, notifier)
    val hotels: HotelRepository = LocalHotelRepository(db, notifier)
    val bookings: BookingRepository = LocalBookingRepository(db, hotels, notifier)
    val pms: PmsRepository = LocalPmsRepository(db, hotels, notifier)
    val admin: AdminRepository = LocalAdminRepository(db, notifier)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready

    init {
        scope.launch {
            runCatching { Seed.seedIfEmpty(db) }.onFailure { it.printStackTrace() }
            _ready.value = true
        }
    }
}

class EthioHotelsApp : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

val Context.container: AppContainer get() = (applicationContext as EthioHotelsApp).container
