from subprocess import call

def changeUsr(newUsr):
    file = open('/etc/squid3/squid.conf.backup', 'r')
    file_w = open('/etc/squid3/squid.conf', 'w')
    lines = file.readlines()

    for line in lines:
        if(line.startswith('acl allowed_sites')):
            file_w.write('acl allowed_sites dstdom_regex -i "/etc/squid3/' + newUsr + '.acl"\n')
        else:
            file_w.write(line)

    call(['squid3', '-k', 'reconfigure'])

#changeUsr('teste')
changeUsr('allow')
