package com.meshproxy.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.meshproxy.databinding.ActivityMainBinding
import com.meshproxy.service.MeshProxyService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isRunning = false

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()

        binding.buttonToggle.setOnClickListener {
            if (!isRunning) {
                startMeshService()
            } else {
                stopMeshService()
            }
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun startMeshService() {
        val mode = when {
            binding.radioClient.isChecked -> "Клиент"
            binding.radioSource.isChecked -> "Источник"
            else -> "Авто"
        }

        val intent = Intent(this, MeshProxyService::class.java).apply {
            action = "ACTION_START"
            putExtra("EXTRA_MODE", mode)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        isRunning = true
        binding.buttonToggle.text = "Остановить сеть"
        binding.textViewStatus.text = "Статус: Работает ($mode)"
        Toast.makeText(this, "MeshProxy запущен в режиме $mode", Toast.LENGTH_SHORT).show()
    }

    private fun stopMeshService() {
        val intent = Intent(this, MeshProxyService::class.java).apply {
            action = "ACTION_STOP"
        }
        startService(intent)

        isRunning = false
        binding.buttonToggle.text = "Запустить сеть"
        binding.textViewStatus.text = "Статус: Остановлено"
        binding.textViewStats.text = "Передано: 0 КБ | Источников: 0"
        Toast.makeText(this, "MeshProxy остановлен", Toast.LENGTH_SHORT).show()
    }
}
