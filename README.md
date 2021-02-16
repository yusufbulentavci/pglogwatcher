# pglogwatcher
pglogwatcher service for linux systems

- process postgresql logs in csv format
- convert csv lines into json in different file
- json namespace is compatible with elastic stack
- rollover; keeps track of new csv file, rollover when needed
- online; tails csv file and process new csv lines
- duration; extract duration value as a json key
- summarizes; follow session and merges parse-bind-command-command csv line into one json line with extra duration parameters.
- virtual session; adds virtual session paramter, 'discard all' increases virtual session id
- telnet terminal to monitor

# installation
- Only for linux systems
- Download and extract pglogwatcher-install.tar.gz
- Run install.sh in pglogwatcher directory
- Service is ready start, before starting let's configure
- Service runs with postgres user

# configuration
In postgresql.conf
```
log_filename = 'postgresql-%Y-%m-%d_%H_%M_%S'
log_destination = 'csvlog'	
log_line_prefix = ''
log_directory = '/xxx/log/'	
```

In /etc/pglogwatcher.ini
```
[dir-1]
path=/xxx/log
```
# start
```
systemctl start pglogwatcher.service
systemctl enable pglogwatcher.service
```
# monitor
```
systemctl status pglogwatcher.service
log file /var/log/pglogwatcher/pglogwatcher.log
```
# telnet
```
>telnet 127.0.0.1 2300
>status
>exit
```
