#!/bin/bash

function doMasterPull {
    echo; echo "Doing git pull on $1"
    cd $1
    git checkout master
    if [ $? != 0 ]; then echo ERROR_CHECKOUT_MASTER; exit 1; fi
    git svn fetch --all
    if [ $? != 0 ]; then echo ERROR_PULL_MASTER; exit 2; fi
    git svn rebase --all
    if [ $? != 0 ]; then echo ERROR_PULL_MASTER; exit 2; fi
}

# add helper script to PATH
test ! -e ~/bin && mkdir ~/bin
cp ./helper/doStashAndPull.sh ~/bin 

# set root directory
SCRIPT_DIR=$PWD
RSQE_HOME=$SCRIPT_DIR/../..

# pull the latest RSQE code
cd $RSQE_HOME
doStashAndPull.sh

cd $SCRIPT_DIR

exit 0



