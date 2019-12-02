package source.main;

public class Timer {
	
	private double startTime;
	private double requestTime;
	
	private final double SECOND = 1000000000;
	
	public Timer()
	{
		startTime = Math.floor(System.nanoTime() / SECOND);
		requestTime = startTime;
	}
	
	public double getCurrentTime()
	{
		return Math.floor(System.nanoTime() / SECOND);
	}
	
	public double getStartTime()
	{
		return startTime;
	}
	
	public double getRequestTime()
	{
		return requestTime;
	}
	
	public void logRequest()
	{
		requestTime = Math.floor(System.nanoTime() / SECOND);
	}
	
	public boolean canRequest()
	{
		if (getCurrentTime() - requestTime > 5)
			return true;
		else
			return false;
	}
}
