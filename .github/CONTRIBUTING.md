# Contributing to MD2K software development

MD2K supports and encourages contributions that further the functionality and
usefulness of our platform.

Table of Contents
=================

  * [Contributing to MD2K software development](#contributing-to-md2k-software-development)
    * [Git branches - persistent](#git-branches---persistent)
    * [Git branches - transient](#git-branches---transient)
    * [Git keys](#git-keys)
    * [Creating pull requests for upstream merges](#creating-pull-requests-for-upstream-merges)


## Git branches - persistent
- master: contain latest repository development efforts. All new features should
be based on this branches

## Git branches - transient
The following branch-naming patterns are intended for branches whose sole purpose
of existence is pull request submission. These branches should have concise naming.

Pull requests should only be made for concise-named branches that follow these patterns:

| format   | description                                                             |
|-----------------|-------------------------------------------------------------------------|
| bugfix/*        | pull request branch, contains one bugfix                                |
| documentation/* | pull request branch, contains documentation work                        |
| enhancement/*   | pull request branch, contains one enhancement to existing functionality |
| feature/*       | pull request branch, contain a new feature                              |
| refactoring/*   | pull request branch, contains code refactoring                          |

## Git keys
Repository keys are utilized to track released software versions and are baed on this guide (http://semver.org/)
which follow this pattern `vX.Y.Z`:
- `X`: MAJOR version when you make incompatible API changes
- `Y`: MINOR version when you add functionality in a backwards-compatible manner
- `Z`: PATCH version when you make backwards-compatible bug fixes.

## Creating pull requests for upstream merges

Commits:
- your commits should be easily readable, with concise comments;
- your commits should follow the KISS principle: do one thing, and do it well.

Branching:
- see section about branches above.
- branch your contribution branch from this repository's master branch;
- branch name: feature/my-shiny-new-snoopy-feature-title for new features;
- branch name: bugfix/my-totally-non-hackish-workaround for bugfixes;

Pull requests:
- one pull request should contain one change only
    (one bugfix or one feature at a time);
- if you have developed multiple features and/or bugfixes, create separate
    branches for each one of them, and request merges for each branch;
- MD2K uses Travis-CI for testing builds. When you submit a pull request,
    wait for Travis-CI to finish the build and see if everything went
    smoothly.
- the cleaner you code/change/changeset is, the faster it will be merged.
