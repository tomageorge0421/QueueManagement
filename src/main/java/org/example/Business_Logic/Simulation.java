package org.example.Business_Logic;

import org.example.GUI.SecondInterface;
import org.example.Model.Server;
import org.example.Model.Task;

import java.sql.SQLOutput;
import java.util.*;

import java.io.FileWriter;
import java.io.IOException;

public class Simulation implements Strategy {
    private static int simulationTimeMax;
    private static int currentTime = 0;
    private static int numberOfClients;
    private static int numberOfQueues;
    private static int arrivalTimeMin;
    private static int arrivalTimeMax;
    private static int serviceTimeMin;
    private static int serviceTimeMax;

    private static int bifa;

    private static final List<Server> serverLanes = new ArrayList<>();
    private static final PriorityQueue<Task> taskQueue = new PriorityQueue<>(Comparator.comparingInt(Task::getArrivalTime));
    private static final Random random = new Random();

    private static SecondInterface i2;

    private double avgServiceTime = 0;

    private int peakHour = 0;

    private int numberOfPersonsForWaiting = 0;

    private int SumWaitingTime = 0;

    private int longestDimension = -1;

    private FileWriter fw;

    {
        try {
            fw = new FileWriter("output1.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Simulation(SecondInterface i2, int simulationTimeMax, int bifa, int numberOfClients, int numberOfQueues, int arrivalTimeMin, int arrivalTimeMax, int serviceTimeMin, int serviceTimeMax) {
        this.i2 = i2;
        this.simulationTimeMax = simulationTimeMax;
        this.bifa = bifa;
        this.numberOfClients = numberOfClients;
        this.numberOfQueues = numberOfQueues;
        this.arrivalTimeMin = arrivalTimeMin;
        this.arrivalTimeMax = arrivalTimeMax;
        this.serviceTimeMin = serviceTimeMin;
        this.serviceTimeMax = serviceTimeMax;
    }

    public void Simulate() throws InterruptedException, IOException {
        //SecondInterface i2 = new SecondInterface(this.numberOfClients,this.numberOfQueues,this.bifa, this.simulationTimeMax, this.arrivalTimeMin, this.arrivalTimeMax, this.serviceTimeMin,this.serviceTimeMax);
        for (int i = 0; i < this.numberOfQueues; i++) {
            Server lane = new Server(String.valueOf(i), fw);
            this.serverLanes.add(lane);
            lane.start();
        }
        for (int i = 0; i < this.numberOfClients; i++) {
            int arrivalTime = random.nextInt(this.arrivalTimeMax - this.arrivalTimeMin + 1) + this.arrivalTimeMin;
            int serviceTime = random.nextInt(this.serviceTimeMax - this.serviceTimeMin + 1) + this.serviceTimeMin;
            this.taskQueue.add(new Task(i + 1, arrivalTime, serviceTime));
            System.out.println("Generated client " + (i + 1) + " with arrival time " + arrivalTime + " and service time " + serviceTime);
            fw.write("Generated client " + (i + 1) + " with arrival time " + arrivalTime + " and service time " + serviceTime + "\n");
        }
        i2.updateClienti(this.taskQueue);

        while (this.currentTime <= this.simulationTimeMax) {
            while (!this.taskQueue.isEmpty() && this.taskQueue.peek().getArrivalTime() <= this.currentTime) {
                Task client = this.taskQueue.poll();
                assignTaskToQueue(client);
            }
            Thread.sleep(1000);
            System.out.println(this.currentTime);
            fw.write(this.currentTime + "\n");
            this.currentTime++;
            for (Server lane : serverLanes) {
                Queue<Task> wholeTasks = lane.getWholeTasks();
                    /*for(Task task : wholeTasks)
                        System.out.println(task);*/
                System.out.println(wholeTasks);
                fw.write(wholeTasks + "\n");
            }
        }

        for (Server lane : this.serverLanes) {
            lane.terminate();
        }

        for (Server lane : this.serverLanes) {
            lane.join();
        }
        System.out.println("Simulation ended.");
        System.out.println("AVERAGE SERVICE TIME " + this.avgServiceTime / this.numberOfClients);
        System.out.println("PEAK HOUR " + this.peakHour);
        System.out.println("WAITING TIME IS " + this.SumWaitingTime);
        System.out.println("PERSONS FOR WATING " + this.numberOfPersonsForWaiting);
        if (this.numberOfPersonsForWaiting != 0 && this.SumWaitingTime != 0)
            System.out.println("AVERAGE WAITING TIME " + (double) this.SumWaitingTime / this.numberOfPersonsForWaiting);
        else System.out.println("AVERAGE WAITING TIME " + 0);
        fw.write("Simulation ended." + "\n");
        fw.write("AVERAGE SERVICE TIME " + this.avgServiceTime / this.numberOfClients + "\n");
        fw.write("PEAK HOUR " + this.peakHour + "\n");
        fw.write("WAITING TIME IS " + this.SumWaitingTime + "\n");
        fw.write("PERSONS FOR WATING " + this.numberOfPersonsForWaiting + "\n");
        if (this.numberOfPersonsForWaiting != 0 && this.SumWaitingTime != 0)
            fw.write("AVERAGE WAITING TIME " + (double) this.SumWaitingTime / this.numberOfPersonsForWaiting + "\n");
        else fw.write("AVERAGE WAITING TIME " + 0 + "\n");
        fw.close();
    }

    private synchronized void assignTaskToQueue(Task task) throws IOException {//aici trebe modificat pt strategy
        //bifa = 0 shortest time
        //bifa = 1 shortest queue
        if (this.bifa == 0) {
            Server selectedServer = serverLanes.get(0);
            int temporaryLongestDimension = selectedServer.GetQueueLength();
            int temporaryPeakHour = getCurrentTime();
            if (this.longestDimension < temporaryLongestDimension) this.longestDimension = temporaryLongestDimension;
            Queue<Task> coada = selectedServer.getWholeTasks();
            int bestTime = 0;
            for (Task client : coada) {
                bestTime = bestTime + client.getServiceTime();
            }
            for (Server lane : serverLanes) {
                //int laneScore = lane.getTotalServiceTime();
                Queue<Task> coada2 = null;
                if (lane != serverLanes.get(0)) coada2 = lane.getWholeTasks();
                else continue;
                int lineTime = 0;
                for (Task client2 : coada2) {
                    lineTime = lineTime + client2.getServiceTime();
                }
                int verifyLongerDimension = lane.GetQueueLength();
                if (this.longestDimension < verifyLongerDimension) {
                    this.longestDimension = verifyLongerDimension;
                    temporaryPeakHour = getCurrentTime();
                }
            }
            addTaskTime(serverLanes, task);
            i2.updateQueues(this.serverLanes);
        } else if (this.bifa == 1) {
            Server selectedServer = serverLanes.get(0);
            int temporaryLongestDimension = selectedServer.GetQueueLength();
            int temporaryPeakHour = getCurrentTime();
            int bestScore = selectedServer.GetQueueLength();
            if (this.longestDimension < temporaryLongestDimension) this.longestDimension = temporaryLongestDimension;
            for (Server lane : serverLanes) {
                int laneScore = lane.GetQueueLength();
                int verifyLongerDimension = lane.GetQueueLength();
                if (this.longestDimension < verifyLongerDimension) {
                    this.longestDimension = verifyLongerDimension;
                    temporaryPeakHour = getCurrentTime();
                }
            }
            addTaskQueue(serverLanes, task);
            i2.updateQueues(this.serverLanes);
        }
    }

    public static int getCurrentTime() {
        return currentTime;
    }

    @Override
    public void addTaskTime(List<Server> serverLanes, Task task) {
        Server selectedServer = serverLanes.get(0);
        int temporaryLongestDimension = selectedServer.GetQueueLength();
        int temporaryPeakHour = getCurrentTime();
        if (this.longestDimension < temporaryLongestDimension) this.longestDimension = temporaryLongestDimension;
        Queue<Task> coada = selectedServer.getWholeTasks();
        int bestTime = 0;
        for (Task client : coada) {
            bestTime = bestTime + client.getServiceTime();
        }
        for (Server lane : serverLanes) {Queue<Task> coada2 = null;
            if (lane != serverLanes.get(0)) coada2 = lane.getWholeTasks();
            else continue;
            int lineTime = 0;
            for (Task client2 : coada2) {
                lineTime = lineTime + client2.getServiceTime();
            }
            int verifyLongerDimension = lane.GetQueueLength();
            if (lineTime < bestTime) {
                selectedServer = lane;
                bestTime = lineTime;
            }
            if (this.longestDimension < verifyLongerDimension) {
                this.longestDimension = verifyLongerDimension;
                temporaryPeakHour = getCurrentTime();
            }
        }
        selectedServer.addTask(task);
        synchronized (fw) {
            System.out.println("Client " + task.getId() + " " + task.getArrivalTime() + " " + task.getServiceTime() + " assigned to queue " + selectedServer.getNameOfQueue());
            try {
                fw.write("Client " + task.getId() + " " + task.getArrivalTime() + " " + task.getServiceTime() + " assigned to queue " + selectedServer.getNameOfQueue() + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        this.avgServiceTime += task.getServiceTime();
        this.peakHour = temporaryPeakHour;
        Server selectedServerAux = selectedServer;
        if (selectedServerAux.getTopTask() != null && selectedServerAux.GetQueueLength() > 1) {
            int ctr = 0;
            int suma = 0;
            int waitingTime = 0;
            Queue<Task> firstClient = selectedServerAux.getWholeTasks();
            for (Task client : firstClient) {
                if (client != task) {
                    waitingTime = waitingTime + client.getServiceTime();
                } else break;
            }
            this.SumWaitingTime += waitingTime;
            this.numberOfPersonsForWaiting += 1;
        }
    }

    @Override
    public void addTaskQueue(List<Server> serverLanes, Task task) {
        Server selectedServer = serverLanes.get(0);
        int temporaryLongestDimension = selectedServer.GetQueueLength();
        int temporaryPeakHour = getCurrentTime();
        int bestScore = selectedServer.GetQueueLength();
        if (this.longestDimension < temporaryLongestDimension) this.longestDimension = temporaryLongestDimension;
        for (Server lane : serverLanes) {
            int laneScore = lane.GetQueueLength();
            int verifyLongerDimension = lane.GetQueueLength();
            if (laneScore < bestScore) {
                selectedServer = lane;
                bestScore = laneScore;
            }
            if (this.longestDimension < verifyLongerDimension) {
                this.longestDimension = verifyLongerDimension;
                temporaryPeakHour = getCurrentTime();
            }
        }
        selectedServer.addTask(task);
        synchronized (fw) {
            System.out.println("Client " + task.getId() + " " + task.getArrivalTime() + " " + task.getServiceTime() + " assigned to queue " + selectedServer.getNameOfQueue());
            try {
                fw.write("Client " + task.getId() + " " + task.getArrivalTime() + " " + task.getServiceTime() + " assigned to queue " + selectedServer.getNameOfQueue() + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.avgServiceTime += task.getServiceTime();
            this.peakHour = temporaryPeakHour;
            Server selectedServerAux = selectedServer;
            if (selectedServerAux.getTopTask() != null && selectedServerAux.GetQueueLength() > 1) {
                int ctr = 0;
                int suma = 0;
                int waitingTime = 0;
                Queue<Task> firstClient = selectedServerAux.getWholeTasks();
                for (Task client : firstClient) {
                    if (client != task) {
                        waitingTime = waitingTime + client.getServiceTime();
                    } else break;
                }
                this.SumWaitingTime += waitingTime;
                this.numberOfPersonsForWaiting += 1;
            }
        }
    }
}
