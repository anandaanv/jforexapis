package jforez.plugin.loader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.ISystemListener;
import com.dukascopy.api.system.JFAuthenticationException;
import com.dukascopy.api.system.JFVersionException;

import singlejartest.Main;

public class ForexDataDownloader {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    //url of the DEMO jnlp
    private String jnlpUrl = "http://platform.dukascopy.com/demo/jforex.jnlp";
    //user name
    private String userName = "DEMO2tnimF";
    //password
    private String password = "tnimF";
    
    private Instrument symbol;

	private IClient client;

	private String interval;
	
	private int lookbackSpans;
	

	public ForexDataDownloader(String jnlpUrl, String userName, String password, String symbol, String interval, int lookbackSpans) {
		super();
		this.jnlpUrl = jnlpUrl;
		this.userName = userName;
		this.password = password;
		this.symbol = Instrument.valueOf(symbol);
		this.interval = interval;
		this.lookbackSpans = lookbackSpans;
	}

	public List<DataItem> fetchData() throws Exception {
        initialize();

        //wait for it to connect
        int i = 10; //wait max ten seconds
        while (i > 0 && !client.isConnected()) {
            Thread.sleep(1000);
            i--;
        }
        if (!client.isConnected()) {
            LOGGER.error("Failed to connect Dukascopy servers");
            System.exit(1);
        }

        
        //start the strategy
        LOGGER.info("Starting strategy");
        HistoricalDataLoader strategy = new HistoricalDataLoader(symbol, Period.valueOf(interval), lookbackSpans);
        
        
        Set<Instrument> instruments = new HashSet<>();
        instruments.add(symbol);
        LOGGER.info("Subscribing instruments...");
        client.setSubscribedInstruments(instruments);
        
        
		client.startStrategy(strategy);
//		client.stopStrategy(num);
		return strategy.getItems();
    }

	private void initialize() throws ClassNotFoundException, IllegalAccessException, InstantiationException,
			JFAuthenticationException, JFVersionException, Exception {
		client = ClientFactory.getDefaultInstance();
        //set the listener that will receive system events
        client.setSystemListener(getListener());

        LOGGER.info("Connecting...");
        
        //connect to the server using jnlp, user name and password
        client.connect(jnlpUrl, userName, password);

	}

	private ISystemListener getListener() {
		return new ISystemListener() {
            private int lightReconnects = 3;

        	@Override
        	public void onStart(long processId) {
                LOGGER.info("Strategy started: " + processId);
                client.stopStrategy(processId);
        	}

			@Override
			public void onStop(long processId) {
                LOGGER.info("Strategy stopped: " + processId);
                if (client.getStartedStrategies().size() == 0) {
                    System.exit(0);
                }
			}

			@Override
			public void onConnect() {
                LOGGER.info("Connected");
                lightReconnects = 3;
			}

			@Override
			public void onDisconnect() {
                getDisconnectHandler(client);
			}

			private void getDisconnectHandler(final IClient client) {
				Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (lightReconnects > 0) {
                            client.reconnect();
                            --lightReconnects;
                        } else {
                            do {
                                try {
                                    Thread.sleep(60 * 1000);
                                } catch (InterruptedException e) {
                                }
                                try {
                                    if(client.isConnected()) {
                                        break;
                                    }
                                    client.connect(jnlpUrl, userName, password);

                                } catch (Exception e) {
                                    LOGGER.error(e.getMessage(), e);
                                }
                            } while(!client.isConnected());
                        }
                    }
                };
                new Thread(runnable).start();
			}
		};
	}
}
