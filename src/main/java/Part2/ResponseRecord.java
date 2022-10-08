package Part2;

public class ResponseRecord {
  private long startTime;
  private String postType;
  private int latency;
  private int responseCode;

  public ResponseRecord(long startTime, String postType, int latency, int responseCode) {
    this.startTime = startTime;
    this.postType = postType;
    this.latency = latency;
    this.responseCode = responseCode;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public String getPostType() {
    return postType;
  }

  public void setPostType(String postType) {
    this.postType = postType;
  }

  public int getLatency() {
    return latency;
  }

  public void setLatency(int latency) {
    this.latency = latency;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(int responseCode) {
    this.responseCode = responseCode;
  }

  public String[] getString() {
    return new String[] {
        String.valueOf(this.startTime), this.postType, String.valueOf(this.latency), String.valueOf(responseCode)
    };
  }

  @Override
  public String toString() {
    return startTime + ","
         + postType + "," + latency + ","
         + responseCode;
  }
}
