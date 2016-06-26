package com.qozix.tileview.tiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.animation.AnimationUtils;

import com.qozix.tileview.detail.DetailLevel;
import com.qozix.tileview.geom.FloatMathHelper;
import com.qozix.tileview.graphics.BitmapProvider;

public class Tile {

  private static final int DEFAULT_TRANSITION_DURATION = 200;

  private int mWidth;
  private int mHeight;
  private int mLeft;
  private int mTop;
  private int mRight;
  private int mBottom;

  private int mRow;
  private int mColumn;

  private float mDetailLevelScale;

  private Object mData;
  private Bitmap mBitmap;

  private Rect mIntrinsicRect = new Rect();
  private Rect mScaledRect = new Rect();

  public double renderTimestamp;

  private boolean mTransitionsEnabled;

  private int mTransitionDuration = DEFAULT_TRANSITION_DURATION;

  private Paint mPaint;

  private TileCanvasView mParentTileCanvasView;

  private DetailLevel mDetailLevel;

  public Tile(int column, int row, int width, int height, Object data, DetailLevel detailLevel) {
    mRow = row;
    mColumn = column;
    mWidth = width;
    mHeight = height;
    mLeft = column * width;
    mTop = row * height;
    mRight = mLeft + mWidth;
    mBottom = mTop + mHeight;
    mData = data;
    mDetailLevel = detailLevel;
    mDetailLevelScale = mDetailLevel.getScale();
    mIntrinsicRect.set(mLeft, mTop, mRight, mBottom);
    mScaledRect.set(  // TODO: maybe RectF and round at final computation - to avoid 1.51 + 1.51 + 1.51 - 1.99
      FloatMathHelper.scale(mLeft, mDetailLevelScale),
      FloatMathHelper.scale(mTop, mDetailLevelScale),
      FloatMathHelper.scale(mRight, mDetailLevelScale),
      FloatMathHelper.scale(mBottom, mDetailLevelScale)
    );
  }

  public int getWidth() {
    return mWidth;
  }

  public int getHeight() {
    return mHeight;
  }

  public int getLeft() {
    return mLeft;
  }

  public int getTop() {
    return mTop;
  }

  public int getRow() {
    return mRow;
  }

  public int getColumn() {
    return mColumn;
  }

  public Object getData() {
    return mData;
  }

  public Bitmap getBitmap() {
    return mBitmap;
  }

  public boolean hasBitmap() {
    return mBitmap != null;
  }

  public Rect getScaledRect() {
    return mScaledRect;
  }

  public void setTransitionDuration(int transitionDuration) {
    mTransitionDuration = transitionDuration;
  }

  public void stampTime() {
    renderTimestamp = AnimationUtils.currentAnimationTimeMillis();
  }

  public void setTransitionsEnabled(boolean enabled) {
    mTransitionsEnabled = enabled;
  }

  public DetailLevel getDetailLevel() {
    return mDetailLevel;
  }

  public float getRendered() {
    if(!mTransitionsEnabled) {
      return 1;
    }
    double now = AnimationUtils.currentAnimationTimeMillis();
    double ellapsed = now - renderTimestamp;
    float progress = (float) Math.min(1, ellapsed / mTransitionDuration);
    if(progress == 1) {
      mTransitionsEnabled = false;
    }
    return progress;
  }

  public boolean getIsDirty() {
    return mTransitionsEnabled && getRendered() < 1f;
  }

  public Paint getPaint() {
    if(!mTransitionsEnabled) {
      return null;
    }
    if(mPaint == null) {
      mPaint = new Paint();
    }
    float rendered = getRendered();
    int opacity = (int) (rendered * 255);
    mPaint.setAlpha(opacity);
    return mPaint;
  }

  void generateBitmap(Context context, BitmapProvider bitmapProvider) {
    if(mBitmap != null) {
      return;
    }
    mBitmap = bitmapProvider.getBitmap(this, context);
  }

  void setParentTileCanvasView(TileCanvasView tileCanvasView) {
    mParentTileCanvasView = tileCanvasView;
  }

  void destroy(boolean shouldRecycle) {
    destroy(shouldRecycle, true);
  }

  void destroy(boolean shouldRecycle, boolean shouldRemove) {
    if(shouldRecycle && mBitmap != null && !mBitmap.isRecycled()) {
      mBitmap.recycle();
    }
    mBitmap = null;
    if(shouldRemove && mParentTileCanvasView != null) {
      mParentTileCanvasView.removeTile(this);
    }
  }

  /**
   * @param canvas The canvas the tile's bitmap should be drawn into
   * @return True if the tile is dirty (drawing output has changed and needs parent validation)
   */
  boolean draw(Canvas canvas) {  // TODO: this might squish edge images
    if(mBitmap != null) {
      canvas.drawBitmap(mBitmap, mIntrinsicRect, mScaledRect, getPaint());
    }
    return getIsDirty();
  }

  @Override
  public int hashCode() {
    int hash = 17;
    hash = hash * 31 + getColumn();
    hash = hash * 31 + getRow();
    hash = hash * 31 + (int) (1000 * getDetailLevel().getScale());
    return hash;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) {
      return true;
    }
    if(o instanceof Tile) {
      Tile m = (Tile) o;
      return m.getRow() == getRow()
        && m.getColumn() == getColumn()
        && m.getDetailLevel().getScale() == getDetailLevel().getScale();
    }
    return false;
  }

}
