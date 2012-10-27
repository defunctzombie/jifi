# jifi

```
This project is not maintained and exists here for reference only
```

Jifi is a java based program loader for legacy [FRC controllers](http://www.ifirobotics.com/rc.shtml) designed by [IFI robotics](http://www.ifirobotics.com/). It should also work on old vex robotics controllers from the same era.

![Java IFI Loader](http://shtylman.github.com/jifi/screenshot.png)

## use

Jifi can be used as a GUI or CLI app.

```shell
$ jifi <device> <hex file>
```

## install

* Windows: [jifi_0.1.3_win32.zip](http://shtylman.com/misc/legacy/frc/jifi/jifi_0.1.3_win32.zip)
* Linux: [jifi_0.1.3_linux32.tar.gz](http://shtylman.com/misc/legacy/frc/jifi/jifi_0.1.3_linux32.tar.gz)

## protocol

The protocol is based on Microchip [Application Note 851](http://www.microchip.com/stellent/idcplg?IdcService=SS_GET_PAGE&nodeId=1824&appnote=en012031) with some additional elements not present in the application node.

See the [wiki](https://github.com/shtylman/jifi/wiki) for protocol details.

## history

Jifi was developed to be able to download code and view terminal output for the old FRC robotics system in Linux. This was when they were still using Microchip chips and downloading code over a serial cable. At the time, there was not good way to do this in Linux. Many existing C based loaders that were no longer maintained and did not work for certain versions of the FRC system. The java loader works on both Linux and Windows platforms (sorry macs) and provides similar functionality as the old IFI loader (code download and terminal view).

