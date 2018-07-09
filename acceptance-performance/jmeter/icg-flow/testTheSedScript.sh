#!/usr/bin/bash

git checkout icg-flow-test-plan.jmx

 ./fixTheIcgJmeterFile.sh icg-flow-test-plan.jmx

git diff -w icg-flow-test-plan.jmx
