Welcome!

Running robonobo
----------------
To run the program, execute the provided shell script 
'robonobo', which runs java with the right arguments and the robonobo jar
file.  Depending on your system, you may have to make this file executable:

$ chmod 755 robonobo

If you want to move this jarfile somewhere else, you will need to update 
the shell script to point to the new jar location.


Handling robonobo urls
----------------------
To automatically open playlist links in robonobo, you will need to 
associate the 'rbnb' protocol with robonobo on your system. Before you do
this, we recommend you put the robonobo jar and shell script somewhere
sensible, such as /usr/local/lib and /usr/local/bin - you will have to
update the shell script with the jar location.

If you are using GNOME, run the following commands (change 
/usr/local/bin/robonobo to wherever you have put the shell script)

$ gconftool -s /desktop/gnome/url-handlers/rbnb/enabled --type bool true
$ gconftool -s /desktop/gnome/url-handlers/rbnb/command --type string "/usr/local/bin/robonobo %s"
$ gconftool -s /desktop/gnome/url-handlers/rbnb/needs_terminal --type bool false

If you are using KDE, copy the file rbnb.protocol (which should be in this
directory) into the 'services' directory in your KDEHOME, which is probably
/usr/share/KDE4. If you have put the robonobo shell script somewhere other
than /usr/local/bin, edit the rbnb.protocol file with the new location
before you copy it.


For any questions or comments, see our feedback page at
http://getsatisfaction.com/robonobo or else email us at help@robonobo.com.
 
Thanks for using robonobo!
