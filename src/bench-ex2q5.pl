#!/usr/bin/env perl
use strict;
use warnings;



my $str_network_size = "network.size";
my $str_random_seed = "random.seed";
my $str_init = "Initialisation";
my $scope;
my $file_base;

my $file_first = "
SECONDES 1000
MINUTES 60*SECONDES
HEURES 60*MINUTES
VERB 0

debug_config none
simulation.endtime 12*HEURES

init.initialisation Initialisation

control.graph.positionprotocol position
control.graph.time_slow 0.0002
control.graph.step 1
control.graph.emitter emitter
control.graph.neighborprotocol neighbor

protocol.gossip GossipProtocolImpl
protocol.gossip.verbose VERB

control.gossipcontroller GossipController
control.gossipcontroller.nb_diffusions 500
control.gossipcontroller.at 0
control.gossipcontroller.emitter emitter
control.gossipcontroller.position position

control.density DensityController
control.density.neighbours neighbor
control.density.from 1*HEURES
control.density.step 2*MINUTES
control.density.verbose VERB

protocol.position PositionProtocolImpl
protocol.position.maxspeed 20
protocol.position.minspeed 1
protocol.position.width 1500
protocol.position.height 1500
protocol.position.pause 20*MINUTES

protocol.emitter ProbabilisticEmitter
protocol.emitter.latency 90
protocol.emitter.scope 300
protocol.emitter.verbose VERB
protocol.emitter.positionprotocol position

protocol.neighbor NeighborProtocolImpl
protocol.neighbor.period 3000
protocol.neighbor.timer_delay 3500
protocol.neighbor.verbose VERB


initial_position_strategy Strategy5Init
next_destination_strategy Strategy4Next

initial_position_strategy.positionprotocol position
initial_position_strategy.emitter emitter
initial_position_strategy.distance_init_min 200
initial_position_strategy.distance_init_max 200


next_destination_strategy.positionprotocol position
next_destination_strategy.emitter emitter
next_destination_strategy.distance_min 200
next_destination_strategy.distance_max 200
    ";

my @nodes = (20, 30, 40, 50, 60, 70, 80, 90, 100, 120, 140, 160, 200);
my @probabilities = (0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0);

my $key_emitter_scope = "protocol.emitter.scope";
my $key_spi = "initial_position_strategy";
my $key_sd = "next_destination_strategy";
my $key_scope = "protocol.emitter.scope";
my $value_spi;
my $value_sd;

my $filename;

srand(localtime);

$file_base = "cfg_bench";

my  $strat_position;
my $strat_move;
my $i;

my $random_seed;

my $run_cmd;
my $key_cfg = "CFG";


my $key_peersim = "DIR_PEERSIM";
my $val_peersim = $ARGV[0];
my $cmd_peersim = join("=", $key_peersim, $val_peersim);

my $key_net_size = "network.size";
my $key_proba = "protocol.emitter.probability";


sub date {
#    my $dt   = DateTime->now;   # Stores current date and time as datetime object
#    my $date = $dt->ymd;   # Retrieves date as a string in 'yyyy-mm-dd' format
#    my $time = $dt->hms;   # Retrieves time as a string in 'hh:mm:ss' format

#    my $wanted = "$date $time";   # creates 'yyyy-mm-dd hh:mm:ss' string
#    return join("_",$dt->ymd, dt->hms);
#    return localtime->strftime('%F-%X');
    my $ret = `date +%F-%T\n`;
    chomp $ret;
    print "date: ", $ret, "\n";
    return $ret;
}


my $bench_dir;

$bench_dir = join("_", "bench_ex2q5", date());
$bench_dir = join("/", "results", $bench_dir);

print "bench_dir: ", $bench_dir, ", \n";

system("mkdir -p $bench_dir");


sub get_config_filename() {
    return join "/", $bench_dir, $filename;
}



print "\nBenchmark directory ", $bench_dir, "\n", "\n-------\n";

# bench(size)
my $it;
my $runs = 5;

# Faut faire gaffe au nombre de runs. Là on a une boucle allant de 20 à 200 noeuds
# puis sur chaque noeud elle itère sur toutes les probabilités. Tout ça dans une
# boucle $number_of_exps.
my $all_exps = $runs * scalar @nodes * scalar @probabilities;
my $iteration = 0;
for (my $number_of_exps = 0; $number_of_exps < $runs ; $number_of_exps++) {

    foreach my $size (@nodes)
	# calcul des ebscisses densités déjà
    {
	foreach my $proba (@probabilities) {
	    my $filename =  join "_", $file_base, $size, $proba, $number_of_exps;
	    $filename = join "/", $bench_dir, $filename;
	    print "iteration ", $iteration, "of exp", $all_exps, "\n";
	    print $filename;
	    print "\n";
	    bench($size, $filename, $proba);
#	    SubRoutine($Element);
	    $iteration++;
	}
    }


}


if (system(join(" ", "make", $cmd_peersim)) != 0) {
    print "make failed.\n";;
    exit;
}


sub bench {
    my $i;
    my $net_size = shift;
    my $filename = shift;
    my $probability = shift;

    my $results_file;


    $random_seed = rand(100);

    open (my $bench_file, '>', $filename) or die "Could not open file ";
    print $bench_file join(" ", $key_net_size, $net_size), "\n";
    #	    print $bench_file $file_first, "\n", join(" ", $key_spi, $value_spi), "\n";
    #	    print $bench_file join(" ", $key_sd, $value_sd), "\n";
    #	    print $bench_file join(" ", $key_scope, $i), "\n";
    print $bench_file $file_first, "\n";
    print $bench_file join(" ", $str_random_seed, $random_seed, "\n");
    print $bench_file join(" ", $key_proba, $probability);
    $run_cmd = join(" ",
		    "make run",
		    join("=", $key_cfg, $filename),
		    join("=", $key_peersim, $val_peersim)
	);
    print "Executing `", $run_cmd, "`\n";

    close $filename;
    if (system($run_cmd) != 0) {
	print "run_cmd failed.\n";
    }


    $results_file = join(".", $filename, "results");
    print "Results file ", $results_file, "seed ", $random_seed, "\n";
    open (my $bench_res, '<', $results_file) or die "Could not open ", $results_file, "\n";
    #	$results_file =~ s/^$//g;
    print "Results file:\n";
    print $results_file;
    print "\n\n";
    close($results_file);
}



    #[
    #~pod


#control.graph GraphicalMonitor
#control.graph.positionprotocol position
#control.graph.time_slow 0.0002
#control.graph.step 1
#control.graph.emitter emitter
#control.graph.neighborprotocol neighbor

#control.density DensityController
#control.density.neighbours neighbor
#control.density.step 1000
#control.density.verbose 1

#protocol.position PositionProtocolImpl
#protocol.position.maxspeed 20
#protocol.position.minspeed 1
#protocol.position.width 1500
#protocol.position.height 1500
#protocol.position.pause 1200
#
#protocol.emitter EmitterImpl
#protocol.emitter.latency 90
#protocol.emitter.scope 125
#protocol.emitter.positionprotocol position
#
#protocol.neighbor NeighborProtocolImpl
#protocol.neighbor.period 3000
#protocol.neighbor.timer_delay 3500
#
#
#initial_position_strategy Strategy5Init
#next_destination_strategy Strategy1InitNext
#
#initial_position_strategy.positionprotocol position
#initial_position_strategy.emitter emitter
#
#
#next_destination_strategy.positionprotocol position
#next_destination_strategy.emitter emitter
#next_destination_strategy.distance_min 100
#next_destination_strategy.distance_max 500
#
#    ~cut
# ]
#
