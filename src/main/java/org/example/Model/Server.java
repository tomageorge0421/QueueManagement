package org.example.Model;

import java.util.LinkedList;
import java.util.Queue;

import java.io.FileWriter;
import java.io.IOException;

public class Server extends Thread {
    private Queue<Task> tasks = new LinkedList<>();
    private String nameOfQueue;
    private boolean running = true;

    private FileWriter fw;

    public Server(String name,FileWriter fw) {
        this.nameOfQueue = name;
        this.fw = fw;
    }

    public int GetQueueLength() {
        return tasks.size();
    }

    public synchronized Task nextTask() throws InterruptedException {
        while (tasks.isEmpty() && running) {
            wait();
        }
        if (!tasks.isEmpty()) {
            return tasks.peek();
        } else {
            return null;
        }
    }


    public void terminate() {
        running = false;
        synchronized (this) {
            this.notifyAll();
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                Task task = nextTask();
                if (task != null) {
                    //Task task2 =tasks.element();
                    synchronized (fw){
                        System.out.println("Starting service for task " + task.getId() + " in queue " + nameOfQueue);
                        try {
                            fw.write("Starting service for task " + task.getId() + " in queue " + nameOfQueue + "\n");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    while(task.getServiceTime()!=0){
                        Thread.sleep(1000L);
                        task.setServiceTime(task.getServiceTime()-1);
                        this.totalServiceTime -=1;
                    }
                    synchronized (fw){
                        System.out.println("Client " + task.getId() + " has been served in queue " + nameOfQueue);
                        try {
                            fw.write("Client " + task.getId() + " has been served in queue " + nameOfQueue + "\n");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                tasks.remove(task);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int totalServiceTime = 0;

    public synchronized void addTask(Task task) {
        tasks.add(task);
        this.totalServiceTime += task.getServiceTime();
        notify();
    }

    public int getTotalServiceTime() {
        return totalServiceTime;
    }

    public String getNameOfQueue() {
        return nameOfQueue;
    }

    public Queue<Task> getTasks() {
        return tasks;
    }

    public void checkTasks(){
        if(this.tasks.isEmpty()) System.out.println("E GOL");
    }

    public Task getTopTask() {
        return tasks.element();
    }

    public Queue<Task> getWholeTasks() {
        Queue<Task> newTasks = new LinkedList<>();
        for(Task task : tasks)
            newTasks.add(task);
        return newTasks;
    }


    @Override
    public synchronized String toString() {
        String taskuri = "";
        for(Task index: tasks)
            taskuri = taskuri + index + "\n";
        return "Server{" +
                "tasks=" + taskuri +
                ", nameOfQueue='" + nameOfQueue + '\'' +
                ", running=" + running +
                ", totalServiceTime=" + totalServiceTime +
                "} " + super.toString();
    }
}
