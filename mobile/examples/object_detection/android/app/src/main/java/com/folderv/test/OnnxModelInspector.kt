package com.folderv.test

import android.content.Context
import android.util.Log
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OrtException
import java.io.File
import java.io.FileOutputStream

class OnnxModelInspector(private val context: Context) {

    companion object {
        private const val TAG = "OnnxModelInspector"
    }

    private val ortEnvironment: OrtEnvironment = OrtEnvironment.getEnvironment()

    /**
     * Inspects an ONNX model file from a given path.
     *
     * @param modelPath The absolute path to the .onnx model file.
     */
    fun inspectModel(modelPath: String) {
        var session: OrtSession? = null
        val modelFile = File(modelPath)

        if (!modelFile.exists()) {
            Log.e(TAG, "Model file does not exist at path: $modelPath")
            return
        }
        if (!modelFile.canRead()) {
            Log.e(TAG, "Cannot read model file at path: $modelPath. Check permissions.")
            return
        }

        Log.i(TAG, "Attempting to inspect ONNX model: $modelPath")

        try {
            // 1. 创建 SessionOptions (可选, 可以配置线程数等)
            val options = OrtSession.SessionOptions()
            // 例如: options.setIntraOpNumThreads(2)

            // 2. 尝试创建 OrtSession
            // 这是关键步骤，如果文件不是有效的 ONNX 模型，这里会抛出 OrtException
            session = ortEnvironment.createSession(modelPath, options)

            // 3. 如果成功加载，说明是有效的 ONNX 模型
            Log.i(TAG, "Successfully loaded ONNX model. This appears to be a valid ONNX file.")
            Log.i(TAG, "Model Path: $modelPath")

            // 4. 获取并打印模型元数据
            val metadata = session.metadata
            Log.i(TAG, "--- Model Metadata ---")
            Log.i(TAG, "Producer: ${metadata.producerName}")
            Log.i(TAG, "Graph Name: ${metadata.graphName}")
            Log.i(TAG, "Description: ${metadata.description}")
            Log.i(TAG, "Domain: ${metadata.domain}")
            Log.i(TAG, "Version: ${metadata.version}")
            metadata.customMetadata.forEach { (key, value) ->
                Log.i(TAG, "Custom Metadata: $key = $value")
            }

            // 5. 获取并打印输入节点信息
            Log.i(TAG, "--- Input Nodes (Count: ${session.inputCount}) ---")
            session.inputNames.forEach { name ->
                val inputInfo = session.getInputInfo()[name]
                inputInfo?.let {
                    Log.i(TAG, "Input Node Name: ${it.name}")
                    Log.i(TAG, "  Type: ${it.info.type}")
                    Log.i(TAG, "  Shape: ${it.info.shape.contentToString()}") // For arrays
                }
            }

            // 6. 获取并打印输出节点信息
            Log.i(TAG, "--- Output Nodes (Count: ${session.outputCount}) ---")
            session.outputNames.forEach { name ->
                val outputInfo = session.getOutputInfo()[name]
                outputInfo?.let {
                    Log.i(TAG, "Output Node Name: ${it.name}")
                    Log.i(TAG, "  Type: ${it.info.type}")
                    Log.i(TAG, "  Shape: ${it.info.shape.contentToString()}")
                }
            }

        } catch (e: OrtException) {
            Log.e(TAG, "Failed to load or parse ONNX model. It might not be a valid ONNX file or is corrupted.", e)
            Log.e(TAG, "Details: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "An unexpected error occurred during model inspection.", e)
        } finally {
            // 7. 关闭会话释放资源
            try {
                session?.close()
                Log.i(TAG, "ONNX session closed.")
            } catch (e: OrtException) {
                Log.e(TAG, "Error closing ONNX session.", e)
            }
            // OrtEnvironment 通常在应用生命周期结束时关闭，或者当不再需要 ONNX Runtime 时
            // ortEnvironment.close()
        }
    }

    /**
     * Helper function to copy a model from the assets folder to internal app storage.
     * ONNX Runtime needs a file path, and direct access to assets is not always straightforward
     * for native libraries. Copying to internal storage is a common pattern.
     *
     * @param assetFileName The name of the model file in the assets folder (e.g., "model.onnx").
     * @param outputFileName The desired name for the model file in internal storage.
     * @return The absolute path to the copied model file, or null if copying failed.
     */
    fun copyModelFromAssetsToInternalStorage(assetFileName: String, outputFileName: String): String? {
        val outputFile = File(context.filesDir, outputFileName)

        // Optional: If you don't want to overwrite, check if it exists
        // if (outputFile.exists()) {
        //     Log.i(TAG, "Model already exists in internal storage: ${outputFile.absolutePath}")
        //     return outputFile.absolutePath
        // }

        try {
            context.assets.open(assetFileName).use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Log.i(TAG, "Model copied from assets to: ${outputFile.absolutePath}")
            return outputFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error copying model '$assetFileName' from assets", e)
            return null
        }
    }
}