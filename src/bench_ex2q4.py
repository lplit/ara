#! /usr/bin/python

import sys
import os
import numpy
import math

nodes           = [20, 30, 40, 50, 60, 70, 80, 90, 100, 120, 140, 160, 200]
experiences     = [0, 1, 2, 3, 4]
files_treated   = 0 # Total files visited
l_skipped       = [] 
total_xps       = len(nodes)*len(experiences)
skipped_lines   = [] # Debug shit
fails           = [] # Debug shit, files not found
csv_summaries   = [] # This gets dumped to file at the end

print "Parsing files in ", sys.argv[1]
print "Gonna treat", total_xps , "files."

files = os.listdir(sys.argv[1])
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
        fname = "cfg_bench_" + str(size) + "_" + str(iteration) +".results"
        try: 
            with open(os.path.join(sys.argv[1], fname)) as f:
                line = f.readline().strip()
                if len(line) == 0 or line[0] == "nan":
                    print "Empty line, skip"
                    l_skipped.append((size, iteration))
                    skipped_lines.append(line)
                    fails.append(("l1_empty", fname))
                    continue

                att_mean, att_stdev, er_mean, er_stdev = line.strip().split(";")

                line = f.readline().strip()
                if len(line) == 0 or line[0] == "nan":
                    print "Empty line, skip"
                    l_skipped.append((size, iteration))
                    skipped_lines.append(line)
                    fails.append(("l2_empty", fname))
                    continue

                # At this point we know the file is valid, can count it
                bench_files_opened += 1

                densi_mean, densi_stdev, _ = line.strip().split(";")

                # 'replace' calls for barbaric locales using ',' as splitter
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
    np_atts = numpy.average(numpy.array(atts))
    np_atts_stdev = numpy.average(numpy.array(atts_stdev))
    np_er = numpy.average(numpy.array(ers))
    np_er_stdev = numpy.average(numpy.array(ers_stdev))
    np_den = numpy.average(numpy.array(densities))
    np_den_stdev = numpy.average(numpy.array(densities_stdev))

    c_atts = ("%.4f %s %.4f") % (np_atts, "+-", np_atts_stdev)
    c_er = ("%.4f %s %.4f") % (np_er, "+-", np_er_stdev)
    c_dens = ("%.4f %s %.4f") % (np_den, "+-", np_den_stdev)

    progress = float(files_treated)/float(total_xps)*100

    # CSV much
    line = ("%d;%.2f;%.2f;%.2f;%.2f;%.2f") \
            % (size, np_atts, np_atts_stdev,\
                    np_er, np_er_stdev, np_den)
    
    csv_summaries.append(line)
    print line

    print '[%.2f%%](%d/%d) - %d files\nN: %d' % \
        (progress, files_treated, total_xps, bench_files_opened, size)
    print "Atts\t", c_atts
    print "Ers\t", c_er
    print "Dens\t", c_dens, "\n"

print "Errors"
for e in zip(fails, l_skipped, skipped_lines):
    print e

print
# Save to file
csv_title = "size;att;att_stdev;er;er_stdev;den"
print csv_title
for r in csv_summaries:
    print r


with open(os.path.join(sys.argv[1], "summary.csv"), 'w+') as f:
    f.write(csv_title + os.linesep)
    for r in csv_summaries:
        f.write(r + os.linesep)
    f.close()