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

/**
 * Plugin that sets the version of a project according to the git description of the most
 * recent commit.
 */
class GitVersionPlugin implements Plugin<Project> {

    void apply(Project project) {
        def gitDir = findGitDirectory(project.projectDir)
        def gitWrapper = getGitWrapper(gitDir)
        def target = getTargetObject(gitWrapper, project.projectDir, gitDir)
        def version = getVersion(gitWrapper, target)
        project.version = version
    }

    /**
     * Walks up the file system attempting to find a directory or file named '.git'.
     *
     * @param startingDir The directory to start looking in
     * @return The located .git file or folder, or {@code null} if not found
     */
    static def findGitDirectory(File startingDir) {
        def target = startingDir
        def gitDir = new File(target, '.git')
        while (!gitDir.exists() && target.parentFile != null) {
            target = target.parentFile
            gitDir = new File(target, '.git')
        }

        return gitDir
    }

    /**
     * Reads the contents of a .git file from a submodule, and follows the link to determine the
     * actual location of the git repository.
     *
     * @param linkFile The file containing the git link
     * @return The path to the actual git repository
     */
    static def findLinkedGitDirectory(File linkFile) {
        return new File(linkFile.parentFile, linkFile.text.replace('gitdir: ','').trim())
    }

    /**
     * Creates a JGit Git wrapper for the specified git directory.
     *
     * @param gitDir The git directory (or submodule link file) to create a wrapper for.
     * @return A Git wrapper for the given directory.
     */
    static def getGitWrapper(File gitDir) {
        def resolvedGitDir = gitDir.isDirectory() ? gitDir : findLinkedGitDirectory(gitDir)
        return Git.wrap(new FileRepositoryBuilder().setGitDir(resolvedGitDir).build())
    }

    /**
     * Gets the Object ID of the most recent commit to the project directory.
     *
     * @param gitWrapper The git wrapper to use.
     * @param projectDir The directory to limit commits to. Any commits that do not touch this
     * directory will be ignored.
     * @param gitDir The .git directory or file for the given project
     * @return The Object ID of the most recent commit in the project.
     */
    static def getTargetObject(Git gitWrapper, File projectDir, File gitDir) {
        def path = getRelativePath(gitDir.parentFile, projectDir)
        // TODO: Handle the case where there are no commits in a project.
        return getLogCommand(gitWrapper, path).call().first().id
    }

    /**
     * Creates and configures a log command, in order to get the most recent commit.
     *
     * @param gitWrapper The git wrapper to use.
     * @param path The relative path to limit the log to. Ignored if empty.
     * @return A log command configured to list the single most recent commit in the path.
     */
    static def getLogCommand(Git gitWrapper, String path) {
        def log = gitWrapper.log()
        if (!path.empty) {
            log.addPath(path)
        }
        return log.setMaxCount(1)
    }

    /**
     * Gets the relative path between two files.
     *
     * @param from The file to use as a base
     * @param to The file the relative path should point to
     * @return A relative path between the two files
     */
    static def getRelativePath(File from, File to) {
        return from.toPath().relativize(to.toPath()).toString()
    }

    /**
     * Constructs a version string for the specified Object ID.
     *
     * @param gitWrapper The git wrapper to use
     * @param objectId The Object ID of the commit to get a version for
     * @return The git-describe version string for the object ID, with '-SNAPSHOT' appended.
     */
    static def getVersion(Git gitWrapper, ObjectId objectId) {
        def gitVersion = gitWrapper.describe().setTarget(objectId).call()
        // TODO: Add a setting to enable releases
        return gitVersion + '-SNAPSHOT'
    }

}
