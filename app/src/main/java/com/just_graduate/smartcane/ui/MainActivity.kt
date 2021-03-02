package com.just_graduate.smartcane.ui

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.just_graduate.smartcane.PERMISSIONS
import com.just_graduate.smartcane.R
import com.just_graduate.smartcane.REQUEST_ALL_PERMISSION
import com.just_graduate.smartcane.databinding.ActivityMainBinding
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageCapture
import androidx.core.app.ActivityCompat
import com.just_graduate.smartcane.util.*
import com.just_graduate.smartcane.util.Util.Companion.textToSpeech
import com.just_graduate.smartcane.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.util.concurrent.ExecutorService


class MainActivity : AppCompatActivity() {
    private val viewModel by viewModel<MainViewModel>()
    var mBluetoothAdapter: BluetoothAdapter? = null
    var recv: String = ""

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraCaptureButton: Button

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.viewModel = viewModel

        if (!hasPermissions(this, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
        }

        cameraCaptureButton = findViewById(R.id.camera_capture_button)

        // View Component 들이 ViewModel Observing 시작
        initObserving()

        cameraCaptureButton.setOnClickListener {
            takePhoto()
        }

        outputDirectory = getOutputDirectory()

    }

    private fun takePhoto() {}

    private fun startCamera() {}

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                viewModel.onClickConnect()
            }
        }

    private fun initObserving() {
        // Progress
        viewModel.inProgress.observe(this, {
            if (it.getContentIfNotHandled() == true) {
                viewModel.inProgressView.set(true)
            } else {
                viewModel.inProgressView.set(false)
            }
        })

        // Progress text
        viewModel.progressState.observe(this, {
            viewModel.txtProgress.set(it)
        })

        // Bluetooth On 요청
        viewModel.requestBleOn.observe(this, {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startForResult.launch(enableBtIntent)
        })

        // Bluetooth Connect/Disconnect Event
        viewModel.connected.observe(this, {
            if (it != null) {
                if (it) {
                    viewModel.setInProgress(false)
                    viewModel.btnConnected.set(true)
                    Util.showNotification(getString(R.string.success_connect_cane))
                    textToSpeech(getString(R.string.success_connect_cane))
                } else {
                    viewModel.setInProgress(false)
                    viewModel.btnConnected.set(false)
                    Util.showNotification(getString(R.string.disconnect_connect_cane))
                    textToSpeech(getString(R.string.disconnect_connect_cane))
                }
            }
        })

        // Bluetooth Connect Error
        viewModel.connectError.observe(this, {
            Util.showNotification("연결 도중 오류가 발생하였습니다. 기기를 확인해주세요.")
            viewModel.setInProgress(false)
        })

        //Data Receive
        viewModel.putTxt.observe(this, {
            if (it != null) {
                recv += it
                viewModel.txtRead.set(recv)
            }
        })
    }

    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (context?.let { ActivityCompat.checkSelfPermission(it, permission) }
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    // Permission check
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ALL_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
                } else {
                    requestPermissions(permissions, REQUEST_ALL_PERMISSION)
                    Toast.makeText(this, "Permissions must be granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.txtRead.set("Here you can see the message come")
    }

    override fun onPause() {
        super.onPause()
        viewModel.unregisterReceiver()
    }

    override fun onBackPressed() {
        // super.onBackPressed()
        viewModel.setInProgress(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "SmartCane"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}