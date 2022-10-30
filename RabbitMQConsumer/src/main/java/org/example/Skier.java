package org.example;

public class Skier {
  private int skierID;
  private int resortID;
  private int liftID;
  private int seasonID;
  private int dayID;
  private int time;
//  skierID - between 1 and 100000
//  resortID - between 1 and 10
//  liftID - between 1 and 40
//  seasonID - 2022
//  dayID - 1
//  time - between 1 and 360


  public Skier(int skierID, int resortID, int liftID, int seasonID, int dayID, int time) {
    this.skierID = skierID;
    this.resortID = resortID;
    this.liftID = liftID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.time = time;
  }

  public int getSkierID() {
    return skierID;
  }

  public void setSkierID(int skierID) {
    this.skierID = skierID;
  }

  public int getResortID() {
    return resortID;
  }

  public void setResortID(int resortID) {
    this.resortID = resortID;
  }

  public int getLiftID() {
    return liftID;
  }

  public void setLiftID(int liftID) {
    this.liftID = liftID;
  }

  public int getSeasonID() {
    return seasonID;
  }

  public void setSeasonID(int seasonID) {
    this.seasonID = seasonID;
  }

  public int getDayID() {
    return dayID;
  }

  public void setDayID(int dayID) {
    this.dayID = dayID;
  }

  public int getTime() {
    return time;
  }

  public void setTime(int time) {
    this.time = time;
  }

  @Override
  public String toString() {
    return "Skier{" +
        "skierID=" + skierID +
        ", resortID=" + resortID +
        ", liftID=" + liftID +
        ", seasonID=" + seasonID +
        ", dayID=" + dayID +
        ", time=" + time +
        '}';
  }
}
