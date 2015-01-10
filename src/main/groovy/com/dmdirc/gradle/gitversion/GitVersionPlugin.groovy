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

package com.dmdirc.gradle.gitversion

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project

class GitVersionPlugin implements Plugin<Project> {

    void apply(Project project) {
        def gitDir = findGitDirectory(project)
        def gitWrapper = getGitWrapper(gitDir)
        def target = getTargetObject(gitWrapper, project.projectDir, gitDir)
        def version = getVersion(gitWrapper, target)
        project.version = version
    }

    static def findGitDirectory(Project project) {
        def target = project.projectDir
        def gitDir = new File(target, '.git')
        while (!gitDir.exists() && target.parentFile != null) {
            target = target.parentFile
            gitDir = new File(target, '.git')
        }

        return gitDir
    }

    static def findLinkedGitDirectory(File linkFile) {
        return new File(linkFile.parentFile, linkFile.text.replace('gitdir: ','').trim())
    }

    static def getGitWrapper(File gitDir) {
        def resolvedGitDir = gitDir.isDirectory() ? gitDir : findLinkedGitDirectory(gitDir)
        return Git.wrap(new FileRepositoryBuilder().setGitDir(resolvedGitDir).build())
    }

    static def getTargetObject(Git gitWrapper, File projectDir, File gitDir) {
        def path = getRelativePath(gitDir.parentFile, projectDir)
        // TODO: Handle the case where there are no commits in a project.
        return getLogCommand(gitWrapper, path).setMaxCount(1).call().first().id
    }

    static def getLogCommand(Git gitWrapper, String path) {
        def log = gitWrapper.log()
        if (!path.empty) {
            log.addPath(path)
        }
        return log.setMaxCount(1)
    }

    static def getRelativePath(File from, File to) {
        return from.toPath().relativize(to.toPath()).toString()
    }

    static def getVersion(Git gitWrapper, ObjectId objectId) {
        def gitVersion = gitWrapper.describe().setTarget(objectId).call()
        // TODO: Add a setting to enable releases
        return gitVersion + '-SNAPSHOT'
    }

}
