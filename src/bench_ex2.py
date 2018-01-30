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

print "Parsing files in ", sys.argv[1]
for file in os.listdir(sys.argv[1]):
    # #if file.endswith(".results"):
    # p = os.path.join(sys.argv[1], file)
    # print file
    # size = file.split("_")[2]
    # prob = file.split("_")[3]
    # wave = file.split("_")[4].split(".")[0]
    # print size, prob, wave
    for proba in probas:
        for size in nodes:
            atts = []
            atts_stdev = []
            ers = []
            ers_stdev = []
            densities = []
            densities_stdev = []

            for bench in experiences:
                fname = "cfg_bench_" + str(size) + "_" + str(proba) + "_" + str(bench) +".results"
                with open(os.path.join(sys.argv[1], fname)) as f:
                    print "Successfully opened file " + str(fname)
                    line = f.readline()
                    # Here files are formatted as follows: 
                    # line0: att_mean, att_stdev, er_mean, er_stdev
                    # line1: densite_mean, dens_stdev, stdev/densi (ignore)
                    att_mean, att_stdev, er_mean, er_stdev = line.strip().split(";")
                    atts.append(float(att_mean))
                    atts_stdev.append(float(att_stdev))
                    ers.append(er_mean)
                    ers_stdev.append(er_stdev)
                    print "Line1", att_mean, att_stdev, er_mean, er_stdev
                    line = f.readline()
                    densi_mean, densi_stdev, _ = line.strip().split(";")
                    print "Line2", densi_mean, densi_stdev
                    print "Key", (proba, size), "value:"
                    print
            # 1 Bench treatment done, do maths on values, clean tables
            np_atts = numpy.array(atts)
            np_atts_stdev = numpy.array(atts_stdev)
            np_er = numpy.array(ers)
            np_er_stdev = numpy.array(ers_stdev)


            ccol1 = ("%.4f %s %.4f") % (numpy.average(np_atts), "+-", numpy.average(np_atts_stdev))
            print ccol1
            ccol2 = ("%.4f %s %.4f") % (numpy.average(nc2), "+-", numpy.std(nc2))
            ccol3 = ("%.4f %s %.4f") % (numpy.average(nc3), "+-", numpy.std(nc3))
            print "|", ccol1, "|", ccol2, "|", ccol3, "|"

            atts.clear()
            atts_stdev.clear()
            ers.clear()
            ers_stdev.clear()


        # with open(p) as f:
        #     c1 = []
        #     c2 = []
        #     c3 = []
        #     lines = f.readlines()
        #     for line in lines:
        #         col1, col2, col3 = line.split(';')
        #         col3 = float(col3.strip().replace(',', '.'))
        #         col2 = float(col2.strip().replace(',', '.'))
        #         col1 = float(col1.strip().replace(',', '.'))

        #         c1.append(col1)
        #         c2.append(col2)
        #         c3.append(col3)

        #     nc1 = numpy.array(c1)
        #     nc2 = numpy.array(c2)
        #     nc3 = numpy.array(c3)

        #     print "\n"
        #     print f.name, " over ", len(lines), "iterations."
        #     ccol1 = ("%.4f %s %.4f") % (numpy.average(nc1), "+-", numpy.std(nc1))
        #     ccol2 = ("%.4f %s %.4f") % (numpy.average(nc2), "+-", numpy.std(nc2))
        #     ccol3 = ("%.4f %s %.4f") % (numpy.average(nc3), "+-", numpy.std(nc3))
        #     print "|", ccol1, "|", ccol2, "|", ccol3, "|"
