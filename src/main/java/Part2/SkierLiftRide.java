package Part2;

import io.swagger.client.model.LiftRide;
import java.util.concurrent.ThreadLocalRandom;

public class SkierLiftRide {

  private final int skierID;
  private final int resortID;
  private final int liftID;
  private final String seasonID;
  private final String dayID;
  private final int time;
  private final LiftRide liftRide;

  public SkierLiftRide() {
    this.skierID = ThreadLocalRandom.current().nextInt(1, 100000);
    this.resortID = ThreadLocalRandom.current().nextInt(1, 10);
    this.liftID = ThreadLocalRandom.current().nextInt(1,40);
    this.seasonID = "2022";
    this.dayID = "1";
    this.time = ThreadLocalRandom.current().nextInt(1, 360);
    this.liftRide = new LiftRide();
    liftRide.setTime(this.time);
    liftRide.setLiftID(this.liftID);
  }

  public LiftRide getLiftRide() {
    return liftRide;
  }

  public int getSkierID() {
    return skierID;
  }

  public int getResortID() {
    return resortID;
  }

  public int getLiftID() {
    return liftID;
  }

  public String getSeasonID() {
    return seasonID;
  }

  public String getDayID() {
    return dayID;
  }

  public int getTime() {
    return time;
  }
}
