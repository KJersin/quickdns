# Quickdns cli and Certbot authenticator hook

This is a small tool for accessing the DNS records via the command line of the Danish DNS service provider 
[Quickdns](https://www.quickdns.dk).

It's main purpose is to allow for fully scripted retrieval- and renewal of Letsencrypt [wildcard certificates](https://letsencrypt.org/docs/faq/#does-let-s-encrypt-issue-wildcard-certificates).  
I does so by being able to act as an [authentication hook](https://eff-certbot.readthedocs.io/en/stable/using.html#hooks) for [Certbot](https://certbot.eff.org/) which does the actual acme communication with [Letsencrypt](https://letsencrypt.org/).

## It works like this (dry run example)

```shell
$ sudo certbot certonly --manual --preferred-challenges=dns \
  --manual-auth-hook "quickdns-certbot-auth -l $USER" \
  --manual-cleanup-hook "quickdns-certbot-auth -l $USER" \
  --dry-run -d example.com
[sudo] password for user: 
Saving debug log to /var/log/letsencrypt/letsencrypt.log
Simulating a certificate request for example.com
Hook '--manual-auth-hook' for example.com ran with output:
 _acme-challenge succesfull inserted and succesfull requested via DNS lookup
The dry run was successful.
```

### Comments

`-l $USER`: Is the user the quickdns application will run as.  
Certbot has to run as root (private keys are only to be read by root). But the `--manual-auth-hook` application does not need root privilege to do it's job and unfortunatly `certbot` does not obay to the [principle of least privilege](https://en.wikipedia.org/wiki/Principle_of_least_privilege) when calling the hook application with root privilege. The shell script `quickdns-certbot-auth` tries to rectify this error by lowering the privilege level before calling the actual `quickdns` application.

`--manual`: Means that `certbot` won't setup a timer service to automatically renew the certificate every 60 days.  
To me it's not at big deal as this particullar job is a minor thing in a larger scheme controlled by an ansible playbook to distribute the certifacate(s) to multiple servers used for https proxy, database connection encryption, etc.  
BUT: it should be possible though. Let me know if this is needed. Or even better, has information of how to do it.

`succesfull requested via DNS lookup`: The `quickdns` application waits for the newly inserted DNS record to show up on the global DNS network before returning control to `certbot`. It does so by repeatetly querying the Google dns service [https://dns.qoogle/resolve] ([human readable version](https://dns.google/query?name=&rr_type=TXT&ecs=)) every 7 seconds. At max for 2½ minutes.  
During testing I found that it on avarage takes about 1½ minutes for the records to show up. But I have seen times as low as 15 seconds.

## Installation

### [Certbot](https://certbot.eff.org/)

__Most distros (using snap)__:  
`sudo snap install --classic certbot ; sudo ln -s /snap/bin/certbot /usr/bin/certbot`

### [Java 17+](https://openjdk.org/)

I settled on targiting java version 17 as this seems to be the default jdk for the conservative distros like Debian (bookworm) and the latest enterprise versions from Redhat. But any version of 17 or above should be sufficent. I myself are using version 21 as my default java (Arch Linux).

__Arch Linux and variants__  
`sudo pacman -S jdk-openjdk`

__Debian/Ubuntu/Raspberry PI OS, etc.__:  
`sudo apt install default-jdk`

### [Quickdns](https://github.com/KJersin/quickdns)

`wget -q -O - https://repo.jersin.dk/repository/maven-public/dk/jersin/quickdns/1.2/quickdns-1.2-dist.tar.gz | sudo tar --no-same-owner -zxvf - -C /usr/bin`

## Usage

### First run

```
$ quickdns
*** No command given and the program is NOT running as a Certbot authentication hook
Usage: quickdns [-hV] [-c=<configPath>] [-u=<url>] [COMMAND]
Edit Quickdns records
  -c, --configuration=<configPath>
                    Configuration containing Quickdns login information and default url
                      Default: ~/.java/.userPrefs/quickdns.conf
                    Note: To change (or view) the configuration use the command:
                      quickdns configure
  -h, --help        Show this help message and exit.
  -u, --url=<url>   Root page
                      Default: https://www.quickdns.dk
  -V, --version     Print version information and exit.
Commands:
  acme       Inserts, removes or lists _acme-challenge record(s)
             NOTE: If both --insert and --clear are specified then --clear is excuted before --insert
                   If neither --insert nor --clear is specified the current (if any) _acme-challenge record(s) are listed
  zones      List all available Zones
  records    List records for one or more domains
  configure  Sets the default configuration location and optional the Quickdns login and url arguments:
               Values set are automatically used when quickdns is run without any (or a subset) of the configuration arguments.
  certbot    Handles the Certbot authentication hooks by using arguments from the CERTBOT_* environment variables.
             See: https://eff-certbot.readthedocs.io/en/stable/using.html#hooks
             Also: Please use the bash script quickdns-certbot-auth to avoid running as root.
Configuration (/home/user/.java/.userPrefs/quickdns.conf):
  email   : <not-set>
  password: <not-set>
  url     : https://www.quickdns.dk
To finish the configuration use the command:
  quickdns configure --email=<enter-your-login-email-here> --password
NOTE: Do not supply your password on the command line. You will be prompted.
```

### Configure

Next step is to follow the instructions above `To finish the configuration use the command:`  
```
$ quickdns configure --email=anders@andeby.dk --password
Enter value for --password (Login password): 
Configuration (/home/user/.java/.userPrefs/quickdns.conf):
  email   : anders@andeby.dk
  password: ***
  url     : https://www.quickdns.dk

```

### Test login and list some entries

`quickdns zones`

`quickdns records example.com` _Substitute example.com with one or more of the zone names (domains) returned from the zone listning above_

__NOTE__: The output of the listnings are fairly rough (standard java `object.toString()` kind of output). I might change that in a future version, but initially those listnings wasn't the goal but merely a result of data I allready had obtained in order to fullfill my main objective of inserting and removing _acme-challenge records.

### Help

All commands has help associated just by running the command without arguments (in a future version I will actually implement `-h`).  
For the `acme` command this amounts to:

```
$ quickdns acme
Missing required parameter: '<domain>'
Usage: quickdns acme [-lrw] [-c=<configPath>] [-i=<validation>] [-u=<url>] <domain>
Inserts, removes or lists _acme-challenge record(s)
NOTE: If both --insert and --clear are specified then --clear is excuted before --insert
      If neither --insert nor --clear is specified the current (if any) _acme-challenge record(s) are listed
      <domain>      Domain of the records
  -c, --configuration=<configPath>
                    Configuration containing Quickdns login information and default url
                      Default: ~/.java/.userPrefs/quickdns.conf
                    Note: To change (or view) the configuration use the command:
                      quickdns configure
  -i, --insert=<validation>
                    Insert an _acme-challenge TXT record
  -l, --list        List _acme-challenge TXT record(s)
  -r, --clear       Removes all existing _acme-challenge TXT record(s)
  -u, --url=<url>   Root page
                      Default: https://www.quickdns.dk
  -w, --wait        Wait for the inserted _acme-challenge record to show up on https://dns.google/resolve
                    A maximum waittime of 147 seconds is inforced. In which case the program will exit with code 147
                    NOTE: Only valid in combination with --insert
```

### Certbot integration

I allready showed this in the intro. But just to be complete:

```shell
$ sudo certbot certonly --manual --preferred-challenges=dns \
  --manual-auth-hook "quickdns-certbot-auth -l $USER" \
  --manual-cleanup-hook "quickdns-certbot-auth -l $USER" \
  --dry-run -d example.com
```

## Windows or Mac

As for as I understand Certbot is fully supported on these platforms as well. And my quickdns application should be able to run as long as a java version 17 or higher has been installed. But there might be some minor kinks with configuration file locations etc. to be sorted.

If you're using one of these two platforms and are interrested in using the quickdns application then please give me a shout. I do have at Windows installation (for gaming) and a Mac M1 laptop for the road.

Or simply try it out. The quickdns application itself consists of a single runnable (uber) jar (dependencies are sharded in). So simply download the jar file [quickdns-1.2.jar](https://repo.jersin.dk/repository/maven-public/dk/jersin/quickdns/1.2/quickdns-1.2.jar) and run it.

```
java -jar quickdns-1.2.jar
```

## Final words

I hope that this will be usefull for others than me and any kind of suggestions or comments are welcome.

/ Kim Jersin