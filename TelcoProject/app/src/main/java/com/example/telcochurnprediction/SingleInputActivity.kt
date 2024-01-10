package com.example.telcochurnprediction

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject

class SingleInputActivity : AppCompatActivity() {


    private val itemsInPaymentMethodMenu = arrayOf("Electronic check","Mailed check","Bank transfer (automatic)","Credit card (automatic)")
    private val itemsInContractMenu = arrayOf("Month-to-month","One year","Two year")
    private val itemsInInternetServiceMenu = arrayOf("DSL","Fiber optic","No")

    private var onlineSecurityState: Boolean = false
    private var onlineBackupState: Boolean = false
    private var deviceProtectionState: Boolean = false
    private var techSupportState: Boolean = false
    private var streamingTvState: Boolean = false
    private var streamingMoviesState: Boolean = false

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.singleinputpage)

        val gender = findViewById<EditText>(R.id.editTextGender)
        val tenure = findViewById<EditText>(R.id.editTextTenure)
        val monthlyCharges = findViewById<EditText>(R.id.editTextMonthlyCharges)
        val totalCharges = findViewById<EditText>(R.id.editTextTotalCharges)

        val paymentMethod = findViewById<TextView>(R.id.textViewPaymentMethod)
        val contract = findViewById<TextView>(R.id.textViewContract)
        val internetService = findViewById<TextView>(R.id.textViewInternetService)

        val partner = findViewById<Switch>(R.id.switchPartner)
        val dependents = findViewById<Switch>(R.id.switchDependents)
        val phoneService = findViewById<Switch>(R.id.switchPhoneService)
        val paperlessBilling = findViewById<Switch>(R.id.switchPaperlessBilling)
        val seniorCitizen = findViewById<Switch>(R.id.switchSeniorCitizen)
        val multipleLines = findViewById<Switch>(R.id.switchMultipleLines)

        val dropDownPaymentMethod = findViewById<ImageView>(R.id.dropDownPaymentMethod)
        val dropDownContract = findViewById<ImageView>(R.id.dropDownContract)
        val dropDownInternetService = findViewById<ImageView>(R.id.dropDownInternetService)

        val internetServiceMenu = findViewById<ImageButton>(R.id.internetservicedependendsButton)
        val predictButton = findViewById<Button>(R.id.predictButton)

        val popupMenuPaymentMethod = PopupMenu(this@SingleInputActivity, dropDownPaymentMethod)
        val popupMenuContract = PopupMenu(this@SingleInputActivity, dropDownContract)
        val popupMenuInternetService = PopupMenu(this@SingleInputActivity, dropDownInternetService)

        for ((increment, items) in itemsInPaymentMethodMenu.withIndex()) {
            popupMenuPaymentMethod.menu.add(Menu.NONE,increment,increment,items)
        }

        for ((increment, items) in itemsInContractMenu.withIndex()) {
            popupMenuContract.menu.add(Menu.NONE,increment,increment,items)
        }

        for ((increment, items) in itemsInInternetServiceMenu.withIndex()) {
            popupMenuInternetService.menu.add(Menu.NONE,increment,increment,items)
        }

        popupMenuPaymentMethod.setOnMenuItemClickListener { menuItem ->
            paymentMethod.text = menuItem.title.toString()
            false
        }
        popupMenuContract.setOnMenuItemClickListener { menuItem ->
            contract.text = menuItem.title.toString()
            false
        }
        popupMenuInternetService.setOnMenuItemClickListener { menuItem ->
            internetService.text = menuItem.title.toString()
            false
        }

        dropDownPaymentMethod.setOnClickListener {
            popupMenuPaymentMethod.show()
        }
        dropDownContract.setOnClickListener {
            popupMenuContract.show()
        }
        dropDownInternetService.setOnClickListener {
            popupMenuInternetService.show()
        }

        phoneService.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                multipleLines.visibility = View.VISIBLE
            } else {
                multipleLines.visibility = View.INVISIBLE
            }
        }

        internetService.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                // Check if the text in the TextView is "certainValue"
                if (editable.toString() == "DSL" || editable.toString() == "Fiber optic") {
                    internetServiceMenu.visibility = View.VISIBLE
                } else {
                    internetServiceMenu.visibility = View.INVISIBLE
                    onlineSecurityState = false
                    onlineBackupState = false
                    deviceProtectionState = false
                    techSupportState = false
                    streamingTvState = false
                    streamingMoviesState = false
                }
            }
        })

        internetServiceMenu.setOnClickListener{
            showInternetServiceDependents()
        }

        fun handlePredictionResult(result: String, data: String){
            println("Received Results: $result")

            val dataJson = JSONObject(data)

            val inputSection = dataJson.getJSONObject("Inputs")
            val input1Array = inputSection.getJSONArray("input1")
            val customerDataObject = input1Array.getJSONObject(0)


            val json = JSONObject(result)
            val results = json.getJSONObject("Results")
            val webServiceOutput = results.getJSONArray("WebServiceOutput0").getJSONObject(0)

            val churnPrediction = webServiceOutput.getDouble("ChurnPrediction").toString()
            val probability = webServiceOutput.getDouble("Probability").toString()

            saveDataInDatabase(customerDataObject, churnPrediction, probability)

            showResultPopup(churnPrediction,probability)
        }

        predictButton.setOnClickListener{
            val handler = AzureHandler()
            handler.getPrediction(gender.text.toString(),seniorCitizen.isChecked,partner.isChecked,dependents.isChecked,tenure.text.toString(),phoneService.isChecked,paperlessBilling.isChecked,monthlyCharges.text.toString(), totalCharges.text.toString(),multipleLines.isChecked,internetService.text.toString(),onlineSecurityState,onlineBackupState,deviceProtectionState,techSupportState,streamingTvState,streamingMoviesState,contract.text.toString(),paymentMethod.text.toString(),::handlePredictionResult)
        }


    }

    private fun saveDataInDatabase(data: JSONObject, churn: String, probability: String){

        val currentUser = auth.currentUser

        val userID = currentUser?.uid
        val customerID = data.getString("customerID")

        val userInputDataRef: DocumentReference = db.collection("PredictionHistory").document(userID.toString()).collection(customerID).document("InputData")
        userInputDataRef.set(
            mapOf(
                "gender" to data.getString("gender"),
                "SeniorCitizen" to data.getString("SeniorCitizen"),
                "Partner" to data.getString("Partner"),
                "Dependents" to data.getString("Dependents"),
                "tenure" to data.getString("tenure"),
                "PhoneService" to data.getString("PhoneService"),
                "MultipleLines" to data.getString("MultipleLines"),
                "InternetService" to data.getString("InternetService"),
                "OnlineSecurity" to data.getString("OnlineSecurity"),
                "OnlineBackup" to data.getString("OnlineBackup"),
                "DeviceProtection" to data.getString("DeviceProtection"),
                "TechSupport" to data.getString("TechSupport"),
                "StreamingTV" to data.getString("StreamingTV"),
                "StreamingMovies" to data.getString("StreamingMovies"),
                "Contract" to data.getString("Contract"),
                "PaperlessBilling" to data.getString("PaperlessBilling"),
                "PaymentMethod" to data.getString("PaymentMethod"),
                "MonthlyCharges" to data.getString("MonthlyCharges"),
                "TotalCharges" to data.getString("TotalCharges")
            )
        )
        val userPredictionRef: DocumentReference = db.collection("PredictionHistory").document(userID.toString()).collection(customerID).document("Prediction")
        userPredictionRef.set(
            mapOf(
                "Churn" to churn,
                "Probability" to  probability
            )
        )
    }

    private fun showResultPopup(churn: String, probability: String) {
        runOnUiThread {
            val myDialog = Dialog(this)
            myDialog.setContentView(R.layout.resultpopup)
            myDialog.setCancelable(true)

            val churnResult = myDialog.findViewById<TextView>(R.id.textViewChurnValue)
            val probabilityResult = myDialog.findViewById<TextView>(R.id.textViewProbabilityValue)

            churnResult.text = churn
            probabilityResult.text = probability

            myDialog.show()
        }
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun showInternetServiceDependents() {
        val myDialog = Dialog(this)
        myDialog.setContentView(R.layout.internetservicedependents)
        myDialog.setCancelable(true)

        val onlineSecurity = myDialog.findViewById<Switch>(R.id.switchOnlineSecurity)
        val onlineBackup = myDialog.findViewById<Switch>(R.id.switchOnlineBackup)
        val deviceProtection = myDialog.findViewById<Switch>(R.id.switchDeviceProtection)
        val techSupport = myDialog.findViewById<Switch>(R.id.switchTechSupport)
        val streamingTV = myDialog.findViewById<Switch>(R.id.switchStreamingTV)
        val streamingMovies = myDialog.findViewById<Switch>(R.id.switchStreamingMovies)

        val exitButton = myDialog.findViewById<ImageView>(R.id.imageViewExit)

        onlineSecurity.isChecked = onlineSecurityState
        onlineBackup.isChecked = onlineBackupState
        deviceProtection.isChecked = deviceProtectionState
        techSupport.isChecked = techSupportState
        streamingTV.isChecked = streamingTvState
        streamingMovies.isChecked = streamingMoviesState

        onlineSecurity.setOnCheckedChangeListener{_, isChecked ->
            onlineSecurityState = isChecked
        }
        onlineBackup.setOnCheckedChangeListener{_, isChecked ->
            onlineBackupState = isChecked
        }
        deviceProtection.setOnCheckedChangeListener{_, isChecked ->
            deviceProtectionState = isChecked
        }
        techSupport.setOnCheckedChangeListener{_, isChecked ->
            techSupportState = isChecked
        }
        streamingTV.setOnCheckedChangeListener{_, isChecked ->
            streamingTvState = isChecked
        }
        streamingMovies.setOnCheckedChangeListener{_, isChecked ->
            streamingMoviesState = isChecked
        }

        exitButton.setOnClickListener{
            myDialog.dismiss()
        }

        myDialog.show()
    }
}