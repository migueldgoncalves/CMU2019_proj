package pt.ulisboa.tecnico.cmov.proj.Dropbox;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * Async task to upload a file to a directory
 */
public class UploadFileTask extends AsyncTask<String, Void, FileMetadata> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public UploadFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(FileMetadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(result);
        }
    }

    @Override
    protected FileMetadata doInBackground(String... params) {

        File localFile = new File(mContext.getFilesDir().getPath() + "/" + params[0]);
        try{
            if(localFile.createNewFile()){
                System.out.println("File Created Successfuly!");
                FileWriter out = new FileWriter(localFile);
                out.write("This is a Test String To Ensure File Has Content");
                out.close();
            }
            else {
                throw new IOException("Could Not Create File!");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if (localFile != null) {

            String remoteFolderPath = params[1];
            String remoteFileName = params[0];

            try{
                InputStream inputStream = new FileInputStream(localFile);

                FileMetadata result = mDbxClient.files().uploadBuilder(remoteFolderPath + "/" + remoteFileName).withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);

                SharedLinkMetadata sharedLinkMetadata = mDbxClient.sharing().createSharedLinkWithSettings("/Peer2Photo/Teste");
                System.out.println(sharedLinkMetadata);

                return result;

            } catch (DbxException | IOException e) {
                mException = e;
                e.printStackTrace();
            }
        }

        return null;
    }

    public interface Callback {
        void onUploadComplete(FileMetadata result);
        void onError(Exception e);
    }
}
