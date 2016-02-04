package jforez.plugin.loader;

import java.util.Date;

public class DataItem {
	@Override
	public String toString() {
		return "DataItem [date=" + date + ", open=" + open + ", close=" + close + ", high=" + high + ", low=" + low
				+ ", volume=" + volume + "]";
	}
	private Date date;
	private double open;
	private double close;
	private double high;
	private double low;
	private double volume;
	
	public DataItem(Date date, double open, double close, double high, double low, double maxVolume) {
		super();
		this.date = date;
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.volume = maxVolume;
	}
	public Date getDate() {
		return date;
	}
	public double getOpen() {
		return open;
	}
	public double getClose() {
		return close;
	}
	public double getHigh() {
		return high;
	}
	public double getLow() {
		return low;
	}
	public double getVolume() {
		return volume;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public void setClose(double close) {
		this.close = close;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public void setVolume(double volume) {
		this.volume = volume;
	}
}
