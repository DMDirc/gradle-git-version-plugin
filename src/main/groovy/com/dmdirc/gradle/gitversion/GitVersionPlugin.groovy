/*
 * Copyright (c) 2006-2015 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.gradle.gitversion;

import org.mdonoughe.JGitDescribeTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

class GitVersionPlugin implements Plugin<Project> {

    void apply(Project project) {
        def jgit = new JGitDescribeTask()
        jgit.dir = getGitDirectory(project)

        def subdir = getRelativeSubdir(project, jgit.dir)
        if (!subdir.isEmpty()) {
            jgit.subdir = subdir
        }

        def version = jgit.description
        project.version = getProjectVersion(version)
    }

    File getGitDirectory(Project project) {
        def target = project.projectDir
        def gitDir = new File(target, '.git')
        while (!gitDir.exists() && target.parentFile != null) {
            target = target.parentFile
            gitDir = new File(target, '.git')
        }
        return gitDir
    }

    String getRelativeSubdir(Project project, File gitDir) {
        def parent = gitDir.parentFile.absolutePath
        def target = project.projectDir.absolutePath
        return target.substring(parent.length())
    }

    String getProjectVersion(String gitVersion) {
        if (gitVersion.matches('.*-[0-9]+-[0-9a-f]+$')) {
            return gitVersion + '-SNAPSHOT'
        } else {
            return gitVersion
        }
    }

}
