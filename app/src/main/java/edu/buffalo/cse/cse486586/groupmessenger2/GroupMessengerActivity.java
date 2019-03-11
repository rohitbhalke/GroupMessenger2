package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static String [] REMOTE_PORTS = {"11108","11112","11116", "11120","11124"};
    // ,"11112","11116","11120","11124"
    // , "11112","11116","11120","11124"

    static final int SERVER_PORT = 10000;
    static int SEQUENCE_NUMBER = 0;

    public static PriorityQueue<Message> queue = new PriorityQueue<Message>(10, new customComparator());
    public static int maximumProposedNumber = 0;
    public static int maximumAggreedSequenceNum = 0;
    public static int num = 0;
    public static final String MULTICAST_MESSAGE = "MULTICAST";
    public static final String SEQUENCE_MESSAGE = "SEQUENCE";
    public static final String FAILED_MESSAGE = "FAILED" ;

    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";

    private ContentResolver mContentResolver;
    private Uri mUri;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        mContentResolver = getContentResolver();
        mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");

        /*
         * Calculate the port number that this AVD listens on.
         * It is just a hack that I came up with to get around the networking limitations of AVDs.
         * The explanation is provided in the PA1 spec.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }



        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        final EditText editText = (EditText) findViewById(R.id.editText1);
        final Button send = (Button) findViewById(R.id.button4);
        /* Register on key listner for click handler*/

        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                TextView localTextView = (TextView) findViewById(R.id.textView1);
                localTextView.append("\t" + msg); // This is one way to display a string.

                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });
    }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    /***
     * ServerTask is an AsyncTask that should handle incoming messages. It is created by
     * ServerTask.executeOnExecutor() call in SimpleMessengerActivity.
     *
     * Please make sure you understand how AsyncTask works by reading
     * http://developer.android.com/reference/android/os/AsyncTask.html
     *
     * @author stevko
     *
     */
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            Socket socket = null;
            DataInputStream dis;
            DataOutputStream dos;
            try {
                while(true) {
                    socket = serverSocket.accept();
                    InputStream stream = socket.getInputStream();

                    InputStreamReader inputStream = new InputStreamReader(stream);
                    dis = new DataInputStream(stream);
                    //BufferedReader br = new BufferedReader(inputStream);
                    String messageReceived = "";
                    String message = dis.readUTF();


                    Log.i("TAG", "Before Logging");
                    messageReceived = message;
                    Log.i("TAG", "Message Received" + messageReceived);
                    String[] splittedMessage = messageReceived.split(";");

                    String messageType = splittedMessage[0].split(":")[1];

                    if(messageType.equals(MULTICAST_MESSAGE)) {
                        //queue.add()
                        //System.out.println("Message Length::" + splittedMessage.length);
                        String sender = splittedMessage[1].split(":")[1];
                        //System.out.println("sender::" + sender);
                        String actualMessage = splittedMessage[2].split(":")[1];
                        //System.out.println("actualMessage::" + actualMessage);
                        String receiver = splittedMessage[3].split(":")[1];
                        //System.out.println("receiver::" + receiver);
                        String messageId = splittedMessage[4].split(":")[1];
                        //System.out.println("messageId::" + messageId);

                        int proposedNumber = Math.max(maximumProposedNumber, maximumAggreedSequenceNum) + 1;
                        maximumProposedNumber = proposedNumber;

                        Message storeMessage = new Message(MULTICAST_MESSAGE, actualMessage, sender, receiver, false, messageId, maximumProposedNumber);

                        Log.i("MESSAGE_CONTENT", messageId);
                        queue.add(storeMessage);

                        // Now send the proposed number back to the sender process

                        //String type, String actualMessage, String senderId, String receiverId, int proposed
                        Message proposedMessage = new Message("PROPOSED", actualMessage, sender, receiver, maximumProposedNumber);
                        Log.i("ProposedNumber", "ProposedNumber for message " + proposedNumber +"  " + actualMessage);
                        OutputStream outputStream = socket.getOutputStream();
                        dos = new DataOutputStream(outputStream);
                        dos.writeUTF(proposedMessage.getString());
//                        outputStream.write(String.valueOf(proposedNumber).getBytes());
//                        outputStream.close();
                        System.out.println("MULTICAST Message Detected");

                    }
                    else if(messageType.equals(SEQUENCE_MESSAGE)) {

                        System.out.println("Message Length::" + splittedMessage.length);
                        String messageId = splittedMessage[5].split(":")[1];
                        String sequenceNumber = splittedMessage[4].split(":")[1];
                        String sender = splittedMessage[1].split(":")[1];
                        maximumAggreedSequenceNum = Math.max(maximumAggreedSequenceNum, Integer.valueOf(sequenceNumber));
                        Message msg = getTheMessage(messageId, sender);
                        Log.i("GOT_SEQUENCE1", splittedMessage[2].split(":")[1] +"   " + sequenceNumber + " :: " + msg.isDeliverable);
                        msg.isDeliverable = true;
                        msg.sequenceNumber = Integer.valueOf(sequenceNumber);
                        queue.add(msg);
                        Log.i("GOT_SEQUENCE2", splittedMessage[2].split(":")[1] +"   " + sequenceNumber + " :: " + msg.isDeliverable);
                        showQueueStatus();
                        deliverMessages();
                    }
                    else if(messageType.equals(FAILED_MESSAGE)) {

                        String failedPort = splittedMessage[1].split(":")[1];
                        Log.i("Failed_PORT", failedPort);
                        removeMessageFromFailedPort(failedPort);
                    }


                    //this.publishProgress(messageReceived);
                    socket.close();
                    inputStream.close();
                    dis.close();

                }
            }
            catch(IOException e){
                Log.e(TAG, "Client Disconnected");
            }
            catch (Exception e) {
                Log.e(TAG, "Failed to accept connection");
            }finally {
                try{
                    if(socket != null)
                        socket.close();
                }
                catch (IOException e){
                    Log.e(TAG, "Error while disconnecting socket");
                }
            }
            return null;
        }

        private void removeMessageFromFailedPort(String failedPort) {
            for(Message msg : queue) {
                Log.i("REMOVE_MESSAGE", msg.sender);
                if(msg.sender.equals(failedPort)) {
                    queue.remove(msg);
                }
            }
        }

        private void deliverMessages() {
            ContentValues cv = new ContentValues();
            while(queue.size()>0 && queue.peek().isDeliverable) {
                Message msg = queue.poll();
                //Log.i("DELIVERED")
                cv.put(KEY_FIELD, String.valueOf(num));
                cv.put(VALUE_FIELD, msg.message);
                mContentResolver.insert(mUri, cv);
                this.publishProgress(msg.message);
                Log.i("DELIVERED", msg.message+"  seqNo: " + String.valueOf(msg.sequenceNumber));
                num++;
            }
        }

        private void showQueueStatus() {
            StringBuilder sb = new StringBuilder();
            Iterator<Message> itr = queue.iterator();
            while(itr.hasNext()) {
                Message temp = itr.next();
                sb.append(temp.message+"   " + temp.isDeliverable +"   "+ temp.sender +"\n");
            }
            Log.i("QUEUE_STATUS", sb.toString());
        }

        private Message getTheMessage(String msgId, String sender) {
            for(Message msg : queue) {
                if(msg.messageID.equals(msgId) && msg.sender.equals(sender)) {
                    queue.remove(msg);
                    return msg;
                }
            }
            Log.i("NULL", "Failed To Update Queue");
            return null;
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append(strReceived);
            localTextView.append("\n");

            /*
                        Store the messages in Content Provider
             */

            ContentValues cv = new ContentValues();
            cv.put(KEY_FIELD, String.valueOf(SEQUENCE_NUMBER++));
            cv.put(VALUE_FIELD, strReceived);
            mContentResolver.insert(mUri, cv);

            String filename = "SimpleMessengerOutput";
            String string = strReceived + "\n";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "File write failed");
            }

            return;
        }
    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     *
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        ArrayList<Message> proposedNumbers = new ArrayList<Message>();

        @Override
        protected Void doInBackground(String... msgs) {

            RandomNumberGenerator random = new RandomNumberGenerator();

            String senderPort = msgs[1];
            InputStreamReader inputStream = null;
            BufferedReader br = null;
            Socket socket = null;
            OutputStream stream = null;
            String msgToSend = msgs[0];
            DataOutputStream dos = null;
            DataInputStream dis = null;
            String messageId = String.valueOf(random.getRandomNumber());
            String remote = null;
            try {
                for(String PORT : REMOTE_PORTS) {
                    remote = PORT;
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(PORT));
                    stream = socket.getOutputStream();

                    // From here send the MULTICAST message

                    Message message = new Message(MULTICAST_MESSAGE, msgToSend, senderPort, PORT, messageId);
                    String stringMessage = message.getString();
                    OutputStreamWriter out = new OutputStreamWriter(stream,
                            "UTF-8");

                    dos = new DataOutputStream(stream);
                    dos.writeUTF(stringMessage);

                    stream.flush();
                    out.flush();


                    try {
                        dis = new DataInputStream(socket.getInputStream());

                        String proposedMessageReceived = dis.readUTF();
                        Message proposedMessage = getProposedMessage(proposedMessageReceived);

                    /*
                            While adding to the arraylist make sure that the proposed
                            number is generated for the message you sent and it was from the sender process
                     */
                        if (proposedMessage.sender.equals(senderPort) && proposedMessage.message.equals(msgToSend)) {
                            proposedNumbers.add(proposedMessage);
                        }
                    }
                    catch (SocketTimeoutException te) {
                        Log.e("Socket_Timeout_Exce", PORT);
                        sendFailedPortMessage(remote);
                    }
                    catch (IOException exp) {
                        Log.e("CLIENT_FAILED_EXCEPTION", PORT);
                        sendFailedPortMessage(remote);
                    }
                    catch (Exception e) {
                        Log.e("ACCEPT_PROPOSED_NO", "Exception while accepting Proposed Number");
                    }
                }
            }
            catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException", e.fillInStackTrace());
            }
            finally {
                try{
                    socket.close();
                    stream.close();
                    dis.close();
                    dos.close();
                    socket.close();
                    stream.close();

                }
                catch (IOException e){
                    Log.e(TAG, "Error while disconnecting socket");
                }
            }


            int maxProposedNumber = getMaxProposedNumber(proposedNumbers);
            Log.i("AGREED", "Agrred Sequence Number For::" + msgToSend + " is :: " + maxProposedNumber  + " , suggestions were::" + displayProposedNumbers(proposedNumbers));
            System.out.println("Largest proposed Number Is::" + maxProposedNumber);

            // Now send the number with sequence number

            // **************************************************** Remaining
            String clientPort = null;
            try {
                for(String PORT : REMOTE_PORTS) {
                    clientPort = PORT;
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(PORT));
                    stream = socket.getOutputStream();

                    // From here send the MULTICAST message

                    Message message = new Message(SEQUENCE_MESSAGE, msgToSend, senderPort, PORT, maxProposedNumber, messageId);
                    String stringMessage = message.getString();
                    OutputStreamWriter out = new OutputStreamWriter(stream,
                            "UTF-8");

                    dos = new DataOutputStream(stream);
                    dos.writeUTF(stringMessage);
                    //out.write(stringMessage, 0, stringMessage.length());
                    //stream.write(stringMessage.getBytes("UTF-8"));
                    stream.flush();
                    out.flush();
                }
                //Thread.sleep(500);
            }
            catch (SocketTimeoutException te) {
                Log.e("Socket_Timeout_Exce", clientPort);
                sendFailedPortMessage(clientPort);
            }
            catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
                sendFailedPortMessage(clientPort);
            }
            catch (IOException exp) {
                Log.e("CLIENT_FAILED_EXCEPTION", clientPort);
                sendFailedPortMessage(clientPort);
            }
             finally {
                try{
                    socket.close();
                    stream.close();
                    socket.close();
                }
                catch (IOException e){
                    Log.e(TAG, "Error while disconnecting socket");
                }
            }

            return null;
        }
    }

    public static void sendFailedPortMessage(String remote) {
        Socket socket = null;
        OutputStream stream = null;
        try {

            DataOutputStream dos = null;

            String[] newPorts = new String[REMOTE_PORTS.length-1];
            int itr=0;
            for(int i=0; i<REMOTE_PORTS.length;i++) {
                if(!REMOTE_PORTS[i].equals(remote)) {
                    newPorts[itr++] = REMOTE_PORTS[i];
                }
            }

            // REMOTE_PORTS = newPorts;
            Log.i("New_Length_of_REMOTE", String.valueOf(REMOTE_PORTS.length));

            for(String PORT1 : newPorts) {
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(PORT1));
                stream = socket.getOutputStream();
                Message message1 = new Message(FAILED_MESSAGE, remote);
                String stringMessage1 = message1.getString();
                OutputStreamWriter out1 = new OutputStreamWriter(stream,
                        "UTF-8");

                dos = new DataOutputStream(stream);
                dos.writeUTF(stringMessage1);

                stream.flush();
                out1.flush();
                socket.close();
                dos.close();
                stream.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Message getProposedMessage(String message) {

        //Message proposedMessage = new Message();
        String[] splittedMessage = message.split(";");
        String messageType = splittedMessage[0].split(":")[1];
        String sender = splittedMessage[1].split(":")[1];
        String msg = splittedMessage[2].split(":")[1];
        String receiver = splittedMessage[3].split(":")[1];
        String proposedNumber = splittedMessage[4].split(":")[1];
        Message proposedMessage = new Message(messageType,msg,sender, receiver, Integer.valueOf(proposedNumber));
        return proposedMessage;
    }

    public static String displayProposedNumbers(ArrayList<Message> al) {
        StringBuilder sb = new StringBuilder();
        for(Message m : al) {
            sb.append(m.proposedNumber);
            sb.append("  ");
        }
        return sb.toString();
    }


    public static int getMaxProposedNumber (ArrayList<Message> set) {
        int max = Integer.MIN_VALUE;
        for(Message msg : set) {
            //Log.i("SET_VALUE", String.valueOf(num));
            max = Math.max(msg.proposedNumber, max);
        }
        return max;
    }
}
