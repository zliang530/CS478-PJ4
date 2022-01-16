package com.example.edward_liang_project4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.Collections;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final int START_GAME = 0;
    public static final int INCREMENT = 0;
    public static final int SEND_TO_P2 = 1;
    public static final int SEND_TO_P1_RESULTS = 2;
    public static final int SEND_TO_P2_RESULTS = 3;
    public static final int PROCESS_GUESS = 4;
    public static final int P1_WIN= 5;
    public static final int P2_WIN = 6;

    int p1Guess;
    int p2Guess;

    PlayerOne p1;
    PlayerTwo p2;

    Handler h1;
    Handler h2;

    Button newGame;
    TextView tv1;
    TextView tv2;

    int counter;

    ListView mListView1;
    ListView mListView2;
    ArrayAdapter <String>a1;
    ArrayAdapter <String>a2;

    int moves;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            Message myMsg;
            Toast toast;
            switch (msg.what) {
                case INCREMENT:
                    counter++;

                    if(counter == 2){
                        Message msgStart = h1.obtainMessage(START_GAME) ;
                        h1.sendMessage(msgStart) ;
                    }
                    break;
                case SEND_TO_P2:

                    a1.add("Player 1 Guessed " + b.getInt("guess"));

                    myMsg = h2.obtainMessage(PROCESS_GUESS);
                    myMsg.setData(b);

                    h2.sendMessage(myMsg);
                    break;
                case SEND_TO_P1_RESULTS:

                    if(b.getInt("correct") != 0){
                        a1.add("Number Of Correct Digit & Correct Index: " + b.getInt("correct"));
                    }
                    if(b.getInt("partial") != 0){
                        a1.add("Number Of Correct Digit & Wrong Index: " + b.getInt("partial"));
                    }
                    if(b.getInt("incorrect") != -1){
                        a1.add("Missed Digit: " + b.getInt("incorrect"));
                    }

                    a2.add("Player 2 Guessed " + b.getInt("guess"));

                    b.putInt("processed", 1);
                    myMsg = h1.obtainMessage(PROCESS_GUESS);
                    myMsg.setData(b);
                    h1.sendMessage(myMsg);
                    break;
                case SEND_TO_P2_RESULTS:

                    if(b.getInt("correct") != 0){
                        a2.add("Number Of Correct Digit & Correct Index: " + b.getInt("correct"));
                    }
                    if(b.getInt("partial") != 0){
                        a2.add("Number Of Correct Digit & Wrong Index: " + b.getInt("partial"));
                    }
                    if(b.getInt("incorrect") != -1){
                        a2.add("Number Of Missed Digit: " + b.getInt("incorrect"));
                    }

                    moves++;
                    if (moves == 20){
                        toast = Toast.makeText(getApplicationContext(), "Players Reached Move Limit, Start New Game?", Toast.LENGTH_LONG);
                        toast.show();
                        break;
                    }

                    a1.add("Player 1 Guessed " + b.getInt("guess"));

                    b.putInt("processed", 1);
                    myMsg = h2.obtainMessage(PROCESS_GUESS);
                    myMsg.setData(b);
                    h2.sendMessage(myMsg);
                    break;
                case P1_WIN:
                    toast = Toast.makeText(getApplicationContext(), "Player One Win!", Toast.LENGTH_LONG);
                    toast.show();
                    break;
                case P2_WIN:
                    toast = Toast.makeText(getApplicationContext(), "Player Two Win", Toast.LENGTH_LONG);
                    toast.show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newGame = (Button)findViewById(R.id.NewGame);
        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);

        mListView1 = (ListView) findViewById(R.id.list1);
        mListView2 = (ListView) findViewById(R.id.list2);

        resetList();

        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moves=0;
                counter=0;
                resetList();

                p1 = new PlayerOne(mHandler);
                p2 = new PlayerTwo(mHandler);

                // set the random numbers generated
                tv1.setText(p1.getNum().toString());
                tv2.setText(p2.getNum().toString());

                // start the threads
                p1.start();
                p2.start();

            }
        });

    }

    // one of the methods guess for the correct number
    // guess is what the player guessed
    // revealed is what the other player revealed as incorrect if any
    static int randomScramblerMethod(int guess, ArrayList<Integer> revealed){

        String[] g = String.valueOf(guess).split("");
        ArrayList<Integer> digit = new ArrayList<>();
        int k=0;
        while (k<4){
            digit.add(Integer.parseInt(g[k]));
            k++;
        }

        if (revealed.size() == 0){
            Collections.shuffle(digit);
        }
        else{
            ArrayList<Integer> indexGoodDigit = new ArrayList<>();
            ArrayList<Integer> goodDigit = new ArrayList<>();
            for (int i =0; i<4; i++){
                for (int j =0; j<revealed.size(); j++){
                    if (digit.get(i) == revealed.get(j)){

                        break;
                    }
                    else if (j == revealed.size()-1){

                        indexGoodDigit.add(i);
                        goodDigit.add(digit.get(i));
                    }
                }
            }
            Collections.shuffle(goodDigit);
            for (int i=0; i<goodDigit.size(); i++){
                digit.set(indexGoodDigit.get(i), goodDigit.get(i));
            }

            Integer[] n = {0,1,2,3,4,5,6,7,8,9};
            ArrayList<Integer> num = new ArrayList<>();
            for(Integer number : n){
                num.add(number);
            }
            for (int i=0; i<revealed.size(); i++){
                num.remove(Integer.valueOf(revealed.get(i)));
            }
            for (Integer val: digit){
                num.remove(Integer.valueOf(val));
            }
            for (int i=0; i<revealed.size(); i++){
                int randomIndex = new Random().nextInt(num.size());

                if(digit.indexOf(Integer.valueOf(revealed.get(i))) == 0 ){
                    while(num.get(randomIndex) == 0){
                        randomIndex = new Random().nextInt(num.size());
                    }
                }
                digit.set(digit.indexOf(Integer.valueOf(revealed.get(i))), num.get(randomIndex));
                num.remove(num.get(randomIndex));
            }

        }
        return (digit.get(0) * 1000) + (digit.get(1) * 100) + (digit.get(2) * 10) + (digit.get(3) * 1);
    }
    void resetList(){
        a1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new ArrayList<>());
        mListView1.setAdapter(a1);

        a2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new ArrayList<>());
        mListView2.setAdapter(a2);
    }

    /*
        compare a players guessed answer and actual answer and return a size 1 array containing
        arr[0] == num guessed correctly & in correct position
        arr[1] == num guessed correctly & in wrong position
        arr[2] == num guessed incorrectly and randomly chose to be shown back to the player
    */
    int[] processGuess(int testX, int testY){
        // turn int into strings and splitting them for easier handling
        String[] arrX = String.valueOf(testX).split("");
        String[] arrY = String.valueOf(testY).split("");

        // result array to be returned
        int[] results = new int[3];

        ArrayList<Integer> incorrect = new ArrayList<>();
        results[0] = 0;
        results[1] = 0;
        results[2] = -1;

        // find number of correctly guessed numbers in the right/wrong position
        for (int i=0; i<4; i++){
            for(int j=0; j<4; j++){
                if (i==j  && arrX[j].equals(arrY[i])){
                    results[0]= results[0] + 1;
                }
                else if(arrX[j].equals(arrY[i])){
                    results[1]= results[1] + 1;
                }
            }
        }

        // find incorrect numbers
        boolean exist;
        for (int i=0; i<4; i++){
            exist = false;
            for(int j = 0; j<4 && !exist; j++){
                if(arrY[j].equals(arrX[i])){
                    exist = true;
                }
                if (j==3 && exist==false){
                    incorrect.add(Integer.parseInt(arrX[i]));
                }
            }
        }

        int max = incorrect.size();
        if (max != 0){
            int randomIndex = new Random().nextInt(max - 0) + 0;
            results[2] = incorrect.get(randomIndex);
        }

        return results;

    }


    Integer generateNumber(){
        int thousand=0;
        int hundred=0;
        int ten=0;
        int one=0;
        int max = 9;

        int randomIndex;
        ArrayList<Integer> num = new ArrayList<>(Arrays.asList(0,1,2,3,4,5,6,7,8,9));

        for(int i = 0; i<4; i++){
            randomIndex = new Random().nextInt(max - 0) + 0;
            if(i==0){
                while(randomIndex == 0){
                    randomIndex = new Random().nextInt(max - 0) + 0;
                }
                thousand = num.get(randomIndex);
            }
            else if(i==1){
                hundred = num.get(randomIndex);
            }
            else if(i==2){
                ten = num.get(randomIndex);
            }
            else{
                one = num.get(randomIndex);
            }
            num.remove(randomIndex);
            max--;
        }

        return new Integer(((1000 * thousand) + (100 * hundred) + (10 * ten) + (one)));
    }

    class PlayerOne extends Thread{
        Handler mainHandler;
        Integer p1Num;
        ArrayList<Integer> badNum;
        PlayerOne(Handler h){
            p1Num = generateNumber();
            mainHandler = h;
            badNum = new ArrayList<>();
        }

        Integer getNum(){
            return p1Num;
        }
        @Override
        public void run() {
            Looper.prepare();
            h1 = new Handler(Looper.myLooper()){
                public void handleMessage(Message msg){
                    Bundle b = msg.getData();
                    Message myMsg;
                    switch (msg.what) {
                        case START_GAME:
                            b = new Bundle();
                            p1Guess = generateNumber();
                            b.putInt("guess", p1Guess);
                            Message messageToPlayer = mainHandler.obtainMessage(SEND_TO_P2);
                            messageToPlayer.setData(b);
                            mainHandler.sendMessage(messageToPlayer);
                            break;
                        case PROCESS_GUESS:
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                            }

                            int[] results = processGuess(b.getInt("guess"), p1Num);

                            if(results[0] == 4){
                                myMsg = mainHandler.obtainMessage(P2_WIN);
                                myMsg.setData(b);
                                mainHandler.sendMessage(myMsg);
                                break;
                            }
                            b = new Bundle();

                            // strategy:
                            // first ever move is to generate a random number
                            // afterwards use the function below
                            // this function keeps track of the number of the missed numbers
                            if (b.containsKey("processed")){
                                badNum.add(Integer.valueOf(results[2]));
                                p1Guess = randomScramblerMethod(p1Guess,badNum);
                            }
                            else{
                                p1Guess = generateNumber();
                            }

                            b.putInt("guess", p1Guess);
                            b.putInt("correct", results[0]);
                            b.putInt("partial", results[1]);
                            b.putInt("incorrect", results[2]);

                            myMsg = mainHandler.obtainMessage(SEND_TO_P2_RESULTS);
                            myMsg.setData(b);
                            mainHandler.sendMessage(myMsg);
                            break;
                    }
                }
            };
            Message msg = mainHandler.obtainMessage(INCREMENT) ;
            mainHandler.sendMessage(msg) ;

            Looper.loop();
        }
    }
    class PlayerTwo extends Thread{
        Integer p2Num;
        Handler mainHandler;

        PlayerTwo(Handler h){
            p2Num = generateNumber();
            mainHandler = h;

        }

        Integer getNum(){
            return p2Num;
        }

        @Override
        public void run() {
            Looper.prepare();
            h2 = new Handler(Looper.myLooper()){
                public void handleMessage(Message msg){
                    Bundle b = msg.getData();
                    Message myMsg;
                    switch (msg.what) {
                        case PROCESS_GUESS:
                            try {
                                Thread.sleep(2000);
                            }
                            catch (InterruptedException e) {
                            }

                            int[] results = processGuess(b.getInt("guess"), p2Num);

                            if(results[0] == 4){
                                myMsg = mainHandler.obtainMessage(P1_WIN);
                                myMsg.setData(b);
                                mainHandler.sendMessage(myMsg);
                                break;
                            }

                            b = new Bundle();

                            // strategy
                            // keep generating random number until hitting the jackpot
                            p2Guess = generateNumber();

                            b.putInt("guess", p2Guess);
                            b.putInt("correct", results[0]);
                            b.putInt("partial", results[1]);
                            b.putInt("incorrect", results[2]);

                            myMsg = mainHandler.obtainMessage(SEND_TO_P1_RESULTS);
                            myMsg.setData(b);
                            mainHandler.sendMessage(myMsg);
                            break;
                    }
                }
            };
            Message msg = mainHandler.obtainMessage(INCREMENT) ;
            mainHandler.sendMessage(msg) ;

            Looper.loop();
        }
    }
}