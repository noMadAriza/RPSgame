package com.RPS.utilities;

import java.util.LinkedList;

public class LinkedListPair {
    private LinkedList<?> list1;
    private LinkedList<?> list2;

    public LinkedListPair(LinkedList<?> list1, LinkedList<?>  list2) {
        this.list1 = list1;
        this.list2 = list2;
    }

    public LinkedList<?> getList1() {
        return list1;
    }

    public LinkedList<?> getList2() {
        return list2;
    }
}
