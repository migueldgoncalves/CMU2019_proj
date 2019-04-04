package pt.ulisboa.tecnico.cmov.proj.Dropbox;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.CreateSharedLinkWithSettingsErrorException;
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

    private static void printProgress(long uploaded, long size) {
        System.out.printf("Uploaded %12d / %12d bytes (%5.2f%%)\n", uploaded, size, 100 * (uploaded / (double) size));
    }

    @Override
    protected FileMetadata doInBackground(String... params) {

        String OPERATION_MODE = params[2];

        if(OPERATION_MODE.equals("NEW_ALBUM")){
            //###############################ATENCAO###############################################
            //NESTE MODO DE OPERACAO A SINTAXE DOS PARAMETROS E A SEGUINTE
            //PARAMS 0 ---> NOME DO FICHEIRO REMOTO
            //PARAMS 1 ---> NOME DA PASTA REMOTA ONDE VAMOS GUARDAR O FICHEIRO
            //PARAMS 2 ---> OPERACAO

            File localFile = new File(mContext.getFilesDir().getPath() + "/" + params[0]);
            try{
                if(localFile.createNewFile()){
                    System.out.println("File Created Successfuly!");
                }
                else {
                    System.out.println("The File Already Exists in Local Storage");
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

                    SharedLinkMetadata sharedLinkMetadata = mDbxClient.sharing().createSharedLinkWithSettings("/Peer2Photo/" + remoteFileName);
                    System.out.println(sharedLinkMetadata);

                    return result;

                }catch (CreateSharedLinkWithSettingsErrorException e){
                    System.out.println("The File Already has a shared Link Associated with it!");
                } catch (DbxException | IOException e) {
                    mException = e;
                    e.printStackTrace();
                }

            }
        }else if (OPERATION_MODE.equals("NEW_PHOTO")) {
            //###############################ATENCAO###############################################
            //NESTE MODO DE OPERACAO A SINTAXE DOS PARAMETROS E A SEGUINTE
            //PARAMS 0 ---> NOME DO FICHEIRO REMOTO
            //PARAMS 1 ---> NOME DA PASTA REMOTA ONDE VAMOS GUARDAR O FICHEIRO
            //PARAMS 2 ---> OPERACAO
            //PARAMS 3 ---> PATH DA FOTO NA LOCAL STORAGE
            //PARAMS 4 ---> NOME DO ALBUM (SLICE) NA LOCAL STORAGE DO USER
            File localFile = new File(params[3]);

            if (localFile != null) {

                String remoteFolderPath = params[1];
                String remoteFileName = params[0];

                try {
                    InputStream inputStream = new FileInputStream(localFile);

                    FileMetadata result = mDbxClient.files().uploadBuilder(remoteFolderPath + "/" + remoteFileName).withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);

                    try{
                        SharedLinkMetadata sharedLinkMetadata = mDbxClient.sharing().createSharedLinkWithSettings("/Peer2Photo/" + remoteFileName);
                        System.out.println(sharedLinkMetadata);

                        //Alter the local slice to Send new version to cloud
                        File localSlice = new File(mContext.getFilesDir().getPath() + "/" + params[4]);
                        FileWriter out = new FileWriter(localSlice);
                        out.append(sharedLinkMetadata.getUrl().replace("?dl=0", "?dl=1"));
                        out.flush();
                        out.close();

                        new UploadFileTask(mContext, mDbxClient, new Callback() {
                            @Override
                            public void onUploadComplete(FileMetadata result) {
                                System.out.println("Catalog Updated!");
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        }).execute(params[4], "/Peer2Photo", "NEW_ALBUM");

                        return result;

                    }catch (CreateSharedLinkWithSettingsErrorException e){
                        System.out.println("The Shared Link Already Exists");
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                } catch (DbxException | IOException e) {
                    mException = e;
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public interface Callback {
        void onUploadComplete(FileMetadata result);
        void onError(Exception e);
    }
}