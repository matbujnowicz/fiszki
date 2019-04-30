package tech.fiszki.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import tech.fiszki.R;
import tech.fiszki.logic.Association;
import tech.fiszki.logic.RepetitionManager;
import tech.fiszki.logic.RepetitionManagerMock;
import tech.fiszki.logic.TextToSpeech;
import tech.fiszki.logic.Word;
import tech.fiszki.logic.WordSelector;
import tech.fiszki.logic.WordSelectorMock;
import tech.fiszki.logic.WordSimilarity;
import tech.fiszki.logic.WordSimilarityMock;

public class ReviewActivity extends AppCompatActivity {
    private List<Word> wordsReviewList;
    private Word currentWord;
    private int currentWordCount = 0;
    private double averageSimilarity;
    private ImageView image;
    private EditText response;

    private static final int WORD_COUNT=2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        WordSelector wordSelector = new WordSelectorMock();
        wordsReviewList = wordSelector.nextWordsToReview(WORD_COUNT);

        image = findViewById(R.id.image);
        final ScrollView scrollView = findViewById(R.id.scrollView);

        response = findViewById(R.id.response);
        response.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    image.setVisibility(View.GONE);
                    scrollView.setVisibility(View.GONE);
                }
                else {
                    image.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.VISIBLE);
                }
            }
        });

        Button go = findViewById(R.id.go);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!response.getText().toString().matches("")) {
                    RepetitionManager repetitionManager = new RepetitionManagerMock();
                    WordSimilarity wordSimilarity = new WordSimilarityMock();
                    double similarity = wordSimilarity.checkSimilarity(currentWord, response.getText().toString());
                    repetitionManager.saveRepetition(currentWord, similarity);
                    averageSimilarity += similarity;
                    Toast.makeText(ReviewActivity.this, "Similarity: " + Double.toString(similarity), Toast.LENGTH_SHORT).show();
                    response.clearFocus();
                    response.setText("");
                    hideKeyboard();
                    currentWordCount++;
                    displayWord();
                }
            }
        });

        ImageView speaker = findViewById(R.id.speaker);
        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextToSpeech textToSpeech = new TextToSpeech() {
                    @Override
                    public void read(String text) {
                        Toast.makeText(ReviewActivity.this, "reading "+text, Toast.LENGTH_SHORT).show();
                    }
                };
                textToSpeech.read(currentWord.getOriginalWord());
            }
        });

        displayWord();
    }

    private void displayWord() {
        if(currentWordCount < WORD_COUNT) {
            currentWord = wordsReviewList.get(currentWordCount);

            TextView originalWord = findViewById(R.id.originalWord);
            originalWord.setText(currentWord.getOriginalWord());

            image.setImageResource(getImageId(getApplicationContext(),currentWord.getOriginalWord()));


            LinearLayout associations = findViewById(R.id.associations);
            associations.removeAllViews();
            for(Association association : currentWord.getAssociations()) {
                TextView textView = new TextView(this);
                textView.setText(association.getAssociationWord());
                textView.setTextSize(25f);
                associations.addView(textView);
            }

        }
        else {
            Toast.makeText(this, "Average: "+Double.toString(averageSimilarity/WORD_COUNT), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    private int getImageId(Context context, String imageName) {
        return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            response.clearFocus();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void hideKeyboard() {
        View view = findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}