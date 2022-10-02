package com.bpandof.appdeporte

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class LoginActivity : AppCompatActivity() {

    companion object{
        lateinit var useremail: String
        lateinit var providerSession: String
    }

    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var lyTerms: LinearLayout

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        lyTerms = findViewById<LinearLayout>(R.id.lyTerms)
        lyTerms.visibility = View.INVISIBLE

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        mAuth = FirebaseAuth.getInstance()
    }

    fun login(view: View){
        loginUser()
    }
    private fun loginUser(){
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful)  goHome(email, "email")
                else{
                    if (lyTerms.visibility == View.INVISIBLE) lyTerms.visibility = View.VISIBLE
                    else{
                        var cbAcept = findViewById<CheckBox>(R.id.cbAcept)
                        if (cbAcept.isChecked) register()
                    }
                }
            }

    }

    private fun goHome(email: String, provider: String){

        useremail = email
        providerSession = provider

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun register(){
//        email = findViewById<EditText>(R.id.etEmail).toString()
//        password = findViewById<EditText>(R.id.etPassword).toString()
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){

                    var dateRegister = SimpleDateFormat("dd/MM/yyyy").format(Date())
                    var dbRegister = FirebaseFirestore.getInstance()
                    dbRegister.collection("users").document(email).set(hashMapOf(
                        "user" to email,
                        "dateRegister" to dateRegister
                    ))

                    goHome(email, "email")
                }
                else Toast.makeText(this, "Error, algo ha ido mal :(", Toast.LENGTH_SHORT).show()
            }
    }

}