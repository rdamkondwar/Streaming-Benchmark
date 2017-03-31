
#count=$#
#echo $count
project=$1
hostname=$2

remote_mach="rohitsd@$hostname.$project.spark-heron-pg0.utah.cloudlab.us"
ssh -t $remote_mach 'rm -rf /users/rohitsd/Streaming-Benchmark'
#echo $remote_mach
ssh -t $remote_mach 'git clone https://github.com/rdamkondwar/Streaming-Benchmark.git'
ssh -t $remote_mach 'sudo bash /users/rohitsd/Streaming-Benchmark/scripts/prereq_install.sh'
