package com.castlabs.sdk.demos;

import android.app.Application;
import android.support.annotation.Nullable;
import android.util.Log;

import com.castlabs.android.PlayerSDK;
import com.castlabs.android.SdkConsts;
import com.castlabs.android.drm.MemoryKeyStore;
import com.castlabs.android.network.Request;
import com.castlabs.android.network.Response;
import com.castlabs.sdk.debug.DebugPlugin;
import com.castlabs.sdk.drm.DrmDeviceTimeCheckerPlugin;
import com.castlabs.sdk.ima.ImaPlugin;
import com.castlabs.sdk.okhttp.OkHttpPlugin;
import com.castlabs.sdk.oma.OmaPlugin;
import com.castlabs.sdk.subtitles.SubtitlesPlugin;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import okhttp3.OkHttpClient;

public class DemoApp extends Application {
	private static final String TAG = "DemoApp";

	@Override
	public void onCreate() {
		super.onCreate();

		// Setup the castlabs SDK and register the plugins used
		// by this demo.
		//
		// To demonstrate the license storage per application run,
		// we initialize the the default store to use an in memory store that
		// will not persistently store the keys. Consider using the SharedPreferencesKeyStore
		// to persist keySetIds in the Applications shared preferences or implement your own.
		PlayerSDK.DEFAULT_KEY_STORE = new MemoryKeyStore();

		// (Optional) Setup a KeyStore containing our trusted CAs
		PlayerSDK.SSL_KEY_STORE = createCustomSSLKeyStore();

		// (Optional) Use OkHttp and a custom client builder setup to allow debugging
		// network traffic
		Stetho.initializeWithDefaults(this);
		PlayerSDK.register(new OkHttpPlugin(
				new OkHttpClient.Builder().addNetworkInterceptor(new StethoInterceptor()))
		);

		// (Optional) Register the OMA plugin
		PlayerSDK.register(new OmaPlugin(false));

		// (Optional) Register the extended subtitles plugin
		PlayerSDK.register(new SubtitlesPlugin());

		// (Optional) Register the IMA ads plugin
		ImaPlugin imaPlugin = new ImaPlugin();
		imaPlugin.setEnabled(true);
		// (Optional) Set the IMA SDK settings
		ImaSdkSettings imaSdkSettings = ImaSdkFactory.getInstance().createImaSdkSettings();
		imaSdkSettings.setLanguage("en");
		imaPlugin.setImaSdkSettings(imaSdkSettings);
		PlayerSDK.register(imaPlugin);

		// Register and the Debug plugin. NOTE: This is not intended to be used in production
		PlayerSDK.register(new DebugPlugin.Builder()
				.logOpenBundle(true)
				.logDownloadStarted(true)
				.logDownloadCompleted(true)
				.logDownloadCanceled(true)
				.logDownloadUpstreamDiscarded(true)
				.logDownloadError(true)

				.logResponseType(Response.DATA_TYPE_DRM_PROVISION)
				.logResponseType(Response.DATA_TYPE_DRM_LICENSE)
				.logRequestType(Request.DATA_TYPE_DRM_PROVISION)
				.logRequestType(Request.DATA_TYPE_DRM_LICENSE)
				.logRequestType(Request.DATA_TYPE_MANIFEST)
				.logRequestType(Request.DATA_TYPE_SEGMENT)
				.logRequestType(Request.DATA_TYPE_OTHER)

				//.debugOverlay(true)
				.get()
		);

		// (Optional) DrmDeviceTimeChecker
		PlayerSDK.register(new DrmDeviceTimeCheckerPlugin());
		PlayerSDK.PLAYBACK_HD_CONTENT = SdkConsts.ALLOW_HD_CLEAR_CONTENT |
				SdkConsts.ALLOW_HD_DRM_SOFTWARE |
				SdkConsts.ALLOW_HD_DRM_ROOT_OF_TRUST |
				SdkConsts.ALLOW_HD_DRM_SECURE_MEDIA_PATH;


		// Initialize the SDK with all registered plugins
		PlayerSDK.init(getApplicationContext());
	}

	@Nullable
	private KeyStore createCustomSSLKeyStore() {
		KeyStore keyStore = null;
		try {
			// As an example we load the CA intermediate certificate from the assets into the key store
			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			InputStream caInput = getApplicationContext().getAssets().open("DigiCertSHA2HighAssuranceServerCA.crt");
			Certificate certificate;
			try {
				certificate = certificateFactory.generateCertificate(caInput);
				System.out.println("certificate = " + ((X509Certificate) certificate).getSubjectDN());
			} finally {
				caInput.close();
			}

			keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null, null);
			keyStore.setCertificateEntry("ca", certificate);
		} catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException exception) {
			Log.e(TAG, "Error setting custom SSL key store: " + exception.getMessage());
		}
		return keyStore;
	}
}
