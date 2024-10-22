package com.example.aplicacionawsclase5

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var txtUserInfo: TextView
    private lateinit var btnGetUser: Button
    private lateinit var btnLogin: Button
    private lateinit var edtRut: EditText
    private lateinit var edtPassword: EditText
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtUserInfo = findViewById(R.id.txtUserInfo)
        btnGetUser = findViewById(R.id.btnGetUser)
        btnLogin = findViewById(R.id.btnLogin)
        edtRut = findViewById(R.id.edtRut)
        edtPassword = findViewById(R.id.edtPassword)

        btnGetUser.setOnClickListener {
            userInfo()
        }

        btnLogin.setOnClickListener {
            login()
        }
    }

    private fun userInfo() {
        val url = "http://34.233.95.121:8081/users"

        val request: Request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("USER_INFO_ERROR", "Error en la conexión: ${e.message}")
                runOnUiThread {
                    txtUserInfo.text = "Error al obtener información"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                if (!response.isSuccessful || responseData == null) {
                    runOnUiThread {
                        txtUserInfo.text = "Error al obtener datos: ${response.message}"
                    }
                    return
                }

                try {
                    val users = JSONArray(responseData)
                    val formattedInfo = StringBuilder()
                    for (i in 0 until users.length()) {
                        val user = users.getJSONObject(i)
                        formattedInfo.append("Nombre: ${user.getString("nombre")}\n")
                        formattedInfo.append("Correo: ${user.getString("correo")}\n")
                        formattedInfo.append("RUT: ${user.getString("rut")}\n\n")
                    }
                    runOnUiThread {
                        txtUserInfo.text = formattedInfo.toString().trim()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("USER_INFO_ERROR", "Error al procesar datos: ${e.message}")
                    runOnUiThread {
                        txtUserInfo.text = "Error al procesar la información"
                    }
                }
            }
        })
    }

    private fun login() {
        val url = "http://34.233.95.121:8081/login"
        var rut = edtRut.text.toString().replace(".", "").replace("-", "")
        val password = edtPassword.text.toString()

        val json = JSONObject()
        json.put("rut", rut)
        json.put("contraseña", password)

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            json.toString()
        )

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    txtUserInfo.text = "Error de conexión"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                runOnUiThread {
                    if (response.isSuccessful && responseData != null) {
                        // Parseamos los datos que vienen en el JSON
                        try {
                            val jsonObject = JSONObject(responseData)
                            val message = jsonObject.getString("message")
                            val nombre = jsonObject.getString("nombre")
                            val correo = jsonObject.getString("correo")

                            // Mostramos los datos de forma ordenada y clara
                            val formattedInfo = """
                                $message
                                ---------------------------
                                Nombre: $nombre
                                Correo: $correo
                                RUT: ${edtRut.text.toString()}
                            """.trimIndent()

                            txtUserInfo.text = formattedInfo
                        } catch (e: Exception) {
                            e.printStackTrace()
                            txtUserInfo.text = "Error al procesar la información"
                        }
                    } else {
                        txtUserInfo.text = "Inicio de sesión fallido"
                    }
                }
            }
        })
    }
}
