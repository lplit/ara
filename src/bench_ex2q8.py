#! /usr/bin/python

# A run pour chaque valeur de K 



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
print_summaries = [] # For printing

print "Parsing files in ", sys.argv[1]
print "Gonna treat", total_xps , "files."

files = os.listdir(sys.argv[1])

for tmpf in files:
    if not tmpf.endswith(".results" ):
        continue

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
    files_treated += 1
    with open(os.path.join(sys.argv[1], tmpf)) as f:

        line = f.readline().strip()
        if len(line) == 0 or line[0] == "nan":
            print "First line empty, skip"
            fails.append(("l1_empty", tmpf))
            continue

        att_mean, att_stdev, er_mean, er_stdev = line.split(";")

        line = f.readline().strip() 
        if len(line) == 0 or line[0] == "nan":
            print "Second line empty, skip"
            fails.append(("l2_empty", tmpf))
            continue
        
        densi_mean, densi_stdev, _ = line.strip().split(";")


        # At this point we know the file is valid, can count it
        bench_files_opened += 1


        # 'replace' calls for barbaric locales using ',' as splitter
        den = float(densi_mean.replace(',', '.'))
        dendev = float(densi_stdev.replace(',', '.'))
        densities.append(den)
        densities_stdev.append(dendev)

        att = float(att_mean.replace(',', '.'))
        attdev = float(att_stdev.replace(',', '.'))
        atts.append(att)
        atts_stdev.append(attdev)

        er = float(er_mean.replace(',', '.'))
        erdev = float(er_stdev.replace(',', '.'))
        ers.append(er)
        ers_stdev.append(erdev)

        csv_line = ("%.2f;%.2f;%.2f;%.2f;%.2f") \
                % (att, attdev, er, erdev, den)
        csv_summaries.append(csv_line)

        print_line = ("| %.2f | %.2f | %.2f | %.2f | %.2f |") \
                % (att, attdev, er, erdev, den)
        print_summaries.append(print_line)

    
    # 1 Bench set treatment done, do maths on values, clean tables
# np_atts = numpy.average(numpy.array(atts))
# np_atts_stdev = numpy.average(numpy.array(atts_stdev))
# np_er = numpy.average(numpy.array(ers))
# np_er_stdev = numpy.average(numpy.array(ers_stdev))
# np_den = numpy.average(numpy.array(densities))
# np_den_stdev = numpy.average(numpy.array(densities_stdev))

# c_atts = ("%.4f %s %.4f") % (np_atts, "+-", np_atts_stdev)
# c_er = ("%.4f %s %.4f") % (np_er, "+-", np_er_stdev)
# c_dens = ("%.4f %s %.4f") % (np_den, "+-", np_den_stdev)

# progress = float(files_treated)/float(total_xps)*100

# CSV much
# csv_line = ("%.2f;%.2f;%.2f;%.2f;%.2f") \
#         % (np_atts, np_atts_stdev,\
#                 np_er, np_er_stdev, np_den)

# print_line = ("%.2f & %.2f & %.2f & %.2f & %.2f \\ \hline") \
#         % (np_atts, np_atts_stdev,\
#                 np_er, np_er_stdev, np_den)

# print_summaries.append(print_line)
# csv_summaries.append(csv_line)

# print line

## Gonna spit out results in the following format 
## att ; att stdev ; er ; er stdev ; densite 

# print '[%.2f%%](%d/%d) - %d files\nN: %d' % \
#     # (progress, files_treated, total_xps, bench_files_opened, size)
# print "Atts\t", c_atts
# print "Ers\t", c_er
# print "Dens\t", c_dens, "\n"
# print
# Save to file

print "Fails:"
for e in fails: 
    print e

print

csv_title = "att;att_stdev;er;er_stdev;den"
print_title = "|att|att_stdev|er|er_stdev|den|"

print print_title
for r in print_summaries:
    print r


with open(os.path.join(sys.argv[1], "summary.csv"), 'w+') as f:
    f.write(csv_title + os.linesep)
    for r in csv_summaries:
        f.write(r + os.linesep)
    f.close()