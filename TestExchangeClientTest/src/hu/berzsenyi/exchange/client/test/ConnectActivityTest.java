package hu.berzsenyi.exchange.client.test;

import com.robotium.solo.Solo;

import hu.berzsenyi.exchange.client.ui.ConnectActivity;
import hu.berzsenyi.exchange.client.ui.MainActivity;
import hu.berzsenyi.exchange.client.ui.StockBuyActivity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;

public class ConnectActivityTest extends
		ActivityInstrumentationTestCase2<ConnectActivity> {

	private static final String IP = "10.0.2.2";
	private static final int PORT = 8080;

	private static final String USER_NAME = "TheBroker", PASSWORD = "pswd";

	private Solo mSolo;
	private Instrumentation mInstrumentation;
	private ConnectActivity mConnectActivity;
	private MainActivity mMainActivity;
	private StockBuyActivity mStockBuyActivity;

	private Object mLock = new Object();
	private String mFailMessage;
	private boolean mTestSucceeded;

	public ConnectActivityTest() {
		super(ConnectActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mInstrumentation = getInstrumentation();
		mConnectActivity = getActivity();
		mSolo = new Solo(getInstrumentation(), mConnectActivity);

	}

	public void testConnect_badIp() {
		mSolo.clearEditText(0);
		mSolo.typeText(0, USER_NAME);
		mSolo.clearEditText(1);
		mSolo.typeText(1, PASSWORD);
		mSolo.clearEditText(2);
		mSolo.typeText(2, "129.168.0.233");
		mSolo.clearEditText(3);
		mSolo.typeText(3, "8080");
		mSolo.clickOnButton(0);

		mFailMessage = null;
		mTestSucceeded = false;

		mConnectActivity.setTestListener(new ConnectActivity.TestListener() {

			private boolean connectingShown = false, connectingGone = false;

			@Override
			public void onCouldNotConnectDialogShow(Dialog dialog) {
				if (!connectingShown || !connectingGone) {
					mFailMessage = "'Could not connect' dialog came too early";
				} else { // success :)
					dialog.dismiss();
					mTestSucceeded = true;
				}

				synchronized (mLock) {
					mLock.notify();
				}
			}

			@Override
			public void onConnectingDialogShow(Dialog dialog) {
				if (connectingShown || connectingGone) {
					mFailMessage = "'Connecting' dialog came in wrong time";

					synchronized (mLock) {
						mLock.notify();
					}
				} else {
					connectingShown = true;
				}
			}

			@Override
			public void onConnectingDialogGone(Dialog dialog) {
				if (!connectingShown || connectingGone) {
					mFailMessage = "'Connecting' dialog went in wrong time";

					synchronized (mLock) {
						mLock.notify();
					}
				} else {
					connectingGone = true;
				}
			}
		});

		synchronized (mLock) {
			try {
				mLock.wait(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		mConnectActivity.setTestListener(null);

		if (!mTestSucceeded && mFailMessage == null)
			mFailMessage = "Timeout";

		assertNotNull(mFailMessage);
	}

	public void testConnect() {
		mSolo.clearEditText(0);
		mSolo.typeText(0, USER_NAME);
		mSolo.clearEditText(1);
		mSolo.typeText(1, PASSWORD);
		mSolo.clearEditText(2);
		mSolo.typeText(2, IP);
		mSolo.clearEditText(3);
		mSolo.typeText(3, PORT + "");

		ActivityMonitor mainActivityMonitor = mInstrumentation.addMonitor(
				MainActivity.class.getName(), null, false);
		ActivityMonitor stockBuyActivityMonitor = mInstrumentation.addMonitor(
				StockBuyActivity.class.getName(), null, false);

		mSolo.clickOnButton(0);

		try {

			mMainActivity = (MainActivity) mainActivityMonitor
					.waitForActivityWithTimeout(10000);

			if (mMainActivity == null)
				fail("Could not connect. MainActivity has not come.");

			mStockBuyActivity = (StockBuyActivity) stockBuyActivityMonitor
					.waitForActivityWithTimeout(2000);

			if (mStockBuyActivity == null)
				fail("StockBuyActivity has not appeared.");
		} finally {

			mInstrumentation.removeMonitor(mainActivityMonitor);
			mInstrumentation.removeMonitor(stockBuyActivityMonitor);

			if (mMainActivity != null)
				mMainActivity.finish();
			if (mStockBuyActivity != null)
				mStockBuyActivity.finish();
		}
	}
}
