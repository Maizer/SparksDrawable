package com.maizer.drawable;

import java.util.Iterator;
import java.util.LinkedList;

import com.maizer.curve.BesselCurveCreater;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.RectF;
import android.os.Looper;
import android.util.Log;

/**
 * 
 * @author Maizer
 *
 */
public class SparksDrawable extends DrawableCompatible {

	public static final String TAG = SparksDrawable.class.getCanonicalName();

	public enum Shape {
		RECT, CIRCLE, TRIANGLE, RANDOM
	}

	private LinkedList<Spark> mSparkList = new LinkedList<Spark>();
	private LinkedList<Spark> mSparkBufferList = new LinkedList<Spark>();
	private SparkVariate mSparkVariate = new SparkVariate();
	private Shape mShape = Shape.RANDOM;
	private int mCount = 10;
	private int mTime;
	private int mFromColor = Color.BLACK;
	private int mToColor = Color.WHITE;
	private boolean isPostEnable;

	private float mFromX;
	private float mFromY;
	private float mMinX;
	private float mMaxX;
	private float mMinY;
	private float mMaxY;

	public void setSparkShape(Shape s) {
		mShape = s;
	}

	public void setSparkColorRange(int fc, int tc) {
		mFromColor = fc;
		mToColor = tc;

	}

	public void setSparksCount(int c) {
		mCount = c;
	}

	public int getSparksCount() {
		return mCount;
	}

	public void setPostDelayEnable(boolean b, int time) {
		unscheduleSelf(mRunnable);
		if (isPostEnable = b) {
			mTime = time;
			scheduleSelf(mRunnable, mTime);
		}
	}

	public void postSparksEffect() {
		postSparksEffect(mFromX, mFromY, mMinX, mMaxX, mMinY, mMaxY);
	}

	public void setSparksEffect(final float fromX, final float fromY, final float minX, final float maxX,
			final float minY, final float maxY, final int fc, final int tc) {

		mFromX = fromX;
		mFromY = fromY;
		mMaxX = maxX;
		mMaxY = maxY;
		mMinX = minX;
		mMinY = minY;

		if (fc != 0 || tc != 0) {
			setSparkColorRange(fc, tc);
		}
	}

	public void postSparksEffect(float fromX, float fromY, float minX, float maxX, float minY, float maxY) {
		postSparksEffect(fromX, fromY, minX, maxX, minY, maxY, 0, 0);
	}

	public void postSparksEffect(float fromX, float fromY, float minX, float maxX, float minY, float maxY, int c) {
		postSparksEffect(fromX, fromY, minX, maxX, minY, maxY, c, c);
	}

	public void postSparksEffect(final float fromX, final float fromY, final float minX, final float maxX,
			final float minY, final float maxY, final int fc, final int tc) {
		if (Looper.getMainLooper().getThread() == Thread.currentThread()) {

			mFromX = fromX;
			mFromY = fromY;
			mMaxX = maxX;
			mMaxY = maxY;
			mMinX = minX;
			mMinY = minY;

			if (fc != 0 || tc != 0) {
				setSparkColorRange(fc, tc);
			}

			mSparkVariate.setRange(minX, maxX, minY, maxY);
			for (int i = 0; i < mCount; i++) {
				if (!mSparkBufferList.isEmpty()) {
					mSparkList.add(configSpark(mSparkBufferList.removeLast(), fromX, fromY));
				} else {
					mSparkList.add(configSpark(new Spark(), fromX, fromY));
				}
			}
			scheduleSelf();
		} else {
			scheduleSelf(new Runnable() {

				@Override
				public void run() {
					postSparksEffect(fromX, fromY, minX, maxX, minY, maxY, fc, tc);
				}
			}, 0);
		}
	}

