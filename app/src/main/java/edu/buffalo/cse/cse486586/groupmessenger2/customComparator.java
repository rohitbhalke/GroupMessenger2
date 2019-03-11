package edu.buffalo.cse.cse486586.groupmessenger2;

import java.util.Comparator;

public class customComparator implements Comparator<Message> {

    @Override
    public int compare(Message m1, Message m2) {
        if(m1.sequenceNumber == m2.sequenceNumber){
            // If sequence numbers are same then user sender ID
            return Integer.compare(Integer.valueOf(m1.sender), Integer.valueOf(m2.sender));
        }
        else {
            // return based on sequence number
            return Integer.compare(m1.sequenceNumber, m2.sequenceNumber);
        }
    }
}
