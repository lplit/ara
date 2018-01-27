#!/usr/bin/env perl
use strict;
use warnings;



my $str_network_size = "network.size";
my $str_random_seed = "random.seed";
my $str_init = "Initialisation";
my $scope;
my $file_base;

my $file_first =
    "debug_config none

SECONDES 1000
MINUTES 60*SECONDES
HEURES 60*MINUTES

network.size 50
simulation.endtime 8*HEURES


init.initialisation Initialisation

control.graph.positionprotocol position
control.graph.time_slow 0.0002
control.graph.step 1
control.graph.emitter emitter
control.graph.neighborprotocol neighbor

control.density DensityController
control.density.neighbours neighbor
control.density.from 1*HEURES
control.density.step 2*MINUTES
control.density.verbose 1


protocol.position PositionProtocolImpl
protocol.position.maxspeed 20
protocol.position.minspeed 1
protocol.position.width 1500
protocol.position.height 1500
protocol.position.pause 20*MINUTES

protocol.emitter EmitterImpl
protocol.emitter.latency 90

protocol.emitter.positionprotocol position

protocol.neighbor NeighborProtocolImpl2
protocol.neighbor.period 3000
protocol.neighbor.timer_delay 3500

    ";

my $file_second = "
initial_position_strategy.positionprotocol position
initial_position_strategy.emitter emitter


next_destination_strategy.positionprotocol position
next_destination_strategy.emitter emitter
next_destination_strategy.distance_min 100
next_destination_strategy.distance_max 500
";

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

$bench_dir = join("_", "bench", date());


system("mkdir $bench_dir");
if (system(join(" ", "make", $cmd_peersim)) != 0) {
    print "make failed.\n";;
    exit;
}

sub get_config_filename() {
    return join "/", $bench_dir, $filename;
}






$strat_position = 1;
$strat_move = 1;
$value_spi = "Strategy1InitNext";
$value_sd = "Strategy1InitNext";

bench($strat_move, $strat_position, $value_spi, $value_sd);

$strat_position = 3;
$strat_move = 3;
$value_spi = "Strategy3InitNext";
$value_sd = "Strategy3InitNext";

bench($strat_move, $strat_position, $value_spi, $value_sd);



sub bench {
    my $i;
    my $str_mov = shift;
    my $str_pos = shift;
    my $val_spi = shift;
    my $val_sd = shift;;
    my $results_file;

    print "\nBenchmark directory ", $bench_dir, "\n", $str_mov, " ", $str_pos, " ", $val_spi, " ", $val_sd, "\n-------\n";

    for ($i = 125; $i <= 1000; $i += 125) {
	for (my $k = 0; $k < 5; $k++) {
	    $random_seed = rand(100);
	    my $filename =  join "_", $file_base, $i, $str_pos, $str_mov;
	    $filename = join "/", $bench_dir, $filename;
	    open (my $bench_file, '>', $filename) or die "Could not open file ";
	    print $bench_file $file_first, "\n", join(" ", $key_spi, $value_spi), "\n";
	    print $bench_file join(" ", $key_sd, $value_sd), "\n";
	    print $bench_file join(" ", $key_scope, $i), "\n";
	    print $bench_file join(" ", $str_random_seed, $random_seed, "\n");
	    print $bench_file $file_second;
	    $run_cmd = join(" ",
			    "make run",
			    join("=", $key_cfg, $filename),
			    join("=", $key_peersim, $val_peersim)
		);
	    print "Executing ", $run_cmd, ", iteration ", $k, "\n";

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
    }
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
