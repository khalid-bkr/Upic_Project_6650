package Part1;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientThread extends Thread {

  private final int MAX_REQS = 5;
  public static final int NUMBER_OF_REQS = 2000;
  private static final String BasePath = "http://LB1-51c29d36fedf641a.elb.us-west-2.amazonaws.com/testServerSkier_war/Skier/";
//  private static final String BasePath = "http://34.221.200.14:8080/testServerSkier_war/Skier/";
//  private static final String BasePath = "http://localhost:8080/testServerSkier_war/Skier/";
  private final SkiersApi apiInstance;
  private final CountDownLatch completed;

  private final AtomicInteger successCallCount;
  private final AtomicInteger failCallCount;
  @Override
  public void run() {
    try {
      apiInstance.getApiClient().setBasePath(BasePath);
      int failCount = 0;
      for (int i = 0; i < NUMBER_OF_REQS; i++) {
        SkierLiftRide current_req = new SkierLiftRide();
        int current_try = 0;
        long latency_start = System.currentTimeMillis();
        while (current_try++ < MAX_REQS) {
          ApiResponse<Void> res =  apiInstance.writeNewLiftRideWithHttpInfo(current_req.getLiftRide(), current_req.getResortID(), current_req.getSeasonID(), current_req.getDayID(), current_req.getSkierID());
          if (res.getStatusCode() == 200 || res.getStatusCode() == 201) {
            break;
          }
        }
        if (current_try >= MAX_REQS) {
          failCount++;
        }
        long latency_end = System.currentTimeMillis();
        System.out.println(latency_end - latency_start);
      }
      successCallCount.getAndAdd(NUMBER_OF_REQS - failCount);
      failCallCount.getAndAdd(failCount);
      completed.countDown();
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  public ClientThread(CountDownLatch completed, AtomicInteger successCallCount, AtomicInteger failCallCount) {
    apiInstance = new SkiersApi();
    apiInstance.getApiClient().setBasePath(BasePath);
    this.completed = completed;
    this.successCallCount = successCallCount;
    this.failCallCount = failCallCount;
  }
}
