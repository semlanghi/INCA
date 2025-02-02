#!/bin/bash


#$1 RESULTS_DIR
#$2 NRECORDS_TO_PROCESS
#$3 GRANULARITY

MEMORY="15g"

for ((z=1; z<=2; z++)); do

  echo "Comparative Evaluation on Window Size/Slide"
  constraint_strictness=(1)
  window_size=(10 100 1000 10000)
  window_size_slide_factor=(2 5)


  name_experiment=("Issa")

  # Iterate over the array of strings
  for ((i=0; i<${#name_experiment[@]}; i++)); do
      echo "Starting Experiment: ${name_experiment[$i]}"
      for cs in "${constraint_strictness[@]}"; do
          echo "Current Constraint Strictness: $cs"
          for ws in "${window_size[@]}"; do
              echo "Current Window Size: $ws"
              for wssf in "${window_size_slide_factor[@]}"; do
                  echo "Current Size and Slide Factor: $wssf"
                  for ((j=1; j<=1; j++)); do
                      echo "Iteration $j"
                      topic="reviews-nrecords-$3"
                      java -Xmx"$MEMORY" -cp ../target/my-project-name-jar-with-dependencies.jar "com.limos.fr.${name_experiment[$i]}"Review "$cs" 0 "$((ws*1000))" $(((ws/wssf)*1000)) "$1" "$2" $(($2*2)) "$topic" &> ${name_experiment[$i]}-review.out &
                      wait
                      topic="gps-nrecords-$3"
                      java -Xmx"$MEMORY" -cp ../target/my-project-name-jar-with-dependencies.jar "com.limos.fr.${name_experiment[$i]}"GPS "$cs" 0 "$ws" $((ws/wssf)) "$1" "$2" $(($2*2)) "$topic" &> ${name_experiment[$i]}-gps.out &
                      wait
                      topic="linearroad-nrecords-$3"
                      java -Xmx"$MEMORY" -cp ../target/my-project-name-jar-with-dependencies.jar "com.limos.fr.${name_experiment[$i]}"LinearRoad "$cs" 0 "$((ws*1000))" $(((ws/wssf)*1000)) "$1" "$2" $(($2*2)) "$topic" &> ${name_experiment[$i]}-linearroad.out &
                      wait
                      topic="stocks-nrecords-$3"
                      java -Xmx"$MEMORY" -cp ../target/my-project-name-jar-with-dependencies.jar "com.limos.fr.${name_experiment[$i]}"StockCombo "$cs" 0 "$((ws*1000))" $(((ws/wssf)*1000)) "$1" "$2" $(($2*2)) "$topic" &> ${name_experiment[$i]}-stock-combo.out &
                      wait
                  done
              done
          done
      done
  done

done
