package com.semusi.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.semusi.PlacesModule2Test.R
import com.semusi.AppICEApplication
import org.json.JSONArray
import org.json.JSONObject
import semusi.activitysdk.Api
import semusi.activitysdk.ContextSdk
import semusi.context.utility.AppICEConstants
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainactivity)
        val buttonView = findViewById<View>(R.id.button0)
        ContextSdk.markViewAsSensitive(buttonView)
        setupData()
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
                content += """<b> BaseUrl : -> </b>${ContextSdk.getBaseUrl(this@MainActivity)}<-"""+"<br>".trimIndent()
                content += """<b> Place : -> </b>${userData.locationType}<- Via : ->${userData.locationProvider}<-"""+"<br>".trimIndent()
                content += """<b> UserInfo : -> </b>${ContextSdk.getUser(this@MainActivity)}<-"""+"<br>".trimIndent()
                content += """<b> Interest : -> </b>${userData.userInterestData}<-"""+"<br>".trimIndent()
                tx.text = Html.fromHtml(content )
            }
        } catch (e: Exception) {
        }
    }

    @SuppressLint("SetTextI18n", "CutPasteId")
    private fun setupButtons() {

        val btnDID = findViewById<View>(R.id.generate_did) as Button
        btnDID.setOnClickListener {
            var uuid = UUID.randomUUID();
            ContextSdk.setDeviceId(this, uuid.toString())
        }

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
            val intent = Intent(this@MainActivity, GeofenceActivity::class.java)
            startActivity(intent)
        }

        val a2btn = findViewById<Button>(R.id.a2btn)
        a2btn.setOnClickListener {
            val intent = Intent(this@MainActivity, InboxActivity::class.java)
            startActivity(intent)
        }

        val dozeBtn = findViewById<View>(R.id.dozeBtn) as Button
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            dozeBtn.visibility = View.GONE
        } else {
            dozeBtn.setOnClickListener {
                try {
                    val intent = Intent()
                    val packageName = this@MainActivity.applicationContext.packageName
                    val pm = this@MainActivity.applicationContext.getSystemService(POWER_SERVICE) as PowerManager
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    this@MainActivity.applicationContext.startActivity(intent)
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
            ContextSdk.tagEvent(val2, eventKey, this@MainActivity)
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
        playBtn.setOnClickListener { ContextSdk.openPlayServiceUpdate(this@MainActivity.applicationContext) }
        val e1 = findViewById<View>(R.id.event1) as Button
        e1.setOnClickListener {
            val arr = arrayOf(
                "38433079e-d371-4c25-a8d0-8fd2c65190b8"
            )
            ContextSdk.setUser(arr, applicationContext)
            val map = HashMap<String, Any>()
            map["state"] = true
            map["id"] = "aman"
            ContextSdk.tagEventObj(
                "Login",
                map,
                this@MainActivity.applicationContext
            )


            //==========================================
            // where condition
            //==========================================
            val where = JSONArray()
            val whereObjects1 = JSONObject();
            whereObjects1.put(AppICEConstants.OPERANDS, AppICEConstants.POST_ID)
            whereObjects1.put(AppICEConstants.OPERATOR, AppICEConstants.EQUALS)
            whereObjects1.put(AppICEConstants.VALUE, "123")


            where.put(whereObjects1)

            //============================================
            // group by
            //============================================
            val groupByData = JSONArray();
            groupByData.put(AppICEConstants.POST_ID)
            groupByData.put(AppICEConstants.AUTHOR_NAME+" DESC")
            groupByData.put(3)

//
//            AppInboxController.getInstance()
//                .where(where)
//                .groupBy(groupByData)
//                .submit(object : AppInboxCallback {
//                    override fun onAppInboxSuccess(data: List<AppICEInboxMessage>?) {
//                    }
//
//                    override fun onAppInboxFailure(message: String?) {
//                    }
//                },applicationContext)

            //==================================================
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
                this@MainActivity.applicationContext
            )
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
                this@MainActivity.applicationContext
            )
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
                    this@MainActivity.applicationContext
                )
            } catch (e: Exception) {
            }
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
                    this@MainActivity.applicationContext
                )
            } catch (e: Exception) {
            }
        }

        // Payment-failed
        val e6 = findViewById<View>(R.id.event6) as Button
        e6.setOnClickListener {
            val map = HashMap<String, Any>()
            map["state"] = false
            ContextSdk.tagEventObj("FD_Success", map, this@MainActivity.applicationContext)
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