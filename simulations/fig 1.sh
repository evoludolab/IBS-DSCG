#!/bin/bash

# load prll
source ~/.bash_profile

# mixed, Bd & dB, w=1
#prll -c 8 -s 'java -jar ../build/scanCSD.jar \
#--benefitfcn 0  --b1 1 --b2 0 --costfcn 0 --c1 $1 --c2 0 \
#--geometry M --initmean 0.1 --initsdev 0.01 --intertype a \
#--mutation 0.01 --mutationsdev 0.01 --mutationtype g \
#--numinter 1 --popsize 100x --popupdate B --fitnessmap exp --selection 1 \
#--reportfreq 10 --generations 10000 --distribution --seed=0' \
#$(seq 0 0.01 1.01) > mixed.w1Bd.data

prll -c 8 -s 'java -jar ../build/scanCSD.jar \
--benefitfcn 0  --b1 1 --b2 0 --costfcn 0 --c1 $1 --c2 0 \
--geometry M --initmean 0.1 --initsdev 0.01 --intertype a \
--mutation 0.01 --mutationsdev 0.01 --mutationtype g \
--numinter 1 --popsize 100x --popupdate d --fitnessmap exp --selection 1 \
--reportfreq 10 --generations 10000 --distribution --seed=0' \
$(seq 0 0.01 1.01) > mixed.w1dB.data

# mixed, Bd & dB, w=10
#prll -c 8 -s 'java -jar ../build/scanCSD.jar \
#--benefitfcn 0  --b1 1 --b2 0 --costfcn 0 --c1 $1 --c2 0 \
#--geometry M --initmean 0.1 --initsdev 0.01 --intertype a \
#--mutation 0.01 --mutationsdev 0.01 --mutationtype g \
#--numinter 1 --popsize 100x --popupdate B --fitnessmap exp --selection 10 \
#--reportfreq 10 --generations 10000 --distribution --seed=0' \
#$(seq 0 0.01 1.01) > mixed.w10Bd.data

prll -c 8 -s 'java -jar ../build/scanCSD.jar \
--benefitfcn 0  --b1 1 --b2 0 --costfcn 0 --c1 $1 --c2 0 \
--geometry M --initmean 0.1 --initsdev 0.01 --intertype a \
--mutation 0.01 --mutationsdev 0.01 --mutationtype g \
--numinter 1 --popsize 100x --popupdate d --fitnessmap exp --selection 10 \
--reportfreq 10 --generations 10000 --distribution --seed=0' \
$(seq 0 0.01 1.01) > mixed.w10dB.data

# mixed, Bd & dB, w=100
#prll -c 8 -s 'java -jar ../build/scanCSD.jar \
#--benefitfcn 0  --b1 1 --b2 0 --costfcn 0 --c1 $1 --c2 0 \
#--geometry M --initmean 0.1 --initsdev 0.01 --intertype a \
#--mutation 0.01 --mutationsdev 0.01 --mutationtype g \
#--numinter 1 --popsize 100x --popupdate B --fitnessmap exp --selection 100 \
#--reportfreq 10 --generations 10000 --distribution --seed=0' \
#$(seq 0 0.01 1.01) > mixed.w100Bd.data

prll -c 8 -s 'java -jar ../build/scanCSD.jar \
--benefitfcn 0  --b1 1 --b2 0 --costfcn 0 --c1 $1 --c2 0 \
--geometry M --initmean 0.1 --initsdev 0.01 --intertype a \
--mutation 0.01 --mutationsdev 0.01 --mutationtype g \
--numinter 1 --popsize 100x --popupdate d --fitnessmap exp --selection 100 \
--reportfreq 10 --generations 10000 --distribution --seed=0' \
$(seq 0 0.01 1.01) > mixed.w100dB.data

# neumann, Bd & dB, w=1
#prll -c 8 -s 'java -jar ../build/scanCSD.jar \
#--benefitfcn 0  --b1 1 --b2 0 --costfcn 0 --c1 $1 --c2 0 \
#--geometry n --initmean 0.1 --initsdev 0.01 --intertype a \
#--mutation 0.01 --mutationsdev 0.01 --mutationtype g \
#--numinter 1 --popsize 100x --popupdate B --fitnessmap exp --selection 1 \
#--reportfreq 10 --generations 10000 --distribution --seed=0' \
#$(seq 0 0.01 1.01) > neumann.w1Bd.data

prll -c 8 -s 'java -jar ../build/scanCSD.jar \
--benefitfcn 0  --b1 1 --b2 0 --costfcn 0 --c1 $1 --c2 0 \
--geometry n --initmean 0.1 --initsdev 0.01 --intertype a \
--mutation 0.01 --mutationsdev 0.01 --mutationtype g \
--numinter 1 --popsize 100x --popupdate d --fitnessmap exp --selection 1 \
--reportfreq 10 --generations 10000 --distribution --seed=0' \
$(seq 0 0.01 1.01) > neumann.w1dB.data

# neumann, Bd & dB, w=10
#prll -c 8 -s 'java -jar ../build/scanCSD.jar \
#--benefitfcn 0  --b1 1 --b2 0 --costfcn 0 --c1 $1 --c2 0 \
#--geometry n --initmean 0.1 --initsdev 0.01 --intertype a \
#--mutation 0.01 --mutationsdev 0.01 --mutationtype g \
#--numinter 1 --popsize 100x --popupdate B --fitnessmap exp --selection 10 \
#--reportfreq 10 --generations 10000 --distribution --seed=0' \
#$(seq 0 0.01 1.01) > neumann.w10Bd.data

prll -c 8 -s 'java -jar ../build/scanCSD.jar \
--benefitfcn 0  --b1 1 --b2 0 --costfcn 0 --c1 $1 --c2 0 \
--geometry n --initmean 0.1 --initsdev 0.01 --intertype a \
--mutation 0.01 --mutationsdev 0.01 --mutationtype g \
--numinter 1 --popsize 100x --popupdate d --fitnessmap exp --selection 10 \
--reportfreq 10 --generations 10000 --distribution --seed=0' \
$(seq 0 0.01 1.01) > neumann.w10dB.data

# neumann, Bd & dB, w=100
#prll -c 8 -s 'java -jar ../build/scanCSD.jar \
#--benefitfcn 0  --b1 1 --b2 0 --costfcn 0 --c1 $1 --c2 0 \
#--geometry n --initmean 0.1 --initsdev 0.01 --intertype a \
#--mutation 0.01 --mutationsdev 0.01 --mutationtype g \
#--numinter 1 --popsize 100x --popupdate B --fitnessmap exp --selection 100 \
#--reportfreq 10 --generations 10000 --distribution --seed=0' \
#$(seq 0 0.01 1.01) > neumann.w100Bd.data

prll -c 8 -s 'java -jar ../build/scanCSD.jar \
--benefitfcn 0  --b1 1 --b2 0 --costfcn 0 --c1 $1 --c2 0 \
--geometry n --initmean 0.1 --initsdev 0.01 --intertype a \
--mutation 0.01 --mutationsdev 0.01 --mutationtype g \
--numinter 1 --popsize 100x --popupdate d --fitnessmap exp --selection 100 \
--reportfreq 10 --generations 10000 --distribution --seed=0' \
$(seq 0 0.01 1.01) > neumann.w100dB.data

