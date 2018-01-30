#! /usr/bin/python

import sys
import os
import numpy
import math

# Here's what we're doing here:
# for each pair (size, proba) 
#     forall round: {rounds table}
#     open file
#     grab att + stdev
#     grab densite + stdev 
#     add to history
# Calc mean and stdev over history
# Storing as key, value
# keys are tuples (size, proba)
# values are ()

probas = [0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]
nodes =  [20, 30, 40, 50, 60, 70, 80, 90, 100, 120, 140, 160, 200]
experiences = [0, 1, 2, 3, 4]
total_xps = len(probas)*len(nodes)*len(experiences)

print "Parsing files in ", sys.argv[1]
print "Gonna treat", total_xps , "files."
files_treated = 0
files = os.listdir(sys.argv[1])
l_skipped = 0

for proba in probas:
    for size in nodes:
        atts = []
        atts_stdev = []
        ers = []
        ers_stdev = []
        densities = []
        densities_stdev = []
        for bench in experiences:
            files_treated = files_treated + 1
            progress = float(files_treated)/float(total_xps)*100
            print '%.2f%% done (%d of %d), %d files total, %d lines skipped so far' % (progress, files_treated, total_xps, len(files)/2, l_skipped)
            fname = "cfg_bench_" + str(size) + "_" + str(proba) + "_" + str(bench) +".results"
            try: 
                with open(os.path.join(sys.argv[1], fname)) as f:
                    #print "Successfully opened file " + str(fname)
                    line = f.readline().strip()
                    if len(line) == 0:
                        print "Empty line, skip"
                        l_skipped = l_skipped+1
                        continue
                    
                    # Here files are formatted as follows: 
                    # line0: att_mean, att_stdev, er_mean, er_stdev
                    # line1: densite_mean, dens_stdev, stdev/densi (ignore)
                    att_mean, att_stdev, er_mean, er_stdev = line.strip().split(";")
                    atts.append(float(att_mean))
                    atts_stdev.append(float(att_stdev))
                    ers.append(float(er_mean))
                    ers_stdev.append(float(er_stdev))
                    line = f.readline()
                    densi_mean, densi_stdev, _ = line.strip().split(";")
            except (OSError, IOError) as e:
                continue
        
        # 1 Bench treatment done, do maths on values, clean tables
        np_atts = numpy.array(atts)
        np_atts_stdev = numpy.array(atts_stdev)
        np_er = numpy.array(ers)
        np_er_stdev = numpy.array(ers_stdev)

        ccol1 = ("%.4f %s %.4f") % (numpy.average(np_atts), "+-", numpy.average(np_atts_stdev))
        ccol2 = ("%.4f %s %.4f") % (numpy.average(np_er), "+-", numpy.average(np_er_stdev))
        print "Atts", ccol1
        print "Ers", ccol2, "\n", 
