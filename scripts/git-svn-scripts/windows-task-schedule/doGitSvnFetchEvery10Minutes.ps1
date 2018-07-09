
# start log transaction
$ErrorActionPreference="SilentlyContinue"
Stop-Transcript | out-null
$ErrorActionPreference = "Continue" # or "Stop"

# Obtain RSQE home directory
$RSQE_HOME = "$PSScriptRoot\..\..\.."
Start-Transcript -path $RSQE_HOME\git-svn.log -append

# Fetch latest repository code
cd "$RSQE_HOME"

# blow away the db cache files to prevent a recurring error: "Could not unmemoize function `lookup_svn_merge', because it was not memoized to begin with at..."
# See http://stackoverflow.com/questions/3135477/git-svn-rebase-resulted-in-byte-order-is-not-compatible-error
Get-ChildItem -Path E:\work\SQE\git-svn\rsqe\.git\svn\.caches -Include *.* -File -Recurse | foreach { $_.Delete()}


Write-Host "Running git svn fetch --fetch-all against..."
pwd
git svn fetch --fetch-all

# stop log transaction
Stop-Transcript

# sleep to allow me to see the result (success or failure)
Start-Sleep -Seconds 10



