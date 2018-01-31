#! /usr/bin/python

import sys
import os
import numpy
import math

# Here's what we're doing here:
# for each pair (size, proba, bench) 
#     open file
#     grab att + stdev
#     grab densite + stdev 
#     add to history
# Calc mean and stdev over history
# values are ()

probas          = [0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1]
nodes           = [20, 30, 40, 50, 60, 70, 80, 90, 100, 120, 140, 160, 200]
experiences     = [0, 1, 2, 3, 4]
files_treated   = 0 # Total files visited
l_skipped       = [] 
total_xps       = len(probas)*len(nodes)*len(experiences)
skipped_lines   = [] # Debug shit
fails           = [] # Debug shit, files not found

print "Parsing files in ", sys.argv[1]
print "Gonna treat", total_xps , "files."

files = os.listdir(sys.argv[1])
for proba in probas:
    for size in nodes:
        atts = []
        atts_stdev = []
        ers = []
        ers_stdev = []
        densities = []
        densities_stdev = []
        bench_files_opened = 0

        # Here files are formatted as follows: 
        # line0: att_mean, att_stdev, er_mean, er_stdev
        # line1: densite_mean, dens_stdev, stdev/densi (ignore)
        for iteration in experiences:
            files_treated += 1
            fname = "cfg_bench_" + str(size) + "_" + str(proba) + "_" + str(iteration) +".results"
            try: 
                with open(os.path.join(sys.argv[1], fname)) as f:
                    line = f.readline().strip()
                    if len(line) == 0 or line[0] == "nan":
                        print "Empty line, skip"
                        l_skipped.append((proba, size, iteration))
                        skipped_lines.append(line)
                        continue

                    att_mean, att_stdev, er_mean, er_stdev = line.strip().split(";")

                    line = f.readline().strip()
                    if len(line) == 0 or line[0] == "nan":
                        print "Empty line, skip"
                        l_skipped.append((proba, size, iteration))
                        skipped_lines.append(line)
                        continue

                    # At this point we know the file is valid, can count it
                    bench_files_opened += 1

                    densi_mean, densi_stdev, _ = line.strip().split(";")

                    # 'replace' calls for barbarian locales using ',' as splitter
                    densities.append(float(densi_mean.replace(',', '.')))
                    densities_stdev.append(float(densi_stdev.replace(',', '.')))

                    atts.append(float(att_mean.replace(',', '.')))
                    atts_stdev.append(float(att_stdev.replace(',', '.')))

                    ers.append(float(er_mean.replace(',', '.')))
                    ers_stdev.append(float(er_stdev.replace(',', '.')))

            except (OSError, IOError) as e:
                fails.append((e, fname))
                continue
        
        # 1 Bench set treatment done, do maths on values, clean tables
        np_atts = numpy.array(atts)
        np_atts_stdev = numpy.array(atts_stdev)
        np_er = numpy.array(ers)
        np_er_stdev = numpy.array(ers_stdev)
        np_den = numpy.array(densities)
        np_den_stdev = numpy.array(densities_stdev)


        c_atts = ("%.4f %s %.4f") % (numpy.average(np_atts), "+-", numpy.average(np_atts_stdev))
        c_er = ("%.4f %s %.4f") % (numpy.average(np_er), "+-", numpy.average(np_er_stdev))
        c_dens = ("%.4f %s %.4f") % (numpy.average(np_den), "+-", numpy.average(np_den_stdev))

        progress = float(files_treated)/float(total_xps)*100

        print '[%.2f%%](%d/%d) - %d files\nP: %.2f N: %d' % \
            (progress, files_treated, total_xps, bench_files_opened, proba, size)
        print "Atts\t", c_atts
        print "Ers\t", c_er
        print "Dens\t", c_dens
        print l_skipped
        print skipped_lines, "\n\n"

print "Errors", fails