package com.example.train;

import java.util.Date;

public class Exercise {
        private String name;
        private int reps;
        private int sets;
        private String date;
        public Exercise(String name, int reps, int sets,String  data) {
            this.name = name;
            this.reps = reps;
            this.sets = sets;
            this.date=data;
        }

        public String getName() {
            return name;
        }
        public String getDate() {
            return this.date;
        }

        public int getReps() {
            return reps;
        }

        public int getSets() {
            return sets;
        }
}
