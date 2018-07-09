#!/bin/bash

function doMasterPush {
    echo; echo "Doing git push on $1"
    cd $1
    git checkout master
    if [ $? != 0 ]; then echo ERROR_CHECKOUT_MASTER; exit 1; fi
    git svn dcommit
    if [ $? != 0 ]; then echo ERROR_PULL_MASTER; exit 2; fi
}

# add helper script to PATH
test ! -e ~/bin && mkdir ~/bin
cp ./helper/doStashAndPush.sh ~/bin 

# set root directory
SCRIPT_DIR=$PWD
RSQE_HOME=$SCRIPT_DIR/../..

# pull the latest RSQE code
cd $RSQE_HOME
doStashAndPush.sh

cd $SCRIPT_DIR

exit 0



