package com.wells.mic

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread
import kotlin.math.pow
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private val requestMic = 1001
    private val sampleRate = 48000
    private val gain = 10.0.pow(10.0 / 20.0).toFloat() // +10 dB

    @Volatile private var capturing = false
    private var recorder: AudioRecord? = null
    private var suppressor: NoiseSuppressor? = null

    private lateinit var statusText: TextView
    private lateinit var detailsText: TextView
    private lateinit var micButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        detailsText = findViewById(R.id.detailsText)
        micButton = findViewById(R.id.micButton)

        micButton.setOnClickListener {
            if (capturing) stopMic() else ensurePermissionAndStart()
        }
    }

    private fun ensurePermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startMic()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), requestMic)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestMic && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            startMic()
        } else if (requestCode == requestMic) {
            statusText.text = "Permissão de microfone necessária"
        }
    }

    private fun startMic() {
        if (capturing) return

        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuffer <= 0) {
            statusText.text = "Não foi possível preparar o microfone"
            return
        }

        val localRecorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBuffer * 2
        )

        if (localRecorder.state != AudioRecord.STATE_INITIALIZED) {
            localRecorder.release()
            statusText.text = "Microfone não inicializado"
            return
        }

        recorder = localRecorder
        suppressor = if (NoiseSuppressor.isAvailable()) {
            NoiseSuppressor.create(localRecorder.audioSessionId)?.also { it.enabled = true }
        } else null

        try {
            localRecorder.startRecording()
        } catch (_: Exception) {
            suppressor?.release()
            suppressor = null
            localRecorder.release()
            recorder = null
            statusText.text = "Não foi possível iniciar a captura"
            return
        }

        capturing = true
        statusText.text = "Microfone ligado"
        micButton.text = "🎤  DESLIGAR"
        detailsText.text = "Ganho experimental: +10 dB\nSupressão de ruído: ${if (suppressor != null) "ON" else "indisponível no aparelho"}"

        thread(name = "WellsMicCapture") {
            val pcm = ShortArray(minBuffer / 2)
            while (capturing) {
                val count = localRecorder.read(pcm, 0, pcm.size)
                if (count > 0) {
                    // Pipeline v0.1: aplica o ganho no PCM. Na próxima etapa,
                    // estes mesmos frames serão enviados ao Receiver do Windows.
                    for (i in 0 until count) {
                        val amplified = (pcm[i] * gain).roundToInt()
                        pcm[i] = amplified.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                }
            }
        }
    }

    private fun stopMic() {
        if (!capturing && recorder == null) return
        capturing = false

        try { recorder?.stop() } catch (_: Exception) { }
        suppressor?.release()
        suppressor = null
        recorder?.release()
        recorder = null

        statusText.text = "Microfone desligado"
        micButton.text = "🎤  LIGAR"
        detailsText.text = "Ganho experimental: +10 dB\nSupressão de ruído: aguardando"
    }

    override fun onStop() {
        stopMic()
        super.onStop()
    }

    override fun onDestroy() {
        stopMic()
        super.onDestroy()
    }
}
