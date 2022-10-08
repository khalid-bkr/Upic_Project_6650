package Part2;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CopyOnWriteArrayList;

public class CSVWriter {

  public static void givenDataArray_whenConvertToCSV_thenOutputCreated(
      CopyOnWriteArrayList<ResponseRecord> records) throws IOException {
    File csvOutputFile = new File("Res_records.csv");
    if(csvOutputFile.exists()) csvOutputFile.delete();
    try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
        pw.println("start_time,Request_type,latency,StatusCode" );
        records.forEach(record ->{pw.println(record.toString() ); });

    }
  }
  }