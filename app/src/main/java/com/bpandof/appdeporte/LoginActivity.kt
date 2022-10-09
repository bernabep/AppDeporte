package com.bpandof.appdeporte

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.TextureView
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.Utils
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class LoginActivity : AppCompatActivity() {

    companion object {
        lateinit var useremail: String
        lateinit var providerSession: String
    }

    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private var password2 by Delegates.notNull<String>()
    private var termsAcept by Delegates.notNull<Boolean>()
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etRepPassword: EditText
    private lateinit var lyTerms: LinearLayout
    private lateinit var tvLogin: TextView
    private lateinit var cbAcept: CheckBox
    private var loginOrRegister: String = "Login"

    //signingoogle
    private val RESULT_CODE_SIGN_IN = 100  // Can be any integer unique to the Activity
    val callbackManager = CallbackManager.Factory.create()


    //private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest


    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        etRepPassword = findViewById(R.id.etRepPassword)
        etRepPassword.visibility = View.INVISIBLE

        lyTerms = findViewById(R.id.lyTerms)
        lyTerms.visibility = View.INVISIBLE

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        cbAcept = findViewById(R.id.cbAcept)
        termsAcept = cbAcept.isChecked

        mAuth = FirebaseAuth.getInstance()

        this.manageButtonLogin()
        etEmail.doOnTextChanged { text, start, before, count -> manageButtonLogin() }
        etPassword.doOnTextChanged { text, start, before, count -> manageButtonLogin() }
        etRepPassword.doOnTextChanged { text, start, before, count -> manageButtonLogin() }


    }

    private fun controlIsRegistering(enable: Boolean) {
        if (enable) {
            loginOrRegister = "register"

        } else loginOrRegister = "login"
    }

    public override fun onStart() {
        super.onStart()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            goHome(currentUser.email.toString(), currentUser.providerId)
        }

    }

    public override fun onResume() {
        super.onResume()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            goHome(currentUser.email.toString(), currentUser.providerId)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val starMain = Intent(Intent.ACTION_MAIN)
        starMain.addCategory(Intent.CATEGORY_HOME)
        starMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(starMain)
    }

    private fun manageButtonLogin() {
        var tvLogin = findViewById<TextView>(R.id.tvLogin)
        var cbAcept = findViewById<CheckBox>(R.id.cbAcept)

        email = etEmail.text.toString()
        password = etPassword.text.toString()
        password2 = etRepPassword.text.toString()


        if (loginOrRegister == "login" && (TextUtils.isEmpty(password) || !ValidateEmail.isEmail(
                email
            )) || (loginOrRegister == "register" && (password2 != password || !cbAcept.isChecked))
        ) {
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            tvLogin.isEnabled = false
        } else {
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            tvLogin.isEnabled = true
        }
    }

    fun login(view: View) {
        loginUser()
    }


    private fun loginUser() {
        var tvLogin = findViewById<TextView>(R.id.tvLogin)
        var tvForgotPassword = findViewById<TextView>(R.id.txtForgotPassword)
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    goHome(email, "email")
                    controlIsRegistering(false)
                } else {
                    controlIsRegistering(true)
                    if (lyTerms.visibility == View.INVISIBLE) {
                        lyTerms.visibility = View.VISIBLE
                        etRepPassword.visibility = View.VISIBLE
                        tvForgotPassword.visibility = View.INVISIBLE
                        tvLogin.text = resources.getString(R.string.tvRegistrar)
                        tvLogin.isEnabled = false
                        tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))

                    } else {
                        var cbAcept = findViewById<CheckBox>(R.id.cbAcept)
                        if (cbAcept.isChecked) register()
                    }
                }
            }
    }

    private fun goHome(email: String, provider: String) {
        Log.d("Funciones", "goHome")
        useremail = email
        providerSession = provider

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun register() {
//        email = findViewById<EditText>(R.id.etEmail).toString()
//        password = findViewById<EditText>(R.id.etPassword).toString()
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    var dateRegister = SimpleDateFormat("dd/MM/yyyy").format(Date())
                    var dbRegister = FirebaseFirestore.getInstance()
                    dbRegister.collection("users").document(email).set(
                        hashMapOf(
                            "user" to email,
                            "dateRegister" to dateRegister
                        )
                    )

                    goHome(email, "email")
                } else Toast.makeText(this, "Error, algo ha ido mal :(", Toast.LENGTH_SHORT).show()
            }
    }

    fun goTerms(view: View) {
        val intent = Intent(this, TermsActivity::class.java)
        startActivity(intent)
    }

    fun forgotPassword(view: View) {

        //  val intent = Intent(this.ForgotPasswordActivity::class.java)
        resetPassword()


    }

    private fun resetPassword() {
        var e = etEmail.text.toString()
        if (!TextUtils.isEmpty(e)) {
            mAuth.sendPasswordResetEmail(e)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) Toast.makeText(
                        this,
                        "Email Enviado a $e",
                        Toast.LENGTH_SHORT
                    ).show()
                    else Toast.makeText(
                        this,
                        "No se encontró el usuario con este correo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else
            Toast.makeText(this, "Indica un email", Toast.LENGTH_SHORT).show()
    }

    fun callTermsAcept(view: View) {
        manageButtonLogin()
    }

    fun callSignInGoogle(view: View) {
        signInGoogle()
    }

    private fun signInGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_cliente_id))
            .requestEmail()
            .build()


        var googleSignInClient = GoogleSignIn.getClient(this, gso)

        startActivityForResult(googleSignInClient.signInIntent, RESULT_CODE_SIGN_IN)
    }

    fun callSignInFacebook(view: View) {
        signInFacebook()
    }

    private fun signInFacebook() {

        Log.d("Funciones", "signInFacebook")
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    result.let {
                        val token = it.accessToken
                        val credential = FacebookAuthProvider.getCredential(token.token)
                        mAuth.signInWithCredential(credential).addOnCompleteListener {
                            if (it.isSuccessful) {
                                email = it.result.user?.email.toString()
                                var dateRegister = SimpleDateFormat("dd/MM/yyyy").format(Date())
                                var dbRegister = FirebaseFirestore.getInstance()
                                dbRegister.collection("users").document(email).set(
                                    hashMapOf(
                                        "user" to email,
                                        "dateRegister" to dateRegister
                                    )
                                )
                                Log.d("Funciones", "email? $email")
                                goHome(email, "Facebook")
                            } else
                                showError("Facebook")

                        }
                    }
                }

                override fun onCancel() {
                    Log.d("Funciones", "onCancel")
                    showError("Facebook")
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, "facebook:onError", error)
                    showError("Facebook")
                }
            })
    }

    private fun showError(provider: String) {
        Log.d("Funciones", "showError")
        Toast.makeText(this, "Error en la conexión con $provider", Toast.LENGTH_LONG).show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("Funciones", "onActivityResult")
        callbackManager.onActivityResult(requestCode, resultCode, data)
        Log.d("Funciones", "callbackManager")
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_CODE_SIGN_IN) {
            Log.d("Funciones", "requestCode")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!

                if (account != null) {
                    email = account.email!!
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    mAuth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            var dateRegister = SimpleDateFormat("dd/MM/yyyy").format(Date())
                            var dbRegister = FirebaseFirestore.getInstance()
                            dbRegister.collection("users").document(email).set(
                                hashMapOf(
                                    "user" to email,
                                    "dateRegister" to dateRegister
                                )
                            )
                            goHome(email, "Google")
                        } else
                            showError("Google")
                    }
                }
            } catch (e: ApiException) {
                showError("Google")
            }
        }
    }
}





