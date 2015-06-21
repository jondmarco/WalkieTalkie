package jdm.walkietalkie.threads;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;

public class RecordingThread extends Thread {

    private BluetoothSocket mSocket;
    private boolean isRecording = true;
    private AudioManager audioManager;

    public RecordingThread(BluetoothSocket mSocket) {
        this.mSocket = mSocket;
    }

    public void run() {
        recordAudio();
    }

    public void recordAudio() {
        int frequency = 44100;
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

        try {
            //Create a DataOutputStream to write the audio data into the sockets output stream.
            OutputStream os = mSocket.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bos);

            // Create a new AudioRecord object to1 record the audio.
            int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    frequency, channelConfiguration,
                    audioEncoding, bufferSize);

            short[] buffer = new short[bufferSize];
            audioRecord.startRecording();

            //while (isRecording) {
            while(isRecording) {
                    //TODO: When to stop recording? Toggle button?
                    int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                    for (int i = 0; i < bufferReadResult; i++) {
                        dos.writeShort(buffer[i]);
                    }
            }

            audioRecord.stop();
            dos.close();

        } catch (Throwable t) {

        }
    }
}
 
