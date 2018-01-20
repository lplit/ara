#! /usr/bin/python

import sys
import os
import numpy
import math

print "Parsing files in ", sys.argv[1]
for file in os.listdir(sys.argv[1]):
    if file.endswith(".results"):
        p = os.path.join(sys.argv[1], file)
        with open(p) as f:
            c1 = []
            c2 = []
            c3 = []
            lines = f.readlines()
            for line in lines:
                col1, col2, col3 = line.split(';')
                col3 = float(col3.strip().replace(',', '.'))
                col2 = float(col2.strip().replace(',', '.'))
                col1 = float(col1.strip().replace(',', '.'))

                c1.append(col1)
                c2.append(col2)
                c3.append(col3)

            nc1 = numpy.array(c1)
            nc2 = numpy.array(c2)
            nc3 = numpy.array(c3)

            print "\n"
            print f.name, " over ", len(lines), "iterations."
            ccol1 = ("%.4f %s %.4f") % (numpy.average(nc1), "+-", numpy.std(nc1))
            ccol2 = ("%.4f %s %.4f") % (numpy.average(nc2), "+-", numpy.std(nc2))
            ccol3 = ("%.4f %s %.4f") % (numpy.average(nc3), "+-", numpy.std(nc3))
            print "|", ccol1, "|", ccol2, "|", ccol3, "|"
