# StealthWeb: A Censor-Proof Distributed Web Service that Self-Replicates Failed Nodes
**Luca Ostertag-Hill and Dylan Hayton-Ruffner**

**May 14, 2019**

1. Our system is configured to run on 14 machines from the clusters. The files are located in dahayton directory on each machine and run by that user. 

2. To run our system, ssh into dahayton@54.209.66.61 and enter the DistributedStealthWeb folder. You can also just unpack the DistributedStealthWeb Folder in the tarball we turned in on the master. This shouldn’t be necessary though unless the servers have been wiped. Navigate to scripts/system_commands

3. To clear the system, which shouldn’t be neccessary, run (This will stop all running nodes and delete them):
    1. `./run_on_all_ips.sh stop_java.sh`
    2. `./run_on_all_ips.sh stop_httpd.sh` 
    3. `./run_on_all_ips.sh danger_delete_all.sh`



4. Run `./spawn_backend.sh`, this will load a node on each server
specified in the /system_config/starting-ip-list.txt file. Feel free to add and delete ips! This will take 3-4 minutes.

5. Navigate to /scripts/upload and run `./upload_files.sh`, this will upload all files in /system_config/dist-index.txt. Feel free to add more mappings and files. New files must be placed in the html directory in scripts. Specify all files names with a ‘/‘ in dist-index.txt.

6. Navigate back to system_commands and run `./start_backend.sh`, this will start all the apache servers on port 8505. These can be contacted from a browser for testing.

7. Open up a new terminal window and ssh to the master. Navigate to the src directory in DistributedStealthWeb. The command to start the RmiServer (or master) can be found in rmi_server_run_command.txt. Run that command and it should start the master. You will see a few initial loading messages and then regular output from the ping. In the event of a failure, it will pause and load new nodes.

8. In the other window, which should still be in the scripts directory, run `./start_proxies.sh`, a bunch of ips will print out and then a corresponding hello world message, as each proxy tests its connection to the master. This process may have to be manually exited, so give it a minute and then close it after the messages have stoped printing. 

9. TEST! Contact one of the proxies, running on port 8080, in a chrome browser and ask for one of the html documents! 

10. To cause failures use the induce_failure.sh script in the system_commands folder. Just pass it an ip to shut down like this: `./induce_failure.sh 3.83.10.22`
