#!/bin/bash

# mixed, dB, w=10
java -jar ../build/scanCSD.jar -N 100x -GM --initmean 0.1 --initsdev 0.01 --mutation 0.01 --mutationsdev 0.01 --mutationtype g --costfcn 1 --benefitfcn 11 --c1 2 --c2 -0.2,-0.75,0.011 --b1 1.4,2.6,0.024 --b2 -0.25 --intertype a --popupdate d --numinter 4 --selection 10 --fitnessmap exp --generations 10000 --reportfreq 10 --distribution --seed=0 --append mixed.b1c2w10dB.data

# mixed, dB, w=100
#java -jar ../build/scanCSD.jar -N 100x -GM --initmean 0.1 --initsdev 0.01 --mutation 0.01 --mutationsdev 0.01 --mutationtype g --costfcn 1 --benefitfcn 11 --c1 2 --c2 -0.2,-0.75,0.011 --b1 1.4,2.6,0.024 --b2 -0.25 --intertype a --popupdate d --numinter 4 --selection 100 --fitnessmap exp --generations 10000 --reportfreq 10 --distribution --seed=0 --append mixed.b1c2w100dB.data

# mixed, dB, w=1
#java -jar ../build/scanCSD.jar -N 100x -GM --initmean 0.1 --initsdev 0.01 --mutation 0.01 --mutationsdev 0.01 --mutationtype g --costfcn 1 --benefitfcn 11 --c1 2 --c2 -0.2,-0.75,0.011 --b1 1.4,2.6,0.024 --b2 -0.25 --intertype a --popupdate d --numinter 4 --selection 1 --fitnessmap exp --generations 10000 --reportfreq 10 --distribution --seed=0 --append mixed.b1c2w1dB.data
