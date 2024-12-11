package com.example.train;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.widget.Toast;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Exercise> exerciseList;
    private ExerciseAdapter exerciseAdapter;
    private RecyclerView recyclerView;
    private EditText exerciseNameInput, repsInput, setsInput, dataInput;
    private Button addExerciseButton, saveProgressButton;
    private SharedPreferences sharedPreferences;
    private Map<String, Exercise> previousExercises;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        exerciseList = new ArrayList<>();
        exerciseAdapter = new ExerciseAdapter(exerciseList);
        previousExercises = new HashMap<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(exerciseAdapter);

        exerciseNameInput = findViewById(R.id.exerciseNameInput);
        repsInput = findViewById(R.id.repsInput);
        dataInput = findViewById(R.id.setDate);
        setsInput = findViewById(R.id.setsInput);
        addExerciseButton = findViewById(R.id.addExerciseButton);
        saveProgressButton = findViewById(R.id.saveProgressButton);

        sharedPreferences = getSharedPreferences("TrainingData", Context.MODE_PRIVATE);
        loadProgress();

        addExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = exerciseNameInput.getText().toString();
                String reps = repsInput.getText().toString();
                String sets = setsInput.getText().toString();
                String data = dataInput.getText().toString();

                if (name.isEmpty() || reps.isEmpty() || sets.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int repsInt = Integer.parseInt(reps);
                int setsInt = Integer.parseInt(sets);
                Exercise newExercise = new Exercise(name, repsInt, setsInt,data);

                if (previousExercises.containsKey(name)) {
                    Exercise previousExercise = previousExercises.get(name);
                    if (repsInt > previousExercise.getReps() || setsInt > previousExercise.getSets()) {
                        Toast.makeText(MainActivity.this, "Progress made in " + name + "!", Toast.LENGTH_SHORT).show();
                    }
                }

                previousExercises.put(name, newExercise);
                exerciseList.add(newExercise);
                exerciseAdapter.notifyDataSetChanged();

                exerciseNameInput.setText("");
                repsInput.setText("");
                setsInput.setText("");
            }
        });

        saveProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProgress();
                Toast.makeText(MainActivity.this, "Progress saved!", Toast.LENGTH_SHORT).show();
            }
        });

        createNotificationChannel();
        scheduleReminder();
    }

    private void saveProgress() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> exerciseSet = new HashSet<>();

        for (Exercise exercise : exerciseList) {
            exerciseSet.add(exercise.getName() + "," + exercise.getReps() + "," + exercise.getSets());
        }

        editor.putStringSet("Exercises", exerciseSet);
        editor.apply();
    }

    private void loadProgress() {
        Set<String> exerciseSet = sharedPreferences.getStringSet("Exercises", new HashSet<>());

        for (String exerciseData : exerciseSet) {
            String[] parts = exerciseData.split(",");
            if (parts.length == 3) {
                String name = parts[0];
                int reps = Integer.parseInt(parts[1]);
                int sets = Integer.parseInt(parts[2]);
                String date = "";
                Exercise exercise = new Exercise(name, reps, sets,date);
                exerciseList.add(exercise);
                previousExercises.put(name, exercise);
            }
        }

        exerciseAdapter.notifyDataSetChanged();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "TrainingReminderChannel";
            String description = "Channel for training reminders";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("trainReminder", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void scheduleReminder() {
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long timeAtButtonClick = System.currentTimeMillis();
        long tenSecondsInMillis = 1000 * 10;

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeAtButtonClick + tenSecondsInMillis,
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}