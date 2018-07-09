#!/bin/bash
CONF_XML=$1

declare -a APPS_LIST
APPS_LIST=$(cat $CONF_XML | awk '
BEGIN {x=0}
{
    if ($0~"<Application id") {x=1; print "_"}
    if (x==1) {printf "%s", $0}
    if ($0~"</Application>") {x=0}
}')

IFS_ORIG=$IFS
IFS=$'_'

for app in $APPS_LIST
do
    if [ $app ]
    then
        IFS=$' \n\r\t'
        id="$(echo $app | cut -d'"' -f2)"
        host="$(echo $app | cut -d'"' -f6)"
        port=$(echo $app | cut -d'"' -f8)

        echo "$CONF_XML : $id $port"

        IFS=$'_'
    fi
done

IFS=$IFS_ORIG