	@Override
	public void draw(Canvas canvas) {
		Iterator<Spark> spIterator = mSparkList.iterator();

		while (spIterator.hasNext()) {
			Spark spark = spIterator.next();
			if (!spark.draw(canvas)) {
				addSparkToBuffer(spark);
				spIterator.remove();
			}
		}
		if (!mSparkList.isEmpty()) {
			scheduleSelf();
		} else if (isPostEnable && mTime == 0) {
			// if(mTime == 0) {
			scheduleSelf(mRunnable, 0);
			// }else {
			// scheduleSelf(mRunnable, mTime);
			// }
		}
	}

	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			postSparksEffect(mFromX, mFromY, mMinX, mMaxX, mMinY, mMaxY);
			if (isPostEnable && mTime != 0) {
				scheduleSelf(this, mTime);
			}
		}
	};

	public void setSparkSize(float minW, float maxW, float minH, float maxH) {
		mSparkVariate.setSize(minW, maxW, minH, maxH);
	}

	private Spark configSpark(Spark spark, float fromX, float fromY) {
		float centerX = mSparkVariate.getNextCenterX();
		float centerY = mSparkVariate.getNextCenterY();
		float toX = mSparkVariate.getNextToX(centerX, fromX);
		float toY = mSparkVariate.getNextToY(centerY, fromY);

		spark.reset(mFromColor == mToColor ? mFromColor : mFromColor + (int) getRandom(mToColor - mFromColor), mShape,
				mSparkVariate.getNextWidth(), mSparkVariate.getNextHeight(), fromX, fromY, centerX, centerY, toX, toY,
				0.01f, 10);
		return spark;
	}

	private void addSparkToBuffer(Spark spark) {
		mSparkBufferList.add(spark);
	}

	private float getRandom(double v) {
		return (float) (Math.random() * v);
	}

	private class SparkVariate {

		private float minWidth = 10;
		private float maxWidth = 10;
		private float minHeight = 10;
		private float maxHeight = 10;
		private float minX;
		private float maxX;
		private float minY;
		private float maxY;

		public void setRange(float minX, float maxX, float minY, float maxY) {
			this.minX = minX;
			this.maxX = maxX;
			this.minY = minY;
			this.maxY = maxY;
		}

		public void setSize(float minW, float maxW, float minH, float maxH) {
			minWidth = minW > maxW ? maxW : minW;
			maxWidth = minW > maxW ? minW : maxW;
			minHeight = minH > maxH ? maxH : minH;
			maxHeight = minH > maxH ? minH : maxH;
		}

		public float getNextWidth() {
			if (minWidth == maxWidth) {
				return minWidth;
			}
			return (float) (getRandom(maxWidth - minWidth) + minWidth);
		}

		public float getNextHeight() {
			if (minHeight == maxHeight) {
				return minHeight;
			}
			return (float) (getRandom(maxHeight - minHeight) + minHeight);
		}

		public float getNextCenterX() {
			return (float) (getRandom(maxX - minX) + minX);
		}

		public float getNextCenterY() {
			return (float) (getRandom(maxY - minY) + minY);
		}

		public float getNextToX(float centerX, float fromX) {
			if (centerX > fromX) {
				return (float) (getRandom(maxX - centerX) + centerX);
			}
			return (float) (getRandom(centerX - minX) + minX);
		}

		public float getNextToY(float centerY, float fromY) {
			return (float) (getRandom(((maxY - centerY) * 0.7f)) + centerY + (maxY - centerY) * 0.3f);
		}

	}

	private class Spark {
		private Paint paint = new Paint();
		private Path path = new Path();
		private RectF rectF = new RectF();
		private float[] besselsX = new float[3];
		private float[] besselsY = new float[3];
		private float angleself;
		private float incrementAngle;
		private float width;
		private float height;
		private float t;
		private float incrementT;
		private float triangleSpace;
		private Shape shape;
		private int color;

		private Spark() {
		}

		/**
		 * @param1 color
		 * @param2 shape
		 * @param3 rect w
		 * @param4 rect h
		 * @param5 bessel fromeX
		 * @param6 bessel fromeY
		 * @param7 bessel centerX
		 * @param8 bessel centerY
		 * @param9 bessel toX
		 * @param10 bessel toY
		 * @param11 increment t
		 */

		private Spark(int c, Shape mode, float... params) {
			mShape = mode;
			width = params[0];
			height = params[1];
			besselsX[0] = params[2];
			besselsY[0] = params[3];
			besselsX[1] = params[4];
			besselsY[1] = params[5];
			besselsX[2] = params[6];
			besselsY[2] = params[7];
			incrementT = params[8];
			incrementAngle = params[9];
			reset(c);
			path.setFillType(FillType.INVERSE_WINDING);
		}

		/**
		 * @param1 color
		 * @param2 shape
		 * @param3 rect w
		 * @param4 rect h
		 * @param5 bessel fromeX
		 * @param6 bessel fromeY
		 * @param7 bessel centerX
		 * @param8 bessel centerY
		 * @param9 bessel toX
		 * @param10 bessel toY
		 * @param11 increment t
		 */
		public void reset(int c, Shape mode, float... params) {
			mShape = mode;
			width = params[0];
			height = params[1];
			besselsX[0] = params[2];
			besselsY[0] = params[3];
			besselsX[1] = params[4];
			besselsY[1] = params[5];
			besselsX[2] = params[6];
			besselsY[2] = params[7];
			incrementT = params[8];
			incrementAngle = params[9];
			reset(c);
		}

		public void reset(int c) {
			color = c;
			t = 0;
			if (mShape == Shape.RANDOM) {
				int random = (int) (getRandom(2.99d));
				switch (random) {
				case 0:
					shape = Shape.RECT;
					break;
				case 1:
					shape = Shape.CIRCLE;
					break;
				case 2:
					shape = Shape.TRIANGLE;
					break;
				}
			} else {
				shape = mShape;
			}

			switch (shape) {
			case RANDOM:
			case TRIANGLE:
				if (width < height) {
					height = width;
				} else if (width > height) {
					width = height;
				}
				triangleSpace = (float) (width - Math.sqrt(Math.pow(width, 2) - Math.pow(width / 2, 2))) / 2;
				break;
			}
		}

		public boolean draw(Canvas canvas) {
			if (t >= 1f) {
				t = 1f;
			}
			float x = (float) BesselCurveCreater.getBesselCustomValue(t, besselsX);
			float y = (float) BesselCurveCreater.getBesselCustomValue(t, besselsY);

			paint.setColor(color);
			paint.setAlpha((int) (255 - (255 * t)));
			rectF.left = x;
			rectF.top = y;
			rectF.right = x + width;
			rectF.bottom = y + height;

			canvas.rotate(angleself, rectF.centerX(), rectF.centerY());
			switch (shape) {
			case RECT:
				canvas.drawRect(rectF, paint);
				break;
			case CIRCLE:
				canvas.drawOval(rectF, paint);
				break;
			case TRIANGLE:
				drawTriangle(x, y, canvas);
				break;
			}
			canvas.rotate(-angleself, rectF.centerX(), rectF.centerY());
			angleself += incrementAngle;
			if (angleself > 360) {
				angleself = 360;
			} else if (angleself == 360) {
				angleself = 0;
			}
			if (t >= 1f) {
				return false;
			}
			t += incrementT;
			return true;
		}

		private void drawTriangle(float x, float y, Canvas canvas) {

			float top = rectF.top + triangleSpace;
			float bottom = rectF.bottom - triangleSpace;

			// canvas.drawLine(rectF.centerX(), top, rectF.left, bottom, paint);
			// canvas.drawLine(rectF.left, bottom, rectF.right, bottom, paint);
			// canvas.drawLine(rectF.right, bottom, rectF.centerX(), top,
			// paint);
			path.rewind();
			path.moveTo(rectF.centerX(), top);
			path.lineTo(rectF.left, bottom);
			path.lineTo(rectF.right, bottom);
			path.lineTo(rectF.centerX(), top);
			canvas.drawPath(path, paint);
		}

	}

	@Override
	public void setAlpha(int alpha) {
		//notsupport
	}

	@Override
	public void setColorFilter(ColorFilter colorFilter) {
		//notsupport
	}

	@Override
	public int getOpacity() {
		return 0;
	}

}
