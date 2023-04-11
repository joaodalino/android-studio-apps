package com.example.fenix

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    // inicializa variaveis
    private lateinit var etEmailCreate : EditText
    private lateinit var etSenhaCreate : EditText
    private lateinit var etUsernameCreate : EditText
    private lateinit var btnCadastre : Button
    private lateinit var btnLoginReturn : Button

    private lateinit var dbRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // associa variavel com id
        etEmailCreate = findViewById(R.id.etEmailCreate)
        etSenhaCreate = findViewById(R.id.etSenhaCreate)
        etUsernameCreate = findViewById(R.id.etUsername)
        btnCadastre = findViewById(R.id.btnCadastre)
        btnLoginReturn = findViewById(R.id.btnVoltaLogin)

        dbRef = FirebaseDatabase.getInstance().getReference("Usuarios")

        // retorna pra tela de login
        btnLoginReturn?.setOnClickListener(){
            startActivity(Intent(this, MainActivity::class.java))
        }

        // salva dados na firebase
        btnCadastre.setOnClickListener{
            saveUserData()
        }
    }
    private fun saveUserData() {
        // adquirindo informassoes
        val email = etEmailCreate.text.toString()
        val senha = etSenhaCreate.text.toString()
        val username = etUsernameCreate.text.toString()

        // verifica se esta vazio
        if (email.isEmpty()) {
            etEmailCreate.error = "Digite um email"
        }
        if (senha.isEmpty()) {
            etSenhaCreate.error = "Digite uma senha"
        }
        if (username.isEmpty()){
            etUsernameCreate.error = "Digite um nome de usuário"
        }

        val userId = dbRef.push().key!!

        val user = UserModel(email,senha,username)

        dbRef.child(userId).setValue(user)
            .addOnCompleteListener{
                Toast.makeText(this, "Usuário criado com sucesso", Toast.LENGTH_LONG).show()
                etEmailCreate.text.clear()
                etSenhaCreate.text.clear()
                etUsernameCreate.text.clear()
                val intent = Intent(applicationContext,MainActivity::class.java)
                startActivity(intent)
            }.addOnFailureListener{err ->
                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
            }
    }
}
