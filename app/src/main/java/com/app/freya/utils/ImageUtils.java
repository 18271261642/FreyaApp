package com.app.freya.utils;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import timber.log.Timber;

public class ImageUtils {


    public static List<Bitmap> getGifData(File gifFile) {
        com.bumptech.glide.load.resource.gif.GifDrawable g;
        List<Bitmap> lt = new ArrayList<>();
        try {
            GifDrawable gifDrawable = new GifDrawable(gifFile);
            int count = gifDrawable.getNumberOfFrames();
            Timber.e("-------帧数=" + count);
            for (int i = 0; i < count; i++) {
                Bitmap bt = gifDrawable.seekToFrameAndGet(i);

                //判断尺寸，小于320x172的提示不合法
                int width = bt.getWidth();
                int height = bt.getHeight();

                //Timber.e("--------尺寸=" + width + " height=" + height);
                if (width < 320 || height < 172) {
                    break;
                }
                lt.add(bt);
            }

            return lt.size() > 10 ? lt.subList(0, 10) : lt;
        } catch (Exception e) {
            e.printStackTrace();
            lt.clear();
            return lt;
        }

    }


    public static List<Bitmap> getGifDataBitmap(File gifFile) {
        List<Bitmap> lt = new ArrayList<>();
        try {
            GifDrawable gifDrawable = new GifDrawable(gifFile);
            int count = gifDrawable.getNumberOfFrames();
            Timber.e("-------帧数=" + count);
            for (int i = 0; i < count; i++) {
                Bitmap bt = gifDrawable.seekToFrameAndGet(i);

                //判断尺寸，小于320x172的提示不合法
                int width = bt.getWidth();
                int height = bt.getHeight();

                //Timber.e("---111-----尺寸=" + width + " height=" + height);
                if (width < 320 || height < 172) {
                    lt.clear();
                    break;
                }


                if (width == 320 && height == 172) {
                    lt.add(bt);
                } else {
                    //计算偏移量
                    int x = width / 2 - 160;
                    int y = height / 2 - 81;


                    Bitmap newBitmap = Bitmap.createBitmap(bt, x, y, 320, 172);

                    //Timber.e("---222-----尺寸=" + newBitmap.getWidth() + " height=" + newBitmap.getHeight());
                    lt.add(newBitmap);
                }

            }


            int size = lt.size();
            if (size == 0) {
                return lt;
            }
            if (size > 9) {
                List<Bitmap> bigList = new ArrayList<>();
                int number = size / 9;
                Timber.e("-------number=" + number);
                for (int k = 0; k < size; k += number) {
                    if ((k + number) < size) {
                        bigList.add(lt.get(k));
                    }
                }
                return bigList;
            } else {
                return lt;
            }

        } catch (Exception e) {
            e.printStackTrace();
            lt.clear();
            return lt;
        }

    }


    public static int getGifAnimationDuration(File gifFile) {
        try {
            GifDrawable gifDrawable = new GifDrawable(gifFile);
            int duration = gifDrawable.getDuration();
            return duration;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }


    public static List<Bitmap> getGifDataBitmapNoClip(File gifFile) {
        com.bumptech.glide.load.resource.gif.GifDrawable g;
        List<Bitmap> lt = new ArrayList<>();
        try {
            GifDrawable gifDrawable = new GifDrawable(gifFile);
            int count = gifDrawable.getNumberOfFrames();
            Timber.e("-------帧数=" + count);
            for (int i = 0; i < count; i++) {
                Bitmap bt = gifDrawable.seekToFrameAndGet(i);

                //判断尺寸，小于320x172的提示不合法
                int width = bt.getWidth();
                int height = bt.getHeight();

                //Timber.e("---111-----尺寸=" + width + " height=" + height);
                if (width < 320 || height < 172) {
                    lt.clear();
                    break;
                }
                lt.add(bt);

            }


            int size = lt.size();
            if (size == 0) {
                return lt;
            }
            if (size > 9) {
                List<Bitmap> bigList = new ArrayList<>();
                int number = size / 9;
                Timber.e("-------number=" + number);
                for (int k = 0; k < size; k += number) {
                    if ((k + number) < size) {
                        bigList.add(lt.get(k));
                    }
                }
                return bigList;
            } else {
                return lt;
            }

        } catch (Exception e) {
            e.printStackTrace();
            lt.clear();
            return lt;
        }


    }


    public static String getPathToUri(Activity activity, Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.managedQuery(uri, proj, null, null, null);
        int actual_image_column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        //游标跳到首位，防止越界
        cursor.moveToFirst();
        return cursor.getString(actual_image_column_index);
    }


    public static void saveMyBitmap(Bitmap mBitmap,String fileName)  {
        File f = new File( fileName);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 70, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
