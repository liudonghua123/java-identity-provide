#!/usr/bin/env bash

declare LOCATION

LOCATION=$(dirname $0)

$LOCATION/runclass.sh net.shibboleth.idp.cli.impl.ModuleManagerCLI --ansi --home "$LOCATION/.." "$@"
