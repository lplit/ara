#!/usr/bin/env perl
use strict;
use warnings;



my $key_control_init =
my $str_network_size = "network.size";
my $str_random_seed = "random.seed";
my $str_init = "Initialisation";
my @scopes;
my $scope;
my $file_base;

my $file_first = "
debug_config none
simulation.endtime 50000
random.seed 4
network.size 50

init.initialisation Initialisation

control.graph.positionprotocol position
control.graph.time_slow 0.0002
control.graph.step 1
control.graph.emitter emitter
control.graph.neighborprotocol neighbor

control.density DensityController
control.density.neighbours neighbor
control.density.step 1000
control.density.verbose 1

protocol.position PositionProtocolImpl
protocol.position.maxspeed 20
protocol.position.minspeed 1
protocol.position.width 1500
protocol.position.height 1500
protocol.position.pause 1200000

protocol.emitter EmitterImpl
protocol.emitter.latency 90

protocol.emitter.positionprotocol position

protocol.neighbor NeighborProtocolImpl
protocol.neighbor.period 3000
protocol.neighbor.timer_delay 3500


initial_position_strategy Strategy1InitNext
next_destination_strategy Strategy1InitNext

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

$file_base = "cfg_bench";

my  $strat_position;
my $strat_move;
my $i;

my $bench_dir = "bench";

mkdir $bench_dir;

my $run_cmd = "make CFG";
my $key_cfg = "CFG";
my $key_peersim = "DIR_PEERSIM";
my $val_peersim = $ARGV[0];


$strat_position = 1;
$strat_move = 1;
$value_spi = "Strategy1InitNext";
$value_sd = "Strategy1InitNext";

for (my $j = 0; $j < 5; $j++) {
    bench($strat_move, $strat_position, $value_spi, $value_sd);
}



$strat_position = 3;
$strat_move = 3;
$value_spi = "Strategy3InitNext";
$value_sd = "Strategy3InitNext";

for (my $j = 0; $j < 5; $j++) {
    bench($strat_move, $strat_position, $value_spi, $value_sd);
}



sub bench {
    my $i;
    my $str_mov = shift;
    my $str_pos = shift;
    my $val_spi = shift;
    my $val_sd = shift;;
    my $results_file;


    print "\n\n", $str_mov, " ", $str_pos, " ", $val_spi, " ", $val_sd, "\n-------\n";

    for ($i = 125; $i <= 1000; $i += 125) {
	my $filename =  join "_", $file_base, $scope, $str_pos, $str_mov;
	push @scopes, $i;
	$filename = join "/", $bench_dir, $filename;
	open (my $bench_file, '>', $filename) or die "Could not open file ";
	print $bench_file $file_first, "\n", join(" ", $key_spi, $value_spi), "\n";
	print $bench_file join(" ", $key_sd, $value_sd), "\n";
	print $bench_file join(" ", $key_scope, $i), "\n";
	$run_cmd = join(" ",
			"make run",
			join("=", $key_cfg, $filename),,
			join("=", $key_peersim, $val_peersim)
	    );
	print $run_cmd;
	print "\n";
	#    print $bench_file
	close $filename;
	print "\nOUAIS", `$run_cmd`;
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
