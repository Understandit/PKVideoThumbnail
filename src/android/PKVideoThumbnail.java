/**
 * PKVideoThumbnail
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.photokandy.PKVideoThumbnail;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaResourceApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.*;
import android.provider.MediaStore;
import android.net.Uri;
import android.database.Cursor;
import android.util.Log;
import android.content.Context;

import java.io.*;

/**
 * This class echoes a string called from JavaScript.
 */
public class PKVideoThumbnail extends CordovaPlugin {

    public static Bitmap createVideoThumbnail(Context context, Uri uri, int kind) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            bitmap = retriever.getFrameAtTime();
        }
        catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        }
        catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        }
        finally {
            try {
                retriever.release();
            }
            catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
/*    if (kind == MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
        bitmap = ThumbnailUtils.extractThumbnail(bitmap,
                ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL,
                ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
    }*/
        return bitmap;
    }


    /**
     * Executes the request and returns PluginResult.
     *
     * @param action        The action to execute.
     * @param args          JSONArry of arguments for the plugin.
     * @param callbackId    The callback id used when calling back into JavaScript.
     * @return              A PluginResult object with a status and message.
     */
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        try {
            if (action.equals("createThumbnail")) {

                final String sourceVideo = args.getString(0);
                final String targetImage = args.getString(1);
                final Context context = this.cordova.getActivity().getApplicationContext();

                Bitmap thumbnail = createVideoThumbnail(context, Uri.parse(sourceVideo), MediaStore.Images.Thumbnails.MINI_KIND);
                // Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail ( sourceVideo, MediaStore.Images.Thumbnails.MINI_KIND);

                FileOutputStream theOutputStream;
                try {
                    File theOutputFile = new File (targetImage.substring(7));
                    if (!theOutputFile.exists()) {
                        if (!theOutputFile.createNewFile()) {
                            callbackContext.error("Could not save thumbnail.");
                            return true;
                        }
                    }
                    if (theOutputFile.canWrite()) {
                        theOutputStream = new FileOutputStream(theOutputFile);
                        if (theOutputStream != null) {
                            thumbnail.compress(CompressFormat.JPEG, 75, theOutputStream);
                        }
                        else {
                            callbackContext.error("Could not save thumbnail; target not writeable");
                            return true;
                        }
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    callbackContext.error("I/O exception saving thumbnail");
                    return true;
                }
                callbackContext.success(targetImage);
                return true;
            }
            else {
                return false;
            }
        }
        catch (JSONException e) {
            callbackContext.error("JSON Exception");
            return true;
        }
    }
}
