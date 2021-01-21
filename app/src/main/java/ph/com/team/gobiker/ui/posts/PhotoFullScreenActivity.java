package ph.com.team.gobiker.ui.posts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import ph.com.team.gobiker.R;

public class PhotoFullScreenActivity  extends Activity {

    @SuppressLint("NewApi")


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_full);

        String filestring = getIntent().getExtras().getString("filestring");

        ImageView imgDisplay;
        Button btnClose;


        imgDisplay = (ImageView) findViewById(R.id.imgDisplay);
        btnClose = (Button) findViewById(R.id.btnClose);


        btnClose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PhotoFullScreenActivity.this.finish();
            }
        });


        Picasso.with(PhotoFullScreenActivity.this).load(filestring).into(imgDisplay);

    }
}
