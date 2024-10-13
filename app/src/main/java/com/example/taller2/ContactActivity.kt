package com.example.taller2

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller2.databinding.ActivityContactBinding

class ContactActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactBinding
    var mProjection: Array<String>? = null
    var mCursor: Cursor? = null
    var mContactsAdapter: ContactsAdapter? = null

    companion object {
        const val MY_PERMISSION_REQUEST_READ_CONTACTS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Usar View Binding para inflar la vista
        binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        // Inicializar adaptador y vista
        mProjection = arrayOf(ContactsContract.Profile._ID, ContactsContract.Profile.DISPLAY_NAME_PRIMARY)
        mContactsAdapter = ContactsAdapter(this, null, 0)
        binding.list.adapter = mContactsAdapter

        // Solicitar permiso al iniciar la actividad
        checkAndRequestPermission()
    }

    fun initView() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            mCursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI, mProjection, null, null, null
            )
            mContactsAdapter?.changeCursor(mCursor)
        }
    }

    private fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permiso concedido
                updateUIWithPermissionStatus(true)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.READ_CONTACTS
            ) -> {
                // Mostrar justificación y solicitar permiso
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_CONTACTS),
                    MY_PERMISSION_REQUEST_READ_CONTACTS
                )
            }
            else -> {
                // Solicitar permiso directamente
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_CONTACTS),
                    MY_PERMISSION_REQUEST_READ_CONTACTS
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSION_REQUEST_READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso concedido
                    updateUIWithPermissionStatus(true)
                    Toast.makeText(this, "¡Gracias!", Toast.LENGTH_SHORT).show()
                } else {
                    // Permiso denegado
                    updateUIWithPermissionStatus(false)
                    Toast.makeText(this, "¡Funcionalidad limitada!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUIWithPermissionStatus(isGranted: Boolean) {
        if (isGranted) {
            binding.textView1.text = ""
            binding.textView1.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            initView()
        } else {
            binding.textView1.text = "¡PERMISO DENEGADO!"
            binding.textView1.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        }
    }
}
