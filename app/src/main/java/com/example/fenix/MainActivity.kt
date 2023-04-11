package com.example.fenix

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError


class MainActivity : AppCompatActivity() {
    // inicializa variaveis
    private lateinit var glLoginButton : Button
    private lateinit var fLoginButton : Button
    private lateinit var signInButton : Button
    private lateinit var logInButton : Button
    private lateinit var etEmailLogin : EditText
    private lateinit var etSenhaLogin : EditText

    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code:Int=123
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseApp.initializeApp(this)

        // associa variavel a id
        glLoginButton = findViewById(R.id.loginWithGoogle)
        fLoginButton = findViewById(R.id.loginWithFacebook)
        signInButton = findViewById(R.id.signInButton)
        logInButton = findViewById(R.id.iniciarSessao)
        etEmailLogin = findViewById(R.id.etEmailLogin)
        etSenhaLogin = findViewById(R.id.etSenhaLogin)

        // Cofigura o GoogleSignIn dentro do onCreate
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // joga o valor de gso dentro do GoogleSignInClient
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // inicializa a variável firebaseAuth
        firebaseAuth= FirebaseAuth.getInstance()

        // Atribui o login ao botao
        glLoginButton?.setOnClickListener(){
            Toast.makeText(this,"Logando ...",Toast.LENGTH_SHORT).show()
            signInGoogle()
        }

        // vai para a pagina de cadastro
        signInButton?.setOnClickListener(){
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        logInButton?.setOnClickListener(){
            getUsersData()
        }
    }
    // signInGoogle() fun
    private fun signInGoogle(){
        val signInIntent:Intent=mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }

    // provide task and data for GoogleAccount
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==Req_Code){
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    // this is where we update the UI after Google signin takes place
    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try{
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if(account != null){
                updateUi(account)
            }
        } catch (e:ApiException){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    // this is where we specify what UI updation are needed after google signin has taken place.
    private fun updateUi(account: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener{task->
            if(task.isSuccessful){
                SavedPreference.setEmail(this,account.email.toString())
                SavedPreference.setUsername(this,account.displayName.toString())
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun getUsersData(){
        dbRef = FirebaseDatabase.getInstance().getReference("Usuarios")
        dbRef.orderByChild("email").equalTo(etEmailLogin.text.toString().trim()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot é o nó "usuário" com todos os filhos com id 0
                    for (user in dataSnapshot.children) {
                        // faça algo com os "usuários" individuais
                        val userModel = user.getValue(UserModel::class.java)
                        if (userModel != null) {
                            if (userModel.senha == etSenhaLogin.text.toString().trim()) {
                                val intent = Intent(applicationContext,HomePage::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(applicationContext, "Senha incorreta", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, "Usuário não encontrado", Toast.LENGTH_LONG).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("firebase", "Error getting data")
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if(GoogleSignIn.getLastSignedInAccount(this)!=null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}

