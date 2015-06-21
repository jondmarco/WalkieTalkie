package jdm.walkietalkie.threads;

import android.bluetooth.BluetoothSocket;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread{

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private RecordingThread recordingThread;


    public ConnectedThread(BluetoothSocket socket) {
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {

            int audioLength = 4096; //TODO: WHAT SIZE IS IDEAL? Maybe this size is too large causing more delay in audio?
            short[] audio = new short[audioLength];
            BufferedInputStream bis;
            DataInputStream dis;
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, //used to stream the audio
                    44100,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    audioLength,
                    AudioTrack.MODE_STREAM);





            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try{
                    bis = new BufferedInputStream(mmInStream);
                    dis = new DataInputStream(bis);

                    int i = 0;
                    //fill up the audio
                    while (dis.available() > 0) {
                        if (i == audioLength) {
                            break;
                        }
                        audio[i] = dis.readShort();
                        i++;
                    }

                    //TODO: Send the audio short array to the handler and play audio in Activity?
                    audioTrack.write(audio, 0, audioLength); //write the audio to the audio track
                    audioTrack.play(); //play audio on speakers
                    audioTrack.flush();

                    audio = null;
                    audio = new short[audioLength];

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }

        public void startRecordingThread() {
            if (recordingThread == null ) {
                recordingThread = new RecordingThread(mmSocket);
                recordingThread.start();
            }
        }

}


