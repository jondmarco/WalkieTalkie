package jdm.walkietalkie.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by tmast_000 on 6/13/2015.
 */
public class RecordingThread extends Thread {
    private ConnectedThread connectedThread;
    private BluetoothSocket mSocket;

    private boolean isRecording = true;
    public RecordingThread(ConnectedThread connectedThread, BluetoothSocket mSocket) {
        this.connectedThread = connectedThread;
        this.mSocket = mSocket;
    }

        public void run() {
            // Recieve audio from mic and send to connected Thread as bytes
            //String toWrite = "Hello !!!";
            //connectedThread.write(toWrite.getBytes());

            //MediaRecorder recorder = new MediaRecorder();
            record();
        }


    public void record() {
        int frequency = 11025;
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/reverseme.pcm");

// Delete any previous recording.
        if (file.exists())
            file.delete();


// Create the new file.
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create " + file.toString());
        }

        try {
// Create a DataOuputStream to write the audio data into the saved file.
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


            while (isRecording) {
                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                for (int i = 0; i < bufferReadResult; i++)
                    dos.writeShort(buffer[i]);
            }


            audioRecord.stop();
            dos.close();

        } catch (Throwable t) {

        }
    }
}
