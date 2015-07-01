#!/bin/bash
## Script fast execution, fewest possible debug options

BASE_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
if [ -z "$GRAAL_HOME" ]; then
  if [ -d "$BASE_DIR/../graal" ]; then
    GRAAL_HOME="$BASE_DIR/../graal"
  elif [ -d "$BASE_DIR/../GraalVM" ]; then
    GRAAL_HOME="$BASE_DIR/../GraalVM"
  elif [ -d '/home/smarr/Projects/SOM/graal' ]; then
    GRAAL_HOME='/home/smarr/Projects/SOM/graal'
  elif [ -d '/Users/smarr/Projects/PostDoc/Truffle/graal' ]; then
    GRAAL_HOME='/Users/smarr/Projects/PostDoc/Truffle/graal'
  else
    echo "Please set GRAAL_HOME, could not be found automatically."
    exit 1
  fi
fi

STD_FLAGS="-G:-TraceTruffleInlining \
           -G:-TraceTruffleCompilation \
           -G:+TruffleCompilationExceptionsAreFatal "
#-G:+TruffleSplitting 

if [ -z "$GRAAL_FLAGS" ]; then
  GRAAL_FLAGS="$STD_FLAGS "
fi

if [ ! -z "$DBG" ]; then
  GRAAL_DEBUG_SWITCH='-d'
fi

if [ ! -z "ASSERT" ]; then
  USE_ASSERT="-esa -ea "
fi

exec $GRAAL_HOME/mxtool/mx $GRAAL_DEBUG_SWITCH --vm server vm $GRAAL_FLAGS $GF \
   -Xss160M $USE_ASSERT \
   -Xbootclasspath/a:build/classes:$GRAAL_HOME/build/truffle.jar \
   -classpath target/golo-3.0.0-SNAPSHOT-distribution/golo-3.0.0-SNAPSHOT/etc:target/golo-3.0.0-SNAPSHOT-distribution/golo-3.0.0-SNAPSHOT/lib/asm-5.0.3.jar:target/golo-3.0.0-SNAPSHOT-distribution/golo-3.0.0-SNAPSHOT/lib/json-simple-1.1.1.jar:target/golo-3.0.0-SNAPSHOT-distribution/golo-3.0.0-SNAPSHOT/lib/jcommander-1.48.jar:target/golo-3.0.0-SNAPSHOT-distribution/golo-3.0.0-SNAPSHOT/lib/txtmark-0.13.jar:target/golo-3.0.0-SNAPSHOT-distribution/golo-3.0.0-SNAPSHOT/lib/golo-3.0.0-SNAPSHOT.jar \
   -Dapp.name=golo -Dapp.pid=10979 \
   -Dapp.repo=target/golo-3.0.0-SNAPSHOT-distribution/golo-3.0.0-SNAPSHOT/lib \
   -Dapp.home=target/golo-3.0.0-SNAPSHOT-distribution/golo-3.0.0-SNAPSHOT \
   -Dbasedir=target/golo-3.0.0-SNAPSHOT-distribution/golo-3.0.0-SNAPSHOT \
   fr.insalyon.citi.golo.cli.Main golo --truffle --files \
   "$@"
