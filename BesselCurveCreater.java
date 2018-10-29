package com.maizer.curve;

public class BesselCurveCreater {

	private static final Object[] TEMP = new Object[3];


	private static final float[] get(int size) {
		synchronized (TEMP) {
			for (int i = 0; i < 3; i++) {
				Object obj = TEMP[i];
				if (obj != null) {
					float[] mF = (float[]) obj;
					if (mF.length >= size) {
						TEMP[i] = null;
						return mF;
					}
				}
			}
		}
		return new float[size];
	}

	private static final void recyle(float[] f) {
		synchronized (TEMP) {
			if (f != null && f.length < 100) {
				for (int i = 0; i < 3; i++) {
					Object obj = TEMP[i];
					if (obj == null) {
						TEMP[i] = f;
						return;
					}
				}
			}
		}
	}

	public static final double getBesselCustomValueForGradient(float t, int value, float... values) {
		int length = values.length;
		if (length == 0) {
			throw new IllegalArgumentException("argument length == 0 !");
		}
		if (length <= 1) {
			return values[0];
		}
		return getBesselCustomValueForGradient(t, value, length, values);
	}


	private static final double getBesselCustomValueForGradient(float t, int value, int length, float[] values) {
		if (length <= 0) {
			return values[0];
		}
		float countValue = values[length - 1] - values[0];

		return 0;
	}

	public static final double getBesselCustomValue(float t, float... values) {
		int length = values.length;
		if (length == 0) {
			throw new IllegalArgumentException("argument length == 0 !");
		}
		if (length <= 1) {
			return values[0];
		}
		return getBesselCustomValue(t, length, values);
	}

	private static final double getBesselCustomValue(float t, int length, float[] values) {
		if (length <= 0) {
			return values[0];
		}
		float[] mValues = get(length - 1);
		for (int i = 0; i < length - 1; i++) {
			mValues[i] = t * values[i + 1] - (t - 1) * values[i];
		}
		recyle(mValues);
		return getBesselCustomValue(t, length - 1, mValues);
	}

}
