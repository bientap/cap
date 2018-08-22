package com.castlabs;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

/**
 * Implementation of the {@link LicenseLoader} that will return license data from the
 * Manifest meta-data. You need to add a meta-data entry in your manifest using the key
 * {@code castlabs-license}, for example:
 * <p>
 * <pre><code>
 * &lt;application&gt;
 *     ...
 *     &lt;meta-data
 *         android:name="castlabs-license"
 *         android:value="..." /&gt;
 * &lt;/application&gt;
 * </code></pre>
 *
 * This license loader will access the meta-data and load the license from there or
 * raise an exception if the license entry could not be found.
 *
 * @see com.castlabs.android.PlayerSDK#init(Context, LicenseLoader)
 * @since 3.3.0
 */
@PublishedSource
public class ManifestLicenseLoader implements LicenseLoader {
	private static final String META_DATA_KEY = "castlabs-license";
	private final Context context;
	private byte[] licenseData;

	public ManifestLicenseLoader(Context context) {
		this.context = context.getApplicationContext();
	}

	public byte[] getLicenseData() throws Exception {
		if (licenseData == null) {
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			String licenseString = bundle.getString(META_DATA_KEY);
			if (licenseString == null || licenseString.isEmpty()) {
				throw new IllegalArgumentException(
						"No castlabs-license meta-data entry found in " +
								"the Manifest! Unable to load License!");
			}
			licenseData = licenseString.getBytes("UTF-8");
		}
		return licenseData;
	}

}
