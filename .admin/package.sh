#!/bin/sh
#
# Helper script to create cleaned up snapshots
#

rm casquatch-snapshot.tgz
tar $(for l in $(cat .gitignore); do echo -n '--exclude '$l' ';done)  -czf casquatch-snapshot.tgz *
