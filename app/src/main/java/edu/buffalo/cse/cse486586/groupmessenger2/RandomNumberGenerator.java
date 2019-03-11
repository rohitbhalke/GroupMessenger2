package edu.buffalo.cse.cse486586.groupmessenger2;

public class RandomNumberGenerator {
    public static int min = 1;
    public int getRandomNumber() {
        int range = 10000;
        int uniqueId = (int)Math.random() * range + min;
        min++;
        return uniqueId;
    }
}