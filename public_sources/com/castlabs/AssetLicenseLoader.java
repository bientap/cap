package com.castlabs;

import android.content.Context;

import com.castlabs.android.PlayerSDK;
import com.castlabs.utils.IOUtils;

import java.io.InputStream;

/**
 * Implementation of the {@link LicenseLoader} that will load the license file from the
 * Applications assets.
 *
 * By default, the loader searches for a file with the name `license.lic`, but you can
 * create an instance that expects a different name using the {@link #AssetLicenseLoader(String)}
 * constructor.
 *
 * @since 3.0.0
 * @see PlayerSDK#init(Context, LicenseLoader)
 */
@PublishedSource
public class AssetLicenseLoader implements LicenseLoader {
	private final String name;

	/**
	 * Creates an instance of the loader that expects an asset of the name `license.lic`.
	 */
	public AssetLicenseLoader() {
		this("license.lic");
	}

	/**
	 * Create an instance of the loader that expects an asset of the given name.
	 *
	 * @param name the name of the license asset
	 */
	public AssetLicenseLoader(String name) {
		this.name = name;
	}

	public byte[] getLicenseData() throws Exception{
		InputStream is = null;
		try {
			is = PlayerSDK.getContext().getAssets().open(name);
			byte[] fileBytes = new byte[is.available()];
			//noinspection ResultOfMethodCallIgnored
			is.read(fileBytes);
			return fileBytes;
		} finally {
			if (is != null) {
				IOUtils.closeQuietly(is);
			}
		}
	}

}
