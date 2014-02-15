README
======

![https://raw.github.com/joehms22/bmod/master/documents/logo_ideas/turb_empty_small.png](https://raw.github.com/joehms22/bmod/master/documents/logo_ideas/turb_empty_small.png)

Purpose
-------

Bmod (building modeler) is a program built for taking input data corresponding 
to buildings, rooms, devices, meetings held in the rooms, weather, etc. And
producing a predictive model of future energy usage based on this information.

This predictive model can then be used to generate cost-benefit reports for 
new technlologies like solar panels, HVAC improvements, scheduling adjustments
(especially in universities), etc.

Environment
-----------

Bmod was built in Java, and requires at least Java 7 in order to operate. It has
been tested to run on:

* Mac OSX Lion
* Linux (Ubuntu 12.10 +)
* Windows (xp | 7 | 8)

A headless environment has also been built that runs on Linux servers, the
core modeling engine will make use of as many processers as are available.

Domain and Range
----------------

After inputting all scheduling data, it is reasonable to expect a predicted 
power usage +/- 5% of the measured actual usage.

