Gradle Git version plugin
================================================================================

This plugin automatically sets the version of a Gradle project based on the
current git revision.

## Features and description

### Version format

Versions are based on the output of `git describe`. A typical version set by
the plugin will look something like:

    0.2.3-23-gabcabcf-SNAPSHOT

The first part is the most recent git tag (in this case `0.2.3`), then comes
the number of revisions since that tag (`23`), and the abbreviated hash of the
current commit prepended with a 'g' (`gabcabcf`). As of v1.0, the plugin always
appends the '-SNAPSHOT' designator.

### Submodule support

The plugin can be used on a normal git repository, or from within a submodule.
If it finds it is in a submodule, it will locate the correct `.git` directory
within the parent repository.

### Multi-project support

If you have multiple projects within a directory, it is often desirable to
version them independently. For projects that are not at the root of a
repository, the plugin will use the version of the last commit *in that
project's directory*. That is, if you have the following folder structure and
last commits:

    myRepository
    `-- .git
    `-- project1                    # 0.3-329-gabc1234
        `-- src                     # 0.6-1-g2342345
    `-- project2                    # 1.0
        `-- src                     # 1.1-17-g009911c

If project1 and project2 both apply this plugin, project 1's version will be
set to `0.6-1-g2342345-SNAPSHOT`, while project 2's version will be set to
`1.1-17-g009911c-SNAPSHOT`, as they are the two most recent commits within
those projects.

## How to use

The plugin is hosted on bintray. To add it to a project, add the following
dependency and repository to your `build.gradle`:

    buildscript {
        repositories {
            maven { url 'https://dl.bintray.com/dmdirc/releases/' }
        }
        dependencies {
            classpath group: 'com.dmdirc', name: 'git-version', version: '1.0'
        }
    }

And then apply the plugin:

    apply plugin: 'com.dmdirc.git-version'

It's generally best to apply the version plugin before other plugins (such as
the maven-publish plugin) as they may try and use the version.
