#!/bin/bash

function doPull {
  STASH=$(git status -s | grep -v "^??" | wc -l | sed 's/ //g')
  if [ ${STASH} -eq "0" ]
  then
    echo "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
    echo "No local changes so no need to stash"
    git checkout master
    git svn rebase --all
  else
    echo "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
    echo "ALERT:  Local changes so stashing before pulling"
    
    # Ensure empty stash before stashing local changes
    git stash clear
    git stash
    
    # Now, git pull
    git checkout master
    git svn fetch --all
    git svn rebase --all
    
    # only stash pop if there's something on the stash list
  	STASH_LENGTH=$(git stash list | wc -l | sed 's/ //g')
    if [ ${STASH_LENGTH} -ne "0" ]; then
      git stash pop
    fi
  fi
}

doPull
echo
