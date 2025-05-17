package com.folderv.test

import ai.onnxruntime.example.objectdetection.R
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

// In your Activity or Fragment
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Your layout

        val inspector = OnnxModelInspector(applicationContext)

        // --- Option 1: Model is in assets folder ---
        // First, copy the model from assets to a readable file path
        val modelInternalPath = inspector.copyModelFromAssetsToInternalStorage("model.onnx", "my_model_internal.onnx")

        if (modelInternalPath != null) {
            inspector.inspectModel(modelInternalPath)
        } else {
            Log.e("MainActivity", "Failed to get model path from assets.")
        }

        // --- Option 2: Model path is already known (e.g., from user selection or download) ---
        // IMPORTANT: If this path is in external storage, ensure you have READ_EXTERNAL_STORAGE permission
        // (and handle runtime permission requests for Android 6.0+).
        // For Android 10+ with Scoped Storage, direct path access to arbitrary external files
        // might be restricted unless using Storage Access Framework or MANAGE_EXTERNAL_STORAGE.
        // Best practice is to copy user-selected files to your app's cache or files directory.

        // Example: (Assuming you have the file at this path and necessary permissions)
        // val externalModelPath = "/sdcard/Download/another_model.onnx"
        // val externalModelFile = File(externalModelPath)
        // if (externalModelFile.exists()) {
        //     inspector.inspectModel(externalModelPath)
        // } else {
        //     Log.e("MainActivity", "External model not found at: $externalModelPath")
        // }

        // Example: Using a path from getExternalFilesDir (app-specific external storage, no special permission needed for this specific dir)
        // val appSpecificExternalDir = getExternalFilesDir(null)
        // if (appSpecificExternalDir != null) {
        //     val modelInAppExternalDir = File(appSpecificExternalDir, "downloaded_model.onnx")
        //     // (Assume you have downloaded or placed the model here)
        //     if (modelInAppExternalDir.exists()) {
        //         inspector.inspectModel(modelInAppExternalDir.absolutePath)
        //     }
        // }
    }
}