package com.example.mm.blockreader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainListActivity extends ListActivity {

//    protected String [] mAndroidNames = {
//            "Android Beta",
//            "Android 1.0",
//            "Android 1.1",
//            "Cupcake",
//            "Eclair",
//            "Froyo"
//    };
    //protected String [] mBlogPostTitle;
    public static final int NUMBER_OF_POSTS = 20;
    public static final String TAG = MainListActivity.class.getSimpleName();
    protected JSONObject mBlogData;
    protected ProgressBar mprogressbar;
    private static String KEY_TITLE = "title";
    private static String KEY_AUTHOR = "author";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        mprogressbar = (ProgressBar)findViewById(R.id.progressBar1);
//
//        Resources resource = getResources();
//
//        mBlogPostTitle = resource.getStringArray(R.array.android_name);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,mBlogPostTitle);//mAndroidNames
//
//        setListAdapter(adapter);
       // Toast.makeText(this, R.string.no_items,Toast.LENGTH_LONG).show();



        if(isNetworkAvailable()) {

            mprogressbar.setVisibility(View.VISIBLE);
            GetBlogPostsTask getblogpoststask = new GetBlogPostsTask();
            getblogpoststask.execute();
        }
        else{
            Toast.makeText(this,"Network is Available !!",Toast.LENGTH_LONG).show();
        }


    }

    private boolean isNetworkAvailable(){

        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if( networkInfo != null && networkInfo.isConnected()){

            isAvailable = true;
        }

        return isAvailable;

    }
    private class GetBlogPostsTask extends AsyncTask<Object, Void, JSONObject>{

        @Override
        protected JSONObject doInBackground(Object[] params) {

            int responseCode = -1;
            JSONObject jsonResponse = null ;
            try{

                URL blogFeedUrl = new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUMBER_OF_POSTS);
                HttpURLConnection connection = (HttpURLConnection)blogFeedUrl.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){

                    InputStream inputStream = connection.getInputStream();
                    Reader reader = new InputStreamReader(inputStream);

                    int nextCharacter; // read() returns an int, we cast it to char later
                    String responseData = "";
                    while(true){ // Infinite loop, can only be stopped by a "break" statement
                        nextCharacter = reader.read(); // read() without parameters returns one character
                        if(nextCharacter == -1) // A return value of -1 means that we reached the end
                            break;
                        responseData += (char) nextCharacter; // The += operator appends the character to the end of the string
                    }

                     jsonResponse = new JSONObject(responseData);
//                    String status = jsonResponse.getString("status");
//                    Log.v(TAG, status);
//
//                    JSONArray jsonPosts = jsonResponse.getJSONArray("posts");
//                    for( int i = 0; i < jsonPosts.length(); i++){
//                            JSONObject jsonPost = jsonPosts.getJSONObject(i);
//                            String title = jsonPost.getString("title");
//                            Log.v(TAG, "Post " + i  + " : " + title);
//                    }

                }else {
                    Log.i(TAG, "Unsuccessful HTTP Response Code : " + responseCode);
                }
            }catch(MalformedURLException e){
                LogException(e);
            }catch (IOException e){
                LogException(e);
            } catch(JSONException e) {
                LogException(e);
            } catch (Exception e) {
                LogException(e);
            }

           // return "Code: " + responseCode;
            return jsonResponse;
        }

        @Override
        public void onPostExecute(JSONObject result){
            mBlogData = result;
            handleBlogResponse();
        }
    }

    public void handleBlogResponse(){

        mprogressbar.setVisibility(View.INVISIBLE);
        
        if(mBlogData == null){
          
        }else
        {
            try {
             JSONArray  jsonposts = mBlogData.getJSONArray("posts");
                ///mBlogPostTitle = new String[jsonposts.length()];
                ArrayList<HashMap<String,String>>  blogposts = new ArrayList<HashMap<String, String>>();
                for(int i =0; i< jsonposts.length(); i++){
                    JSONObject post = jsonposts.getJSONObject(i);
                    String title = post.getString(KEY_TITLE);
                    title = Html.fromHtml(title).toString();
                    String author = post.getString(KEY_AUTHOR);
                    author = Html.fromHtml(author).toString();
                    //mBlogPostTitle[i] = title;
                    HashMap<String , String> blogpost = new HashMap<String,String >();
                    blogpost.put(KEY_TITLE, title);
                    blogpost.put(KEY_AUTHOR, author);
                    blogposts.add(blogpost);
                }
                //ArrayAdapter<String > adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,mBlogPostTitle );
                String [] Keys = { KEY_TITLE, KEY_AUTHOR };
                int [] ids = { android.R.id.text1,  android.R.id.text2};

                SimpleAdapter adapter = new SimpleAdapter(this, blogposts, android.R.layout.simple_list_item_2, Keys, ids);

                setListAdapter(adapter);
                //Log.v(TAG, " "+mBlogData.toString(2));

            }
            catch (JSONException e) {
              LogException(e);
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        try {
            JSONArray jsonPosts = mBlogData.getJSONArray("posts");
            JSONObject jsonPost = jsonPosts.getJSONObject(position);
            String blogUrl = jsonPost.getString("url");
            Intent intent = new Intent(this, BlogWebViewActivity.class);
            intent.setData(Uri.parse(blogUrl));
            startActivity(intent);
        } catch (JSONException e) {
            LogException(e);
        }


    }

    private void LogException(Exception e) {
        Log.e(TAG, "Exception Caugth " + e);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
