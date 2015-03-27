package com.crawlmb;

import android.util.Log;

import com.crawlmb.keylistener.GameKeyListener;

public class GameThread implements Runnable {

	public enum Request{
		StartGame
			,StopGame
			,SaveGame
			,OnGameExit;

		public static Request convert(int value)
		{
			return Request.class.getEnumConstants()[value];
		}
    };

	/* game thread state */	
	private Thread thread = null;
	private boolean game_thread_running = false;
	private boolean game_restart = false;
	private boolean plugin_change = false;
	private NativeWrapper nativew = null;
	private GameKeyListener state = null;
	
	public GameThread(GameKeyListener s, NativeWrapper nw) {
		nativew = nw;
		state = s;
	}

	public synchronized void send(Request rq) {
		switch (rq) {
		case StartGame:
			start();
			break;
		case StopGame:
			stop();
			break;
		case SaveGame:
			save();
			break;
		case OnGameExit:
			onGameExit();
			break;
		}
	}

	private void start() {
		plugin_change = false;

		// sanity checks: thread must not already be running
		// and we must have a valid canvas to draw upon.
		//			already_running = game_thread_running;
		//	already_initialized = game_fully_initialized;	  


		if (state.fatalError) {

			// don't bother restarting here, we are going down.
			Log.d("Crawl","start.fatalError is set");
		}
		else if (game_thread_running) {
			state.nativew.resize();
		}
		else {
			
			/* time to start crawl */

			/* notify wrapper game is about to start */
			nativew.onGameStart();
			
 			/* initialize keyboard buffer */
			state.resetKeyBuffer();

			game_thread_running = true;

			//Log.d("Crawl","startBand().starting loader thread");

			thread = new Thread(this);
			thread.start();
		}
	}

	private void stop() {
		// signal keybuffer to send quit command to crawl 
		// (this is when the user chooses quit or the app is pausing)

		//Log.d("Crawl","GameThread.Stop()");

		if (!game_thread_running) {
			//Log.d("Crawl","stop().no game running");
			return;
		}
		if (thread == null)  {
			//Log.d("Crawl","stop().no thread");
			return;
		}

		state.signalGameExit();

		//Log.d("Crawl","signalGameExit.waiting on thread.join()");

		try {
			thread.join();
		} catch (Exception e) {
			Log.d("Crawl",e.toString());
		}

		//Log.d("Crawl","signalGameExit.after waiting for thread.join()");
	}

	private void save() {
		//Log.d("Crawl","saveBand()");

		if (!game_thread_running) {
			Log.d("Crawl","save().no game running");
			return;
		}
		if (thread == null) {
			Log.d("Crawl","save().no thread");
			return;
		}
	 
		state.signalSave();
	}

	private void onGameExit() {
		boolean local_restart = false;
			
		Log.d("Crawl","GameThread.onGameExit()");
		game_thread_running = false;

		// if game exited normally, restart!
		local_restart 
			= game_restart 
			= ((!state.getSignalGameExit() || plugin_change) 
			   && !state.fatalError);

		if	(local_restart) 
			state.handler.sendEmptyMessage(CrawlDialog.Action.StartGame.ordinal());
	}

	public void setFullyInitialized() {
		//if (!game_fully_initialized) 
		//	Log.d("Crawl","game is fully initialized");

	}

	public void run() {		
		if (game_restart) {
			game_restart = false;
			/* this hackery is no longer needed after
				serializing all access to GameThread 
				through the sync'd send() method and
			 	use of handlers to initiate async actions.  */
			/*
			try {
				// if restarting, pause for effect (and to let the
				// other game thread unlock its mutex!)
				Thread.sleep(400);
			} catch (Exception ex) {}
			*/
		}

		Log.d("Crawl","GameThread.run");

		/* game is not running, so start it up */
		nativew.gameStart();
	}
}
