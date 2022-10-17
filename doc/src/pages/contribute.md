# Contribute

There are many ways you can help and contribute the Karaf community.

* Try browsing the [documentation](docs/intro)
* [Download](download) and try Apache Karaf K5
* [Browse the source code](https://github.com/apache/karaf-minho). Got an itch to scratch, want to tune some operation or add some feature ?

## Report bugs and feature requests

Did you find a bug or want something implemented? Please report an issue in our [tracker](https://github.com/apache/karaf-minho/issues). 
When creating a bug make sure you document the steps to reproduce the issue and provide all necessary information like OS, versions your use, logs. When creating a feature request document your requirements first. Try to not directly describe the solution.

If you want to dive into development yourself then you can also browse for open issues or features that need to be implemented. Take ownership of an issue and try to fix it. Before doing a bigger change describe the concept/design of what you plan to do. If unsure if the design is good or will be accepted discuss it on the dev list.

## Provide changes in a Pull Request

The best way to provide changes is to fork `karaf-minho` repository on github and provide a pull request with your changes. To make it easy to apply your changes please use the following conventions:

* Every pull request should have a matching issue.
* Do the change in a branch that is named like the issue, e.g. MINHO-xxxx.
	To create this branch you can use:
	```bash
	git clone https://github.com/apache/karaf-minho
	git fetch --all
	git checkout -b K5-xxxx origin/main
	```
	Don't forget to periodically rebase your branch:
	```bash
	git pull --rebase
	git push my-user K5-xxxx --force
	```
* Pull Requests should be based on `main`. We are taking care of cherry picking the Pull Request commit on target branches if needed.
* Every commit in the branch should start with the issue ID, e.g. `[K5-xxxx] More details`.
* If you have a group of commits related to the same change, please squash your commits into one and force push your branch using `git rebase -i origin/main`.
* Test that your change works by adapting or adding tests.
* [Follow the boy scout rule to "Always leave the campground cleaner than you found it."](http://programmer.97things.oreilly.com/wiki/index.php/The_Boy_Scout_Rule)
* Make sure you do a build before doing a Pull Request. The build has to successfully:
	```bash
	mvn clean verify -Prat
	```
* If your Pull Request has conflicts with the `main` then rebase the branch. Pull Requests with conflicts are unlikely to be applied.
* Do not change too much in Pull Request, and avoid to create bunch of Pull Requests at the same time. Step by step and smaller Pull Requests are easier to apply and the review is faster.
* Even if we are monitoring closely the Pull Requests, if you think your Pull Request doesn't move forward fast enough, do not hesitate to ping in a Pull Request comment.
