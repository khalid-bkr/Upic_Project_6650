package Part2;

import Part1.RequestCounterBarrier;
import Part1.SkierLiftRide;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThreadClient {
    private static final String BasePath = "http://35.92.148.132:8080/SkierServer_war/Skier";
//  private static final int NUMBER_OF_THREADS = 64;
    private static int failCount = 0;
    private static final int MAX_REQS = 5;
  private static final int NUMBER_OF_REQUESTS = 10000;

  public static void main(String[] args) throws InterruptedException, ApiException {
    SkiersApi apiInstance = new SkiersApi();
    apiInstance.getApiClient().setBasePath(BasePath);
    AtomicInteger successCallCount = new AtomicInteger(0);
    AtomicInteger failCallCount = new AtomicInteger(0);
    final RequestCounterBarrier counter = new RequestCounterBarrier();
//    CountDownLatch completed = new CountDownLatch(NUMBER_OF_THREADS);
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < NUMBER_OF_REQUESTS; i++) {
        SkierLiftRide current_req = new SkierLiftRide();
//        ApiResponse<Void> res =  apiInstance.writeNewLiftRideWithHttpInfo(current_req.getLiftRide(), current_req.getResortID(), current_req.getSeasonID(), current_req.getDayID(), current_req.getSkierID());
//      new Part1.ClientThread(completed, counter, successCallCount, failCallCount).start();
        long a = System.currentTimeMillis();
        int current_try = 0;
        while (current_try++ < MAX_REQS) {
          ApiResponse<Void> res =  apiInstance.writeNewLiftRideWithHttpInfo(current_req.getLiftRide(), current_req.getResortID(), current_req.getSeasonID(), current_req.getDayID(), current_req.getSkierID());
          if (res.getStatusCode() == 200 || res.getStatusCode() == 201) {
            break;
          }
        }
      long b = System.currentTimeMillis();
      System.out.println((b - a));
        if (current_try >= MAX_REQS) {
          failCount++;
        }
  //        remove later
//        this.counter.inc();
    }
    successCallCount.getAndAdd(NUMBER_OF_REQUESTS - failCount);
    failCallCount.getAndAdd(failCount);
//    completed.await();
    long endTime = System.currentTimeMillis();
    double wallTime = (double)(endTime - startTime) /1000;
    System.out.println("Total REQS " + counter.getVal());
//    System.out.println();
    System.out.println("Start Time: " + TimeUnit.MILLISECONDS.toSeconds(startTime)  );
    System.out.println("End Time: " + TimeUnit.MILLISECONDS.toSeconds(endTime));
    System.out.println("Wall Time: " + wallTime);
    System.out.println("Number of Successful Calls: " + successCallCount.get());
    System.out.println("Number of Failed Calls: " + failCallCount.get());
    System.out.println("Throughput: " + (NUMBER_OF_REQUESTS/wallTime));
  }

}
