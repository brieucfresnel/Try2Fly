package fr.univartois.iutlens.Try2Fly;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class StartGame extends Activity {

    TextView scoreView;
    GameView gameView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_view);

        gameView = findViewById(R.id.gameView);
        scoreView = findViewById(R.id.scoreView);

        setScoreViewText(gameView.score);
    }

    protected void setScoreViewText(int score) {
        scoreView.setText(Integer.toString(gameView.score));
    }

}
