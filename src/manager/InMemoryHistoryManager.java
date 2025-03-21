package manager;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InMemoryHistoryManager implements HistoryManager {
    Node head;
    Node tail;
    private final Map<Integer, Node> history;


    public InMemoryHistoryManager() {
        history = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        remove(task.getId());
        Node newNode = linkLast(task);
        history.put(task.getId(), newNode);
    }

    @Override
    public void remove(int id) {
        Node nodeToRemove = history.get(id);
        if (nodeToRemove != null) {
            if (nodeToRemove.prev == null) {
                head = nodeToRemove.next;
                if (head != null) {
                    head.prev = null;
                }
            } else {
                nodeToRemove.prev.next = nodeToRemove.next;
            }

            if (nodeToRemove.next == null) {
                tail = nodeToRemove.prev;
                if (tail != null) {
                    tail.next = null;
                }
            } else {
                nodeToRemove.next.prev = nodeToRemove.prev;
            }

            history.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> historyList = new ArrayList<>();
        Node current = head;
        while (current != null) {
            historyList.add(current.task);
            current = current.next;
        }
        return historyList;
    }

    private Node linkLast(Task task) {
        Node newNode = new Node(task);
        if (tail == null) {
            head = newNode;
        } else {
            newNode.prev = tail;
            tail.next = newNode;
        }
        tail = newNode;
        return newNode;
    }

    class Node {
        Task task;
        private Node prev;
        private Node next;

        Node(Task task) {
            this.task = task;
        }

        Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }
}