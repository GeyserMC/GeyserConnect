# GeyserConnect with DNS
This contains the basic bind9 configs for use with GeyserConnect

## Setup
1. Install bind9 using `sudo apt install bind9`
2. Download these files and place them inside `/etc/bind/` overwriting the `named.conf.local` file
3. Run `sudo service bind9 restart` to restart the service