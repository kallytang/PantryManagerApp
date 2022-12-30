package dev.kallytang.chomp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.login_activity.*


class LoginActivity : AppCompatActivity() {
    private companion object{
        private const val TAG: String = "LoginActivity"
        private const val RC_GOOGLE_SIGN_IN = 1134
    }
    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        //initialize Firebase Auth
        auth = Firebase.auth

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(this, gso)
        btn_sign_in2.setOnClickListener {
            val signInIntent: Intent = client.signInIntent
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }


    }


    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately

            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    var result = task.result
                    // check if user is a new user
                    if (result?.additionalUserInfo?.isNewUser == true){
                        // direct user to a set up page,
                        toSetUpAccount()
                    }else{
                        val user = auth.currentUser
                        updateUI(user)
                    }

                } else {
                    Toast.makeText(this, "Authentication Failure", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }
    private fun updateUI(currentUser: FirebaseUser?) {
        //go to main activity if signed in
        if(currentUser==null){
            return
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    private fun toSetUpAccount(){
        val intent = Intent(this, AccountSetUpActivity::class.java)
        startActivity(intent)
        finish()
    }
}

//        TODO set up email sign up another time
//        btn_login.setOnClickListener {
//            btn_login.isEnabled = false
//            var email = et_email.text.toString()
//            val password = et_password.text.toString()
//            if (email.isBlank() || password.isBlank()){
//                Toast.makeText(this, "Email/Password can't be empty", Toast.LENGTH_SHORT)
//                return@setOnClickListener
//            }
//            var authEmail = FirebaseAuth.getInstance()
//            authEmail.signInWithEmailAndPassword(email, password).addOnCompleteListener { task->
//                btn_login.isEnabled = true
//                if(task.isSuccessful){
//                    goToMainActivity()
//                }else{
//                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }

//    private fun goToMainActivity() {
//        val intent = Intent(this, MainActivity::class.java)
//        startActivity(intent)
//        finish()
//    }
