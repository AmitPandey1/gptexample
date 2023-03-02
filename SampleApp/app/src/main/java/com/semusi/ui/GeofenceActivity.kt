package com.semusi.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.BuildConfig
import com.semusi.AppICEApplication
import com.semusi.PlacesModule2Test.R
import org.json.JSONArray
import org.json.JSONObject
import semusi.activitysdk.Api
import semusi.activitysdk.ContextSdk
import semusi.geofencing.AIGeofenceController
import semusi.geofencing.GeoCallback
import semusi.geofencing.GeofenceClickedCallback
import java.lang.Exception
import java.util.*

class GeofenceActivity : AppCompatActivity() , GeofenceClickedCallback {

    private val tag = GeofenceActivity::class.java.simpleName
    private val requestCode = 34

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainactivity)
        setupData()
        AIGeofenceController.getInstance().clickedCallback = this
    }

    override fun onStart() {
        super.onStart()
        checkRunTimePermission()
    }

    private fun checkRunTimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this.applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this.applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this.applicationContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                setUpAppICEGeofence()
            } else {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    10
                )
            }
        } else {
            setUpAppICEGeofence()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpAppICEGeofence()
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this@GeofenceActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    // If User Checked 'Don't Show Again' checkbox for runtime permission, then navigate user to Settings
                    val dialog: AlertDialog.Builder = AlertDialog.Builder(applicationContext)
                    dialog.setTitle("Permission Required")
                    dialog.setCancelable(false)
                    dialog.setMessage("You have to Allow permission to access user location")
                    dialog.setPositiveButton("setting", DialogInterface.OnClickListener { _, _ ->
                        run {
                            val i = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts(
                                    "package",
                                    applicationContext.packageName, null
                                )
                            )
                            //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivityForResult(i, 1001)
                        }
                    })
                    val alertDialog: AlertDialog = dialog.create()
                    alertDialog.show()
                }
                //code for deny
            }
        }
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
        when (requestCode) {
            1001 -> {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    setUpAppICEGeofence()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ), 10
                        )
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(
                                arrayOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                ), 10
                            )
                        }
                    }
                }

            }
        }
    }

    private fun setUpAppICEGeofence() {
        println("appICEGeofence : setUpAppICEGeofence")
        AIGeofenceController.getInstance().setGeoCallbackListener(object : GeoCallback {
            override fun onGeoEnter(data: JSONObject, context: Context) {
                println("AIGeofence test : $data")
                try {
                    val jsonObject = data.optJSONObject("data")
                    println("geoEnter : message : $jsonObject")
                    if (jsonObject != null) {
                        val message = jsonObject.optJSONObject("message")
                        if (message != null) {
                            println("geoEnter : message : $message")
                            ContextSdk.handleAppICEPush(message.toString(), null, null, context)
                        }
                    }
                } catch (e: Exception) {
                }
            }

            override fun onGeoExit(data: JSONObject, context: Context) {
                try {
                    val jsonObject = data.optJSONObject("data")
                    if (jsonObject != null) {
                        val message = jsonObject.optJSONObject("message")
                        if (message != null) {
                            println("geoExit : message : $message")
                            ContextSdk.handleAppICEPush(message.toString(), null, null, context)
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }).setUpGeoCallback(applicationContext)
    }

    //====================================
    // permission request and result
    //====================================
    private fun checkPermissions(context: Context?): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (shouldProvideRationale) {
            permissionEntry()
        } else {
            permissionEntry()
        }
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String?>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        Log.i("Geofencing", "onRequestPermissionResult")
//        if (requestCode == this.requestCode) {
//            for (i in permissions.indices) {
//                if (grantResults.isEmpty()) {
//                    Toast.makeText(applicationContext, "permission cancelled", Toast.LENGTH_LONG).show()
//                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    setUpAppICEGeofence()
//                } else {
//                    Toast.makeText(applicationContext, "permission denied", Toast.LENGTH_LONG).show()
//                    val intent = Intent()
//                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                    val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
//                    intent.data = uri
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    startActivity(intent)
//                }
//            }
//        }
//    }

    private fun permissionEntry() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this@GeofenceActivity, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                requestCode
            )
        } else {
            Log.i(tag, "Displaying permission rationale to provide additional context.")
            ActivityCompat.requestPermissions(
                this@GeofenceActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCode
            )
        }
    }

    override fun onNotificationClickedPayloadReceived(`object`: JSONObject, context: Context?) {
        println("test : someValue $`object`")
    }

    private fun setupData() {
        setupUI()
        setupButtons()
    }

    private fun setupUI() {
        try {
            val sdk = ContextSdk(applicationContext)
            val userData = sdk.currentContext
            if (userData != null) {
                val tx = findViewById<View>(R.id.textView2) as TextView
                tx.movementMethod = ScrollingMovementMethod()
                var content = ""
                content += """<b> Epoch : -> </b> ${System.currentTimeMillis() / 1000} <-"""+"<br>"
                content += """<b> GMT : -> </b> ${Date(System.currentTimeMillis()).toGMTString()} <-"""+"<br>"
                content += """<b> Local : -> </b>${Date(System.currentTimeMillis()).toLocaleString()} <-"""+"</br>"
                content += """<b> String : -> </b>${Date(System.currentTimeMillis())} <-"""+"<br>"
                content += """<b> SdkVersion : -> </b>${ContextSdk.getSdkVersion()}<-"""+"<br>".trimIndent()
                content += """<b> BaseUrl : -> </b>${ContextSdk.getBaseUrl(this@GeofenceActivity)}<-"""+"<br>".trimIndent()
                content += """<b> Place : -> </b>${userData.locationType}<- Via : ->${userData.locationProvider}<-"""+"<br>".trimIndent()
                content += """<b> UserInfo : -> </b>${ContextSdk.getUser(this@GeofenceActivity)}<-"""+"<br>".trimIndent()
                content += """<b> Interest : -> </b>${userData.userInterestData}<-"""+"<br>".trimIndent()
                tx.text = Html.fromHtml(content )
            }
        } catch (e: Exception) {
        }
    }

    @SuppressLint("SetTextI18n", "CutPasteId")
    private fun setupButtons() {

        val txt0 = findViewById<View>(R.id.editText0) as EditText
        txt0.setText(Api.getDeviceId(applicationContext))

        val btn0 = findViewById<View>(R.id.button0) as Button
        if (ContextSdk.isSemusiSensing(applicationContext)) {
            btn0.text = "STOP"
            txt0.isEnabled = false
            btn0.tag = "STOP"
        } else {
            btn0.text = "START"
            txt0.isEnabled = true
            btn0.tag = "START"
        }

        btn0.setOnClickListener { v ->
            val tag = v.tag as String
            val fld = findViewById<View>(R.id.editText0) as EditText
            val startVal = fld.text.toString().trim { it <= ' ' }
            if (startVal.isNotEmpty()) {
                if (tag.equals("START", ignoreCase = true)) {
                    Toast.makeText(applicationContext, "AppICE sdk started", Toast.LENGTH_SHORT).show()
                }
            }
            if (tag.equals("START", ignoreCase = true)) {
                AppICEApplication.AppICE.startAppICESdk(applicationContext)
                Toast.makeText(applicationContext, "Sdk started successfully.", Toast.LENGTH_SHORT).show()
            }
            setupUI()
            setupButtons()
        }

        val a1btn = findViewById<Button>(R.id.a1btn)
        a1btn.setOnClickListener {
            val intent = Intent(this@GeofenceActivity, GeofenceActivity::class.java)
            startActivity(intent)
        }

        val a2btn = findViewById<Button>(R.id.a2btn)
        a2btn.setOnClickListener {
            val intent = Intent(this@GeofenceActivity, InboxActivity::class.java)
            startActivity(intent)
        }

        val dozeBtn = findViewById<View>(R.id.dozeBtn) as Button
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            dozeBtn.visibility = View.GONE
        } else {
            dozeBtn.setOnClickListener {
                try {
                    val intent = Intent()
                    val packageName = this@GeofenceActivity.applicationContext.packageName
                    val pm = this@GeofenceActivity.applicationContext.getSystemService(POWER_SERVICE) as PowerManager
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    this@GeofenceActivity.applicationContext.startActivity(intent)
                } catch (e: Exception) {
                }
            }
        }

        val btnCrash = findViewById<Button>(R.id.button10)
        btnCrash.setOnClickListener {
            val s: String? = null
            s!!.substring(10)
        }

        val btn1 = findViewById<View>(R.id.button1) as Button
        btn1.setOnClickListener {
            val fld = findViewById<View>(R.id.editText1) as EditText
            val value = fld.text.toString().trim { it <= ' ' }
            if (value.isNotEmpty()) {
                Toast.makeText(applicationContext, "Alias set in sdk", Toast.LENGTH_LONG).show()
                ContextSdk.setAlias(value, applicationContext)
                setupUI()
                setupButtons()
            }
        }

        val btn2 = findViewById<View>(R.id.button2) as Button
        btn2.setOnClickListener {
            val fld2 = findViewById<View>(R.id.editText2) as EditText
            val val2 = fld2.text.toString().trim { it <= ' ' }
            val fld3 = findViewById<View>(R.id.editText3) as EditText
            val val3 = fld3.text.toString().trim { it <= ' ' }
            val fld4 = findViewById<View>(R.id.editText4) as EditText
            val val4 = fld4.text.toString().trim { it <= ' ' }

            // Basic setup of Custom Events
            val eventKey = HashMap<String, String>()
            eventKey[val3] = val4
            ContextSdk.tagEvent(val2, eventKey, this@GeofenceActivity)
            Toast.makeText(applicationContext, "Custom Event set in sdk", Toast.LENGTH_SHORT).show()
            setupUI()
            setupButtons()
        }

        val btn3 = findViewById<View>(R.id.button3) as Button
        btn3.setOnClickListener {
            val fld2 = findViewById<View>(R.id.editText5) as EditText
            val val2 = fld2.text.toString().trim { it <= ' ' }
            val fld3 = findViewById<View>(R.id.editText6) as EditText
            val val3 = fld3.text.toString().trim { it <= ' ' }

            // Basic setup of Custom variables
            ContextSdk.setCustomVariable(val2, val3, applicationContext)
            Toast.makeText(applicationContext, "Custom Variables set in sdk", Toast.LENGTH_SHORT).show()
            setupUI()
            setupButtons()
        }

        val notificationBtn = findViewById<View>(R.id.notificationBtn) as Button
        if (Build.VERSION.SDK_INT >= 19) {
            notificationBtn.setOnClickListener {
                try {
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                } catch (e: Exception) {
                }
            }
        } else {
            notificationBtn.visibility = View.GONE
        }

        val usageBtn = findViewById<View>(R.id.usageBtn) as Button
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            usageBtn.setOnClickListener {
                try {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    startActivity(intent)
                } catch (e: Exception) {
                }
            }
        } else {
            usageBtn.visibility = View.GONE
        }

        val accessibilityBtn = findViewById<View>(R.id.accessibilityBtn) as Button
        run {
            accessibilityBtn.setOnClickListener {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivityIfNeeded(intent, 0)
            }
        }

        val playBtn = findViewById<View>(R.id.playBtn) as Button
        playBtn.setOnClickListener { ContextSdk.openPlayServiceUpdate(this@GeofenceActivity.applicationContext) }
        val e1 = findViewById<View>(R.id.event1) as Button
        e1.setOnClickListener {
            val arr = arrayOf(
                "384e079e-d371-4c25-a8d0-8fd2c65190b8",
                "38433079e-d371-4c25-a8d0-8fd2c65190b8"
            )
            ContextSdk.setUser(arr, applicationContext)
            val map = HashMap<String, Any>()
            map["state"] = true
            map["id"] = "aman"
            ContextSdk.tagEventObj(
                "Login",
                map,
                this@GeofenceActivity.applicationContext
            )
        }

        // Product-list
        val e2 = findViewById<View>(R.id.event2) as Button
        e2.setOnClickListener {
            val arr = JSONArray()
            arr.put("fashion")
            arr.put("cosmetic")
            val map = HashMap<String, Any>()
            map["products"] = arr
            ContextSdk.tagEventObj(
                "FD_Select",
                map,
                this@GeofenceActivity.applicationContext
            )
            ContextSdk.resumeInApp( applicationContext)
        }

        // Product-view
        val e3 = findViewById<View>(R.id.event3) as Button
        e3.setOnClickListener {
            val map = HashMap<String, Any>()
            map["AccNo"] = ""
            map["FD_Amount"] = ""
            map["Balance"] = ""
            map["DepositType"] = "ShortTerm"
            map["PAN"] = "ShortTerm"
            ContextSdk.tagEventObj(
                "FD_Detail_Verify",
                map,
                this@GeofenceActivity.applicationContext
            )
            ContextSdk.suspendInApp( applicationContext)
        }

        // Add-to-cart
        val e4 = findViewById<View>(R.id.event4) as Button
        e4.setOnClickListener {
            try {
                val ob1 = JSONObject()
                ob1.put("id", 102)
                ob1.put("category", "fashion")
                ob1.put("price", "1452")
                val ob2 = JSONObject()
                ob2.put("id", 217)
                ob2.put("category", "cosmetic")
                ob2.put("price", "1233")
                val arr = JSONArray()
                arr.put(ob1)
                arr.put(ob2)
                val map = HashMap<String, Any>()
                map["cart"] = arr
                ContextSdk.tagEventObj(
                    "FD_Topup_Done",
                    map,
                    this@GeofenceActivity.applicationContext
                )
            } catch (e: Exception) {
            }
            ContextSdk.discardInApp( applicationContext)

        }

        // Proceed-success
        val e5 = findViewById<View>(R.id.event5) as Button
        e5.setOnClickListener {
            try {
                val ob1 = JSONObject()
                ob1.put("id", 102)
                ob1.put("category", "fashion")
                ob1.put("price", "1452")
                val ob2 = JSONObject()
                ob2.put("id", 217)
                ob2.put("category", "cosmetic")
                ob2.put("price", "1233")
                val arr = JSONArray()
                arr.put(ob1)
                arr.put(ob2)
                val cart = JSONObject()
                cart.put("total", 3726)
                cart.put("list", arr)
                val map = HashMap<String, Any>()
                map["state"] = true
                map["cart"] = cart
                ContextSdk.tagEventObj(
                    "FD_Cancel",
                    map,
                    this@GeofenceActivity.applicationContext
                )
            } catch (e: Exception) {
            }
        }

        // Payment-failed
        val e6 = findViewById<View>(R.id.event6) as Button
        e6.setOnClickListener {
            val map = HashMap<String, Any>()
            map["state"] = false
            ContextSdk.tagEventObj("FD_Success", map, this@GeofenceActivity.applicationContext)
        }
        val fld = findViewById<View>(R.id.editText1) as EditText
        fld.setText(ContextSdk.getAlias(applicationContext))
        val userinfo = ContextSdk.getUser(
            applicationContext
        )
        val useradd1 = findViewById<View>(R.id.user1) as EditText
        useradd1.setText(userinfo.name)
        val useradd2 = findViewById<View>(R.id.user2) as EditText
        useradd2.setText(userinfo.email)
        val useradd3 = findViewById<View>(R.id.user3) as EditText
        useradd3.setText(userinfo.phone)
        val useradd4 = findViewById<View>(R.id.user4) as EditText
        useradd4.setText(userinfo.gender)
        val useradd10 = findViewById<View>(R.id.user10) as EditText
        useradd10.setText(userinfo.age.toString() + "")
        val userPost = findViewById<View>(R.id.user11) as Button
        val setTestDevice = findViewById<View>(R.id.user12) as Button
        setTestDevice.tag = "notset"
        setTestDevice.setOnClickListener { }
        userPost.setOnClickListener {
            userinfo.name = useradd1.text.toString()
            userinfo.email = useradd2.text.toString()
            userinfo.phone = useradd3.text.toString()
            userinfo.gender = useradd4.text.toString()
            userinfo.age = useradd10.text.toString().toInt()
            ContextSdk.setUser(userinfo, applicationContext)
            Toast.makeText(applicationContext, "User Info set in sdk", Toast.LENGTH_SHORT).show()
            setupUI()
            setupButtons()
        }
    }
}