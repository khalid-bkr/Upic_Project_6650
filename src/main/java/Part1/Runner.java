package Part1;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Runner {

//  private static final String BasePath = "http://54.185.15.173:8080/SkierServer_war/Skier";
  private static final int NUMBER_OF_THREADS = 100;

  public static void main(String[] args) throws InterruptedException {
//    SkiersApi apiInstance = new SkiersApi();
//    apiInstance.getApiClient().setBasePath(BasePath);
    AtomicInteger successCallCount = new AtomicInteger(0);
    AtomicInteger failCallCount = new AtomicInteger(0);
    final RequestCounterBarrier counter = new RequestCounterBarrier();
    CountDownLatch completed = new CountDownLatch(NUMBER_OF_THREADS);
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < NUMBER_OF_THREADS; i++) {

      new ClientThread(completed, successCallCount, failCallCount).start();
    }
    completed.await();
    long endTime = System.currentTimeMillis();
    double wallTime = (double)(endTime - startTime) /1000;
//    System.out.println();
    System.out.println("Start Time: " + startTime  );
    System.out.println("End Time: " + endTime);
    System.out.println("Wall Time: " + wallTime + " seconds");
    System.out.println("Number of Successful Calls: " + successCallCount.get());
    System.out.println("Number of Failed Calls: " + failCallCount.get());
    System.out.println("Throughput: " + (NUMBER_OF_THREADS * ClientThread.NUMBER_OF_REQS/wallTime) + " Request/second");
  }
}