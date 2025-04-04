package com.example.adminblinkitclone

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.adminblinkitclone.databinding.ProgressDialogBinding
import com.google.firebase.auth.FirebaseAuth

object Utils {

    private var dialog : AlertDialog? = null
    private var firebaseAuthInstance : FirebaseAuth?= null

    fun showDialog(context: Context, message: String){
        val progress = ProgressDialogBinding.inflate(LayoutInflater.from(context))
        progress.tvMessage.text = message
        dialog = AlertDialog.Builder(context).setView(progress.root).setCancelable(false).create()
        dialog!!.show()
    }

    fun hideDialog(){
        dialog?.dismiss()
    }

    fun showToast(context : Context, message: String){
        Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
    }

    fun getAuthInstance(): FirebaseAuth {
        if(firebaseAuthInstance==null){
            firebaseAuthInstance = FirebaseAuth.getInstance()
        }
        return firebaseAuthInstance!!
    }

    fun getCurrentUserId(): String? {
        return firebaseAuthInstance?.currentUser?.uid
    }

    fun getRandomId() : String{
        return (1..25).map{(('A'..'Z')+('a'..'z')+('0'..'9')).random()}.joinToString("")
    }
}