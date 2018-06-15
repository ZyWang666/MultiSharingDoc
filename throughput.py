def calc_throughput():
    f = open("SingleBackendPerformance.txt")
    o = open("output.txt", 'w')
    for line in f:
        data = line.split(' ')
        o.write('%s\n' % (str(int(data[0])*int(data[1])/float(data[2]))))