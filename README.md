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

The following figures show the performance running time of topInc compared to baseline algorithm (compute all answers, sort them and choose the k first) <br\>
![alt time running query Q1](https://github.com/oussissa123/INCA/blob/master/time_Q1.pdf)<br\>
![alt time running query Q2](https://github.com/oussissa123/INCA/blob/master/time_Q2.pdf)<br\>
![alt time running query Q8](https://github.com/oussissa123/INCA/blob/master/time_Q8.pdf)<br\>

The following figures show the  foot print memory comparison of topInc with baseline algorithm <br\>
![alt fp.memory query Q1](https://github.com/oussissa123/INCA/blob/master/memory_Q1.pdf)<br\>
![alt fp.memory query Q2](https://github.com/oussissa123/INCA/blob/master/memory_Q2.pdf)<br\>
![alt fp.memory query Q8](https://github.com/oussissa123/INCA/blob/master/memory_Q8.pdf)
