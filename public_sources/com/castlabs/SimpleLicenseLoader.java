package com.castlabs;

import android.content.Context;
import android.support.annotation.NonNull;

import com.castlabs.android.PlayerSDK;

/**
 * Implementation of the {@link LicenseLoader} that will return license data as passed in
 * the constructor.
 *
 * @since 3.3.0
 * @see PlayerSDK#init(Context, LicenseLoader)
 */
@PublishedSource
public class SimpleLicenseLoader implements LicenseLoader {
	private final byte[] licenseData;

	public SimpleLicenseLoader(@NonNull String licenseData) {
		this(licenseData.getBytes());
	}

	public SimpleLicenseLoader(@NonNull byte[] licenseData) {
		this.licenseData = licenseData;
	}

	public byte[] getLicenseData() throws Exception{
		return licenseData;
	}

}
