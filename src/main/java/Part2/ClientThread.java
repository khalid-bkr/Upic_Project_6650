package Part2;

import Part1.SkierLiftRide;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientThread extends Thread {

  private final int MAX_REQS = 5;
  public static final int NUMBER_OF_REQS = 2000;
  private static final String BasePath = "http://54.185.15.173:8080/SkierServer_war/Skier";
  private final SkiersApi apiInstance;
  private final CountDownLatch completed;
  private final AtomicInteger successCallCount;
  private final AtomicInteger failCallCount;


//  private final CopyOnWriteArrayList<ResponseRecord> responseRecords;
//  private final CopyOnWriteArrayList<Integer> responseTimes;
  private final List<ResponseRecord> responseRecords = new ArrayList<>();
  private CopyOnWriteArrayList<List<ResponseRecord>> listOfRecords = new CopyOnWriteArrayList<>();
  @Override
  public void run() {
    try {
      apiInstance.getApiClient().setBasePath(BasePath);
      int failCount = 0;
      for (int i = 0; i < NUMBER_OF_REQS; i++) {
        SkierLiftRide current_req = new SkierLiftRide();
        int current_try = 0;
        long latency_start = System.currentTimeMillis();
        int resCode = 500;
        while (current_try++ < MAX_REQS) {
          ApiResponse<Void> res =  apiInstance.writeNewLiftRideWithHttpInfo(current_req.getLiftRide(), current_req.getResortID(), current_req.getSeasonID(), current_req.getDayID(), current_req.getSkierID());
          resCode = res.getStatusCode();
          if (res.getStatusCode() == 200 || res.getStatusCode() == 201) {
            break;
          }
        }
        if (current_try >= MAX_REQS) {
          failCount++;
        }
        long latency_end = System.currentTimeMillis();
        ResponseRecord resRecord = new ResponseRecord(latency_start, "POST", (int)(latency_end - latency_start), resCode);
//        responseTimes.add((int)(latency_end - latency_start));
        responseRecords.add(resRecord);
        System.out.println(latency_end - latency_start);
      }
      successCallCount.getAndAdd(NUMBER_OF_REQS - failCount);
      failCallCount.getAndAdd(failCount);
      listOfRecords.add(responseRecords);
      completed.countDown();
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  public ClientThread(CountDownLatch completed, AtomicInteger successCallCount, AtomicInteger failCallCount, CopyOnWriteArrayList<List<ResponseRecord>> listOfRecords) {
    apiInstance = new SkiersApi();
    apiInstance.getApiClient().setBasePath(BasePath);
    this.completed = completed;
    this.successCallCount = successCallCount;
    this.failCallCount = failCallCount;
    this.listOfRecords = listOfRecords;
  }
}
