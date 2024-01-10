package com.example.telcochurnprediction

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlin.random.Random

class AzureHandler() {

    private val apiKey: String = "9HRhTbKmK8j5jW1wdQYZIclwbsAbHM3x"

    private val myScope = CoroutineScope(Dispatchers.Default)

    init{
        allowSelfSignedHttps(true)
    }

    private fun allowSelfSignedHttps(allowed: Boolean) {
        if(allowed){
            System.setProperty("http.protocols", "TLSv1,TLSv1.1,TLSv1.2")
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2")
        }
    }

    private fun makeApiRequestAsync(data: String): String {
        return Dispatchers.IO
            .run {
                val url = URL("http://e9baa916-0146-4a1d-81c8-a47cc4abeecc.germanywestcentral.azurecontainer.io/score")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.doOutput = true

                val os: OutputStream = connection.outputStream
                val osw = OutputStreamWriter(os, StandardCharsets.UTF_8)
                osw.write(data)
                osw.flush()
                osw.close()

                return@run try {
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        connection.inputStream.bufferedReader().readText()
                    } else {
                        "The request failed with status code: $responseCode\nResponse Message: ${connection.responseMessage}"
                    }
                } finally {
                    connection.disconnect()
                }
            }
    }

    private fun generateRandomNumber(): Int {
        val length = 5 // Generates a random number between 1 and 5 (inclusive)

        // Generating a random number with the specified length
        var randomNumber = 0
        repeat(length) {
            randomNumber = randomNumber * 10 + Random.nextInt(0, 10)
        }
        return randomNumber
    }
     fun getPrediction(gender: String, seniorCitizen: Boolean, partner:Boolean, dependents:Boolean,
                              tenure:String, phoneService:Boolean, paperlessBilling:Boolean, monthlyCharges:String,
                              totalCharges:String, multipleLines:Boolean, internetService:String,
                              onlineSecurity:Boolean, onlineBackup:Boolean, deviceProtection:Boolean, techSupport:Boolean,
                              streamingTV:Boolean, streamingMovies:Boolean, contract:String, paymentMethod:String, callback: (String, String) -> Unit){

         val customerId = generateRandomNumber()

         val data = """
        {
            "Inputs": {
                "input1": [
                    {
                        "customerID": "$customerId",
                        "gender": "$gender",
                        "SeniorCitizen": "${if(seniorCitizen) "1" else "0"}",
                        "Partner": "${if(partner) "Yes" else "No"}",
                        "Dependents": "${if(dependents) "Yes" else "No"}",
                        "tenure": "$tenure",
                        "PhoneService": "${if(phoneService) "Yes" else "No"}",
                        "MultipleLines": "${if(!phoneService) "No phone service" else if(multipleLines) "Yes" else "No"}",
                        "InternetService": "$internetService",
                        "OnlineSecurity": "${if(internetService== "No") "No internet service" else if(onlineSecurity) "Yes" else "No"}",
                        "OnlineBackup": "${if(internetService== "No") "No internet service" else if(onlineBackup) "Yes" else "No"}",
                        "DeviceProtection": "${if(internetService== "No") "No internet service" else if(deviceProtection) "Yes" else "No"}",
                        "TechSupport": "${if(internetService== "No") "No internet service" else if(techSupport) "Yes" else "No"}",
                        "StreamingTV": "${if(internetService== "No") "No internet service" else if(streamingTV) "Yes" else "No"}",
                        "StreamingMovies": "${if(internetService== "No") "No internet service" else if(streamingMovies) "Yes" else "No"}",
                        "Contract": "$contract",
                        "PaperlessBilling": "${if(paperlessBilling) "Yes" else "No"}",
                        "PaymentMethod": "$paymentMethod",
                        "MonthlyCharges": "$monthlyCharges",
                        "TotalCharges": "$totalCharges",
                        "Churn": "No"
                    }
                ]
            },
            "GlobalParameters": {}
        }
    """.trimIndent()

         println(data)
         myScope.launch {
             try {
                 val result = makeApiRequestAsync(data)

                 callback(result,data)
                 // Handle the result or update UI as needed
             } catch (e: Exception) {
                 e.printStackTrace()
                 // Handle exception
             }
         }

    }
}