package et.hotelbooking.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import et.hotelbooking.app.ui.nav.AppRoot
import et.hotelbooking.app.ui.vm.AppViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = container
        splash.setKeepOnScreenCondition { !container.ready.value }
        setContent {
            val vm: AppViewModel = viewModel { AppViewModel(container) }
            AppRoot(vm)
        }
    }
}
