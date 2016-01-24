package by.aleks.chordbro;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import by.aleks.chordbro.data.Artist;
import by.aleks.chordbro.data.Content;
import by.aleks.chordbro.data.Song;
import com.activeandroid.ActiveAndroid;

import java.util.Map;


public class MainActivity extends AppCompatActivity{


    private TextView artistTv;
    private TextView albumTv;
    private TextView trackTv;

    private Recognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActiveAndroid.initialize(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        artistTv = (TextView)findViewById(R.id.artist);
        albumTv = (TextView)findViewById(R.id.album);
        trackTv = (TextView)findViewById(R.id.track);

        loadAndStart("Hello", "Adele");
        recognizer = new Recognizer(this){

            @Override
            public void onResult(String title, String artist) {
                loadAndStart(title, artist);
            }
        };

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(recognizer).start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(recognizer!=null)
            recognizer.startAudioProcess();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(recognizer!=null)
            recognizer.stopAudioProcess();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    private void loadAndStart(final String title, final String artistName){

        new LastfmImageLoader(){
            @Override
            protected void onPostExecute(byte[] bytes) {
                Artist artist = new Artist();
                artist.name = artistName;
                artist.image = bytes;
                artist.save();

                final Song song = new Song();
                song.artist = artist;
                song.title = title;
                song.save();

                new SongContentLoader(){
                    @Override
                    protected void onPostExecute(Map<String, String> resultMap) {
                        for(String instrument : resultMap.keySet()){
                            Content content = new Content();
                            content.instrument = instrument;
                            content.song = song;
                            content.save();
                        }

                        Intent songIntent = new Intent().setClassName(MainActivity.this, "by.aleks.chordbro.SongActivity");
                        songIntent.putExtra(getString(R.string.artist_key), artistName);
                        songIntent.putExtra(getString(R.string.title_key), title);
                        startActivity(songIntent);
                    }
                }.execute(artistName, title);
            }
        }.execute(artistName);
    }

}
