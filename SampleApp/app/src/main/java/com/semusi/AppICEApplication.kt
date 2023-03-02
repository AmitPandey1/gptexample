package com.semusi

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.bugfender.android.BuildConfig
import com.bugfender.sdk.Bugfender
import com.semusi.AppICEApplication.AppICE.startAppICESdk
import com.semusi.PlacesModule2Test.R
import semusi.activitysdk.*
import semusi.context.counthandler.DataSyncReceiver

class AppICEApplication : AppICEProcessLIfeCycle() {
    override fun onCreate() {
        super.onCreate()

        Bugfender.init(this, getString(R.string.bugfender_key), BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();
        Bugfender.enableUIEventLogging(this);
        Bugfender.enableLogcatLogging();

        AppICE.mInstance = this
        startAppICESdk(applicationContext)
    }

    object AppICE {
        lateinit var mInstance: Application
        fun startAppICESdk(context: Context) {
            ContextApplication.initSdk(context, mInstance)

            // creating config for appICE sdk
            val config = SdkConfig()
            config.setAnalyticsTrackingAllowedState(true)

            // Init sdk with your config
            Api.startContext(context, config)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    override fun appInForeground() {
        DataSyncReceiver.doCombineGetRequest(this.applicationContext)
        ContextSdk.checkIfDeviceRegisterToFCM(applicationContext)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    override fun appInBackground() {
        ContextSdk.checkIfDeviceRegisterToFCM(applicationContext)
    }

}