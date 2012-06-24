#!/bin/bash
if [ ! -f lib/libvirt.so ]; then
    wget -O lib/libvirt.so http://s3.lstoll.net/dump/libvirt.so
fi
export LD_LIBRARY_PATH=./lib
lein test
