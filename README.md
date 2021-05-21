# INCA 
This application allows data inconsistency profiling.

## Code structure

The project is a maven project. One has to install maven, at least version 3.6.3

## Execution 
#### cd IQ
####  mvn clean
####  mvn install
####  mvn spring-boot:run
####  Application is available on address: http://localhost:8080

## Tools 
Application is developed using springBoot in java (version 14.0.1), HTML, CSS and JavaScript. As mentioned above, it managed using maven.


## TopInc performance
To perform top-k query processing, we use our developped algorithm called TopInc algorithm. Below, we show by intensive experiment the performance our topInc algorithm.

The following figures show the performance running time of topInc compared to baseline algorithm (compute all answers, sort them and choose the k first.) Respectively for Q1, Q2 and Q8.

<p align="center">
  <img src="https://github.com/oussissa123/INCA/blob/master/time_Q1-1.png" width="290" alt="time running query Q1">
  <img src="https://github.com/oussissa123/INCA/blob/master/time_Q2-1.png" width="290" alt="time running query Q2">
  <img src="https://github.com/oussissa123/INCA/blob/master/time_Q8-1.png" width="290" alt="time running query Q8">
</p>

The following figures show the  foot print memory comparison of topInc with baseline algorithm (for Q1, Q2 and Q8 respectively)


<p align="center">
  <img src="https://github.com/oussissa123/INCA/blob/master/memory_Q1-1.png" width="290" alt="fp.memory query Q1">
  <img src="https://github.com/oussissa123/INCA/blob/master/memory_Q2-1.png" width="290" alt="fp.memory query Q2">
  <img src="https://github.com/oussissa123/INCA/blob/master/memory_Q8-1.png" width="290" alt="fp.memory query Q8">
</p>


More details can found in our paper https://dl.acm.org/doi/10.14778/3407790.3407815
