#!/usr/bin/env sh
##############################################################################
# Gradle start up script for UN*X
#
# - From the Gradle Distribution (adapted)
##############################################################################

progname="$0"
args="$@"

# Determine the installation directory
PRG="$0"
# need this for relative symlink support
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/""$link"
  fi
done

PRGDIR=`dirname "$PRG"`
exec "$PRGDIR/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null

# Fallback to using java -jar if jar exists
if [ -f "$PRGDIR/gradle/wrapper/gradle-wrapper.jar" ] ; then
  java -jar "$PRGDIR/gradle/wrapper/gradle-wrapper.jar" "$@"
else
  echo "Gradle wrapper jar not found. Run 'gradle wrapper --gradle-version 8.14' to generate it, or install Gradle locally." >&2
  exit 1
fi

