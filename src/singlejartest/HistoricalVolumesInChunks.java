package singlejartest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

/**
 * The strategy demonstrates how to retrieve historical data in chunks, instead of loading all the data at once.
 * In particular, strategy on its start prints historical volumes of 1 hour bars over 7 days following
 * the designated start date in startDateStr.
 *
 */

public class HistoricalVolumesInChunks implements IStrategy {
   
    private IConsole console;
    private IHistory history;
   
    private Period period = Period.DAILY;
    private Instrument instrument = Instrument.EURUSD;
   
    private SimpleDateFormat gmtSdfShort = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat gmtSdfLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   
    private String startDateStr = "2015-12-20";
    private int days = 7;
   
    @Override
    public void onStart(IContext context) throws JFException {
        console = context.getConsole();
        history = context.getHistory();

        gmtSdfShort.setTimeZone(TimeZone.getTimeZone("GMT"));
        gmtSdfLong.setTimeZone(TimeZone.getTimeZone("GMT"));
       
        long startDate = 0;
        try {
            startDate = gmtSdfShort.parse(startDateStr).getTime();
        } catch (ParseException e1) {
            //stop strategy
            console.getErr().println(e1);
            context.stop();
            return;
        }
       
        for (int day = 0; day < days; day++ ){
            long startTime = 0;
            long endTime = 0;
           
            startTime = startDate + Period.DAILY.getInterval() * day;
            endTime = startTime + Period.DAILY.getInterval() - 1; //minus 1 milli to be in the same day

            long startBarTime = history.getBarStart(period, startTime);
            long endBarTime = history.getBarStart(period, endTime);
            List<IBar> bars = history.getBars(instrument, period, OfferSide.BID, startBarTime, endBarTime);
           
            double maxVolume = 0;
            long maxVolumeTime = 0;
            
            double dayHigh = 0.0;
            double dayLow = 0.0;
            double open = 0.0;
            double close = 0.0;
            
            int i = 0;
            
            for(IBar bar : bars){
                if(maxVolume < bar.getVolume()){
                    maxVolume = bar.getVolume();
                    maxVolumeTime = bar.getTime();
                }
                
                if(i == 0){
                	open = bar.getOpen();
                }
                
                if(i == bars.size()-1){
                	close = bar.getClose();
                }
                
                if(dayHigh < bar.getHigh()){
                	dayHigh = bar.getHigh();
                }
                
                if(dayLow < bar.getLow()){
                	dayLow = bar.getLow();
                }
                
                
            }

            
            System.out.println(gmtSdfShort.format(startTime) + "," + open + "," + close + "," + dayHigh + "," + dayLow + "," + maxVolumeTime);
            
//            print(gmtSdfShort.format(startTime) + " max volume of " +period+ " bars: " + maxVolume + " at: " + gmtSdfLong.format(maxVolumeTime));
        }

    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {}

    private void print(Object o) {
        console.getOut().println(o);
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {    }
    @Override
    public void onMessage(IMessage message) throws JFException {    }
    @Override
    public void onAccount(IAccount account) throws JFException {    }
    @Override
    public void onStop() throws JFException {    }

}