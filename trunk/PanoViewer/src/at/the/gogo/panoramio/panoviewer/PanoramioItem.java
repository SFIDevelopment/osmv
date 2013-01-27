/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.the.gogo.panoramio.panoviewer;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.maps.GeoPoint;

/**
 * Holds one item returned from the Panoramio server. This includes the bitmap
 * along with other meta info.
 * 
 */
public class PanoramioItem implements Parcelable {

	private long mId;
	private Bitmap mBitmap;
	private final GeoPoint mLocation;
	private final String mTitle;
	private final String mOwner;
	private final String mThumbUrl;
	private final String mOwnerUrl;
	private final String mPhotoUrl;

	public PanoramioItem(final Parcel in) {
		mId = in.readLong();
//		mBitmap = Bitmap.CREATOR.createFromParcel(in);
		mBitmap = null;
		mLocation = new GeoPoint(in.readInt(), in.readInt());
		mTitle = in.readString();
		mOwner = in.readString();
		mThumbUrl = in.readString();
		mOwnerUrl = in.readString();
		mPhotoUrl = in.readString();
	}

	public PanoramioItem(final long id, final String thumbUrl, final Bitmap b,
			final int latitudeE6, final int longitudeE6, final String title,
			final String owner, final String ownerUrl, final String photoUrl) {
		mBitmap = b;
		mLocation = new GeoPoint(latitudeE6, longitudeE6);
		mTitle = title;
		mOwner = owner;
		mThumbUrl = thumbUrl;
		mOwnerUrl = ownerUrl;
		mPhotoUrl = photoUrl;
	}

	public long getId() {
		return mId;
	}

	public Bitmap getBitmap() {
		return mBitmap;
	}

	public GeoPoint getLocation() {
		return mLocation;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getOwner() {
		return mOwner;
	}

	public String getThumbUrl() {
		return mThumbUrl;
	}

	public String getOwnerUrl() {
		return mOwnerUrl;
	}

	public String getPhotoUrl() {
		return mPhotoUrl;
	}

	public static final Parcelable.Creator<PanoramioItem> CREATOR = new Parcelable.Creator<PanoramioItem>() {
		@Override
		public PanoramioItem createFromParcel(final Parcel in) {
			return new PanoramioItem(in);
		}

		@Override
		public PanoramioItem[] newArray(final int size) {
			return new PanoramioItem[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel parcel, final int flags) {
		parcel.writeLong(mId);
//		mBitmap.writeToParcel(parcel, 0);
		parcel.writeInt(mLocation.getLatitudeE6());
		parcel.writeInt(mLocation.getLongitudeE6());
		parcel.writeString(mTitle);
		parcel.writeString(mOwner);
		parcel.writeString(mThumbUrl);
		parcel.writeString(mOwnerUrl);
		parcel.writeString(mPhotoUrl);
	}
}