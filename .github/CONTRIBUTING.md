# Contributing to Project Casquatch

The following is a set of guidelines for contributing to Project Casquatch on GitHub. These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to this document in a pull request.

#### Table Of Contents

[Code of Conduct](#code-of-conduct)

[Slack](#join-the-tmobile-slack-team)

[How Can I Contribute?](#how-can-i-contribute)
  * [Reporting Bugs](#reporting-bugs)
  * [Suggesting Enhancements](#suggesting-enhancements)
  * [Your First Code Contribution](#your-first-code-contribution)
  * [Pull Requests](#pull-requests)

[Styleguides](#styleguides)
  * [Git Commit Messages](#git-commit-messages)
  * [JavaScript Styleguide](#javascript-styleguide)
  * [Specs Styleguide](#specs-styleguide)
  * [Documentation Styleguide](#documentation-styleguide)

[Additional Notes](#additional-notes)
  * [Issue and Pull Request Labels](#issue-and-pull-request-labels)

## Code of Conduct

This project and everyone participating in it is governed by the [Project Casquatch's Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to [TMobileOSS@T-Mobile.com](mailto:TMobileOSS@T-Mobile.com).

### Join the TMobile Slack team:

* [Join the T-Mobile Slack team](https://tmo-oss-getinvite.herokuapp.com/)
    * Even though Slack is a chat service, sometimes it takes several hours for community members to respond &mdash; please be patient!
    * Use the `#Project-Casquatch` channel for questions about Project Casquatch.
    * Use the `#general` channel for general questions or discussion about anything else.


## How Can I Contribute?
Anyone can contribute to Project Casquatch and we welcome your contributions.

There are multiple ways to contribute: report bugs, improve the docs, and contribute code while following the guidelines below:

### Reporting Bugs

This section guides you through submitting a bug report for Project Casquatch. Following these guidelines helps maintainers and the community understand your report, reproduce the behavior, and find related reports.

Fill out [the required template](ISSUE_TEMPLATE.md), the information it asks for helps us resolve issues faster.

> **Note:** If you find a **Closed** issue that seems like it is the same thing that you're experiencing, open a new issue and include a link to the original issue in the body of your new one.

#### How Do I Submit A Bug Report?

Bugs are tracked as [GitHub issues](https://guides.github.com/features/issues/). Create an issue providing the following information by filling in [the template](ISSUE_TEMPLATE.md).

Explain the problem and include additional details to help maintainers reproduce the problem:

* **Use a clear and descriptive title** for the issue to identify the problem.
* **Describe the exact steps which reproduce the problem** in as many details as possible.
* **Provide specific examples to demonstrate the steps**. Include links to files or GitHub projects, or copy/pasteable snippets, which you use in those examples. If you're providing snippets in the issue, use [Markdown code blocks](https://help.github.com/articles/markdown-basics/#multiple-lines).
* **Describe the behavior you observed after following the steps** and point out what exactly is the problem with that behavior.
* **Explain which behavior you expected to see instead and why.**

### Suggesting Enhancements

Enhancement suggestions are tracked as [GitHub issues](https://guides.github.com/features/issues/). Create an issue with the following information:

* **Use a clear and descriptive title** for the issue to identify the suggestion.
* **Provide a step-by-step description of the suggested enhancement** in as many details as possible.
* **Provide specific examples to demonstrate the steps**. Include copy/pasteable snippets which you use in those examples, as [Markdown code blocks](https://help.github.com/articles/markdown-basics/#multiple-lines).
* **Describe the current behavior** and **explain which behavior you expected to see instead** and why.

### Pull Requests

* Fill in [the required template](PULL_REQUEST_TEMPLATE.md)
* Do not include issue numbers in the PR title
* All code is required to have JUnit test cases [Styleguide](#test-cases)

## Styleguides

### Git Commit Messages

* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Limit the first line to 72 characters or less
* Reference issues and pull requests liberally after the first line

### Test Cases

* Except where DSE exclusive functionality, all test cases must be built using CassandraUnit with default settings
* All schemas are to be created in setUp() within the junittest keyspace
* Models are to be generated and included. Extended models are not to be used in test cases except through a published interface
* All must use @Before and/or @After to setup / destroy unqiue test records
