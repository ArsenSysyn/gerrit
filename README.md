# Deploy Gerrit to AWS (managed by me)
So for deploying Gerrit to AWS we use AWS ECS cluster for external bazel cache container, AWS EFS for storing cache and storing application data, AWS Beanstalk for run our application and a CodeBuild for run all our commands - terraform apply, bazel build, eb deploy. Also all our necessary infrastructure are defined using __Terraform__. 
All source code were forked from official repository Gerrit and there we have __3__ directories managed by me:
  
  - ebs/ - for Beanstalk;
  - aws/ - for Codebuild;
  - terraform/ - terraform code.

More details about all files that stored in these directories are describedd in __README.md__ files in these directories.

Then we have all info from official repository.

---

# Gerrit Code Review

[Gerrit](https://www.gerritcodereview.com) is a code review and project
management tool for Git based projects.

![Maven Central](https://img.shields.io/maven-central/v/com.google.gerrit/gerrit-war) 

## Objective

Gerrit makes reviews easier by showing changes in a side-by-side display,
and allowing inline comments to be added by any reviewer.

Gerrit simplifies Git based project maintainership by permitting any
authorized user to submit changes to the master Git repository, rather
than requiring all approved changes to be merged in by hand by the project
maintainer.

## Documentation

For information about how to install and use Gerrit, refer to
[the documentation](https://gerrit-review.googlesource.com/Documentation/index.html).

## Source

Our canonical Git repository is located on [googlesource.com](https://gerrit.googlesource.com/gerrit).
There is a mirror of the repository on [Github](https://github.com/GerritCodeReview/gerrit).

## Reporting bugs

Please report bugs on the [issue tracker](https://bugs.chromium.org/p/gerrit/issues/list).

## Contribute

Gerrit is the work of hundreds of contributors. We appreciate your help!

Please read the [contribution guidelines](https://gerrit.googlesource.com/gerrit/+/master/SUBMITTING_PATCHES).

Note that we do not accept Pull Requests via the Github mirror.

## Getting in contact

The Developer Mailing list is [repo-discuss on Google Groups](https://groups.google.com/forum/#!forum/repo-discuss).

## License

Gerrit is provided under the Apache License 2.0.

## Build

Install [Bazel](https://bazel.build/versions/master/docs/install.html) and run the following:

        git clone --recurse-submodules https://gerrit.googlesource.com/gerrit
        cd gerrit && bazel build release

## Install binary packages (Deb/Rpm)

The instruction how to configure GerritForge/BinTray repositories is
[here](https://gitenterprise.me/2015/02/27/gerrit-2-10-rpm-and-debian-packages-available/)

On Debian/Ubuntu run:

        apt-get update & apt-get install gerrit=<version>-<release>

_NOTE: release is a counter that starts with 1 and indicates the number of packages that have
been released with the same version of the software._

On CentOS/RedHat run:

        yum clean all && yum install gerrit-<version>[-<release>]

On Fedora run:

        dnf clean all && dnf install gerrit-<version>[-<release>]

## Use pre-built Gerrit images on Docker

Docker images of Gerrit are available on [DockerHub](https://hub.docker.com/u/gerritforge/)

To run a CentOS 8 based Gerrit image:

        docker run -p 8080:8080 gerritcodereview/gerrit[:version]-centos8

To run a Ubuntu 20.04 based Gerrit image:

        docker run -p 8080:8080 gerritcodereview/gerrit[:version]-ubuntu20

_NOTE: release is optional. Last released package of the version is installed if the release
number is omitted._
