# AntTracker

## Overview

AntTracker is a command-line-based issue-tracking system.
It is designed to log bug reports or feature requests and tie them to issues related to your software products.
These issues can then be updated as they are processed by your engineering team,
allowing for clear insights into what issues have been resolved,
and what remains to be worked on.

AntTracker can also log the releases of your software products,
which can then be associated with these issues.
This enables users to see what release fixed a particular issue,
and also see what issues of a product will be resolved in its next upcoming release.
There is no need to keep hundreds of paper bug-report forms neatly stored;
AntTracker does all the organization for you.

## Authors

The authors of AntTracker are
Angela Kim, Eitan Barylko, Micah Baker, and Tyrus Tracey,
part of the software development team at BitCrunch Corporation,
a totally real and non-fictional corporate conglomerate in the tech industry.

## Installation

### Developers

If you are a developer working on AntTracker,
installing the software should be done by compiling the source code.
It is recommended to work on AntTracker from a UNIX environment:
MacOS or Linux (WSL included).

The necessary tooling a developer of AntTracker needs is:

- The `git` version-control system
- The `gradle` build and testing system for Java and Kotlin
- The IntelliJ software development IDE

Given these prerequisites,
your development environment can be set up as follows:

- Clone the AntTracker Git repository: `git clone git@github.com:AntTracker/AntTracker.git (destination path)`
- Open the `(destination path)` in IntelliJ
- Compile AntTracker: `gradle install` into the IntelliJ terminal
- Start AntTracker: `./build/install/AntTracker/bin/AntTracker (command-line option`
