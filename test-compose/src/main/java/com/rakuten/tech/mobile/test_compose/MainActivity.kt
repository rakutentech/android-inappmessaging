package com.rakuten.tech.mobile.test_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.test_compose.ui.Screen1
import com.rakuten.tech.mobile.test_compose.ui.theme.TestcomposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestcomposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Screen1()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        InAppMessaging.instance().logEvent(AppStartEvent())
    }

    override fun onResume() {
        super.onResume()
        InAppMessaging.instance().registerMessageDisplayActivity(this)
    }

    override fun onPause() {
        super.onPause()
        InAppMessaging.instance().unregisterMessageDisplayActivity()
    }
}
//
//@Composable
//fun Greeting(name: String) {
//    Text(text = "Hello $name!")
//    InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent().currencyCode("JPY"))
//}
//
//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    TestcomposeTheme {
//        Greeting("Android")
//    }
//}