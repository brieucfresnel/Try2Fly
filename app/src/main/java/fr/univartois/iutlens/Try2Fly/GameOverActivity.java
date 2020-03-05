package fr.univartois.iutlens.Try2Fly;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

public class GameOverActivity extends AppCompatActivity {
    private String score;
    private String gameOverText;
    private TextView gameOverTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        Bundle extras = getIntent().getExtras();
        score = extras.getString("score");

        gameOverText = "Game Over\nScore: " + score + "\nTap to try again";
        gameOverTextView = findViewById(R.id.gameOverText);
        gameOverTextView.setText(gameOverText);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Intent intent= new Intent(this, StartGame.class);
            startActivity(intent);
        }

        return super.onTouchEvent(event);
    }

}
