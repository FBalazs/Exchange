package hu.berzsenyi.exchange.client.test;

import com.robotium.solo.Solo;

import hu.berzsenyi.exchange.client.ui.ConnectActivity;
import android.app.Dialog;
import android.test.ActivityInstrumentationTestCase2;

public class ConnectActivityTest extends
		ActivityInstrumentationTestCase2<ConnectActivity> {

	private Solo mSolo;
	private ConnectActivity mActivity;

	private Object mLock = new Object();
	private String mFailMessage;
	private boolean mTestSucceeded;

	public ConnectActivityTest() {
		super(ConnectActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = getActivity();
		mSolo = new Solo(getInstrumentation(), mActivity);

	}

	public void testConnect_badIp() {
		mSolo.typeText(0, "TheBroker");
		mSolo.typeText(1, "pswd");
		mSolo.clearEditText(2);
		mSolo.typeText(2, "129.168.0.233");
		mSolo.clearEditText(3);
		mSolo.typeText(3, "8080");
		mSolo.clickOnButton(0);

		mFailMessage = null;
		mTestSucceeded = false;

		mActivity.setTestListener(new ConnectActivity.TestListener() {

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

		mActivity.setTestListener(null);

		if (!mTestSucceeded && mFailMessage == null)
			mFailMessage = "Timeout";

		if (mFailMessage != null)
			fail(mFailMessage);
	}

}
