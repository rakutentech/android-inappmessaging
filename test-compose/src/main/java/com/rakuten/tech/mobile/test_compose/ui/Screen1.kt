package com.rakuten.tech.mobile.test_compose.ui

import MyButton
import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.CustomEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.LoginSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent
import com.rakuten.tech.mobile.test_compose.MainApplication
import com.rakuten.tech.mobile.test_compose.SecondActivity

@Composable
fun Screen1() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val showDialog = remember { mutableStateOf(false) }
        val context = LocalContext.current
        MyButton(
            text = "Close Message",
            onClick = { InAppMessaging.instance().closeMessage() })
        MyButton(
            text = "Launch Second Activity",
            onClick = { context.startActivity(Intent(context, SecondActivity::class.java)) })
        MyButton(
            text = "Change User",
            onClick = { showDialog.value = true} )
        MyButton(
            text = "Launch Successful",
            onClick = { InAppMessaging.instance().logEvent(AppStartEvent()) })
        MyButton(
            text = "Login Successful",
            onClick = { InAppMessaging.instance().logEvent(LoginSuccessfulEvent()) })
        MyButton(
            text = "Purchase Successful",
            onClick = { InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent().currencyCode("JPY")) })
        MyButton(
            text = "Purchase Successful 2x",
            onClick = {
                InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent().currencyCode("JPY"))
                InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent().currencyCode("JPY"))
            })
        MyButton(
            text = "Login Successful 2x",
            onClick = {
                InAppMessaging.instance().logEvent(LoginSuccessfulEvent())
                InAppMessaging.instance().logEvent(LoginSuccessfulEvent())
            })
        MyButton(
            text = "Login and Purchase Successful",
            onClick = {
                InAppMessaging.instance().logEvent(LoginSuccessfulEvent())
                InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent().currencyCode("JPY"))
            })
        MyButton(
            text = "Custom Event: newUser",
            onClick = { InAppMessaging.instance().logEvent(
                CustomEvent("search_event").addAttribute("KEYWORD", "BASKETBALL").addAttribute("foo", 2)) })

        if (showDialog.value) {
            UserInfo(showDialog = showDialog)
        }
    }
}

@Composable
private fun UserInfo(showDialog: MutableState<Boolean>) {
    if (showDialog.value) {
        val app = (LocalContext.current as Activity).application as MainApplication
        var userId by remember { mutableStateOf(app.provider.userId) }
        var accessToken by remember { mutableStateOf(app.provider.accessToken) }
        var idTracking by remember { mutableStateOf(app.provider.idTracking) }

        AlertDialog(
            title = {
                Text(text = "User Information")
            },
            text = {
                Column() {
                    OutlinedTextField(
                        value = userId,
                        onValueChange = { userId = it },
                        label = { Text("User ID") }
                    )
                    OutlinedTextField(
                        value = accessToken,
                        onValueChange = { accessToken = it },
                        label = { Text("Access Token") }
                    )
                    OutlinedTextField(
                        value = idTracking,
                        onValueChange = { idTracking = it },
                        label = { Text("ID Tracking") }
                    )
                }
            },
            onDismissRequest = {showDialog.value = false},
            confirmButton = {
                TextButton(onClick = {
                    if (app.provider.userId != userId) {
                        InAppMessaging.instance().closeMessage()
                    }
                    app.provider.userId = userId
                    app.provider.accessToken = accessToken
                    app.provider.idTracking = idTracking
                    showDialog.value = false
                }) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Screen1Preview() {
    //TestcomposeTheme {
        Screen1()
    //}
}