package Part2;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Runner {

//  private static final String BasePath = "http://54.185.15.173:8080/SkierServer_war/Skier";
  private static final int NUMBER_OF_THREADS = 100;
  private static final CopyOnWriteArrayList<ResponseRecord> responseRecords = new CopyOnWriteArrayList<ResponseRecord>();
  private static final CopyOnWriteArrayList<List<ResponseRecord>> listOfRecords = new CopyOnWriteArrayList<>();
  private static final CopyOnWriteArrayList<Integer> responseTimes = new CopyOnWriteArrayList<Integer>();

  public static void main(String[] args) throws InterruptedException, IOException {
//    apiInstance.getApiClient().setBasePath(BasePath);
    AtomicInteger successCallCount = new AtomicInteger(0);
    AtomicInteger failCallCount = new AtomicInteger(0);
    CountDownLatch completed = new CountDownLatch(NUMBER_OF_THREADS);
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < NUMBER_OF_THREADS; i++) {

      new ClientThread(completed, successCallCount, failCallCount, listOfRecords).start();
    }
    completed.await();

    long endTime = System.currentTimeMillis();
    recordHandler(listOfRecords);
    double wallTime = (double)(endTime - startTime) /1000;

    Collections.sort(responseTimes);
    int median = getMedian(responseTimes);
    int minRes = responseTimes.get(0);
    int maxRes = responseTimes.get(responseTimes.size() - 1);

    double mean = getMean(responseTimes);
    double p99 = getp99(responseTimes);
    CSVWriter.givenDataArray_whenConvertToCSV_thenOutputCreated(responseRecords);

    System.out.println("minRes Time: " + minRes + "ms");
    System.out.println("maxRes Time: " + maxRes + "ms");
    System.out.println("median Time: " + median + "ms");
    System.out.println("mean Time: " + mean + "ms");
    System.out.println("P99 Time: " + p99 + "ms");

    System.out.println("Start Time: " + startTime);
    System.out.println("End Time: " + endTime);
    System.out.println("Wall Time: " + wallTime + " seconds");
    System.out.println("Number of Successful Calls: " + successCallCount.get());
    System.out.println("Number of Failed Calls: " + failCallCount.get());
    System.out.println("Throughput: " + ((NUMBER_OF_THREADS * ClientThread.NUMBER_OF_REQS)/wallTime) + " Request/Second");
  }

  public static int getMedian(CopyOnWriteArrayList<Integer> responseTimes) {
    int middle = responseTimes.size() / 2;
    middle = middle > 0 && middle % 2 == 0 ? middle - 1 : middle;
    int median = responseTimes.get(middle);
    return median;
  }

  public static double getMean(CopyOnWriteArrayList<Integer> responseTimes) {
    int total_sum = 0;
    for (int k = 0; k < responseTimes.size(); k++) {
      total_sum += responseTimes.get(k);
    }
    double mean = (double) (total_sum/responseTimes.size());
    return mean;
  }

  public static double getp99(CopyOnWriteArrayList<Integer> responseTimes) {
    int p99_size = responseTimes.size() - (int)((NUMBER_OF_THREADS * ClientThread.NUMBER_OF_REQS )* ((double)1/100));
    int p1_size = (int)((NUMBER_OF_THREADS * ClientThread.NUMBER_OF_REQS )* ((double)1/100));
    int total_p99_sum = 0;
    for (int k = p99_size; k < responseTimes.size(); k++) {
      total_p99_sum += responseTimes.get(k);
    }
    double p99 = (double) (total_p99_sum/p1_size);
    return p99;
  }

  public static void recordHandler(CopyOnWriteArrayList<List<ResponseRecord>> listOfRecords) {
    listOfRecords.forEach(responseRecords::addAll);
    responseRecords.forEach(record -> {
      responseTimes.add(record.getLatency());
    });
  }
}